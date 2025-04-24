package com.example.stockmanagement.entites

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity
data class Product(
    @PrimaryKey(autoGenerate = true)
    val pid:Int?=null,
    var productname:String,
    val mesurment:String,
    val productcreateddate:Date,
    var LatestpriceofoneUnit:Int,
    var currentstockcount:Int
)