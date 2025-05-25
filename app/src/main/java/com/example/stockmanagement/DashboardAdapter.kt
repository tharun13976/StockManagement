package com.example.stockmanagement

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.stockmanagement.entites.ProductSalesDashboard

class DashboardAdapter(private val productList: MutableList<ProductSalesDashboard>)
 :
    RecyclerView.Adapter<DashboardAdapter.ProductViewHolder>() {

    inner class ProductViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvProductName: TextView = itemView.findViewById(R.id.tvProductName)
        val tvProductCount: TextView = itemView.findViewById(R.id.tvProductCount)
        val tvCurrentPrice: TextView = itemView.findViewById(R.id.tvCurrentPrice)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.dashboard_style, parent, false)
        return ProductViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val product = productList[position]
        holder.tvProductName.text = product.productName
        holder.tvProductCount.text = "${product.stock}"
        holder.tvCurrentPrice.text = "${product.salesCount}"

        // 🔵 Set color dynamically based on stock
        val context = holder.itemView.context
        val stockColor = when (product.stock) {
            0 -> ContextCompat.getColor(context, R.color.saleColor)
            in 1..5 -> ContextCompat.getColor(context, R.color.productColor)
            else -> ContextCompat.getColor(context, R.color.green)
        }
        holder.tvProductCount.setTextColor(stockColor)
    }
    override fun getItemCount(): Int {
        val size = productList.size
        return size
    }
    @SuppressLint("NotifyDataSetChanged")
    fun updateData(newList: List<ProductSalesDashboard>) {
        productList.clear()
        productList.addAll(newList)
        notifyDataSetChanged()
    }
}
