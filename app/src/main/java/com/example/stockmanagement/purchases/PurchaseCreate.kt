package com.example.stockmanagement.purchases

import android.icu.text.SimpleDateFormat
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
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
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Views
        val productname = findViewById<AutoCompleteTextView>(R.id.AET_Product)
        val addeddate = findViewById<EditText>(R.id.ET_NewStockDate)
        val stockcost = findViewById<EditText>(R.id.ET_NewStockCost)
        val stockcount = findViewById<EditText>(R.id.ET_StockCount)

        val dao = ManagementDatabase.getInstance(this).managementDao
        val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        addeddate.setText(dateFormat.format(GetListOfData(this, this).getCurrentDate()))

        val dataFetcher = GetListOfData(this, this)
        dataFetcher.getAllProductNames { productNames ->
            val adapter =
                ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, productNames)
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
                        message = "Are you sure you want to save this Purchase Entry?\n Note: Unable to edit or Delete the Purchase record"
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
                    Log.d("INSERT", "Purchase inserted: Purchase Id ${purchase.puid}")

                    val productrec = dao.getProductByName(productname.text.toString())
                    if (productrec != null) {
                        productrec.currentstockcount += stockcount.text.toString().toIntOrNull() ?: 0
                        productrec.LatestpriceofoneUnit = stockcost.text.toString().toIntOrNull() ?: 0
                        dao.updateProduct(productrec)
                    }
                }
                Toast.makeText(this@PurchaseCreate, "Purchase Entry Saved", Toast.LENGTH_LONG).show()
                finish()
            }
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
            productName.isEmpty() -> "Product name is required"
            !dataFetcher.doesProductExist(productName) -> "Product not found"
            stockaddeddate.isEmpty() -> "Stock added Date is required"
            (stockcount.isEmpty() || stockcount == "0") -> "Product count must be more than 0"
            stockprice.isEmpty() -> "Stock price is required"
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
