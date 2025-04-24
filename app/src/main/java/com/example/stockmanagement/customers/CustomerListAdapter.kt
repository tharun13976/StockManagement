package com.example.stockmanagement.customers

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.stockmanagement.R
import com.example.stockmanagement.entites.Customer

class CustomerListAdapter(
    private var customers: MutableList<Customer>,
    private val context: Context,
    private val onItemClick: (Customer) -> Unit
) : RecyclerView.Adapter<CustomerListAdapter.DataViewer>() {

    inner class DataViewer(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvId: TextView = itemView.findViewById(R.id.tvCustomerId)
        private val tvName: TextView = itemView.findViewById(R.id.tvCustomerName)
        private val tvPhone: TextView = itemView.findViewById(R.id.tvPhoneNumber)
        private val tvBalance: TextView = itemView.findViewById(R.id.tvBalanceAmount)

        fun bind(customer: Customer) {
            tvId.text = customer.cid.toString()
            tvName.text = customer.customername
            tvPhone.text =  customer.phone
            tvBalance.text = "Rs. "+customer.amountbalance.toString()
            itemView.setOnClickListener {
                onItemClick(customer)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DataViewer {
        val view = LayoutInflater.from(context).inflate(R.layout.customerliststyle, parent, false)
        return DataViewer(view)
    }

    override fun onBindViewHolder(holder: DataViewer, position: Int) {
        holder.bind(customers[position])
    }

    override fun getItemCount(): Int = customers.size

    fun updateData(newList: List<Customer>) {
        customers.clear()
        customers.addAll(newList)
        notifyDataSetChanged()
    }
}
