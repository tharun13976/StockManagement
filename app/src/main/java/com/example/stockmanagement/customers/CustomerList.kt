package com.example.stockmanagement.customers

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
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

        // Initialize RecyclerView
        recyclerView = findViewById(R.id.RV_CustomerList)
        recyclerView.layoutManager = LinearLayoutManager(this)

        dao = ManagementDatabase.Companion.getInstance(this).managementDao

        // Load all customers by default
        lifecycleScope.launch {
            val customers = dao.getAllCustomer()
            adapter = CustomerListAdapter(customers.toMutableList(), this@CustomerList) { customer ->
                val nextScreen = Intent(this@CustomerList, CustomerView::class.java)
                nextScreen.putExtra("CUSTOMER_ID", customer.cid)
                startActivity(nextScreen)
            }
            recyclerView.adapter = adapter
        }
        findViewById<Button>(R.id.Btn_FilterByName).setOnClickListener {
            lifecycleScope.launch {
                val customers = dao.getAllCustomerShortByName()
                adapter.updateData(customers)
            }
        }
        findViewById<Button>(R.id.Btn_Filter2).setOnClickListener {
            showInputPopup()
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
    private fun showInputPopup() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Enter the Customer Name")

        val input = AutoCompleteTextView(this)
        input.hint = "Enter something"
        builder.setView(input)

        builder.setPositiveButton("OK") { dialog, _ ->
            val enteredText = input.text.toString()
            if (enteredText.isNotBlank()) {
                Toast.makeText(this, "Input is correct", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Input can't be empty", Toast.LENGTH_SHORT).show()
            }
            dialog.dismiss()
        }

        builder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.cancel()
        }

        builder.show()
    }
}
