package com.example.stockmanagement

import android.app.Activity
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Environment
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import java.io.File

object BackupHelper {
    suspend fun performBackup(
        context: Context,
        activity: Activity? = null,
        checkNotificationPermission: Boolean
    ): Boolean {
        val backupDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).let { File(it, "PeriodicBackups") }

        if (!backupDir.exists()) {
            val created = backupDir.mkdirs()
            if (!created) {
                Log.e("BackupUtils", "Failed to create backup directory: ${backupDir.absolutePath}")
                // Handle error
            }
        }

        return try {
            val success = BackupUtils.backupDatabaseTablesToJson(context, backupDir)
            if (success) {
                val notification = createBackupNotification(context)

                if (!checkNotificationPermission) {
                    NotificationManagerCompat.from(context).notify(101, notification)
                } else {
                    sendNotificationSafely(activity, context, notification)
                }

                Log.d("BackupHelper", "Backup successful.")
                true
            } else {
                Log.d("BackupHelper", "Backup failed in BackupUtils.")
                false
            }
        } catch (e: Exception) {
            Log.e("BackupHelper", "Backup failed: ${e.message}", e)
            false
        }
    }

    private fun createBackupNotification(context: Context): Notification {
        val channelId = "backup_channel"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Backup Notifications",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            NotificationManagerCompat.from(context).createNotificationChannel(channel)
        }

        return NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.backupdone)
            .setContentTitle("Backup Completed")
            .setContentText("Your backup was successfully created.")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()
    }

    private fun sendNotificationSafely(activity: Activity?, context: Context, notification: Notification) {
        val notificationId = 101
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val hasPermission = if (activity != null) {
                ContextCompat.checkSelfPermission(activity, android.Manifest.permission.POST_NOTIFICATIONS)
            } else {
                ContextCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS)
            } == PackageManager.PERMISSION_GRANTED

            if (hasPermission) {
                NotificationManagerCompat.from(context).notify(notificationId, notification)
            } else {
                Log.w("BackupHelper", "POST_NOTIFICATIONS permission not granted or activity was null.")
            }
        } else {
            NotificationManagerCompat.from(context).notify(notificationId, notification)
        }
    }
}
