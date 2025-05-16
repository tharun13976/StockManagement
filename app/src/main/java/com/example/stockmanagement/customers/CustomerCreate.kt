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
import com.example.stockmanagement.GetListOfData
import com.example.stockmanagement.ManagementDatabase
import com.example.stockmanagement.R
import com.example.stockmanagement.entites.Customer
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CustomerCreate : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_customer_create)
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

        // For Getting current date
        val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        val currentDateString = dateFormat.format(System.currentTimeMillis())
        val parsedDate = Date(dateFormat.parse(currentDateString)!!.time)

        val name= findViewById<EditText>(R.id.ET_CustomerName)
        val address= findViewById<EditText>(R.id.ET_CustomerAddress)
        val phone= findViewById<EditText>(R.id.ET_CustomerNo)

        val dataFetcher = GetListOfData(this, this)
        findViewById<Button>(R.id.Btn_SaveCustomer).setOnClickListener {
            lifecycleScope.launch {
                val error = validateInputs(
                    name.text.toString().trim(),
                    address.text.toString().trim(),
                    phone.text.toString().trim(),
                    dataFetcher
                )
                if (error != null) {
                    Toast.makeText(this@CustomerCreate, error, Toast.LENGTH_LONG).show()
                    return@launch
                }

                val confirmed = dataFetcher.showConfirmationDialog(
                    context = this@CustomerCreate,
                    message = "Are you sure you want to save this customer?"
                )
                if (!confirmed) return@launch

                val customer = Customer(
                    cid = null,
                    customername = name.text.toString().trim(),
                    phone =phone.text.toString().trim(),
                    address = address.text.toString().trim(),
                    customercreatedDate = parsedDate,
                    amountbalance = 0
                )
                dao.insertCustomer(customer)
                Log.d("INSERT", "Customer inserted")
                Toast.makeText(this@CustomerCreate, "Contact is Saved", Toast.LENGTH_LONG).show()
                finish()
            }
        }
    }


    suspend fun validateInputs(
        customerName: String,
        address: String,
        phone: String,
        dataFetcher: GetListOfData
    ): String? {
        return when {
            dataFetcher.doesCustomerExist(customerName) -> "Already we have a Customer with Same Name so Change name"
            customerName.isEmpty() -> "Customer name is required"
            customerName.length<5 -> "Customer name should be more than 4 letters"
            address.isEmpty() -> "Address is required"
            phone.isEmpty() -> "Phone number is required"
            phone.length!=10 -> "Phone number is not proper"
            else -> null
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

