package com.example.stockmanagement

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

class MonthlyBackupWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {

        Log.d("MonthlyBackupWorker", "Running backup")

        return if (true) {
            val success = BackupHelper.performBackup(
                context = applicationContext,
                checkNotificationPermission = true
            )
            if (success) {
                Log.d("MonthlyBackupWorker", "Monthly backup succeeded.")
                Result.success()
            } else {
                Log.e("MonthlyBackupWorker", "Monthly backup failed. Will retry.")
                Result.retry()
            }
        } else {
            Result.success()
        }
    }
}
