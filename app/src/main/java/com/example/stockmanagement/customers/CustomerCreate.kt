package com.example.stockmanagement.customers

import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ScrollView
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

        // Apply system + IME insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBarsInsets = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val imeInsets = insets.getInsets(WindowInsetsCompat.Type.ime())

            v.setPadding(
                systemBarsInsets.left,
                systemBarsInsets.top,
                systemBarsInsets.right,
                imeInsets.bottom
            )
            insets
        }

        // Toolbar setup
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.back_icon)

        val dao = ManagementDatabase.getInstance(this).managementDao

        // Date
        val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        val currentDateString = dateFormat.format(System.currentTimeMillis())
        val parsedDate = Date(dateFormat.parse(currentDateString)!!.time)

        val name = findViewById<EditText>(R.id.ET_CustomerName)
        val address = findViewById<EditText>(R.id.ET_CustomerAddress)
        val phone = findViewById<EditText>(R.id.ET_CustomerNo)

        val dataFetcher = GetListOfData(this, this)

        // Scroll to view on focus
        val focusScrollListener = View.OnFocusChangeListener { v, hasFocus ->
            if (hasFocus && v is EditText) scrollToView(v)
        }
        name.onFocusChangeListener = focusScrollListener
        phone.onFocusChangeListener = focusScrollListener
        address.onFocusChangeListener = focusScrollListener

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
                    message = getString(R.string.customer_save_conformation)
                )
                if (!confirmed) return@launch

                val customer = Customer(
                    cid = null,
                    customername = name.text.toString().trim(),
                    phone = phone.text.toString().trim(),
                    address = address.text.toString().trim(),
                    customercreatedDate = parsedDate,
                    amountbalance = 0
                )
                dao.insertCustomer(customer)
                Log.d("INSERT", "Customer inserted")
                Toast.makeText(this@CustomerCreate, getString(R.string.customer_saved), Toast.LENGTH_LONG).show()
                finish()
            }
        }
    }

    private fun scrollToView(view: View) {
        val scrollView = findViewById<ScrollView>(R.id.scrollView)
        scrollView.post {
            scrollView.smoothScrollTo(0, view.top - 20)
        }
    }

    suspend fun validateInputs(
        customerName: String,
        address: String,
        phone: String,
        dataFetcher: GetListOfData
    ): String? {
        return when {
            dataFetcher.doesCustomerExist(customerName) -> getString(R.string.customer_same_name_alert)
            customerName.isEmpty() -> getString(R.string.customer_name_required)
            customerName.length < 5 -> getString(R.string.customer_name_length)
            address.isEmpty() -> getString(R.string.customer_address_required)
            phone.isEmpty() -> getString(R.string.customer_phone_required)
            phone.length != 10 -> getString(R.string.customer_phone_length)
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
