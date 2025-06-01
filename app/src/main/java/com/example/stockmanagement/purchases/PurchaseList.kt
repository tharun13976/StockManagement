package com.example.stockmanagement.purchases

import android.annotation.SuppressLint
import android.app.DatePickerDialog
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
import com.example.stockmanagement.products.ProductList
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class PurchaseList : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: PurchaseListAdapter
    private lateinit var dao: ManagementDao
    private lateinit var clearButton: Button
    private lateinit var selectedTextView: TextView
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

        loadPurchaseList()

        val filterDropdown = findViewById<Spinner>(R.id.Spi_PurchaseFilter)
        clearButton = findViewById(R.id.Btn_FilterClear)
        selectedTextView = findViewById(R.id.TV_SelectedText)
        selectedTextView.visibility = View.GONE
        defaultColor = clearButton.backgroundTintList

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
            override fun onItemSelected(parent: AdapterView<*>, p1: View, position: Int, id: Long) {
                val selectedLabel = parent.getItemAtPosition(position).toString()
                val selectedFilter = filterMap[selectedLabel] ?: FilterType.NONE

                if (selectedFilter != FilterType.NONE) {
                    showClearButton()
                    when (selectedFilter) {
                        FilterType.AVAILABLE -> {
                            lifecycleScope.launch {
                                val purchase = dao.getAllAvailablePurchases()
                                adapter.updateData(purchase.reversed())
                            }
                        }
                        FilterType.PRODUCT_NAME -> {
                            showProductNameFilterDialog()
                        }
                        FilterType.PURCHASE_ID -> {
                            showPurchaseIdDialog()
                        }
                        FilterType.CREATED_DATE -> {
                            showDateFilterDialog()
                        }
                        else -> {}
                    }
                } else {
                    clearButton.visibility = View.GONE
                    selectedTextView.visibility = View.GONE
                    clearButton.backgroundTintList = defaultColor
                    loadPurchaseList()
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
            loadPurchaseList()
        }
    }

    private fun showClearButton() {
        clearButton.visibility = View.VISIBLE
        clearButton.backgroundTintList = ColorStateList.valueOf(Color.RED)
    }

    @SuppressLint("SetTextI18n")
    private fun showDateFilterDialog() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val dateSetListener = DatePickerDialog.OnDateSetListener { _, selectedYear, selectedMonth, selectedDay ->
            val selectedCalendar = Calendar.getInstance().apply {
                set(selectedYear, selectedMonth, selectedDay, 0, 0, 0)
                set(Calendar.MILLISECOND, 0)
            }
            val startOfDay = java.sql.Date(selectedCalendar.timeInMillis)
            selectedCalendar.add(Calendar.DAY_OF_MONTH, 1)

            lifecycleScope.launch {
                showClearButton()
                val purchases = dao.getPurchasesByDate(startOfDay)
                if (purchases.isNotEmpty()) {
                    adapter.updateData(purchases.reversed())
                    selectedTextView.text = "${getString(R.string.filter_purchase_popup_entered_date)}: ${SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(startOfDay)}"
                } else {
                    selectedTextView.text = "${getString(R.string.filter_purchase_popup_result_date)} ${SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(startOfDay)}"
                }
                selectedTextView.visibility = View.VISIBLE
            }
        }
        DatePickerDialog(this, dateSetListener, year, month, day).show()
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
                    val result = dao.getPurchaseByID(enteredId)
                    if (result != null) {
                        adapter.updateData(listOf(result))
                        selectedTextView.text = "${getString(R.string.filter_purchase_popup_entered_id)}: $enteredId"
                    } else {
                        selectedTextView.text = "${getString(R.string.filter_purchase_popup_result_Id)} $enteredId"
                    }
                    selectedTextView.visibility = View.VISIBLE
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
                val list = dao.getPurchaseforProduct(name)
                if (list.isNotEmpty()) {
                    adapter.updateData(list.reversed())
                    selectedTextView.text = "${getString(R.string.filter_purchase_popup_entered_product)}: $name"
                } else {
                    selectedTextView.text = "${getString(R.string.filter_purchase_popup_result_name)} $name"
                }
                selectedTextView.visibility = View.VISIBLE
            }
        }
    }

    private fun showInputDialog(
        title: String,
        inputView: View,
        onApply: (inputText: String) -> Unit
    ) {
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

                if (text.isNotEmpty()) {
                    onApply(text)
                }
                dialog.dismiss()
            }
            .setNegativeButton(getString(R.string.popup_cancel)) { dialog, _ -> dialog.cancel() }
            .show()
    }

    private fun loadPurchaseList() {
        lifecycleScope.launch {
            val purchases = dao.getAllPurchases()
            adapter.updateData(purchases.toMutableList().reversed())
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
