package com.example.stockmanagement

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.facebook.shimmer.ShimmerFrameLayout
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.sql.Date
import java.util.Calendar

class DashboardProduct : Fragment(R.layout.dashboard_product) {

    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var adapter: DashboardAdapter
    private val dao by lazy { ManagementDatabase.getInstance(requireContext()).managementDao }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        recyclerView = view.findViewById(R.id.Rv_productRecyclerView)
        progressBar = view.findViewById(R.id.progressBar)

        recyclerView.layoutManager = GridLayoutManager(context, 1) // 1 column grid
        adapter = DashboardAdapter(mutableListOf())
        recyclerView.adapter = adapter
        loadDashboardData()

    }

    override fun onResume() {
        super.onResume()
        loadDashboardData()
    }

    private fun loadDashboardData() {
        val shimmerLayout = view?.findViewById<ShimmerFrameLayout>(R.id.shimmer_layout)
        shimmerLayout?.stopShimmer()

        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startOfMonth = Date(calendar.timeInMillis)

        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        val endOfMonth = Date(calendar.timeInMillis)

        lifecycleScope.launch {
            try {
                val productData = withContext(Dispatchers.IO) {
                    dao.getProductSalesDashboard(startOfMonth, endOfMonth)
                }
                Log.d("Dashboard", "Product count from DAO: ${productData.size}")
                adapter.updateData(productData.toMutableList())
                shimmerLayout?.stopShimmer()
                shimmerLayout?.visibility = View.GONE
                recyclerView.adapter = adapter // Set real adapter
            } catch (e: Exception) {
                println("Error loading dashboard: ${e.message}")
            }
        }
    }
}