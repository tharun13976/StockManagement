package com.example.stockmanagement

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale

class Stock_Price_Change : AppCompatActivity() {
    private lateinit var dao: ManagementDao
    private lateinit var pro: String
    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_stock_price_change)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setSupportActionBar(findViewById<Toolbar>(R.id.toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.back_icon)

        dao = ManagementDatabase.getInstance(this).managementDao

        val purchaseId = findViewById<EditText>(R.id.ET_RecordId)
        val newPrice = findViewById<EditText>(R.id.ET_NewPrice)
        val proceedButton = findViewById<Button>(R.id.Btn_Proceed)
        val backButton = findViewById<Button>(R.id.Btn_Back)
        val updateButton = findViewById<Button>(R.id.Btn_Update)

        proceedButton.setOnClickListener {
            val givenId = purchaseId.text.toString().toIntOrNull()
            if (givenId == null) {
                Toast.makeText(this, R.string.error_uppage_record_notfound, Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            lifecycleScope.launch {
                val purchaseRec = dao.getPurchaseById(givenId)
                if (purchaseRec == null) {
                    Toast.makeText(this@Stock_Price_Change, R.string.purchase_not_found, Toast.LENGTH_LONG).show()
                    return@launch
                }

                if (purchaseRec.currentstockcount <= 0) {
                    Toast.makeText(this@Stock_Price_Change, R.string.error_uppage_record_not_applicable, Toast.LENGTH_LONG).show()
                    return@launch
                }

                // Lock the ID field
                purchaseId.isEnabled = false

                // Show price and details
                findViewById<LinearLayout>(R.id.LL_PriceFields).visibility = View.VISIBLE
                findViewById<LinearLayout>(R.id.LL_layoutPurchase).visibility = View.VISIBLE
                pro=purchaseRec.productname
                val oldPrice = NumberFormat.getNumberInstance(Locale("en", "IN")).format(purchaseRec.stockprice)
                findViewById<TextView>(R.id.tvPurchaseId).text = "Purchase ID          : ${purchaseRec.puid}"
                findViewById<TextView>(R.id.tvPurchaseProduct).text = "Product Name     : ${purchaseRec.productname}"
                findViewById<TextView>(R.id.tvPurchaseQty).text = "Total No. Product: ${purchaseRec.stockcount}"
                findViewById<TextView>(R.id.tvPurchaseQtyLeft).text = "No. Product Left  : ${purchaseRec.currentstockcount}"
                findViewById<TextView>(R.id.tvPurchasePrice).text = "Existing Price       : Rs.$oldPrice"
                findViewById<TextView>(R.id.tvCreatedDate).text = "Created Date        : ${
                    SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(purchaseRec.stockaddeddate)
                }"

                proceedButton.visibility = View.GONE
                newPrice.visibility = View.VISIBLE
                updateButton.visibility = View.VISIBLE
            }
        }

        updateButton.setOnClickListener {
            val id = purchaseId.text.toString().toIntOrNull()
            val price = newPrice.text.toString().toIntOrNull()

            if (id == null) {
                Toast.makeText(this, "Invalid Record ID", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            if (price == null || price <= 0) {
                Toast.makeText(this, R.string.error_new_invalid_price, Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            lifecycleScope.launch {
                dao.updatePurchasePrice(id, price)

                val productrec = dao.getProductByName(pro)
                if (productrec != null) {
                    productrec.LatestpriceofoneUnit = price
                    dao.updateProduct(productrec)
                }
                Log.d("UPDATE", "Purchase: Purchase Id $id updated with new price $price")
                Toast.makeText(this@Stock_Price_Change, getString(R.string.price_updated), Toast.LENGTH_LONG).show()
                finish()
            }
        }

        backButton.setOnClickListener {
            finish()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean =
        if (item.itemId == android.R.id.home) {
            finish()
            true
        } else super.onOptionsItemSelected(item)
}