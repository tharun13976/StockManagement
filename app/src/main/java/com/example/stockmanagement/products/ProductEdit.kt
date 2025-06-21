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
import com.example.stockmanagement.R
import com.example.stockmanagement.entites.Product
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

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.back_icon)

        val dao = ManagementDatabase.getInstance(this).managementDao
        val productId = intent.getIntExtra("PRODUCT_ID", -1)
        val measurementType = findViewById<Spinner>(R.id.Spi_ProductMeasurement)
        val productName = findViewById<EditText>(R.id.ET_ProductName)

        val measurementUnits = listOf("Kg", "Liter", "Bag", "Nos.")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, measurementUnits)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        measurementType.adapter = adapter

        measurementType.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View?,
                position: Int,
                id: Long
            ) {
                val selectedItem = parent.getItemAtPosition(position).toString()
                Toast.makeText(this@ProductEdit, "Selected: $selectedItem", Toast.LENGTH_SHORT).show()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        var oldName = ""
        lifecycleScope.launch {
            product = dao.getProductById(id = productId)
            product?.let { product ->
                productName.setText(product.productname)
                oldName = product.productname
                val measurementValue = product.measurement
                for (i in 0 until adapter.count) {
                    if (adapter.getItem(i).equals(measurementValue, ignoreCase = true)) {
                        measurementType.setSelection(i)
                        break
                    }
                }
            } ?: run {
                Toast.makeText(this@ProductEdit, "Product Not Found", Toast.LENGTH_LONG).show()
            }
        }

        val dataFetcher = GetListOfData(this, this)
        findViewById<Button>(R.id.Btn_SaveProduct).setOnClickListener {
            lifecycleScope.launch {
                val error = validateInputs(oldName, productName.text.toString().trim(), dataFetcher)
                if (error != null) {
                    Toast.makeText(this@ProductEdit, error, Toast.LENGTH_LONG).show()
                    return@launch
                }

                val confirmed = dataFetcher.showConfirmationDialog(
                    context = this@ProductEdit,
                    message = "Are you sure you want to update this product?"
                )
                if (!confirmed) return@launch

                product?.let {
                    it.productname = productName.text.trim().toString()
                    it.measurement = measurementType.selectedItem.toString()

                    if (productName.text.toString() != oldName) {
                        val updatedSales = dao.updateProductNameInSales(oldName, productName.text.toString().trim())
                        val updatedPurchases = dao.updateProductNameInPurchases(oldName, productName.text.toString().trim())
                        Log.d("UPDATE", "Updated $updatedSales sales and $updatedPurchases purchases")
                    }

                    dao.updateProduct(it)
                    Log.d("UPDATE", "Product Updated: Product Id ${it.pid}")
                    Toast.makeText(this@ProductEdit, "Product is Updated", Toast.LENGTH_LONG).show()
                    finish()
                } ?: run {
                    Toast.makeText(this@ProductEdit, "Product not found", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    suspend fun validateInputs(
        oldName: String,
        productName: String,
        dataFetcher: GetListOfData
    ): String? {
        return when {
            productName.isEmpty() -> getString(R.string.product_name_required)
            !productName.equals(oldName)&& dataFetcher.doesProductExist(productName) -> getString(R.string.product_same_name_alert)
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