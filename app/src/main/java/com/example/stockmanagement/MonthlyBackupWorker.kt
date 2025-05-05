package com.example.stockmanagement

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import java.util.*

class MonthlyBackupWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val today = Calendar.getInstance()
        val dayOfMonth = today.get(Calendar.DAY_OF_MONTH)

        Log.d("MonthlyBackupWorker", "Running backup check: day $dayOfMonth")

        return if (dayOfMonth == 1 || dayOfMonth == 15) {
            val success = BackupHelper.performBackup(
                applicationContext,
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
