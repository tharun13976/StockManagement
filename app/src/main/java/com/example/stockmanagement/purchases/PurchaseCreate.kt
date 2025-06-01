package com.example.stockmanagement.purchases

import android.icu.text.SimpleDateFormat
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import android.widget.ScrollView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.stockmanagement.CustomFilterArrayAdapter
import com.example.stockmanagement.GetListOfData
import com.example.stockmanagement.ManagementDatabase
import com.example.stockmanagement.R
import com.example.stockmanagement.entites.Purchase
import kotlinx.coroutines.launch
import java.util.Date
import java.util.Locale

class PurchaseCreate : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_purchase_create)
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

        // Views
        val productname = findViewById<AutoCompleteTextView>(R.id.AET_Product)
        val addeddate = findViewById<EditText>(R.id.ET_NewStockDate)
        val stockcost = findViewById<EditText>(R.id.ET_NewStockCost)
        val stockcount = findViewById<EditText>(R.id.ET_StockCount)

        addeddate.inputType = InputType.TYPE_NULL
        addeddate.isFocusable = false
        // Scroll to view on focus
        val focusScrollListener = View.OnFocusChangeListener { v, hasFocus ->
            if (hasFocus && v is EditText) scrollToView(v)
        }
        productname.onFocusChangeListener = focusScrollListener
        stockcount.onFocusChangeListener = focusScrollListener
        stockcost.onFocusChangeListener = focusScrollListener

        val dao = ManagementDatabase.getInstance(this).managementDao
        val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        addeddate.setText(dateFormat.format(GetListOfData(this, this).getCurrentDate()))

        val dataFetcher = GetListOfData(this, this)
        dataFetcher.getAllProductNames { productNames ->
            val adapter = CustomFilterArrayAdapter(this, productNames)
            productname.setAdapter(adapter)
            productname.threshold = 1
        }

        addeddate.setOnClickListener {
            GetListOfData.showDatePicker(this, addeddate)
        }

        findViewById<Button>(R.id.Btn_ProductStockSave).setOnClickListener {
            lifecycleScope.launch {
                val error = validateInputs(
                    productname.text.toString(),
                    addeddate.text.toString(),
                    stockcount.text.toString(),
                    stockcost.text.toString(),
                    dataFetcher
                )
                if (error != null) {
                    Toast.makeText(this@PurchaseCreate, error, Toast.LENGTH_LONG).show()
                    return@launch
                }
                else{

                    val confirmed = dataFetcher.showConfirmationDialog(
                        context = this@PurchaseCreate,
                        message = getString(R.string.purchase_conformation)
                    )
                    if (!confirmed) return@launch

                    val purchase = Purchase(
                        puid = null,
                        productname = productname.text.toString(),
                        stockaddeddate = Date(dateFormat.parse(addeddate.text.toString())!!.time),
                        stockcount = stockcount.text.toString().toIntOrNull() ?: 0,
                        currentstockcount = stockcount.text.toString().toIntOrNull() ?: 0,
                        stockprice = stockcost.text.toString().toIntOrNull() ?: 0
                    )
                    dao.insertPurchase(purchase)
                    Log.d("INSERT", "Purchase inserted")

                    val productrec = dao.getProductByName(productname.text.toString())
                    if (productrec != null) {
                        productrec.currentstockcount += stockcount.text.toString().toIntOrNull() ?: 0
                        productrec.LatestpriceofoneUnit = stockcost.text.toString().toIntOrNull() ?: 0
                        dao.updateProduct(productrec)
                    }
                }
                Toast.makeText(this@PurchaseCreate, getString(R.string.purchase_saved), Toast.LENGTH_LONG).show()
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
        productName: String,
        stockaddeddate: String,
        stockcount: String,
        stockprice: String,
        dataFetcher: GetListOfData
    ): String? {
        return when {
            productName.isEmpty() -> getString(R.string.purchase_product_name_required)
            !dataFetcher.doesProductExist(productName) -> getString(R.string.purchase_product_not_found)
            stockaddeddate.isEmpty() -> getString(R.string.purchase_added_date_required)
            (stockcount.isEmpty() || stockcount == "0") -> getString(R.string.purchase_stock_count)
            (stockprice.isEmpty() || stockprice =="0")-> getString(R.string.purchase_stockprice_required)
            else -> null
        }
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
