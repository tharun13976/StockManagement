package com.example.stockmanagement

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

object BackupUtils {
    suspend fun backupDatabaseTablesToJson(context: Context, backupDir: File): Boolean {
        return try {
            val dao = ManagementDatabase.getInstance(context).managementDao
            val gson = Gson()
            val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()) // Updated date format
            val zipFile = File(backupDir, "backup_$date.zip")  // Directly create the zip file with the date in the filename

            // Prepare the database tables to be backed up
            val customerJson = gson.toJson(dao.getAllCustomer())
            val productJson = gson.toJson(dao.getAllProduct())
            val purchaseJson = gson.toJson(dao.getAllPurchases())
            val salesJson = gson.toJson(dao.getAllSales())

            // Create temporary files for each table's data
            val customerFile = File(backupDir, "customers.json")
            val productFile = File(backupDir, "products.json")
            val purchaseFile = File(backupDir, "purchases.json")
            val salesFile = File(backupDir, "sales.json")

            customerFile.writeText(customerJson)
            productFile.writeText(productJson)
            purchaseFile.writeText(purchaseJson)
            salesFile.writeText(salesJson)

            // Zip the files
            zipFiles(listOf(customerFile, productFile, purchaseFile, salesFile), zipFile)

            // Delete the temporary JSON files
            customerFile.delete()
            productFile.delete()
            purchaseFile.delete()
            salesFile.delete()

            Log.d("BackupUtils", "Database tables successfully backed up and zipped.")
            true
        } catch (e: Exception) {
            Log.e("BackupUtils", "Error during database backup: ${e.message}", e)
            false
        }
    }

    // Zip the files into the specified zipFile
    private fun zipFiles(files: List<File>, zipFile: File) {
        ZipOutputStream(FileOutputStream(zipFile)).use { zos ->
            files.forEach { file ->
                FileInputStream(file).use { fis ->
                    val entry = ZipEntry(file.name)
                    zos.putNextEntry(entry)
                    val buffer = ByteArray(1024)
                    var length: Int
                    while (fis.read(buffer).also { length = it } > 0) {
                        zos.write(buffer, 0, length)
                    }
                }
            }
        }
    }
}
