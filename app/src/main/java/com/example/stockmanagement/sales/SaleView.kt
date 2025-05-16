package com.example.stockmanagement.sales

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.MenuItem
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.stockmanagement.ManagementDatabase
import com.example.stockmanagement.R
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale

@Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
class SaleView : AppCompatActivity() {
    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_sale_view)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.back_icon)

        val saleId = intent.getIntExtra("SALES_ID", -1)

        val dao = ManagementDatabase.Companion.getInstance(this).managementDao
        lifecycleScope.launch {
            val sale = dao.getSalesById(saleId)

            sale?.let {
                val numberFormat = NumberFormat.getNumberInstance(Locale("en", "IN"))

                val totalCost = it.costofproductsold?.let { cost ->
                    "Rs. " + numberFormat.format(cost)
                } ?: "N/A"

                val amountGiven = it.amountgiven.let { amt ->
                    "Rs. " + numberFormat.format(amt)
                }

                findViewById<TextView>(R.id.TV_SaleID).text = it.sid.toString()
                findViewById<TextView>(R.id.TV_ProductName).text = it.productname ?: "N/A"
                findViewById<TextView>(R.id.TV_CustomerName).text = it.customername
                findViewById<TextView>(R.id.TV_PurchaseID).text = it.purchaseid?.toString() ?: "N/A"
                findViewById<TextView>(R.id.TV_ProductCount).text = it.saleproductcount?.toString() ?: "N/A"
                findViewById<TextView>(R.id.TV_TotalPrice).text = totalCost
                findViewById<TextView>(R.id.TV_AmountGiven).text = amountGiven
                findViewById<TextView>(R.id.TV_AmountOnly).text = if (it.amountonly == true) "Yes" else "No"
                findViewById<TextView>(R.id.TV_SaleCreDate).text = it.salesdate.let { date -> SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(date) }
            } ?: run {
                // Handle null `sale` gracefully
                Toast.makeText(this@SaleView, "Sale not found", Toast.LENGTH_SHORT).show()
                finish()
            }

            findViewById<Button>(R.id.Btn_Back).setOnClickListener {
                finish()
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