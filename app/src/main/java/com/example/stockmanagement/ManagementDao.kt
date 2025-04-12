package com.example.stockmanagement

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.stockmanagement.entites.Customer
import com.example.stockmanagement.entites.Product
import com.example.stockmanagement.entites.Purchase
import com.example.stockmanagement.entites.Sale

@Dao
interface ManagementDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomer(customer: Customer)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: Product)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSale(sale: Sale)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPurchase(purchase: Purchase)

    // Customer-realted
    // Get all customer
    @Query("SELECT * FROM Customer")
    suspend fun getAllCustomer():List<Customer>

    // Get customer of given name
    @Query("SELECT * FROM Customer WHERE customername = :name")
    suspend fun getCustomerByName(name: String): Customer?


    // Product-related
    // Get all product
    @Query("SELECT * FROM Product")
    suspend fun getAllProduct():List<Product>

    // Get product by given name
    @Query("SELECT * FROM Product WHERE productname = :name")
    suspend fun getProductByName(name: String): Product?

    // Purchase-related
    // Get all purchases with their sales
    @Transaction
    @Query("SELECT * FROM Purchase")
    suspend fun getAllPurchases(): List<Purchase>

    // Filter by product name
    @Transaction
    @Query("SELECT * FROM Purchase WHERE productname = :productName")
    suspend fun getPurchaseWithProduct(productName: String): List<PurchaseAndProduct>

    // Sale-related
    // Get all sale
    @Query("SELECT * FROM Sale")
    suspend fun getAllSales(): List<Sale>

    // Get all sales with customer
    @Transaction
    @Query("SELECT * FROM Sale WHERE customername = :name")
    suspend fun getSalesByCustomerName(name: String): List<Sale>
}