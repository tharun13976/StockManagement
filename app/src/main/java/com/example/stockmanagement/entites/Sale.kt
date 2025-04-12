package com.example.stockmanagement.entites

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity
data class Sale(
    @PrimaryKey(autoGenerate = true)
    val sid: Int?=null,
    val customername:String,
    val productname:String,
    val salesdate:Date,
    val purchaseid:Int,
    val saleproductcount:Int,
    val costofproductsaled:Int,
    val amountgiven:Int,
    val amountonly:Boolean
)
