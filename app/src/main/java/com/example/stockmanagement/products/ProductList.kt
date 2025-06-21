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
    private lateinit var recordCount:TextView

    enum class FilterType {
        NONE,NAME_ASC, NAME_DESC, PRODUCT_NAME, STOCK_AVAILABLE, STOCK_UNAVAILABLE
    }

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
        supportActionBar?.setHomeAsUpIndicator(R.drawable.back_icon)

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
        val selectedTextView = findViewById<TextView>(R.id.TV_SelectedText)
        recordCount = findViewById(R.id.list_record_count)
        selectedTextView.visibility = View.GONE

        val filterMap = mapOf(
            getString(R.string.filter_Product_none) to FilterType.NONE,
            getString(R.string.filter_Product_name_asc) to FilterType.NAME_ASC,
            getString(R.string.filter_product_name_desc) to FilterType.NAME_DESC,
            getString(R.string.filter_product_name) to FilterType.PRODUCT_NAME,
            getString(R.string.filter_product_stock_available) to FilterType.STOCK_AVAILABLE,
            getString(R.string.filter_product_stock_unavailable) to FilterType.STOCK_UNAVAILABLE
        )

        val filterList = filterMap.keys.toList()
        val spinnerAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, filterList)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        filterDropdown.adapter = spinnerAdapter

        filterDropdown.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, p1: View, position: Int, id: Long) {
                val selectedLabel = parent.getItemAtPosition(position).toString()
                val selectedFilter = filterMap[selectedLabel] ?: FilterType.NONE

                if (selectedFilter != FilterType.NONE) {
                    clearButton.visibility = View.VISIBLE
                    clearButton.backgroundTintList = ColorStateList.valueOf(Color.RED)
                    when (selectedFilter) {
                        FilterType.NAME_ASC -> {
                            lifecycleScope.launch {
                                val products = dao.getAllProductSortByName()
                                recordCount.text=products.size.toString()
                                adapter.updateData(products)
                            }
                        }
                        FilterType.NAME_DESC -> {
                            lifecycleScope.launch {
                                val products = dao.getAllProductSortByNameDesc()
                                recordCount.text=products.size.toString()
                                adapter.updateData(products)
                            }
                        }
                        FilterType.PRODUCT_NAME -> {
                            showProductNameFilterDialog()
                        }
                        FilterType.STOCK_AVAILABLE -> {
                            lifecycleScope.launch {
                                val products = dao.getAllProductAvailableSortByCount()
                                recordCount.text=products.size.toString()
                                adapter.updateData(products)
                            }
                        }
                        FilterType.STOCK_UNAVAILABLE -> {
                            lifecycleScope.launch {
                                val products = dao.getAllProductUnavailable()
                                recordCount.text=products.size.toString()
                                adapter.updateData(products)
                            }
                        }
                        else -> {}
                    }
                }else {
                    clearButton.visibility = View.GONE
                    selectedTextView.visibility = View.GONE
                    clearButton.backgroundTintList = defaultColor
                    loadProductList() // or loadPurchaseList()
                }
            }
            override fun onNothingSelected(p0: AdapterView<*>?) {
                clearButton.visibility = View.GONE
            }
        }

        clearButton.setOnClickListener {
            clearButton.backgroundTintList = defaultColor
            filterDropdown.setSelection(0)
            selectedTextView.visibility = View.GONE
            clearButton.visibility = View.GONE
            loadProductList()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun showProductNameFilterDialog() {
        val input = AutoCompleteTextView(this).apply {
            hint = getString(R.string.filter_popup_Enter_Here)
            threshold = 1
            val padding = resources.getDimensionPixelSize(R.dimen.dialog_input_padding)
            setPadding(padding, padding, padding, padding)
        }
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.filter_product_popup_enter_name))
            .setView(input)
            .setPositiveButton(getString(R.string.popup_apply)) { dialog, _ ->
                val name = input.text.toString().trim()
                val selectedText = findViewById<TextView>(R.id.TV_SelectedText)
                if (name.isNotEmpty()) {
                    selectedText.text = "${getString(R.string.filter_product_popup_entered_name)}: $name"
                    selectedText.visibility = View.VISIBLE
                    lifecycleScope.launch {
                        val products= dao.getProductByName(name)
                        if(products != null){
                            recordCount.text="1"
                            adapter.updateData(listOf(products))
                        } else{
                            recordCount.text="0"
                            selectedText.text = getString(R.string.filter_result_product)
                        }
                    }
                } else {
                    selectedText.visibility = View.GONE
                }
                dialog.dismiss()
            }
            .setNegativeButton(getString(R.string.popup_cancel)) { dialog, _ -> dialog.cancel() }
            .show()
        GetListOfData(this, this).getAllProductNames{ names ->
            input.setAdapter(ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, names))
        }

    }

    private fun loadProductList() {
        lifecycleScope.launch {
            val products = dao.getAllProduct()
            recordCount.text=products.size.toString()
            adapter.updateData(products.toMutableList())
        }
    }

    override fun onResume() {
        super.onResume()
        loadProductList()
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
