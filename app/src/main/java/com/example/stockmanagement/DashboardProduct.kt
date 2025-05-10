package com.example.stockmanagement

import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.stockmanagement.entites.ProductSalesDashboard
import kotlinx.coroutines.launch
import java.sql.Date
import java.util.Calendar

//class DashboardProduct : Fragment(R.layout.dashboard_product) {
//
//    @SuppressLint("DefaultLocale")
//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        val recyclerView = view.findViewById<RecyclerView>(R.id.Rv_productRecyclerView)
//        val progressBar = view.findViewById<ProgressBar>(R.id.progressBar) // Ensure this ID matches
//        val spanCount = 1 // Number of columns for the grid
//        recyclerView.layoutManager = GridLayoutManager(context, spanCount)
//
//        val dao = ManagementDatabase.getInstance(requireContext()).managementDao
//        val calendar = Calendar.getInstance()
//
//        // Set the calendar to the first day of the current month
//        calendar.set(Calendar.DAY_OF_MONTH, 1) // Set to 1st day of the month
//        calendar.set(Calendar.HOUR_OF_DAY, 0)
//        calendar.set(Calendar.MINUTE, 0)
//        calendar.set(Calendar.SECOND, 0)
//        calendar.set(Calendar.MILLISECOND, 0)
//        val startOfMonth = Date(calendar.timeInMillis)
//
//        // Set the calendar to the last day of the current month
//        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH)) // Set to last day of the month
//        calendar.set(Calendar.HOUR_OF_DAY, 23)
//        calendar.set(Calendar.MINUTE, 59)
//        calendar.set(Calendar.SECOND, 59)
//        calendar.set(Calendar.MILLISECOND, 999)
//        val endOfMonth = Date(calendar.timeInMillis)
//
//        // Show progress bar while loading data
//        progressBar.visibility = View.VISIBLE
//
//        lifecycleScope.launch {
//            try {
//                val record = dao.getProductSalesDashboard(startOfMonth, endOfMonth)
//                // Set the adapter with the fetched data
//                recyclerView.adapter = DashboardAdapter(record)
//                progressBar.visibility = View.GONE // Hide progress bar once data is loaded
//            } catch (e: Exception) {
//                progressBar.visibility = View.GONE
//                // Handle error (for example, show an error message)
//                println("Error fetching product data: ${e.message}")
//            }
//        }
//    }
//}
class DashboardProduct : Fragment(R.layout.dashboard_product) {

    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private val dao by lazy { ManagementDatabase.getInstance(requireContext()).managementDao }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        recyclerView = view.findViewById(R.id.Rv_productRecyclerView)
        progressBar = view.findViewById(R.id.progressBar)

        recyclerView.layoutManager = GridLayoutManager(context, 1) // 1 column grid
        loadDashboardData()

    }

    private fun loadDashboardData() {
        val calendar = Calendar.getInstance()

        // First day of current month
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startOfMonth = Date(calendar.timeInMillis)

        // Last day of current month
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        val endOfMonth = Date(calendar.timeInMillis)

        progressBar.visibility = View.VISIBLE

        lifecycleScope.launch {
            try {
                val record = dao.getProductSalesDashboard(startOfMonth, endOfMonth)
                recyclerView.adapter = DashboardAdapter(record as MutableList<ProductSalesDashboard>)
            } catch (e: Exception) {
                println("Error loading dashboard: ${e.message}")
            } finally {
                progressBar.visibility = View.GONE
            }
        }
    }
}
