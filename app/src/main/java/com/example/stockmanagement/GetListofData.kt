package com.example.stockmanagement

import android.content.Context
import android.widget.EditText
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import android.app.DatePickerDialog
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class GetListOfData(context: Context, private val lifecycleOwner: LifecycleOwner) {
    val dao = ManagementDatabase.getInstance(context).managementDao
    fun getAllProductNames(onResult: (List<String>) -> Unit) {
        lifecycleOwner.lifecycleScope.launch {
            try {
                val productNames = dao.getProductNames()
                onResult(productNames)
            } catch (e: Exception) {
                e.printStackTrace()
                onResult(emptyList())
            }
        }
    }

    fun getAllCustomerNames(onResult: (List<String>) -> Unit) {
        lifecycleOwner.lifecycleScope.launch {
            try {
                val customerNames = dao.getCustomersName() // returns List<Customer>
                onResult(customerNames)
            } catch (e: Exception) {
                e.printStackTrace()
                onResult(emptyList())
            }
        }
    }

    suspend fun doesCustomerExist(name: String): Boolean {
        return try {
            val customerNames = dao.getCustomersName()
            customerNames.contains(name)
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun doesProductExist(name: String): Boolean {
        return try {
            val productNames = dao.getProductNames() // This must be suspend
            productNames.contains(name)
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }

    }
    fun getCurrentDate(): Date {
        val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        val currentDateString = dateFormat.format(System.currentTimeMillis())
        val parsedDate = Date(dateFormat.parse(currentDateString)!!.time)
        return parsedDate
    }

    fun formatIndianNumber(number: Int): String {
        val formatter = NumberFormat.getNumberInstance(Locale("en", "IN"))
        return formatter.format(number)
    }

    companion object {
        fun showDatePicker(context: Context, editText: EditText, format: String = "dd-MM-yyyy") {
            val calendar = Calendar.getInstance()
            val dateFormat = SimpleDateFormat(format, Locale.getDefault())
            editText.text.toString().takeIf { it.isNotEmpty() }?.let {
                try {
                    calendar.time = dateFormat.parse(it) ?: calendar.time
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            DatePickerDialog(
                context,
                { _, year, month, dayOfMonth ->
                    calendar.set(year, month, dayOfMonth)
                    editText.setText(dateFormat.format(calendar.time))
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }
    }
}