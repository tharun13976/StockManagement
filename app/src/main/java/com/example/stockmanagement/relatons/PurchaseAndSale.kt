package com.example.stockmanagement.relatons

import androidx.room.Embedded
import androidx.room.Relation
import com.example.stockmanagement.entites.Purchase
import com.example.stockmanagement.entites.Sale

data class PurchaseAndSale(
    @Embedded val purchase: Purchase,
    @Relation(
        parentColumn = "puid",
        entityColumn = "purchaseid"
    )
    val sale:List<Sale>
)