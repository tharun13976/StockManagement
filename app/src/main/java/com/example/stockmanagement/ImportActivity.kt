package com.example.stockmanagement

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.stockmanagement.entites.Customer
import com.example.stockmanagement.entites.Product
import com.example.stockmanagement.entites.Purchase
import com.example.stockmanagement.entites.Sale
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.util.zip.ZipInputStream

class ImportActivity : AppCompatActivity() {
    private lateinit var dao: ManagementDao
    private val gson = Gson()
    private lateinit var loadingOverlay: View

    private val zipFileLauncher = registerForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri == null) {
            showAlert("No File Selected", "Please choose a valid ZIP file.")
            return@registerForActivityResult
        }

        contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)

        if (!isZipFile(uri)) {
            showAlert("Invalid File", "Please select a ZIP file.")
            return@registerForActivityResult
        }

        lifecycleScope.launch {
            try {
                contentResolver.openInputStream(uri)?.use { inputStream ->
                    val byteArray = inputStream.readBytes()
                    processZipFile(byteArray.inputStream())
                } ?: showAlert("Error", "Unable to read the selected file.")
            } catch (e: Exception) {
                Log.e("SAF", "File read error: ${e.message}")
                showAlert("Error", "Failed to import ZIP: ${e.localizedMessage}")
            }
        }
    }

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_import)

        loadingOverlay = findViewById(R.id.loadingOverlay)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        dao = ManagementDatabase.getInstance(this).managementDao

        findViewById<Button>(R.id.btnImportZip).setOnClickListener {
            selectZipFile()
        }
    }

    private fun selectZipFile() {
        zipFileLauncher.launch(arrayOf("application/zip", "application/octet-stream"))
    }

    private fun isZipFile(uri: Uri): Boolean {
        val fileName = getFileName(uri)
        return fileName?.endsWith(".zip", ignoreCase = true) == true
    }

    private fun getFileName(uri: Uri): String? {
        var name: String? = null
        val cursor = contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                name = it.getString(it.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME))
            }
        }
        return name
    }

    private fun showAlert(title: String, message: String) {
        AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("OK", null)
            .show()
    }

    private fun processZipFile(zipInputStream: InputStream) {
        loadingOverlay.visibility = View.VISIBLE
        CoroutineScope(Dispatchers.IO).launch {
            val zip = ZipInputStream(zipInputStream)
            var entry = zip.nextEntry

            val customers = mutableListOf<Customer>()
            val products = mutableListOf<Product>()
            val purchases = mutableListOf<Purchase>()
            val sales = mutableListOf<Sale>()

            while (entry != null) {
                val name = entry.name
                val content = zip.readEntryContent()

                try {
                    when {
                        name.contains("customers.json", true) -> {
                            val list = gson.fromJson(content, Array<Customer>::class.java).toList()
                            customers.addAll(list)
                        }

                        name.contains("products.json", true) -> {
                            val list = gson.fromJson(content, Array<Product>::class.java).toList()
                            products.addAll(list)
                        }

                        name.contains("purchases.json", true) -> {
                            val list = gson.fromJson(content, Array<Purchase>::class.java).toList()
                            purchases.addAll(list)
                        }

                        name.contains("sales.json", true) -> {
                            val list = gson.fromJson(content, Array<Sale>::class.java).toList()
                            sales.addAll(list)
                        }

                        else -> {
                            Log.w("ImportZip", "Unknown file skipped: $name")
                        }
                    }
                } catch (e: Exception) {
                    Log.e("ImportZip", "Failed to parse $name: ${e.message}")
                }

                zip.closeEntry()
                entry = zip.nextEntry
            }

            dao.insertAllCustomer(customers)
            dao.insertAllProduct(products)
            dao.insertAllPurchase(purchases)
            dao.insertAllSale(sales)

            withContext(Dispatchers.Main) {
                loadingOverlay.visibility = View.GONE
                Toast.makeText(this@ImportActivity, "Data imported successfully!", Toast.LENGTH_LONG).show()
            }
        }
    }


    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    private fun ZipInputStream.readEntryContent(): String {
        val buffer = ByteArray(1024)
        val output = StringBuilder()
        var length: Int
        while (this.read(buffer).also { length = it } > 0) {
            output.append(String(buffer, 0, length))
        }
        return output.toString()
    }
}
