package com.example.stockmanagement.purchases

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
import com.example.stockmanagement.sales.SaleList
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale

class PurchaseView : AppCompatActivity() {
    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_purchase_view)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.back_icon)

        val purchaseId = intent.getIntExtra("PURCHASE_ID", -1)

        val dao = ManagementDatabase.Companion.getInstance(this).managementDao
        lifecycleScope.launch {
            val purchase =dao.getPurchaseByID(purchaseId)
            val product = dao.getProductByName(purchase.productname)
            val cost = NumberFormat.getNumberInstance(Locale("en", "IN")).format(purchase.stockprice)+"per "+ product?.measurement.toString()
            purchase.let {
                findViewById<TextView>(R.id.TV_PurchaseID).text = it.puid.toString()
                findViewById<TextView>(R.id.TV_ProductName).text = it.productname.toString()
                findViewById<TextView>(R.id.TV_StockCount).text = it.stockcount.toString()
                findViewById<TextView>(R.id.TV_AvailableCount).text = it.currentstockcount.toString()
                findViewById<TextView>(R.id.TV_StockPrice).text = "Rs. $cost"
                findViewById<TextView>(R.id.TV_PurchaseCreDate).text = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(it.stockaddeddate)
            }
        }

        findViewById<Button>(R.id.Btn_SeeSaleRecord).setOnClickListener {
            val nextScreen = Intent(this, SaleList::class.java)
            nextScreen.putExtra("FILTER_PURCHASE_ID", purchaseId.toString())
            startActivity(nextScreen)
        }

        findViewById<Button>(R.id.Btn_Back).setOnClickListener {
            finish()
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