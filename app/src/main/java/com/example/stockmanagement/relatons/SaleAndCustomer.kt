package com.example.stockmanagement.relatons

import androidx.room.Embedded
import androidx.room.Relation
import com.example.stockmanagement.entites.Customer
import com.example.stockmanagement.entites.Sale

data class SaleAndCustomer(
    @Embedded val customer: Customer,
    @Relation(
        parentColumn = "customername",
        entityColumn = "customername"
    )
    val sale:List<Sale>
)