package com.example.stockmanagement

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import java.util.concurrent.TimeUnit
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.Locale


class MainActivity : AppCompatActivity() {

    private lateinit var viewPager: ViewPager2
    private lateinit var tabLayout: TabLayout

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        loadLocale() // Load saved language before UI setup
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightStatusBars = true
        window.statusBarColor = Color.WHITE
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val rootView = findViewById<View>(R.id.main)
        ViewCompat.setOnApplyWindowInsetsListener(rootView) { view, insets ->
            val systemBarsInsets = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(
                systemBarsInsets.left,systemBarsInsets.top, systemBarsInsets.right, systemBarsInsets.bottom
            )
            insets
        }

        // Set up Toolbar
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.exit_icon)


        // ViewPager & Tabs
        viewPager = findViewById(R.id.viewPager)
        tabLayout = findViewById(R.id.tabLayout)
        viewPager.adapter = MainPagerAdapter(this)

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> getString(R.string.tab_product)
                1 -> getString(R.string.tab_create_record)
                2 ->getString(R.string.tab_record_list)
                else -> ""
            }
        }.attach()

        val sharedPreferences = getSharedPreferences("app_data", MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        val storedMillis = sharedPreferences.getLong("stored_date", 0L)
        val currentTimeMillis = System.currentTimeMillis()
        val fifteenDaysInMillis = 15L * 24 * 60 * 60 * 1000

        if (storedMillis == 0L) {
            // First-time setup: store current time and skip backup check
            editor.putLong("stored_date", currentTimeMillis).apply()
            Log.d("Backup", "Backup date initialized.")
            return
        }

        if (currentTimeMillis - storedMillis >= fifteenDaysInMillis) {
            // 15 days passed – schedule backup and update timestamp
            editor.putLong("stored_date", currentTimeMillis).apply()
            Log.d("Backup", "Backup scheduled after 15 days.")
            setupPeriodicBackup()
        }
    }

    private fun setupPeriodicBackup() {
        val workRequest = PeriodicWorkRequestBuilder<MonthlyBackupWorker>(30, TimeUnit.DAYS)
            .setConstraints(
                Constraints.Builder()
                    .setRequiresCharging(true)
                    .setRequiresBatteryNotLow(true)
                    .build()
            )
            .build()

        WorkManager.getInstance(applicationContext).enqueueUniquePeriodicWork(
            "MonthlyBackup",
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.toolbar_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            R.id.action_backup -> {
                val nextScreen = Intent(this, ManualBackuppage::class.java)
                startActivity(nextScreen)
                true
            }
            R.id.action_import -> {
                val nextScreen = Intent(this, ImportActivity::class.java)
                startActivity(nextScreen)
                true
            }
            R.id.action_language -> {
                showLanguageSelectionDialog()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
    private fun showLanguageSelectionDialog() {
        val languages = arrayOf("English", "Tamil")
        AlertDialog.Builder(this)
            .setTitle("Select Language")
            .setItems(languages) { _, which ->
                when (which) {
                    0 -> setLocale("en") // English
                    1 -> setLocale("ta") // Tamil
                }
            }
            .create()
            .show()
    }

    private fun setLocale(languageCode: String) {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)
        val config = resources.configuration
        config.setLocale(locale)
        resources.updateConfiguration(config, resources.displayMetrics)

        val prefs = getSharedPreferences("app_prefs", MODE_PRIVATE)
        prefs.edit().putString("app_language", languageCode).apply()

        // Restart the activity
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        finish()
    }

    private fun loadLocale() {
        val prefs = getSharedPreferences("app_prefs", MODE_PRIVATE)
        val language = prefs.getString("app_language", "en") ?: "en"
        val locale = Locale(language)
        Locale.setDefault(locale)
        val config = resources.configuration
        config.setLocale(locale)
        resources.updateConfiguration(config, resources.displayMetrics)
    }
}