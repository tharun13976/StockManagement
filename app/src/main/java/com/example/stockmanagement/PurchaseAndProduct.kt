package com.example.stockmanagement

import androidx.room.Embedded
import androidx.room.Relation
import com.example.stockmanagement.entites.Product
import com.example.stockmanagement.entites.Purchase

data class PurchaseAndProduct(
    @Embedded val purchase: Purchase,
    @Relation(
        parentColumn = "productname",
        entityColumn = "productname"
    )
    val product: Product
)
