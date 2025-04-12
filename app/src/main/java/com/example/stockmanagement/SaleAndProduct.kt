package com.example.stockmanagement

import androidx.room.Embedded
import androidx.room.Relation
import com.example.stockmanagement.entites.Product
import com.example.stockmanagement.entites.Sale

data class SaleAndProduct(
    @Embedded val product: Product,
    @Relation(
        parentColumn ="productname",
        entityColumn ="productname"
    )
    val sales:List<Sale>
)
