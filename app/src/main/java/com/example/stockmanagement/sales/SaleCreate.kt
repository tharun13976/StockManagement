package com.example.stockmanagement.sales

import android.annotation.SuppressLint
import android.icu.text.SimpleDateFormat
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.lifecycleScope
import com.example.stockmanagement.CustomFilterArrayAdapter
import com.example.stockmanagement.GetListOfData
import com.example.stockmanagement.ManagementDatabase
import com.example.stockmanagement.R
import com.example.stockmanagement.entites.Purchase
import com.example.stockmanagement.entites.Sale
import kotlinx.coroutines.launch
import java.util.Date
import java.util.Locale

class SaleCreate : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_sale_create)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBarsInsets = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val imeInsets = insets.getInsets(WindowInsetsCompat.Type.ime())
            v.setPadding(
                systemBarsInsets.left,
                systemBarsInsets.top,
                systemBarsInsets.right,
                imeInsets.bottom
            )
            insets
        }

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.back_icon)

        val customername = findViewById<AutoCompleteTextView>(R.id.AET_CustomerName)
        val saledate = findViewById<EditText>(R.id.ET_SaleDate)
        val productname = findViewById<AutoCompleteTextView>(R.id.AET_ProductName)
        val productcount = findViewById<EditText>(R.id.ET_ProductCount)
        val totalcost = findViewById<TextView>(R.id.TV_TotalCost)
        val amountgiven = findViewById<EditText>(R.id.ET_SalesAmountGiven)
        val amountonly = findViewById<CheckBox>(R.id.CB_OnlyAmount)
        val amountonlylayotchanges = findViewById<LinearLayout>(R.id.LL_amountOnly)

        saledate.inputType = InputType.TYPE_NULL
        saledate.isFocusable = false

        // Scroll to view on focus
        val focusScrollListener = View.OnFocusChangeListener { v, hasFocus ->
            if (hasFocus && v is EditText) scrollToView(v)
        }
        customername.onFocusChangeListener = focusScrollListener
        productname.onFocusChangeListener = focusScrollListener
        productcount.onFocusChangeListener = focusScrollListener
        amountgiven.onFocusChangeListener = focusScrollListener
        amountonly.onFocusChangeListener = focusScrollListener


        val dao = ManagementDatabase.getInstance(this).managementDao
        val dataFetcher = GetListOfData(this, this)

        val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        saledate.setText(dateFormat.format(dataFetcher.getCurrentDate()))

        amountonly.setOnClickListener {
            val isChecked = amountonly.isChecked
            amountonlylayotchanges.visibility = if (isChecked) View.GONE else View.VISIBLE
            if (isChecked) {
                productname.setText("")
                productcount.setText("")
                totalcost.text = ""
            }
        }

        dataFetcher.getAllCustomerNames { names ->
            runOnUiThread {
                val adapter = CustomFilterArrayAdapter(this, names)
                customername.setAdapter(adapter)
                customername.threshold = 1
            }
        }

        dataFetcher.getAllProductNames { names ->
            runOnUiThread {
                val adapter = CustomFilterArrayAdapter(this, names)
                productname.setAdapter(adapter)
                productname.threshold = 1
            }
        }

        saledate.setOnClickListener {
            GetListOfData.showDatePicker(this, saledate)
        }

        var purchaserec: Purchase? = null

        @SuppressLint("SetTextI18n")
        fun updateTotalCost() {
            val selectedProduct = productname.text.toString()
            val saleCount = productcount.text.toString().toIntOrNull() ?: 0
            lifecycleScope.launch {
                var cost = 0
                if (!amountonly.isChecked && selectedProduct.isNotEmpty()) {
                    try {
                        val purchase = dao.getOldestPurchase(selectedProduct)
                        if (purchase == null || purchase.currentstockcount <= 0) {
                            productoutofstock()
                            productname.setText("")
                            return@launch
                        }
                        purchaserec = purchase
                        cost = purchase.stockprice * saleCount
                    } catch (e: Exception) {
                        e.printStackTrace()
                        productoutofstock()
                        productname.setText("")
                    }
                }
                totalcost.text = cost.toString()
            }
        }

        productcount.doOnTextChanged { _, _, _, _ -> updateTotalCost() }
        amountgiven.doOnTextChanged { _, _, _, _ -> updateTotalCost() }
        productname.setOnItemClickListener { _, _, _, _ -> updateTotalCost() }

        findViewById<Button>(R.id.Btn_SaveSale).setOnClickListener {
            lifecycleScope.launch {
                val error = validateInputs(
                    customername.text.toString(),
                    saledate.text.toString(),
                    productname.text.toString(),
                    productcount.text.toString(),
                    amountgiven.text.toString(),
                    amountonly.isChecked,
                    dataFetcher
                )
                if (error != null) {
                    Toast.makeText(this@SaleCreate, error, Toast.LENGTH_LONG).show()
                    return@launch
                }

                val selectedProduct = productname.text.toString()
                val saleCount = productcount.text.toString().toIntOrNull() ?: 0
                val givenAmount = amountgiven.text.toString().toInt()
                val isAmountOnly = amountonly.isChecked

                var purchaseId: Int? = null
                var cost: Int? = null

                if (!isAmountOnly) {
                    try {
                        purchaserec = dao.getOldestPurchase(selectedProduct)
                        val purchase = purchaserec
                        if (purchase == null || purchase.currentstockcount < saleCount) {
                            productcountmissmatch(purchase?.currentstockcount.toString())
                            productcount.setText("")
                            return@launch
                        }
                        purchaseId = purchase.puid ?: 0
                        cost = purchase.stockprice * saleCount

                        purchase.currentstockcount -= saleCount
                        dao.updatePurchase(purchase)

                        dao.getProductByName(selectedProduct)?.let {
                            it.currentstockcount -= saleCount
                            dao.updateProduct(it)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        Toast.makeText(this@SaleCreate, "Unexpected error during stock update", Toast.LENGTH_LONG).show()
                        return@launch
                    }
                }

                val confirmed = dataFetcher.showConfirmationDialog(
                    context = this@SaleCreate,
                    message = "Are you sure you want to save this Sales Entry?\n Note: Unable to edit or Delete the Sales record"
                )
                if (!confirmed) return@launch

                val sale = Sale(
                    sid = null,
                    customername = customername.text.toString(),
                    productname = if (isAmountOnly) "-" else selectedProduct,
                    salesdate = Date(dateFormat.parse(saledate.text.toString())!!.time),
                    purchaseid = if (isAmountOnly) 0 else purchaseId,
                    saleproductcount = if (isAmountOnly) 0 else saleCount,
                    costofproductsold = if (isAmountOnly) 0 else cost,
                    amountgiven = givenAmount,
                    amountonly = isAmountOnly
                )

                dao.getCustomerByname(customername.text.toString())?.let {
                    it.amountbalance += (cost ?: 0) - givenAmount
                    dao.updateCustomer(it)
                }

                dao.insertSale(sale)
                Log.d("INSERT", "Sale inserted")
                Toast.makeText(this@SaleCreate, "Sale Entry Saved", Toast.LENGTH_LONG).show()
                finish()
            }
        }
    }
    private fun scrollToView(view: View) {
        val scrollView = findViewById<ScrollView>(R.id.scrollView)
        scrollView.post {
            scrollView.smoothScrollTo(0, view.top - 20)
        }
    }

    suspend fun validateInputs(
        customerName: String,
        saleDate: String,
        productName: String,
        productCount: String,
        amountGiven: String,
        amountOnly: Boolean,
        dataFetcher: GetListOfData
    ): String? {
        return when {
            !dataFetcher.doesCustomerExist(customerName) -> "Customer not found"
            !dataFetcher.doesProductExist(productName) && !amountOnly -> "Product not found"
            saleDate.isEmpty() -> "Sale Date is required"
            (productCount.isEmpty() || productCount == "0") && !amountOnly -> "Product count must be more than 0"
            amountGiven.isEmpty() -> "Amount given is required"
            else -> null
        }
    }

    fun productcountmissmatch(count: String?) {
        Toast.makeText(this, "Enter product count ≤ $count", Toast.LENGTH_LONG).show()
    }

    fun productoutofstock() {
        Toast.makeText(this, "Product Out of Stock or Not Found", Toast.LENGTH_LONG).show()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (item.itemId == android.R.id.home) {
            finish()
            true
        } else {
            super.onOptionsItemSelected(item)
        }
    }
}
