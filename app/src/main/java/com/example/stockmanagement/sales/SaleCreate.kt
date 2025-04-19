package com.example.stockmanagement.sales

import android.annotation.SuppressLint
import android.icu.text.SimpleDateFormat
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.lifecycleScope
import com.example.stockmanagement.GetListOfData
import com.example.stockmanagement.ManagementDatabase
import com.example.stockmanagement.R
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
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val customername = findViewById<AutoCompleteTextView>(R.id.AET_CustomerName)
        val saledate = findViewById<EditText>(R.id.ET_SaleDate)
        val productname = findViewById<AutoCompleteTextView>(R.id.AET_ProductName)
        val productcount = findViewById<EditText>(R.id.ET_ProductCount)
        val totalcost = findViewById<TextView>(R.id.TV_TotalCost)
        val amountgiven = findViewById<EditText>(R.id.ET_SalesAmountGiven)
        val amountonly = findViewById<CheckBox>(R.id.CB_OnlyAmount)


        val dao = ManagementDatabase.Companion.getInstance(this).managementDao

        val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        saledate.setText(dateFormat.format(GetListOfData(this, this).getCurrentDate()))

        val dataFetcher = GetListOfData(this, this)
        dataFetcher.getAllCustomerNames { customerNames ->
            val adapter =
                ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, customerNames)
            customername.setAdapter(adapter)
            customername.threshold = 1
        }
        dataFetcher.getAllProductNames { productNames ->
            val adapter =
                ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, productNames)
            productname.setAdapter(adapter)
            productname.threshold = 1
        }

        saledate.setOnClickListener {
            GetListOfData.Companion.showDatePicker(this, saledate)
        }
        amountonly.setOnClickListener{
            if (amountonly.isChecked) {
                productname.setText("")
                productcount.setText("")
                totalcost.setText("")
            }
        }
        // Function to update the total cost dynamically
        @SuppressLint("SetTextI18n")
        fun updateTotalCost() {
            val selectedProduct = productname.text.toString()
            val saleCount = productcount.text.toString().toIntOrNull() ?: 0
            lifecycleScope.launch {
                var cost = 0
                if (!amountonly.isChecked && selectedProduct.isNotEmpty()) {
                    try {
                        val purchaseList = dao.getOldestPurchase(selectedProduct)
                        val stockPrice = purchaseList.purchase.stockprice
                        cost = stockPrice * saleCount
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                // Update the total cost on the screen
                totalcost.text = cost.toString()
            }
        }

        // Use doOnTextChanged to update total cost dynamically
        productcount.doOnTextChanged { text, _, _, _ ->
            updateTotalCost() // Update the total cost when text changes
        }

        amountgiven.doOnTextChanged { text, _, _, _ ->
            updateTotalCost() // Update the total cost when text changes
        }
        productname.setOnItemClickListener { _, _, _, _ -> updateTotalCost() }

        findViewById<Button>(R.id.Btn_SaveSale).setOnClickListener {
            if (customername.text.isEmpty()) {
                Toast.makeText(this, "Customer name is not Empty", Toast.LENGTH_LONG).show()
            } else if (saledate.text.isEmpty()) {
                Toast.makeText(this, "Sale Date is not Empty", Toast.LENGTH_LONG).show()
            } else if (productname.text.isEmpty() && !amountonly.isChecked) {
                Toast.makeText(this, "Product name is not Empty", Toast.LENGTH_LONG).show()
            } else if (productcount.text.isEmpty() && !amountonly.isChecked) {
                Toast.makeText(this, "Product count is not Empty", Toast.LENGTH_LONG).show()
            } else if (amountgiven.text.isEmpty()) {
                Toast.makeText(this, "Amount given is not Empty", Toast.LENGTH_LONG).show()
            } else {
                val selectedProduct = productname.text.toString()
                val saleCount = productcount.text.toString().toIntOrNull() ?: 0
                val amount = amountgiven.text.toString().toIntOrNull() ?: 0

                lifecycleScope.launch {
                    var purchaseId = 0
                    var cost = 0

                    if (!amountonly.isChecked) {
                        try {
                            val purchaseList = dao.getOldestPurchase(selectedProduct)
                            purchaseId = purchaseList.purchase.puid ?: 0
                            cost = purchaseList.purchase.stockprice * saleCount
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }

                    val sale = Sale(
                        sid = null,
                        customername = customername.text.toString(),
                        productname = selectedProduct,
                        salesdate = Date(dateFormat.parse(saledate.text.toString())!!.time),
                        purchaseid = purchaseId,
                        saleproductcount = saleCount,
                        costofproductsold = cost,
                        amountgiven = amount,
                        amountonly = amountonly.isChecked
                    )

                    dao.insertSale(sale)
                    Log.d("INSERT", "Sale inserted: $sale")
                    Toast.makeText(this@SaleCreate, "Sale Entry is Saved", Toast.LENGTH_LONG).show()
                    finish()
                }
            }
        }
    }
}