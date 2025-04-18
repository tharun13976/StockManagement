package com.example.stockmanagement.entites

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity
data class Product(
    @PrimaryKey(autoGenerate = true)
    val pid:Int?=null,
    val productname:String,
    val mesurment:String,
    val productcreateddate:Date,
    val currentprice:Int,
    val currentstockcount:Int
)