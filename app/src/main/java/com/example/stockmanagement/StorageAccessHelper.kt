package com.example.stockmanagement

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.DocumentsContract
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.documentfile.provider.DocumentFile
import java.io.File

object StorageAccessHelper {

    private const val PREFS_NAME = "storage_prefs"
    private const val KEY_BACKUP_URI = "backup_uri"

    fun shouldUseSAF(): Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.R

    fun getLegacyBackupDir(): File {
        val dir = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS),
            "StockManagementBackups"
        )
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    fun launchFolderPicker(activity: Activity, launcher: ActivityResultLauncher<Intent>) {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE).apply {
            putExtra(DocumentsContract.EXTRA_INITIAL_URI, Uri.parse("content://com.android.externalstorage.documents/document/primary:Documents"))
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
        }
        launcher.launch(intent)
    }

    fun savePickedUri(context: Context, uri: Uri) {
        context.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_READ_URI_PERMISSION)
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_BACKUP_URI, uri.toString()).apply()
    }

    fun getBackupDocumentFileDir(context: Context): DocumentFile? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val uriString = prefs.getString(KEY_BACKUP_URI, null) ?: return null
        val uri = Uri.parse(uriString)
        return DocumentFile.fromTreeUri(context, uri)
    }

    fun createBackupFileInSAFDir(context: Context, dir: DocumentFile, filename: String): DocumentFile? {
        return dir.createFile("application/zip", filename)
    }
}
