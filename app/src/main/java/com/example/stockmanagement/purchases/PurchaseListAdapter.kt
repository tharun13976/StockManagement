package com.example.stockmanagement.purchases

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.stockmanagement.R
import com.example.stockmanagement.entites.Purchase
import java.text.NumberFormat
import java.util.Locale

class PurchaseListAdapter(
    private var purchases: MutableList<Purchase>,
    private val context: Context,
    private val onItemClick: (Purchase) -> Unit
) : RecyclerView.Adapter<PurchaseListAdapter.DataViewer>() {

    inner class DataViewer(itemView: View) : RecyclerView.ViewHolder(itemView){
        private val Id: TextView = itemView.findViewById(R.id.tvPurchaseId)
        private val name: TextView = itemView.findViewById(R.id.tvPurchaseProductName)
        private val cost: TextView = itemView.findViewById(R.id.tvStockUnitPrice)
        private val count: TextView = itemView.findViewById(R.id.tvStockCount)
        private val currentcount: TextView = itemView.findViewById(R.id.tvCurrentCount)

        fun bind(purchase: Purchase){
            Id.text = purchase.puid.toString()
            name.text = purchase.productname.toString()
            count.text = purchase.stockcount.toString()
            cost.text ="Rs. "+NumberFormat.getNumberInstance(Locale("en", "IN")).format(purchase.stockprice)
            currentcount.text = purchase.currentstockcount.toString()
            itemView.setOnClickListener {
                onItemClick(purchase)
            }
        }
    }

    override fun onCreateViewHolder( parent: ViewGroup, viewType: Int): PurchaseListAdapter.DataViewer {
        val view = LayoutInflater.from(context).inflate(R.layout.purchaseliststyle, parent, false)
        return DataViewer(view)
    }

    override fun onBindViewHolder(holder: PurchaseListAdapter.DataViewer, position: Int) {
        holder.bind(purchases[position])
    }

    override fun getItemCount(): Int =purchases.size

}