package com.example.stockmanagement.sales

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.stockmanagement.R
import com.example.stockmanagement.entites.Sale
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale

class SaleListAdapter(
    private var sales: MutableList<Sale>,
    private val context: Context,
    private val onItemClick: (Sale) -> Unit
) : RecyclerView.Adapter<SaleListAdapter.DataViewer>(){

    inner class DataViewer(itemView: View) : RecyclerView.ViewHolder(itemView){
        private val saleId: TextView = itemView.findViewById(R.id.tvSaleId)
        private val productName: TextView = itemView.findViewById(R.id.tvSaleProductName)
        private val customerName: TextView = itemView.findViewById(R.id.tvSaleCustomerName)
        private val totalCost: TextView = itemView.findViewById(R.id.tvsaleTotalPrice)
        private val productcount: TextView = itemView.findViewById(R.id.tvSaleProductCount)
        private val amountgiven: TextView = itemView.findViewById(R.id.tvsaleAmountGiven)
        private val amountonly: TextView = itemView.findViewById(R.id.tvSaleAmountOnly)
        private val saleDate: TextView = itemView.findViewById(R.id.tvSaleDate)
        private val salePurchaseId: TextView = itemView.findViewById(R.id.tvSalePurchaseId)

        fun bind(sale: Sale){
            saleId.text = sale.sid.toString()
            productName.text = sale.productname ?: "N/A"
            customerName.text = sale.customername
            productcount.text = sale.saleproductcount?.toString() ?: "N/A"
            totalCost.text = if (sale.costofproductsold != null)
                "Rs. " + NumberFormat.getNumberInstance(Locale("en", "IN")).format(sale.costofproductsold)
            else
                "N/A"
            amountgiven.text = "Rs. " + NumberFormat.getNumberInstance(Locale("en", "IN")).format(sale.amountgiven)
            amountonly.text = if (sale.amountonly) "Yes" else "No"
            saleDate.text = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(sale.salesdate)
            salePurchaseId.text = sale.purchaseid?.toString() ?: "N/A"

            itemView.setOnClickListener {
                onItemClick(sale)
            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup,  viewType: Int ): SaleListAdapter.DataViewer {
        val view = LayoutInflater.from(context).inflate(R.layout.saleliststyle, parent, false)
        return DataViewer(view)
    }

    override fun onBindViewHolder(holder: SaleListAdapter.DataViewer, position: Int) {
        holder.bind(sales[position])
    }

    override fun getItemCount(): Int = sales.size

    fun updateData(newList: List<Sale>) {
        sales.clear()
        sales.addAll(newList)
        notifyDataSetChanged()
    }

}