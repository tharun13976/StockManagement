package com.example.stockmanagement

import android.content.Context
import android.widget.EditText
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.datepicker.MaterialDatePicker
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

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
            dao.countCustomerByName(name) > 0
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun doesProductExist(name: String): Boolean {
        return try {
            dao.countProductByName(name) > 0
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }

    }

    suspend fun showConfirmationDialog(
        context: Context,
        title: String = context.getString(R.string.popup_title),
        message: String,
        positiveText: String = context.getString(R.string.popup_save),
        negativeText: String = context.getString(R.string.popup_cancel)
    ): Boolean = suspendCancellableCoroutine { cont ->
        AlertDialog.Builder(context)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton(positiveText) { dialog, _ ->
                cont.resume(true)
                dialog.dismiss()
            }
            .setNegativeButton(negativeText) { dialog, _ ->
                cont.resume(false)
                dialog.dismiss()
            }
            .setOnCancelListener {
                cont.resume(false)
            }
            .show()
    }


    fun getCurrentDate(): Date {
        val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        val currentDateString = dateFormat.format(System.currentTimeMillis())
        val parsedDate = Date(dateFormat.parse(currentDateString)!!.time)
        return parsedDate
    }

//    companion object {
//        fun showDatePicker(context: Context, editText: EditText, format: String = "dd-MM-yyyy") {
//            val calendar = Calendar.getInstance()
//            val dateFormat = SimpleDateFormat(format, Locale.getDefault())
//            editText.text.toString().takeIf { it.isNotEmpty() }?.let {
//                try {
//                    calendar.time = dateFormat.parse(it) ?: calendar.time
//                } catch (e: Exception) {
//                    e.printStackTrace()
//                }
//            }
//            DatePickerDialog(
//                context,
//                { _, year, month, dayOfMonth ->
//                    calendar.set(year, month, dayOfMonth)
//                    editText.setText(dateFormat.format(calendar.time))
//                },
//                calendar.get(Calendar.YEAR),
//                calendar.get(Calendar.MONTH),
//                calendar.get(Calendar.DAY_OF_MONTH)
//            ).show()
//        }
//    }


    companion object {
        fun showDatePicker(context: Context, editText: EditText, format: String = "dd-MM-yyyy") {
            val dateFormat = SimpleDateFormat(format, Locale.getDefault())

            // Pre-select the date if available
            val currentDate = try {
                dateFormat.parse(editText.text.toString())?.time ?: MaterialDatePicker.todayInUtcMilliseconds()
            } catch (e: Exception) {
                MaterialDatePicker.todayInUtcMilliseconds()
            }

            val picker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Select Date")
                .setSelection(currentDate)
                .build()

            picker.addOnPositiveButtonClickListener { selectedDateInMillis ->
                val calendar = Calendar.getInstance()
                calendar.timeInMillis = selectedDateInMillis
                editText.setText(dateFormat.format(calendar.time))
            }

            picker.show((context as AppCompatActivity).supportFragmentManager, "MATERIAL_DATE_PICKER")
        }

        fun showDatePicker1(
            context: Context,
            onDateSelected: (Date) -> Unit,
            format: String = "dd-MM-yyyy"
        ) {
            val dateFormat = SimpleDateFormat(format, Locale.getDefault())

            val currentDate = try {
                MaterialDatePicker.todayInUtcMilliseconds()
            } catch (e: Exception) {
                MaterialDatePicker.todayInUtcMilliseconds()
            }

            val picker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Select Date")
                .setSelection(currentDate)
                .build()

            picker.addOnPositiveButtonClickListener { selectedDateInMillis ->
                val calendar = Calendar.getInstance().apply {
                    timeInMillis = selectedDateInMillis
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                onDateSelected(calendar.time)
            }

            picker.show((context as AppCompatActivity).supportFragmentManager, "MATERIAL_DATE_PICKER")
        }
    }

}