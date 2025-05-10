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
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
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
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class PurchaseList : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: PurchaseListAdapter
    private lateinit var dao: ManagementDao

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_purchase_list)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        // Set up the toolbar
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Initialize DAO
        dao = ManagementDatabase.getInstance(this).managementDao

        // Set up RecyclerView with empty adapter
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
        val clearButton = findViewById<Button>(R.id.Btn_FilterClear)
        val defaultColor = clearButton.backgroundTintList
        findViewById<TextView>(R.id.TV_SelectedText).visibility = View.GONE

        val filterList = listOf("None","Available","Product Name","Purchase ID","Created Date")
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
                        "Available" -> {
                            lifecycleScope.launch {
                                val purchase = dao.getAllAvailablePurchases()
                                adapter.updateData(purchase.reversed())
                            }
                        }
                        "Product Name" -> {
                            showProductNameFilterDialog()
                        }
                        "Purchase ID" -> {
                            showPurchaseIdDialog()
                        }
                        "Created Date" -> {
                            showDateFilterDialog()
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
                val allData = dao.getAllPurchases()
                adapter.updateData(allData.reversed())
            }
        }
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
            val selectedText = findViewById<TextView>(R.id.TV_SelectedText)
            selectedText.text = "Selected Date: ${SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(startOfDay)}"
            selectedText.visibility = View.VISIBLE

            lifecycleScope.launch {
                val purchases = dao.getPurchasesByDate(startOfDay)
                adapter.updateData(purchases.reversed())
            }
        }
        DatePickerDialog(this, dateSetListener, year, month, day).show()
    }

    @SuppressLint("SetTextI18n")
    private fun showPurchaseIdDialog() {
        val input = EditText(this).apply {
            hint = "Enter Here"
            inputType = TYPE_CLASS_NUMBER
        }

        showInputDialog("Enter Purchase ID", "Enter Here", input) { text ->
            val enteredId = text.toIntOrNull()
            val selectedText = findViewById<TextView>(R.id.TV_SelectedText)
            if (enteredId != null && enteredId > 0) {
                lifecycleScope.launch {
                    val result = dao.getPurchaseByID(enteredId)
                    if (result != null) {
                        adapter.updateData(listOf(result))
                        selectedText.text = "Selected ID: $enteredId"
                        selectedText.visibility = View.VISIBLE
                    } else {
                        selectedText.text = "No purchase found for ID $enteredId"
                        selectedText.visibility = View.VISIBLE
                    }
                }
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun showProductNameFilterDialog() {
        val input = AutoCompleteTextView(this).apply {
            hint = "Enter Here"
            threshold = 1
        }

        GetListOfData(this, this).getAllProductNames { names ->
            input.setAdapter(ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, names))
        }

        showInputDialog("Enter Product Name", "Enter Here", input) { name ->
            val selectedText = findViewById<TextView>(R.id.TV_SelectedText)
            selectedText.text = "Selected Product: $name"
            selectedText.visibility = View.VISIBLE
            lifecycleScope.launch {
                val list = dao.getPurchaseforProduct(name)
                adapter.updateData(list.reversed())
            }
        }
    }

    private fun showInputDialog(
        title: String,
        hint: String,
        inputView: View,
        onApply: (inputText: String) -> Unit
    ) {
        val padding = resources.getDimensionPixelSize(R.dimen.dialog_input_padding)
        inputView.setPadding(padding, padding, padding, padding)

        AlertDialog.Builder(this)
            .setTitle(title)
            .setView(inputView)
            .setPositiveButton("Apply") { dialog, _ ->
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
            .setNegativeButton("Cancel") { dialog, _ -> dialog.cancel() }
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