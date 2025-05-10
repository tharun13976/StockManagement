package com.example.stockmanagement

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.stockmanagement.entites.Customer
import com.example.stockmanagement.entites.Product
import com.example.stockmanagement.entites.ProductSalesDashboard
import com.example.stockmanagement.entites.Purchase
import com.example.stockmanagement.entites.Sale
import java.sql.Date

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

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllCustomer(list: List<Customer>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllProduct(list: List<Product>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllPurchase(list: List<Purchase>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllSale(list: List<Sale>)


    // To Get the Entries

    // Customer
    // Get all customer
    @Query("SELECT * FROM Customer")
    suspend fun getAllCustomer():List<Customer>

    // Get all customer sort name by Ascending order
    @Query("SELECT * FROM Customer ORDER BY customername ASC")
    suspend fun getAllCustomerSortByNameAsc():List<Customer>

    // Get all customer sort name by Descending order
    @Query("SELECT * FROM Customer ORDER BY customername DESC")
    suspend fun getAllCustomerSortByNameDesc():List<Customer>

    // Get customer of given ID
    @Query("SELECT * FROM Customer WHERE cid = :id")
    suspend fun getCustomerById(id: Int): Customer?

    // Get customer of given name
    @Query("SELECT * FROM Customer WHERE customername = :name")
    suspend fun getCustomerByname(name: String): Customer?

    // Get customers name only
    @Query("SELECT customername FROM Customer")
    suspend fun getCustomersName():List<String>

    // Get all customer sort by balance amount
    @Query("SELECT * FROM Customer ORDER BY amountbalance DESC")
    suspend fun getAllCustomersSortByBalance():List<Customer>

    // Get customers of given Phone number
    @Query("SELECT * FROM Customer WHERE phone = :phoneNo OR phone LIKE '%' || :phoneNo || '%'")
    suspend fun getAllCustomersPhone(phoneNo: String):List<Customer>


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

    // Get all product sort by name
    @Query("SELECT * FROM Product ORDER BY productname ASC")
    suspend fun getAllProductSortByName():List<Product>

    // Get all product sort by name
    @Query("SELECT * FROM Product ORDER BY productname DESC")
    suspend fun getAllProductSortByNameDesc():List<Product>

    // Get all available product sort by stock count
    @Query("SELECT * FROM  Product WHERE currentstockcount >0 ORDER BY currentstockcount DESC")
    suspend fun getAllProductAvailableSortByCount():List<Product>

    // Get all unavailable product sort by stock count
    @Query("SELECT * FROM  Product WHERE currentstockcount =0 ")
    suspend fun getAllProductUnavailable():List<Product>

    // Purchase-related
    // Get all purchases with their sales
    @Transaction
    @Query("SELECT * FROM Purchase")
    suspend fun getAllPurchases(): List<Purchase>

    @Transaction
    @Query("SELECT * FROM Purchase WHERE puid = :purchaseId")
    suspend fun getPurchaseById(purchaseId: Int): Purchase

    @Transaction
    @Query("SELECT * FROM Purchase WHERE puid = :purchaseId")
    suspend fun getPurchaseByID(purchaseId: Int): Purchase?

    @Transaction
    @Query("SELECT * FROM Purchase WHERE currentstockcount > 0")
    suspend fun getAllAvailablePurchases(): List<Purchase>

//    @Query("SELECT * FROM Purchase WHERE stockaddeddate BETWEEN :start AND :end")
//    suspend fun getPurchasesByDate(start: Date, end: Date): List<Purchase>

    @Query("SELECT * FROM Purchase WHERE stockaddeddate =:date")
    suspend fun getPurchasesByDate(date: Date): List<Purchase>

    // Filter by product name
    @Transaction
    @Query("SELECT * FROM Purchase WHERE productname = :productName")
    suspend fun getPurchaseforProduct(productName: String): List<Purchase>

    // To Get oldest stock of the product
    @Transaction
    @Query("SELECT * FROM Purchase WHERE currentstockcount > 0 AND productname = :productName ORDER BY stockaddeddate ASC LIMIT 1")
    suspend fun getOldestPurchase(productName: String): Purchase

    // Sale-related
    // Get all sale
    @Query("SELECT * FROM Sale")
    suspend fun getAllSales(): List<Sale>

    // Get sales of given ID
    @Transaction
    @Query("SELECT * FROM Sale WHERE sid = :saleid")
    suspend fun getSalesById(saleid: Int): Sale?

    // Get all sales of customer
    @Transaction
    @Query("SELECT * FROM Sale WHERE customername = :name")
    suspend fun getSalesByCustomerName(name: String): List<Sale>

    // Get all sales of customer
    @Transaction
    @Query("SELECT * FROM Sale WHERE customername = :name AND amountonly = 1")
    suspend fun getSalesByCustomersAmountOnly(name: String): List<Sale>

    // Get all sales of product
    @Transaction
    @Query("SELECT * FROM Sale WHERE productname = :name")
    suspend fun getSalesByProductName(name: String): List<Sale>

    // Get all sales of product
    @Transaction
    @Query("SELECT * FROM Sale WHERE purchaseid = :id")
    suspend fun getSalesByPurchaseID(id: Int): List<Sale>

    // Get all sales of amount only
    @Transaction
    @Query("SELECT * FROM Sale WHERE amountonly = 1")
    suspend fun getSalesByAmountOnly(): List<Sale>

    @Transaction
    @Query("SELECT * FROM Sale WHERE salesdate =:date")
    suspend fun getSalesByDate(date: Date): List<Sale>


    @Query("""
    SELECT 
        p.productName AS productName,
        p.currentStockCount AS stock,
        COALESCE(COUNT(s.sid), 0) AS salesCount
    FROM Product p
    LEFT JOIN Sale s 
        ON p.productName = s.productname
        AND s.salesdate BETWEEN :startDate AND :endDate
    GROUP BY p.productName
    ORDER BY p.productname
""")
    suspend fun getProductSalesDashboard(startDate: Date, endDate: Date): List<ProductSalesDashboard>


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