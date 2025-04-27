package com.example.stockmanagement.customers

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.stockmanagement.ManagementDatabase
import com.example.stockmanagement.R
import com.example.stockmanagement.entites.Customer
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale

class CustomerView : AppCompatActivity() {
    private var customer: Customer? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_customer_view)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        // Set up the toolbar
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val dao = ManagementDatabase.Companion.getInstance(this).managementDao

        val customerId = intent.getIntExtra("CUSTOMER_ID", -1)

        lifecycleScope.launch {
            customer = dao.getCustomerById(id = customerId)
            val balance= NumberFormat.getNumberInstance(Locale("en", "IN")).format(customer?.amountbalance)
            customer?.let {
                findViewById<TextView>(R.id.TV_CustomerID).text = it.cid.toString()
                findViewById<TextView>(R.id.TV_CustomerName).text = it.customername
                findViewById<TextView>(R.id.TV_CustomerNo).text = it.phone
                findViewById<TextView>(R.id.TV_CustomerBalance).text = "Rs. $balance"
                findViewById<TextView>(R.id.TV_CustomerAddress).text = it.address
                findViewById<TextView>(R.id.TV_CustomerCreDate).text = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(it.customercreatedDate)
            } ?: run {
                findViewById<TextView>(R.id.TV_CustomerName).text = "Customer not found"
            }
        }

        findViewById<Button>(R.id.Btn_EditCustomer).setOnClickListener {
            val nextScreen = Intent(this, CustomerEdit::class.java)
            nextScreen.putExtra("CUSTOMER_ID", customerId)
            startActivity(nextScreen)
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
        loadCustomerData()
    }
    private fun loadCustomerData() {
        lifecycleScope.launch {
            val customerId = intent.getIntExtra("CUSTOMER_ID", -1)
            val dao = ManagementDatabase.getInstance(this@CustomerView).managementDao
            val customer = dao.getCustomerById(customerId)
            customer?.let {
                findViewById<TextView>(R.id.TV_CustomerName).text = it.customername
                findViewById<TextView>(R.id.TV_CustomerNo).text = it.phone
                findViewById<TextView>(R.id.TV_CustomerAddress).text = it.address
            }
        }
    }
}