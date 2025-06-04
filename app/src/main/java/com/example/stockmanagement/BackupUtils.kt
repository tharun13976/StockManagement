package com.example.stockmanagement

import android.content.Context
import android.net.Uri
import android.util.Log
import com.google.gson.Gson
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

object BackupUtils {

    suspend fun backupDatabaseTablesToJson(context: Context, backupDir: File): Boolean {
        return try {
            // Ensure backup directory exists
            if (!backupDir.exists()) {
                val created = backupDir.mkdirs()
                if (!created) {
                    Log.e("BackupUtils", "Failed to create backup directory: ${backupDir.absolutePath}")
                    return false
                }
            }

            val dao = ManagementDatabase.getInstance(context).managementDao
            val gson = Gson()
            val filename = "backup_${SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.getDefault()).format(Date())}.zip"
            val zipFile = File(backupDir, filename)

            // Convert database entries to JSON
            val customerJson = gson.toJson(dao.getAllCustomer())
            val productJson = gson.toJson(dao.getAllProduct())
            val purchaseJson = gson.toJson(dao.getAllPurchases())
            val salesJson = gson.toJson(dao.getAllSales())

            // Create temporary JSON files
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

            // Delete temporary files
            customerFile.delete()
            productFile.delete()
            purchaseFile.delete()
            salesFile.delete()

            Log.d("BackupUtils", "Backup completed: ${zipFile.absolutePath}")
            true
        } catch (e: Exception) {
            Log.e("BackupUtils", "Error during database backup: ${e.message}", e)
            false
        }
    }

    private fun zipFiles(files: List<File>, zipFile: File) {
        ZipOutputStream(FileOutputStream(zipFile)).use { zos ->
            files.forEach { file ->
                FileInputStream(file).use { fis ->
                    zos.putNextEntry(ZipEntry(file.name))
                    val buffer = ByteArray(1024)
                    var length: Int
                    while (fis.read(buffer).also { length = it } > 0) {
                        zos.write(buffer, 0, length)
                    }
                    zos.closeEntry()
                }
            }
        }
    }

    suspend fun backupDatabaseToUri(context: Context, backupUri: Uri): Boolean {
        return try {
            val dao = ManagementDatabase.getInstance(context).managementDao
            val gson = Gson()

            val customerJson = gson.toJson(dao.getAllCustomer())
            val productJson = gson.toJson(dao.getAllProduct())
            val purchaseJson = gson.toJson(dao.getAllPurchases())
            val salesJson = gson.toJson(dao.getAllSales())

            val tempZipFile = File.createTempFile("temp_backup", ".zip", context.cacheDir)
            zipFiles(
                listOf(
                    writeTempFile("customers.json", customerJson, context),
                    writeTempFile("products.json", productJson, context),
                    writeTempFile("purchases.json", purchaseJson, context),
                    writeTempFile("sales.json", salesJson, context)
                ),
                tempZipFile
            )

            context.contentResolver.openOutputStream(backupUri)?.use { outputStream ->
                tempZipFile.inputStream().use { it.copyTo(outputStream) }
            }

            tempZipFile.delete()
            Log.d("BackupUtils", "Backup saved to SAF location.")
            true
        } catch (e: Exception) {
            Log.e("BackupUtils", "Error backing up to SAF URI", e)
            false
        }
    }

    private fun writeTempFile(fileName: String, content: String, context: Context): File {
        val file = File(context.cacheDir, fileName)
        file.writeText(content)
        return file
    }

}
