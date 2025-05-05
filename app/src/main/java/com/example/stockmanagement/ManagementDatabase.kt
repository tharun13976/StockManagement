package com.example.stockmanagement

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.stockmanagement.entites.Customer
import com.example.stockmanagement.entites.Purchase
import com.example.stockmanagement.entites.Product
import com.example.stockmanagement.entites.Sale

@Database(
    entities = [
        Customer::class,
        Product::class,
        Purchase::class,
        Sale::class
    ],
    version = 1
)
@TypeConverters(Converters::class)
abstract class ManagementDatabase : RoomDatabase() {
    abstract val managementDao: ManagementDao
    companion object{
        @Volatile
        private var INSTANCE: ManagementDatabase ?= null
        fun getInstance(context: Context): ManagementDatabase{
            synchronized(this){
                return INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    ManagementDatabase::class.java,
                    "stock_manage_database_db"
                ).build().also {
                    INSTANCE = it
                }
            }
        }
    }
}