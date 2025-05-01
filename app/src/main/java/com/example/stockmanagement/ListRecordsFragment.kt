package com.example.stockmanagement

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import com.example.stockmanagement.customers.CustomerList
import com.example.stockmanagement.products.ProductList
import com.example.stockmanagement.purchases.PurchaseList
import com.example.stockmanagement.sales.SaleList

class ListRecordsFragment : Fragment() {

    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_list_records, container, false)

        view.findViewById<Button>(R.id.Btn_CustomerList).setOnClickListener {
            startActivity(Intent(requireContext(), CustomerList::class.java))
        }

        view.findViewById<Button>(R.id.Btn_ProductList).setOnClickListener {
            startActivity(Intent(requireContext(), ProductList::class.java))
        }

        view.findViewById<Button>(R.id.Btn_PurchaseList).setOnClickListener {
            startActivity(Intent(requireContext(), PurchaseList::class.java))
        }

        view.findViewById<Button>(R.id.Btn_SalesList).setOnClickListener {
            startActivity(Intent(requireContext(), SaleList::class.java))
        }

        return view
    }
}
