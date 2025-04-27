package com.example.stockmanagement.entites

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity
data class Customer(
    @PrimaryKey(autoGenerate = true)
    val cid: Int?=null,
    var customername: String,
    var phone: String,
    var address: String,
    val customercreatedDate: Date,
    var amountbalance:Int
)