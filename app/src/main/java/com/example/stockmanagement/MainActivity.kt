package com.example.stockmanagement

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.stockmanagement.customers.CustomerCreate
import com.example.stockmanagement.customers.CustomerList
import com.example.stockmanagement.products.ProductCreate
import com.example.stockmanagement.purchases.PurchaseCreate
import com.example.stockmanagement.sales.SaleCreate
import kotlinx.coroutines.launch



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
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setHomeButtonEnabled(true)
        supportActionBar?.title = "Main Menu"

        findViewById<Button>(R.id.Btn_Customer).setOnClickListener {
            val nextscreen = Intent(this, CustomerCreate::class.java)
            startActivity(nextscreen)
        }

        findViewById<Button>(R.id.Btn_Product).setOnClickListener {
            val nextscreen = Intent(this, ProductCreate::class.java)
            startActivity(nextscreen)
        }

        findViewById<Button>(R.id.Btn_Purchase).setOnClickListener {
            val nextscreen = Intent(this, PurchaseCreate::class.java)
            startActivity(nextscreen)
        }

        findViewById<Button>(R.id.Btn_Sales).setOnClickListener {
            val nextscreen = Intent(this, SaleCreate::class.java)
            startActivity(nextscreen)
        }

        findViewById<Button>(R.id.Btn_CustomerList).setOnClickListener {
            val nextscreen = Intent(this, CustomerList::class.java)
            startActivity(nextscreen)
        }


        val dao = ManagementDatabase.getInstance(this).managementDao
        findViewById<Button>(R.id.Btn_test).setOnClickListener {
            Toast.makeText(this, "Test this app", Toast.LENGTH_LONG).show()
            lifecycleScope.launch {
                val customerrec = dao.getAllCustomer()
                println(customerrec)
                val productrec = dao.getAllProduct()
                println(productrec)
                val salerec = dao.getAllSales()
                println(salerec)
                val purchaserec = dao.getAllPurchases()
                println(purchaserec)
            }
        }
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                // Do nothing when home icon is clicked
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}