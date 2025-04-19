package com.example.stockmanagement.purchases

import android.icu.text.SimpleDateFormat
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
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

        val dataFetcher = GetListOfData(this, this)
        val productname = findViewById<AutoCompleteTextView>(R.id.AET_Product)
        val addeddate = findViewById<EditText>(R.id.ET_NewStockDate)
        val stockcost = findViewById<EditText>(R.id.ET_NewStockCost)
        val stockcount = findViewById<EditText>(R.id.ET_StockCount)

        val dao = ManagementDatabase.Companion.getInstance(this).managementDao
        val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())

        dataFetcher.getAllProductNames { productNames ->
            val adapter =
                ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, productNames)
            productname.setAdapter(adapter)
            productname.threshold = 1
        }

        addeddate.setOnClickListener {
            GetListOfData.Companion.showDatePicker(this, addeddate)
        }


        findViewById<Button>(R.id.Btn_ProductStockSave).setOnClickListener {
            if(productname.text.isEmpty()){
                Toast.makeText(this,"Product not as Empty", Toast.LENGTH_LONG).show()
            }
            else if(addeddate.text.isEmpty()){
                Toast.makeText(this,"Stock added Date not as Empty", Toast.LENGTH_LONG).show()
            }
            else if(stockcost.text.isEmpty()){
                Toast.makeText(this,"Total Cost of Stock not as Empty", Toast.LENGTH_LONG).show()
            }
            else if(stockcount.text.isEmpty()){
                Toast.makeText(this,"Stock Count not as Empty", Toast.LENGTH_LONG).show()
            }
            else{
                val purchase = Purchase(
                    puid = null,
                    productname = productname.text.toString(),
                    stockaddeddate = Date(dateFormat.parse(addeddate.text.toString())!!.time),
                    stockcount = stockcount.text.toString().toIntOrNull() ?: 0,
                    currentstockcount = stockcount.text.toString().toIntOrNull() ?: 0,
                    stockprice = stockcost.text.toString().toIntOrNull() ?: 0
                )
                lifecycleScope.launch {
                    dao.insertPurchase(purchase)
                    Log.d("INSERT", "Purchase inserted: $purchase")
                }
                Toast.makeText(this, "Purchase Entry is Saved", Toast.LENGTH_LONG).show()
                finish()
            }
        }


    }
}