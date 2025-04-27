package com.example.stockmanagement.products

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.stockmanagement.R
import com.example.stockmanagement.entites.Product
import java.text.NumberFormat
import java.util.Locale

class ProductListAdapter(
    private var products: MutableList<Product>,
    private val context: Context,
    private val onItemClick: (Product) -> Unit
) : RecyclerView.Adapter<ProductListAdapter.DataViewer>()  {

    inner class DataViewer(itemView: View) : RecyclerView.ViewHolder(itemView){
        private val tvId: TextView = itemView.findViewById(R.id.tvProductId)
        private val tvname: TextView = itemView.findViewById(R.id.tvProductName)
        private val tvcost: TextView = itemView.findViewById(R.id.tvCurrentPrice)
        private val tvcount: TextView = itemView.findViewById(R.id.tvProductCount)

        fun bind(product: Product){
            tvId.text = product.pid.toString()
            tvname.text = product.productname.toString()
            tvcount.text = product.currentstockcount.toString()
            tvcost.text ="Rs. "+NumberFormat.getNumberInstance(Locale("en", "IN")).format(product.LatestpriceofoneUnit)
            itemView.setOnClickListener {
                onItemClick(product)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductListAdapter.DataViewer {
        val view = LayoutInflater.from(context).inflate(R.layout.productliststyle, parent, false)
        return DataViewer(view)
    }

    override fun onBindViewHolder(holder: ProductListAdapter.DataViewer, position: Int) {
        holder.bind(products[position])
    }

    override fun getItemCount(): Int = products.size

    fun updateData(newList: List<Product>) {
        products.clear()
        products.addAll(newList)
        notifyDataSetChanged()
    }
}