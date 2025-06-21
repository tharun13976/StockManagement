package com.example.stockmanagement.customers

import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.Button
import android.widget.EditText
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
import com.example.stockmanagement.entites.Customer
import kotlinx.coroutines.launch

class CustomerEdit : AppCompatActivity() {
    private var customer: Customer? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_customer_edit)
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

        val dao = ManagementDatabase.Companion.getInstance(this).managementDao
        val customerId = intent.getIntExtra("CUSTOMER_ID", -1)

        val name= findViewById<EditText>(R.id.ET_CustomerName)
        val address= findViewById<EditText>(R.id.ET_CustomerAddress)
        val phone= findViewById<EditText>(R.id.ET_CustomerNo)
        var oldname=""

        lifecycleScope.launch {
            customer = dao.getCustomerById(id = customerId)
            customer?.let {
                name.setText(it.customername)
                phone.setText(it.phone)
                address.setText(it.address)
                oldname=it.customername.toString()
            } ?: run {
                Toast.makeText(this@CustomerEdit, getString(R.string.customer_not_found), Toast.LENGTH_LONG).show()
            }
        }

        val dataFetcher = GetListOfData(this, this)
        findViewById<Button>(R.id.Btn_SaveCustomer).setOnClickListener {
            lifecycleScope.launch {
                val error = validateInputs(
                    oldname.toString(),
                    name.text.toString().trim(),
                    address.text.toString().trim(),
                    phone.text.toString().trim(),
                    dataFetcher
                )
                if (error != null) {
                    Toast.makeText(this@CustomerEdit, error, Toast.LENGTH_LONG).show()
                    return@launch
                }

                val confirmed = dataFetcher.showConfirmationDialog(
                    context = this@CustomerEdit,
                    message = getString(R.string.customer_update_conformation)
                )
                if (!confirmed) return@launch

                customer?.let {
                    it.customername = name.text.toString().trim()
                    it.phone = phone.text.toString().trim()
                    it.address = address.text.toString().trim()
                    dao.updateCustomer(it)
                    Log.d("UPDATE", "Customer updated: Customer Id ${it.cid}")
                    if(name.text.toString() != oldname){
                        dao.updateCustomerNameInSales(oldname,name.text.trim().toString())
                    }
                    Log.d("UPDATE", "Customer Updated: Customer Id ${it.cid}")
                    Toast.makeText(this@CustomerEdit, getString(R.string.customer_updated), Toast.LENGTH_LONG).show()
                    finish()
                } ?: run {
                    Toast.makeText(this@CustomerEdit, getString(R.string.customer_not_found), Toast.LENGTH_LONG).show()
                }
            }
        }
    }
    suspend fun validateInputs(
        oldName: String,
        customerName: String,
        address: String,
        phone: String,
        dataFetcher: GetListOfData
    ): String? {
        return when {
            customerName.isEmpty() -> getString(R.string.customer_name_required)
            customerName.length<5 -> getString(R.string.customer_name_length)
            !customerName.equals(oldName)&& dataFetcher.doesCustomerExist(customerName) -> getString(R.string.customer_same_name_alert)
            address.isEmpty() -> getString(R.string.customer_address_required)
            phone.isEmpty() -> getString(R.string.customer_phone_required)
            phone.length != 10 -> getString(R.string.customer_phone_length)
            else -> null
        }
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (item.itemId == android.R.id.home) {
            ExitConfirmation().show(this) {
                finish()
            }
            true
        } else {
            super.onOptionsItemSelected(item)
        }
    }
}