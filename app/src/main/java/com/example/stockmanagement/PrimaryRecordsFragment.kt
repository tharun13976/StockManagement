package com.example.stockmanagement

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import com.example.stockmanagement.customers.CustomerCreate
import com.example.stockmanagement.products.ProductCreate
import com.example.stockmanagement.purchases.PurchaseCreate
import com.example.stockmanagement.sales.SaleCreate

class PrimaryRecordsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_primary_records, container, false)

        view.findViewById<Button>(R.id.Btn_Customer).setOnClickListener {
            startActivity(Intent(requireContext(), CustomerCreate::class.java))
        }
        view.findViewById<Button>(R.id.Btn_Product).setOnClickListener {
            startActivity(Intent(requireContext(), ProductCreate::class.java))
        }
        view.findViewById<Button>(R.id.Btn_Purchase).setOnClickListener {
            startActivity(Intent(requireContext(), PurchaseCreate::class.java))
        }
        view.findViewById<Button>(R.id.Btn_Sales).setOnClickListener {
            startActivity(Intent(requireContext(), SaleCreate::class.java))
        }

        return view
    }
}
