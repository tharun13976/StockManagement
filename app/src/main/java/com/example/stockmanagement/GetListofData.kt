package com.example.stockmanagement

import android.content.Context
import android.widget.EditText
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import android.app.DatePickerDialog
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class GetListOfData(private val context: Context, private val lifecycleOwner: LifecycleOwner) {

    fun getAllProductNames(onResult: (List<String>) -> Unit) {
        val dao = ManagementDatabase.getInstance(context).managementDao
        lifecycleOwner.lifecycleScope.launch {
            val products = dao.getAllProduct()
            val listOfProductNames = products.map { it.productname }
            onResult(listOfProductNames)
        }
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