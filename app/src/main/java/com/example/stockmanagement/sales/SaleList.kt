package com.example.stockmanagement.sales

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.stockmanagement.ManagementDao
import com.example.stockmanagement.ManagementDatabase
import com.example.stockmanagement.R
import kotlinx.coroutines.launch

class SaleList : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: SaleListAdapter
    private lateinit var dao: ManagementDao
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_sale_list)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        // Set up the toolbar
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Sales List"

        // Initialize RecyclerView
        recyclerView = findViewById(R.id.RV_SaleList)
        recyclerView.layoutManager = LinearLayoutManager(this)

        dao = ManagementDatabase.Companion.getInstance(this).managementDao
        lifecycleScope.launch {
            val sales = dao.getAllSales()
            adapter = SaleListAdapter(sales.toMutableList(), this@SaleList){ sales ->
                val nextScreen = Intent(this@SaleList, SaleView::class.java)
                nextScreen.putExtra("SALES_ID", sales.sid)
                startActivity(nextScreen)
            }
            recyclerView.adapter = adapter
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