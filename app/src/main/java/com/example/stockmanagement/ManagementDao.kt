package com.example.stockmanagement

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.stockmanagement.entites.Customer
import com.example.stockmanagement.entites.Product
import com.example.stockmanagement.entites.Purchase
import com.example.stockmanagement.entites.Sale

@Dao
interface ManagementDao {
    // To Create a new Record
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomer(customer: Customer)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: Product)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSale(sale: Sale)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPurchase(purchase: Purchase)


    // To Get the Entries

    // Customer
    // Get all customer
    @Query("SELECT * FROM Customer")
    suspend fun getAllCustomer():List<Customer>

    // Get all customer short by name
    @Query("SELECT * FROM Customer ORDER BY customername asc")
    suspend fun getAllCustomerShortByName():List<Customer>

    // Get customer of given ID
    @Query("SELECT * FROM Customer WHERE cid = :id")
    suspend fun getCustomerById(id: Int): Customer?

    // Get customer of given name
    @Query("SELECT * FROM Customer WHERE customername = :name")
    suspend fun getCustomerByname(name: String): Customer?

    // Get customers name only
    @Query("SELECT customername FROM Customer")
    suspend fun getCustomersName():List<String>


    // Product-related
    // Get all product
    @Query("SELECT * FROM Product")
    suspend fun getAllProduct():List<Product>

    // Get product by given name
    @Query("SELECT * FROM Product WHERE productname = :name")
    suspend fun getProductByName(name: String): Product?

    // Get  products name only
    @Query("SELECT productname FROM Product")
    suspend fun getProductNames():List<String>

    // Get product of given ID
    @Query("SELECT * FROM Product WHERE pid = :id")
    suspend fun getProductById(id: Int): Product?

    // Purchase-related
    // Get all purchases with their sales
    @Transaction
    @Query("SELECT * FROM Purchase")
    suspend fun getAllPurchases(): List<Purchase>

    // Filter by product name
    @Transaction
    @Query("SELECT * FROM Purchase WHERE productname = :productName")
    suspend fun getPurchaseWithProduct(productName: String): List<Purchase>

    // To Get oldest stock of the product
    @Transaction
    @Query("SELECT * FROM Purchase WHERE currentstockcount > 0 AND productname = :productName ORDER BY stockaddeddate ASC LIMIT 1")
    suspend fun getOldestPurchase(productName: String): Purchase

    // Sale-related
    // Sale-related
    // Get all sale
    @Query("SELECT * FROM Sale")
    suspend fun getAllSales(): List<Sale>

    // Get all sales with customer
    @Transaction
    @Query("SELECT * FROM Sale WHERE customername = :name")
    suspend fun getSalesByCustomerName(name: String): List<Sale>

    // To update the Record
    @Update
    suspend fun updateCustomer(customer: Customer)

    @Update
    suspend fun updateProduct(product: Product)

    @Update
    suspend fun updatePurchase(purchase: Purchase)

    @Update
    suspend fun updateSale(sale: Sale)

    @Query("UPDATE Sale SET customerName = :newName WHERE customerName = :oldName")
    suspend fun updateCustomerNameInSales(oldName: String, newName: String)
}