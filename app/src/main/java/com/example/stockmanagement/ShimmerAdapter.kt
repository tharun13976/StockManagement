package com.example.stockmanagement

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

class ShimmerAdapter(private val itemCount: Int = 6) :
    RecyclerView.Adapter<ShimmerAdapter.ShimmerViewHolder>() {

    inner class ShimmerViewHolder(view: View) : RecyclerView.ViewHolder(view)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ShimmerViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.shimmer_dashboard_item, parent, false)
        return ShimmerViewHolder(view)
    }

    override fun onBindViewHolder(holder: ShimmerViewHolder, position: Int) {
        // Nothing needed
    }

    override fun getItemCount(): Int = itemCount
}