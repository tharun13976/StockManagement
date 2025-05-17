package com.example.stockmanagement

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class ManualBackuppage : AppCompatActivity() {

    private val requestPermissionsLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.all { it.value }
        if (allGranted) {
            triggerManualBackup()
        } else {
            Toast.makeText(this, "Required permissions denied.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_manual_backuppage)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val passHint = findViewById<TextView>(R.id.TV_PasswordHint)
        val backupHint = findViewById<TextView>(R.id.TV_BackupHint)
        val pass = findViewById<EditText>(R.id.ET_Password)
        val proceedBut = findViewById<Button>(R.id.Btn_CheckPassword)
        val backupBut = findViewById<Button>(R.id.Btn_Backup)

        // Initial state
        backupHint.visibility = View.GONE
        backupBut.visibility = View.GONE

        proceedBut.setOnClickListener {
            if (pass.text.toString() == "1q2w3e4r@") {
                passHint.visibility = View.GONE
                pass.visibility = View.GONE
                proceedBut.visibility = View.GONE
                backupHint.visibility = View.VISIBLE
                backupBut.visibility = View.VISIBLE
            } else {
                Toast.makeText(this, "Enter Correct Password", Toast.LENGTH_LONG).show()
            }
        }

        backupBut.setOnClickListener {
            requestRequiredPermissions()
        }
    }

    private fun requestRequiredPermissions() {
        val permissions = mutableListOf<String>()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.POST_NOTIFICATIONS)
        }

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        }

        if (permissions.all { ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED }) {
            triggerManualBackup()
        } else {
            requestPermissionsLauncher.launch(permissions.toTypedArray())
        }
    }

    private fun triggerManualBackup() {
        lifecycleScope.launch {
            val success = BackupHelper.performBackup(
                context = this@ManualBackuppage,
                activity = this@ManualBackuppage,
                checkNotificationPermission = false
            )
            Toast.makeText(
                this@ManualBackuppage,
                if (success) "Manual backup successful!" else "Manual backup failed!",
                Toast.LENGTH_SHORT
            ).show()
            finish()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (item.itemId == android.R.id.home) {
            finish()
            true
        } else {
            super.onOptionsItemSelected(item)
        }
    }
}
