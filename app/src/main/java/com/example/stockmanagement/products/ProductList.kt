package com.example.stockmanagement.products

import android.annotation.SuppressLint
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.Spinner
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.stockmanagement.GetListOfData
import com.example.stockmanagement.ManagementDao
import com.example.stockmanagement.ManagementDatabase
import com.example.stockmanagement.R
import kotlinx.coroutines.launch

class ProductList : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ProductListAdapter
    private lateinit var dao: ManagementDao

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_product_list)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Set up the toolbar
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Product List"

        // Initialize DAO
        dao = ManagementDatabase.getInstance(this).managementDao

        // Set up RecyclerView and empty adapter
        recyclerView = findViewById(R.id.RV_ProductList)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = ProductListAdapter(mutableListOf(), this) { product ->
            val nextScreen = Intent(this, ProductView::class.java)
            nextScreen.putExtra("PRODUCT_ID", product.pid)
            startActivity(nextScreen)
        }
        recyclerView.adapter = adapter
        // Load data
        loadProductList()
        val filterDropdown = findViewById<Spinner>(R.id.Spi_ProductFilter)
        val clearButton = findViewById<Button>(R.id.Btn_FilterClear)
        val defaultColor = clearButton.backgroundTintList
        findViewById<TextView>(R.id.TV_SelectedText).visibility = View.GONE

        val filterList = listOf("None","Name ASC","Name DESC","Product Name","Stock Available","Stock Unavailable")
        val spinneradapter = ArrayAdapter(this,android.R.layout.simple_spinner_item, filterList)
        spinneradapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        filterDropdown.adapter = spinneradapter
        var selectedFilter = "None"

        filterDropdown.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, p1: View, position: Int, id: Long) {
                selectedFilter = parent.getItemAtPosition(position).toString()
                if (selectedFilter != "None") {
                    clearButton.visibility = View.VISIBLE
                    clearButton.backgroundTintList=ColorStateList.valueOf(Color.RED)
                    when (selectedFilter) {
                        "Name ASC" -> {
                            lifecycleScope.launch {
                                val product = dao.getAllProductSortByName()
                                adapter.updateData(product)
                            }
                        }
                        "Name DESC" -> {
                            lifecycleScope.launch {
                                val product = dao.getAllProductSortByNameDesc()
                                adapter.updateData(product)
                            }
                        }
                        "Product Name" -> {
                            showProductNameFilterDialog()
                        }
                        "Stock Available" -> {
                            lifecycleScope.launch {
                                val product = dao.getAllProductAvailableSortByCount()
                                adapter.updateData(product)
                            }
                        }
                        "Stock Unavailable" -> {
                            lifecycleScope.launch {
                                val product = dao.getAllProductUnavailable()
                                adapter.updateData(product)
                            }
                        }
                    }
                } else {
                    clearButton.visibility = View.GONE
                }
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
                selectedFilter = "None"
                clearButton.visibility = View.GONE
            }
        }
        clearButton.setOnClickListener {
            clearButton.backgroundTintList=defaultColor
            filterDropdown.setSelection(0) // Reset to "None"
            findViewById<TextView>(R.id.TV_SelectedText).visibility = View.GONE
            lifecycleScope.launch {
                val allData = dao.getAllProduct()
                adapter.updateData(allData)
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun showProductNameFilterDialog() {
        val input = AutoCompleteTextView(this).apply {
            hint = "Enter Here"
            threshold = 1
            val padding = resources.getDimensionPixelSize(R.dimen.dialog_input_padding)
            setPadding(padding, padding, padding, padding)
        }
        AlertDialog.Builder(this)
            .setTitle("Enter Product Name")
            .setView(input)
            .setPositiveButton("Apply") { dialog, _ ->
                val name = input.text.toString().trim()
                val selectedText = findViewById<TextView>(R.id.TV_SelectedText)
                if (name.isNotEmpty()) {
                    selectedText.text = "Selected Product: $name"
                    selectedText.visibility = View.VISIBLE
                    lifecycleScope.launch {
                        val customer= dao.getProductByName(name)
                        if(customer != null){
                            adapter.updateData(listOf(customer))
                        } else{
                            selectedText.text = "Product Not Found"
                        }
                    }
                } else {
                    selectedText.visibility = View.GONE
                }
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ -> dialog.cancel() }
            .show()
        GetListOfData(this, this).getAllProductNames{ names ->
            input.setAdapter(ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, names))
        }

    }

    private fun loadProductList() {
        lifecycleScope.launch {
            val products = dao.getAllProduct()
            adapter.updateData(products.toMutableList())
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
