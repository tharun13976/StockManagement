package com.example.stockmanagement

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.MenuItem
import android.widget.Button
import android.widget.Toast
import java.util.Date
import java.util.Locale
import android.app.Activity
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat

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

        val backupBut = findViewById<Button>(R.id.Btn_Backup)

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

    private val folderPickerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                StorageAccessHelper.savePickedUri(this, uri)
                triggerManualBackup()
            }
        }
    }

    private fun triggerManualBackup() {
        lifecycleScope.launch {
            val backupSuccess = if (StorageAccessHelper.shouldUseSAF()) {
                val dir = StorageAccessHelper.getBackupDocumentFileDir(this@ManualBackuppage)
                if (dir == null) {
                    StorageAccessHelper.launchFolderPicker(this@ManualBackuppage, folderPickerLauncher)
                    return@launch
                }
                val filename = "backup_${SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.getDefault()).format(Date())}.zip"
                val backupFile = StorageAccessHelper.createBackupFileInSAFDir(this@ManualBackuppage, dir, filename)
                backupFile?.uri?.let {
                    BackupUtils.backupDatabaseToUri(this@ManualBackuppage, it)
                } ?: false
            } else {
                val legacyDir = StorageAccessHelper.getLegacyBackupDir()
                BackupUtils.backupDatabaseTablesToJson(this@ManualBackuppage, legacyDir)
            }

            Toast.makeText(
                this@ManualBackuppage,
                if (backupSuccess) getString(R.string.backup_success) else getString(R.string.backup_failed),
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
