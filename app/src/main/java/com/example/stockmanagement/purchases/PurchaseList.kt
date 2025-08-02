package com.example.stockmanagement.purchases

import android.annotation.SuppressLint
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.text.InputType.TYPE_CLASS_NUMBER
import android.view.MenuItem
import android.view.View
import android.widget.*
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
import java.text.SimpleDateFormat
import java.util.*

class PurchaseList : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: PurchaseListAdapter
    private lateinit var dao: ManagementDao
    private lateinit var clearButton: Button
    private lateinit var selectedTextView: TextView
    private lateinit var recordCount: TextView
    private var defaultColor: ColorStateList? = null

    enum class FilterType {
        NONE, PRODUCT_NAME, AVAILABLE, PURCHASE_ID, CREATED_DATE
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_purchase_list)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.back_icon)

        dao = ManagementDatabase.getInstance(this).managementDao

        recyclerView = findViewById(R.id.RV_PurchaseList)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = PurchaseListAdapter(mutableListOf(), this) { purchase ->
            val nextScreen = Intent(this, PurchaseView::class.java)
            nextScreen.putExtra("PURCHASE_ID", purchase.puid)
            startActivity(nextScreen)
        }
        recyclerView.adapter = adapter

        clearButton = findViewById(R.id.Btn_FilterClear)
        selectedTextView = findViewById(R.id.TV_SelectedText)
        recordCount = findViewById(R.id.list_record_count)

        selectedTextView.visibility = View.GONE
        defaultColor = clearButton.backgroundTintList

        setupFilterDropdown()
        loadPurchaseList()

        clearButton.setOnClickListener {
            clearButton.backgroundTintList = defaultColor
            selectedTextView.visibility = View.GONE
            clearButton.visibility = View.GONE
            loadPurchaseList()
        }
    }

    private fun setupFilterDropdown() {
        val filterDropdown = findViewById<Spinner>(R.id.Spi_PurchaseFilter)
        val filterMap = mapOf(
            getString(R.string.filter_purchase_none) to FilterType.NONE,
            getString(R.string.filter_purchase_product_name) to FilterType.PRODUCT_NAME,
            getString(R.string.filter_purchase_product_available) to FilterType.AVAILABLE,
            getString(R.string.filter_purchase_id) to FilterType.PURCHASE_ID,
            getString(R.string.filter_purchase_created_date) to FilterType.CREATED_DATE
        )

        val filterList = filterMap.keys.toList()
        val spinnerAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, filterList)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        filterDropdown.adapter = spinnerAdapter

        filterDropdown.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, p1: View?, position: Int, id: Long) {
                val selectedLabel = parent.getItemAtPosition(position).toString()
                val selectedFilter = filterMap[selectedLabel] ?: FilterType.NONE

                if (selectedFilter != FilterType.NONE) {
                    showClearButton()
                    when (selectedFilter) {
                        FilterType.AVAILABLE -> showAvailableFilter()
                        FilterType.PRODUCT_NAME -> showProductNameFilterDialog()
                        FilterType.PURCHASE_ID -> showPurchaseIdDialog()
                        FilterType.CREATED_DATE -> showDateFilterDialog()
                        else -> {}
                    }
                } else {
                    clearButton.visibility = View.GONE
                    selectedTextView.visibility = View.GONE
                    clearButton.backgroundTintList = defaultColor
                    loadPurchaseList()
                }
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {}
        }
    }

    private fun showClearButton() {
        clearButton.visibility = View.VISIBLE
        clearButton.backgroundTintList = ColorStateList.valueOf(Color.RED)
    }

    private fun showAvailableFilter() {
        lifecycleScope.launch {
            try {
                val purchase = dao.getAllAvailablePurchases()
                adapter.updateData(purchase.reversed())
                selectedTextView.text = getString(R.string.filter_purchase_product_available)
                selectedTextView.visibility = View.VISIBLE
                recordCount.text = purchase.size.toString()
            } catch (e: Exception) {
                Toast.makeText(this@PurchaseList, "Error loading available purchases", Toast.LENGTH_SHORT).show()
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun showDateFilterDialog() {
        GetListOfData.showDatePicker1(this, onDateSelected = { selectedDate ->
            val formattedDate = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(selectedDate)
            val startOfDay = java.sql.Date(selectedDate.time)

            lifecycleScope.launch {
                showClearButton()
                try {
                    val purchases = dao.getPurchasesByDate(startOfDay)
                    adapter.updateData(purchases.reversed())
                    recordCount.text = purchases.size.toString()

                    selectedTextView.text = if (purchases.isNotEmpty()) {
                        "${getString(R.string.filter_purchase_popup_entered_date)}: $formattedDate"
                    } else {
                        "${getString(R.string.filter_purchase_popup_result_date)} $formattedDate"
                    }
                    selectedTextView.visibility = View.VISIBLE
                } catch (e: Exception) {
                    Toast.makeText(this@PurchaseList, "Error filtering by date", Toast.LENGTH_SHORT).show()
                }
            }
        })
    }

    @SuppressLint("SetTextI18n")
    private fun showPurchaseIdDialog() {
        val input = EditText(this).apply {
            hint = getString(R.string.filter_popup_Enter_Here)
            inputType = TYPE_CLASS_NUMBER
        }

        showInputDialog(getString(R.string.filter_purchase_popup_enter_id), input) { text ->
            val enteredId = text.toIntOrNull()
            if (enteredId != null && enteredId > 0) {
                lifecycleScope.launch {
                    showClearButton()
                    try {
                        val result = dao.getPurchaseById(enteredId)
                        if (result != null) {
                            adapter.updateData(listOf(result))
                            recordCount.text = "1"
                            selectedTextView.text = "${getString(R.string.filter_purchase_popup_entered_id)}: $enteredId"
                        } else {
                            adapter.updateData(emptyList())
                            recordCount.text = "0"
                            selectedTextView.text = "${getString(R.string.filter_purchase_popup_result_Id)} $enteredId"
                        }
                        selectedTextView.visibility = View.VISIBLE
                    } catch (e: Exception) {
                        Toast.makeText(this@PurchaseList, "Error filtering by ID", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun showProductNameFilterDialog() {
        val input = AutoCompleteTextView(this).apply {
            hint = getString(R.string.filter_popup_Enter_Here)
            threshold = 1
        }

        GetListOfData(this, this).getAllProductNames { names ->
            input.setAdapter(ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, names))
        }

        showInputDialog(getString(R.string.filter_purchase_popup_enter_product_name), input) { name ->
            lifecycleScope.launch {
                showClearButton()
                try {
                    val list = dao.getPurchaseforProduct(name)
                    adapter.updateData(list.reversed())
                    recordCount.text = list.size.toString()

                    selectedTextView.text = if (list.isNotEmpty()) {
                        "${getString(R.string.filter_purchase_popup_entered_product)}: $name"
                    } else {
                        "${getString(R.string.filter_purchase_popup_result_name)} $name"
                    }
                    selectedTextView.visibility = View.VISIBLE
                } catch (e: Exception) {
                    Toast.makeText(this@PurchaseList, "Error filtering by product", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun showInputDialog(title: String, inputView: View, onApply: (String) -> Unit) {
        val padding = resources.getDimensionPixelSize(R.dimen.dialog_input_padding)
        inputView.setPadding(padding, padding, padding, padding)

        AlertDialog.Builder(this)
            .setTitle(title)
            .setView(inputView)
            .setPositiveButton(getString(R.string.popup_apply)) { dialog, _ ->
                val text = when (inputView) {
                    is EditText -> inputView.text.toString()
                    is AutoCompleteTextView -> inputView.text.toString()
                    else -> ""
                }.trim()

                if (text.isNotEmpty()) onApply(text)
                dialog.dismiss()
            }
            .setNegativeButton(getString(R.string.popup_cancel)) { dialog, _ -> dialog.cancel() }
            .show()
    }

    private fun loadPurchaseList() {
        lifecycleScope.launch {
            try {
                val purchases = dao.getAllPurchases()
                recordCount.text = purchases.size.toString()
                adapter.updateData(purchases.reversed().toMutableList())
            } catch (e: Exception) {
                Toast.makeText(this@PurchaseList, "Error loading purchase list", Toast.LENGTH_SHORT).show()
            }
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
