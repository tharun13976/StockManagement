package com.example.stockmanagement

import android.os.Bundle
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
        val dao = ManagementDatabase.getInstance(this).managementDao

        // Sample Customer
        val customers = listOf(
            Customer(null, "Alice", "9876543210", "New York", Date(System.currentTimeMillis())),
            Customer(null, "Bob", "1234567890", "Los Angeles", Date(System.currentTimeMillis()))
        )

        // Sample Products
        val products = listOf(
            Product(0, "Laptop", "pcs", Date(System.currentTimeMillis()), 1000, 50),
            Product(0, "Phone", "pcs", Date(System.currentTimeMillis()), 500, 100)
        )

        // Sample Purchases
        val purchases = listOf(
            Purchase(null, "Laptop", Date(System.currentTimeMillis()), 10, 950),
            Purchase(null, "Phone", Date(System.currentTimeMillis()), 20, 450)
        )

        // Sample Sales
        val sales = listOf(
            Sale(null, "Alice", "Laptop", Date(System.currentTimeMillis()), 1, 2, 2000, 2000, true),
            Sale(null, "Bob", "Phone", Date(System.currentTimeMillis()), 2, 1, 500, 300, false)
        )
        lifecycleScope.launch {
            customers.forEach { dao.insertCustomer(it) }
            products.forEach { dao.insertProduct(it) }
            purchases.forEach { dao.insertPurchase(it) }
            sales.forEach { dao.insertSale(it) }
        }
    }
}