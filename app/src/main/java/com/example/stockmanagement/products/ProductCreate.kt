package com.example.stockmanagement.products

import android.icu.text.SimpleDateFormat
import android.os.Bundle
import android.util.Log
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
import com.example.stockmanagement.entites.Product
import kotlinx.coroutines.launch
import java.util.Date
import java.util.Locale

class ProductCreate : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_product_create)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        var name=findViewById<EditText>(R.id.ET_ProductName)
        var measurementtype=findViewById<AutoCompleteTextView>(R.id.AET_ProductMeasurement)
        var startdate=findViewById<EditText>(R.id.ET_ProductStartDate)

        val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        startdate.setText(dateFormat.format(GetListOfData(this, this).getCurrentDate()))

        val dao = ManagementDatabase.Companion.getInstance(this).managementDao

        startdate.setOnClickListener {
            GetListOfData.Companion.showDatePicker(this,startdate)
        }

        findViewById<Button>(R.id.Btn_SaveProduct).setOnClickListener {
            if(name.text.isEmpty() || name.text.length<5){
                Toast.makeText(this, "Product Name is not proper.", Toast.LENGTH_LONG).show()
            }
            else if(measurementtype.text.isEmpty()){
                Toast.makeText(this, "Enter the Measurement Type.", Toast.LENGTH_LONG).show()
            }
            else if(startdate.text.isEmpty()){
                Toast.makeText(this, "Enter the Start Date.", Toast.LENGTH_LONG).show()
            }
            else{
                val product = Product(
                    pid = null,
                    productname = name.text.toString(),
                    mesurment = measurementtype.text.toString(),
                    productcreateddate = Date(dateFormat.parse(startdate.text.toString())!!.time),
                    currentprice = 0,
                    currentstockcount = 0
                )
                lifecycleScope.launch {
                    dao.insertProduct(product)
                    Log.d("INSERT", "Product inserted: $product")
                }
                Toast.makeText(this, "Product is Saved", Toast.LENGTH_LONG).show()
                finish()
            }
        }

    }
}