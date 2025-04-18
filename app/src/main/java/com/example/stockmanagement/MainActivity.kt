package com.example.stockmanagement

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.stockmanagement.entites.Customer
import com.example.stockmanagement.entites.Product
import com.example.stockmanagement.entites.Purchase
import com.example.stockmanagement.entites.Sale
import kotlinx.coroutines.launch
import java.sql.Date


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        findViewById<Button>(R.id.Btn_Customer).setOnClickListener {
            val nextscreen = Intent(this, CustomerCreate::class.java)
            startActivity(nextscreen)
        }



        val dao = ManagementDatabase.getInstance(this).managementDao
        findViewById<Button>(R.id.Btn_test).setOnClickListener {
            Toast.makeText(this, "Test this app", Toast.LENGTH_LONG).show()
            lifecycleScope.launch {
                val customerrec = dao.getAllCustomer()
                println(customerrec)
            }
        }
    }
}