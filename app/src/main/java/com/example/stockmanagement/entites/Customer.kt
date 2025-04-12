package com.example.stockmanagement.entites

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity
data class Customer(
    @PrimaryKey(autoGenerate = true)
    val cid: Int?=null,
    val customername: String,
    val phone: String,
    val address: String,
    val customercreatedDate: Date
)