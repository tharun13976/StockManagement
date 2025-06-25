package com.example.stockmanagement.products

import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.stockmanagement.ExitConfirmation
import com.example.stockmanagement.GetListOfData
import com.example.stockmanagement.ManagementDatabase
import com.example.stockmanagement.entites.Product
import com.example.stockmanagement.R
import kotlinx.coroutines.launch

class ProductEdit : AppCompatActivity() {
    private var product: Product? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_product_edit)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Setup toolbar
        setSupportActionBar(findViewById<Toolbar>(R.id.toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.back_icon)

        // Views
        val dao = ManagementDatabase.getInstance(this).managementDao
        val productId = intent.getIntExtra("PRODUCT_ID", -1)
        val measurementType = findViewById<Spinner>(R.id.Spi_ProductMeasurement)
        val productName = findViewById<EditText>(R.id.ET_ProductName)
        val productPrice = findViewById<EditText>(R.id.ET_ProductPrice)

        val measurementUnits = listOf("Kg", "Liter", "Bag", "Nos.")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, measurementUnits)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        measurementType.adapter = adapter

        measurementType.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                // Optional: Handle selection
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        var oldName = ""
        lifecycleScope.launch {
            product = dao.getProductById(productId)
            product?.let { product ->
                productName.setText(product.productname)
                productPrice.setText(product.LatestpriceofoneUnit.toString())
                oldName = product.productname

                // Pre-select measurement in spinner
                val measurementValue = product.measurement
                for (i in 0 until adapter.count) {
                    if (adapter.getItem(i).equals(measurementValue, ignoreCase = true)) {
                        measurementType.setSelection(i)
                        break
                    }
                }
            } ?: Toast.makeText(this@ProductEdit, getString(R.string.product_not_found_error), Toast.LENGTH_LONG).show()
        }

        val dataFetcher = GetListOfData(this, this)
        findViewById<Button>(R.id.Btn_SaveProduct).setOnClickListener {
            lifecycleScope.launch {
                val newName = productName.text.toString().trim()
                val error = validateInputs(oldName, newName, dataFetcher,productPrice.text.toString().toIntOrNull())
                if (error != null) {
                    Toast.makeText(this@ProductEdit, error, Toast.LENGTH_LONG).show()
                    return@launch
                }

                val confirmed = dataFetcher.showConfirmationDialog(
                    context = this@ProductEdit,
                    message = getString(R.string.product_update_conformation)
                )
                if (!confirmed) return@launch

                val price = productPrice.text.toString().toInt()

                product?.let {
                    it.productname = newName
                    it.measurement = measurementType.selectedItem.toString()
                    it.LatestpriceofoneUnit = price

                    if (newName != oldName) {
                        dao.updateProductNameInSales(oldName, newName)
                        dao.updateProductNameInPurchases(oldName, newName)
                        Log.d("UPDATE", "Updated sales and purchases")
                    }

                    dao.updateProduct(it)
                    Log.d("UPDATE", "Product Updated: Product Id ${it.pid}")
                    Toast.makeText(this@ProductEdit, getString(R.string.product_updated), Toast.LENGTH_LONG).show()
                    finish()
                } ?: Toast.makeText(this@ProductEdit, getString(R.string.product_not_found_error), Toast.LENGTH_LONG).show()
            }
        }
    }

    private suspend fun validateInputs(
        oldName: String,
        productName: String,
        dataFetcher: GetListOfData,
        price: Int?
    ): String? {
        return when {
            productName.isEmpty() -> getString(R.string.product_name_required)
            !productName.equals(oldName, ignoreCase = true) &&
                    dataFetcher.doesProductExist(productName) ->
                getString(R.string.product_same_name_alert)
            price==null -> getString(R.string.product_update_price_error)
            else -> null
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (item.itemId == android.R.id.home) {
            ExitConfirmation().show(this) {
                finish()
            }
            true
        } else {
            super.onOptionsItemSelected(item)
        }
    }
}
