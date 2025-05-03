package com.example.stockmanagement.customers

import android.annotation.SuppressLint
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.text.InputFilter
import android.text.InputType.TYPE_CLASS_NUMBER
import android.text.InputType.TYPE_CLASS_PHONE
import android.text.method.DigitsKeyListener
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.stockmanagement.GetListOfData
import com.example.stockmanagement.ManagementDao
import com.example.stockmanagement.ManagementDatabase
import com.example.stockmanagement.R
import kotlinx.coroutines.launch

class CustomerList : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: CustomerListAdapter
    private lateinit var dao: ManagementDao

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_customer_list)

        // Set up the toolbar
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Customer List"

        // Initialize RecyclerView and adapter with an empty list
        recyclerView = findViewById(R.id.RV_CustomerList)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = CustomerListAdapter(mutableListOf(), this@CustomerList) { customer ->
            val nextScreen = Intent(this@CustomerList, CustomerView::class.java)
            nextScreen.putExtra("CUSTOMER_ID", customer.cid)
            startActivity(nextScreen)
        }
        recyclerView.adapter = adapter

        // Initialize DAO
        dao = ManagementDatabase.getInstance(this).managementDao

        // Load initial data
        loadCustomerList()

        val filterDropdown = findViewById<Spinner>(R.id.Spi_PurchaseFilter)
        val clearButton = findViewById<Button>(R.id.Btn_FilterClear)
        val defaultColor = clearButton.backgroundTintList
        findViewById<TextView>(R.id.TV_SelectedText).visibility = View.GONE

        val filterList = listOf("None","Balance","Name ASC","Name DESC","Customer Name","Phone Number")
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
                        "Balance" -> {
                            lifecycleScope.launch {
                                val customer = dao.getAllCustomersSortByBalance()
                                adapter.updateData(customer)
                            }
                        }
                        "Name ASC" -> {
                            lifecycleScope.launch {
                                val customer = dao.getAllCustomerSortByNameAsc()
                                adapter.updateData(customer)
                            }
                        }
                        "Name DESC" -> {
                            lifecycleScope.launch {
                                val customer = dao.getAllCustomerSortByNameDesc()
                                adapter.updateData(customer)
                            }
                        }
                        "Customer Name" -> {
                            showCustomerNameFilterDialog()
                        }
                        "Phone Number" -> {
                            showCustomerPhoneNoDialog()
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
                val allData = dao.getAllCustomer()
                adapter.updateData(allData)
            }
        }
    }
    private fun showCustomerPhoneNoDialog() {
        val input = EditText(this).apply {
            hint = "Enter Here"
            inputType = TYPE_CLASS_PHONE
            val padding = resources.getDimensionPixelSize(R.dimen.dialog_input_padding)
            setPadding(padding, padding, padding, padding)
            filters = arrayOf(InputFilter.LengthFilter(10))
            keyListener = DigitsKeyListener.getInstance("0123456789")
        }
        AlertDialog.Builder(this)
            .setTitle("Enter Phone Number")
            .setView(input)
            .setPositiveButton("Apply") { dialog, _ ->
                val phoneNo = input.text.toString().toIntOrNull()
                val selectedText = findViewById<TextView>(R.id.TV_SelectedText)
                if (phoneNo != null && phoneNo.toString().length >=3 ) {
                    selectedText.visibility = View.VISIBLE
                    lifecycleScope.launch {
                        selectedText.text= "Entered Number: $phoneNo"
                       val result = dao.getAllCustomersPhone(phoneNo.toString())
                        adapter.updateData(result)
                    }
                }
                else{
                    Toast.makeText(this@CustomerList,"Enter Phone Number more than 2 digit", Toast.LENGTH_LONG).show()
                }
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ -> dialog.cancel() }
            .show()
    }

    @SuppressLint("SetTextI18n")
    private fun showCustomerNameFilterDialog() {
        val input = AutoCompleteTextView(this).apply {
            hint = "Enter Here"
            threshold = 1
            val padding = resources.getDimensionPixelSize(R.dimen.dialog_input_padding)
            setPadding(padding, padding, padding, padding)
        }
        AlertDialog.Builder(this)
            .setTitle("Enter Customer Name")
            .setView(input)
            .setPositiveButton("Apply") { dialog, _ ->
                val name = input.text.toString().trim()
                val selectedText = findViewById<TextView>(R.id.TV_SelectedText)
                if (name.isNotEmpty()) {
                    selectedText.text = "Selected Customer: $name"
                    selectedText.visibility = View.VISIBLE
                    lifecycleScope.launch {
                        val customer= dao.getCustomerByname(name)
                        if(customer != null){
                            adapter.updateData(listOf(customer))
                        } else{
                            selectedText.text = "Customer Not Found"
                        }
                    }
                } else {
                    selectedText.visibility = View.GONE
                }
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ -> dialog.cancel() }
            .show()
        GetListOfData(this, this).getAllCustomerNames{ names ->
            input.setAdapter(ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, names))
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


    override fun onResume() {
        super.onResume()
        loadCustomerList()
    }

    private fun loadCustomerList() {
        lifecycleScope.launch {
            val customers = dao.getAllCustomer()
            adapter.updateData(customers.toMutableList())
        }
    }
}
