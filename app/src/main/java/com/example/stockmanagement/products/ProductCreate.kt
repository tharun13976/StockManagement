package com.example.stockmanagement.products

import android.icu.text.SimpleDateFormat
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ScrollView
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.stockmanagement.ExitConfirmation
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
        // Set up the toolbar
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.back_icon)

        var name=findViewById<EditText>(R.id.ET_ProductName)
        var measurementtype=findViewById<Spinner>(R.id.Spi_ProductMeasurement)
        var startdate=findViewById<EditText>(R.id.ET_ProductStartDate)

        startdate.inputType = InputType.TYPE_NULL
        startdate.isFocusable = false

        // Scroll to view on focus
        val focusScrollListener = View.OnFocusChangeListener { v, hasFocus ->
            if (hasFocus && v is EditText) scrollToView(v)
        }
        name.onFocusChangeListener = focusScrollListener


        val measurementunits = listOf(getString(R.string.product_mes_none),"Kg", "Liter", "Bag", "Nos.")
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
                //Toast.makeText(this@ProductCreate, "Selected: $selectedItem", Toast.LENGTH_SHORT).show()
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
                //Toast.makeText(this@ProductCreate, "Selected", Toast.LENGTH_SHORT).show()
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
                    name.text.toString().trim(),
                    measurementtype.selectedItem.toString(),
                    startdate.text.toString(),
                    dataFetcher
                )
                if(error !=  null){
                    Toast.makeText(this@ProductCreate, error, Toast.LENGTH_LONG).show()
                    return@launch
                }

                val confirmed = dataFetcher.showConfirmationDialog(
                    context = this@ProductCreate,
                    message = getString(R.string.product_save_conformation)
                )
                if (!confirmed) return@launch

                val product = Product(
                    pid = null,
                    productname = name.text.toString().trim(),
                    measurement = measurementtype.selectedItem.toString(),
                    productcreateddate = Date(dateFormat.parse(startdate.text.toString())!!.time),
                    LatestpriceofoneUnit = 0,
                    currentstockcount = 0
                )
                dao.insertProduct(product)
                Log.d("INSERT", "Product inserted")
                Toast.makeText(this@ProductCreate, getString(R.string.product_saved), Toast.LENGTH_LONG).show()
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
        measurementType: String,
        startDate: String,
        dataFetcher: GetListOfData
    ): String?{
        return when {
            dataFetcher.doesProductExist(productName) -> getString(R.string.product_same_name_alert)
            productName.isEmpty() -> getString(R.string.product_name_required)
            measurementType.equals(getString(R.string.product_mes_none)) -> getString(R.string.product_mes_required)
            startDate.isEmpty() -> getString(R.string.product_start_date_required)
            else -> null
        }
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (item.itemId == android.R.id.home) {
            var name=findViewById<EditText>(R.id.ET_ProductName)
            if(!name.text.isEmpty()){
                ExitConfirmation().show(this) {
                    finish()
                }
            }
            else{
                finish()
            }
            true
        } else {
            super.onOptionsItemSelected(item)
        }
    }
}