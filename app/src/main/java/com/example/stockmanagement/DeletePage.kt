package com.example.stockmanagement

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import java.util.Date
import java.util.concurrent.TimeUnit

class DeletePage : AppCompatActivity() {

    private lateinit var dao: ManagementDao
    private lateinit var selectedModule: String
    private lateinit var btnProceed: Button
    private lateinit var btnDelete: Button
    private lateinit var etRecordId: EditText
    private lateinit var spModule: Spinner

    private lateinit var layouts: List<View>
    private lateinit var tvMap: Map<String, TextView>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_delete_page)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val sysBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(sysBars.left, sysBars.top, sysBars.right, sysBars.bottom)
            insets
        }

        setSupportActionBar(findViewById<Toolbar>(R.id.toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.back_icon)

        dao = ManagementDatabase.getInstance(this).managementDao

        spModule = findViewById(R.id.Spi_ModuleList)
        etRecordId = findViewById(R.id.ET_RecordId)
        btnProceed = findViewById(R.id.Btn_Proceed)
        btnDelete = findViewById(R.id.Btn_Delete)
        val btnBack = findViewById<Button>(R.id.Btn_Back)

        val layoutCustomer = findViewById<View>(R.id.layoutCustomer)
        val layoutProduct = findViewById<View>(R.id.layoutProduct)
        val layoutPurchase = findViewById<View>(R.id.layoutPurchase)
        val layoutSale = findViewById<View>(R.id.layoutSale)

        layouts = listOf(layoutCustomer, layoutProduct, layoutPurchase, layoutSale)

        tvMap = mapOf(
            "cid" to findViewById(R.id.tvCustomerId),
            "cname" to findViewById(R.id.tvCustomerName),
            "cphone" to findViewById(R.id.tvCustomerPhone),
            "caddress" to findViewById(R.id.tvCustomerAddress),
            "pid" to findViewById(R.id.tvProductId),
            "pname" to findViewById(R.id.tvProductName),
            "puid" to findViewById(R.id.tvPurchaseId),
            "pproduct" to findViewById(R.id.tvPurchaseProduct),
            "pqty" to findViewById(R.id.tvPurchaseQty),
            "sid" to findViewById(R.id.tvSaleId),
            "scustomer" to findViewById(R.id.tvSaleCustomer),
            "sproduct" to findViewById(R.id.tvSaleProduct),
            "sqty" to findViewById(R.id.tvSaleProductCount),
            "samount" to findViewById(R.id.tvSaleAmount)
        )

        val moduleItems = listOf(
            getString(R.string.mod_none),
            getString(R.string.mod_customer),
            getString(R.string.mod_product),
            getString(R.string.mod_purchase),
            getString(R.string.mod_sale)
        )

        spModule.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            moduleItems
        ).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }

        spModule.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, pos: Int, id: Long) {
                selectedModule = parent.getItemAtPosition(pos).toString()
                clearDetails()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        btnBack.setOnClickListener { finish() }

        btnProceed.setOnClickListener {
            val rawId = etRecordId.text.toString()
            if (selectedModule == getString(R.string.mod_none)) showToast(getString(R.string.mod_selection_alert))
            else if (rawId.isEmpty() || rawId.toIntOrNull() == null) showToast(getString(R.string.mod_record_id_alert))
            else showRecordDetails(selectedModule, rawId.toInt())
        }

        btnDelete.setOnClickListener {
            val id = etRecordId.text.toString().toIntOrNull() ?: return@setOnClickListener
            deleteRecord(selectedModule, id)
        }
    }

    private fun clearDetails() {
        layouts.forEach { it.visibility = View.GONE }
    }

    private fun showToast(msgRes: String) {
        Toast.makeText(this, msgRes, Toast.LENGTH_SHORT).show()
        etRecordId.setText("")
    }

    private fun restrictAccess() {
        btnProceed.visibility = View.GONE
        btnDelete.visibility = View.VISIBLE
        spModule.isEnabled = false
        etRecordId.isEnabled = false
    }

    @SuppressLint("SetTextI18n")
    private fun showRecordDetails(module: String, recordId: Int) {
        clearDetails()
        lifecycleScope.launch {
            when (module) {
                getString(R.string.mod_customer) -> dao.getCustomerById(recordId)?.let {
                    if (dao.getSalesCountForCustomer(it.customername) > 0)
                        return@launch showToast(getString(R.string.customer_delete_blocked))
                    layouts[0].visibility = View.VISIBLE
                    tvMap["cid"]?.text = getString(R.string.label_customer_id) + it.cid
                    tvMap["cname"]?.text = getString(R.string.label_customer_name) + it.customername
                    tvMap["cphone"]?.text = getString(R.string.label_customer_phone) + it.phone
                    tvMap["caddress"]?.text = getString(R.string.label_customer_address) + it.address
                    restrictAccess()
                } ?: showToast(getString(R.string.customer_not_found_error))

                getString(R.string.mod_product) -> dao.getProductById(recordId)?.let {
                    if (dao.getPurchaseCountForProduct(it.productname) > 0)
                        return@launch showToast(getString(R.string.product_delete_blocked))
                    layouts[1].visibility = View.VISIBLE
                    tvMap["pid"]?.text = getString(R.string.label_product_id) + it.pid
                    tvMap["pname"]?.text = getString(R.string.label_product_name) + it.productname
                    restrictAccess()
                } ?: showToast(getString(R.string.product_not_found))

                getString(R.string.mod_purchase) -> dao.getPurchaseById(recordId)?.let {
                    if (dao.getSalesCountForPurchase(recordId) > 0)
                        return@launch showToast(getString(R.string.purchase_delete_blocked))
                    layouts[2].visibility = View.VISIBLE
                    tvMap["puid"]?.text = getString(R.string.label_purchase_id) + it.puid
                    tvMap["pproduct"]?.text = getString(R.string.label_purchase_product) + it.productname
                    tvMap["pqty"]?.text = getString(R.string.label_purchase_qty) + it.stockcount
                    restrictAccess()
                } ?: showToast(getString(R.string.purchase_not_found))

                getString(R.string.mod_sale) -> dao.getSalesById(recordId)?.let {
                    val daysDiff = TimeUnit.MILLISECONDS.toDays(Date().time - it.salesdate.time)
                    if (daysDiff > 2) return@launch showToast(getString(R.string.sale_delete_blocked))
                    layouts[3].visibility = View.VISIBLE
                    tvMap["sid"]?.text = getString(R.string.label_sale_id) + it.sid
                    tvMap["scustomer"]?.text = getString(R.string.label_sale_customer) + it.customername
                    tvMap["sproduct"]?.text = getString(R.string.label_sale_product) + it.productname
                    tvMap["sqty"]?.text = getString(R.string.label_sale_qty) + it.saleproductcount
                    tvMap["samount"]?.text = getString(R.string.label_sale_amount) + it.amountgiven
                    restrictAccess()
                } ?: showToast(getString(R.string.sale_not_found))
            }
        }
    }

    private fun deleteRecord(module: String, id: Int) {
        lifecycleScope.launch {
            val deleted = when (module) {
                getString(R.string.mod_customer) -> dao.deleteCustomerById(id)
                getString(R.string.mod_product) -> dao.deleteProductById(id)
                getString(R.string.mod_purchase) -> {
                    val purchaseRec = dao.getPurchaseById(id)
                    dao.getProductByName(purchaseRec?.productname.toString())?.let {
                        if (purchaseRec != null) {
                            it.currentstockcount -= purchaseRec.stockcount
                        }
                        dao.updateProduct(it)
                    }
                    dao.deletePurchaseById(id)
                }
                getString(R.string.mod_sale) -> {
                    val saleRec = dao.getSalesById(id)
                    if (saleRec != null) {
                        val purchase = saleRec.purchaseid?.toInt()?.let { dao.getPurchaseById(it) }
                        purchase?.let {
                            it.currentstockcount += saleRec.saleproductcount?.toInt() ?: 0
                            dao.updatePurchase(it)
                        }
                        val product = dao.getProductByName(saleRec.productname.toString())
                        product?.let {
                            it.currentstockcount += saleRec.saleproductcount?.toInt() ?: 0
                            dao.updateProduct(it)
                        }
                        val customer = dao.getCustomerByname(saleRec.customername.toString())
                        customer?.let {
                            val cost = saleRec.costofproductsold?.toInt() ?: 0
                            val amountGiven = saleRec.amountgiven.toInt()
                            val balance = cost - amountGiven
                            it.amountbalance -= balance
                            dao.updateCustomer(it)
                        }
                    }
                    dao.deleteSaleById(id)
                }
                else -> 0
            }
            if (deleted > 0) {
                Toast.makeText(this@DeletePage, "$module ${getString(R.string.deleted_success)}", Toast.LENGTH_LONG).show()
                Log.d("Deletion", "$module record deleted")
                finish()
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean =
        if (item.itemId == android.R.id.home) {
            finish(); true
        } else super.onOptionsItemSelected(item)
}