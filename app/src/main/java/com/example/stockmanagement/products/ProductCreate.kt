package com.example.stockmanagement.products

import android.icu.text.SimpleDateFormat
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
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
        // Set up the toolbar
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        var name=findViewById<EditText>(R.id.ET_ProductName)
        var measurementtype=findViewById<Spinner>(R.id.Spi_ProductMeasurement)
        var startdate=findViewById<EditText>(R.id.ET_ProductStartDate)

        val measurementunits = listOf("None","Kg", "Liter", "Bag")
        val adapter = ArrayAdapter(this,android.R.layout.simple_spinner_item, measurementunits)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        measurementtype.adapter = adapter

        measurementtype.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                p1: View,
                position: Int,
                id: Long
            ) {
                val selectedItem = parent.getItemAtPosition(position).toString()
                Toast.makeText(this@ProductCreate, "Selected: $selectedItem", Toast.LENGTH_SHORT).show()
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
                Toast.makeText(this@ProductCreate, "Selected", Toast.LENGTH_SHORT).show()
            }
        }
        val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        startdate.setText(dateFormat.format(GetListOfData(this, this).getCurrentDate()))

        val dao = ManagementDatabase.Companion.getInstance(this).managementDao
        startdate.setOnClickListener {
            GetListOfData.Companion.showDatePicker(this,startdate)
        }
        val dataFetcher = GetListOfData(this, this)
        findViewById<Button>(R.id.Btn_SaveProduct).setOnClickListener {
            lifecycleScope.launch {
                val error = validateInputs(
                    name.text.toString(),
                    measurementtype.selectedItem.toString(),
                    startdate.text.toString(),
                    dataFetcher
                )
                if(error !=  null){
                    Toast.makeText(this@ProductCreate, error, Toast.LENGTH_LONG).show()
                    return@launch
                }
                val product = Product(
                    pid = null,
                    productname = name.text.toString(),
                    measurement = measurementtype.selectedItem.toString(),
                    productcreateddate = Date(dateFormat.parse(startdate.text.toString())!!.time),
                    LatestpriceofoneUnit = 0,
                    currentstockcount = 0
                )
                dao.insertProduct(product)
                Log.d("INSERT", "Product inserted: $product")
                Toast.makeText(this@ProductCreate, "Product is Saved", Toast.LENGTH_LONG).show()
                finish()
            }
        }
    }
    suspend fun validateInputs(
        productName: String,
        measurementType: String,
        startDate: String,
        dataFetcher: GetListOfData
    ): String?{
        return when {
            dataFetcher.doesProductExist(productName) -> "Already we have a Product with Same Name so Change name"
            productName.isEmpty() -> "Product name is required"
            measurementType.equals("None") -> "Measurement unit is required.\nNot as None"
            startDate.isEmpty() -> "Product start Date is required"
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