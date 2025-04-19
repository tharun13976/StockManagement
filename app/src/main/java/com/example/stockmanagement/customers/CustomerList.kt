package com.example.stockmanagement.customers

import android.os.Bundle
import android.view.MenuItem
import android.widget.Button
import androidx.appcompat.widget.Toolbar
import androidx.activity.enableEdgeToEdge
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
                // handle click if needed
            }
            recyclerView.adapter = adapter
        }

        findViewById<Button>(R.id.Btn_FilterByName).setOnClickListener {
            lifecycleScope.launch {
                val customers = dao.getAllCustomerShortByName()
                adapter.updateData(customers)

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
