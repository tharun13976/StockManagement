package com.example.stockmanagement

import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var viewPager: ViewPager2
    private lateinit var tabLayout: TabLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        viewPager = findViewById(R.id.viewPager)
        tabLayout = findViewById(R.id.tabLayout)

        viewPager.adapter = MainPagerAdapter(this)

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "Create Record"   // Tab 0 now means Create
                1 -> "Record List"       // Tab 1 now means List
                else -> ""
            }
        }.attach()

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setHomeButtonEnabled(true)
        supportActionBar?.title = "Main Menu"

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
}
