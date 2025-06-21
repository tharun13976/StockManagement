package com.example.stockmanagement.products

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
import com.example.stockmanagement.entites.Product
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale

class ProductView : AppCompatActivity() {
    private var product: Product? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_product_view)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.back_icon)

        val dao = ManagementDatabase.Companion.getInstance(this).managementDao
        val productId = intent.getIntExtra("PRODUCT_ID", -1)
        lifecycleScope.launch {
            product = dao.getProductById(productId)
            val latestprice= NumberFormat.getNumberInstance(Locale("en", "IN")).format(product?.LatestpriceofoneUnit)
            product?.let {
                findViewById<TextView>(R.id.TV_ProductID).text = it.pid.toString()
                findViewById<TextView>(R.id.TV_ProductName).text = it.productname.toString()
                findViewById<TextView>(R.id.TV_ProductMeasureunit).text = it.measurement.toString()
                findViewById<TextView>(R.id.TV_ProductCount).text = it.currentstockcount.toString()
                findViewById<TextView>(R.id.TV_ProductPrice).text = "Rs. $latestprice"
                findViewById<TextView>(R.id.TV_ProductCreDate).text = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(it.productcreateddate)
            }?: run {
                findViewById<TextView>(R.id.TV_ProductName).text = "Product not found"
            }
        }

        findViewById<Button>(R.id.Btn_EditProduct).setOnClickListener {
            val nextScreen = Intent(this, ProductEdit::class.java)
            nextScreen.putExtra("PRODUCT_ID", productId)
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
        loadProductData()
    }
    private fun loadProductData() {
        lifecycleScope.launch {
            val productId = intent.getIntExtra("PRODUCT_ID", -1)
            val dao = ManagementDatabase.getInstance(this@ProductView).managementDao
            val product = dao.getProductById(productId)
            product?.let {
                findViewById<TextView>(R.id.TV_ProductName).text = it.productname.toString()
                findViewById<TextView>(R.id.TV_ProductMeasureunit).text = it.measurement.toString()
            }
        }
    }
}