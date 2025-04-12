package com.example.stockmanagement.entites

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity
data class Purchase(
    @PrimaryKey(autoGenerate = true)
    val puid:Int?=null,
    val productname:String,
    val stockaddeddate:Date,
    val stockcount:Int,
    val stockprice: Int
)
