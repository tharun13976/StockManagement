package com.example.stockmanagement.customers

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
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
        val dao = ManagementDatabase.Companion.getInstance(this).managementDao

        // For Getting current date
        val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        val currentDateString = dateFormat.format(System.currentTimeMillis())
        val parsedDate = Date(dateFormat.parse(currentDateString)!!.time)

        val name= findViewById<EditText>(R.id.ET_CustomerName)
        val address= findViewById<EditText>(R.id.ET_CustomerAddress)
        val phone= findViewById<EditText>(R.id.ET_CustomerNo)
        findViewById<Button>(R.id.Btn_SaveCustomer).setOnClickListener {
            if (name.text.isEmpty() || name.text.length < 5) {
                Toast.makeText(this, "Name is not Proper", Toast.LENGTH_LONG).show()
            } else if (address.text.isEmpty() || address.text.length < 5) {
                Toast.makeText(this, "Adddress is not Proper", Toast.LENGTH_LONG).show()
            } else if (phone.text.isEmpty() || phone.text.length != 10) {
                Toast.makeText(this, "Phone Number is not Proper", Toast.LENGTH_LONG).show()
            } else {
                val customer = Customer(
                    cid = null,
                    customername = name.text.toString(),
                    phone =phone.text.toString(),
                    address = address.text.toString(),
                    customercreatedDate = parsedDate,
                    amountbalance = 0
                )
                lifecycleScope.launch {
                    dao.insertCustomer(customer)
                    Log.d("INSERT", "Customer inserted: $customer")
                }
                Toast.makeText(this, "Contact is Saved", Toast.LENGTH_LONG).show()
                finish()
            }
        }


    }
}