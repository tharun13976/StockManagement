package com.example.stockmanagement

import android.content.Context
import androidx.appcompat.app.AlertDialog

class ExitConfirmation {
    fun show(context: Context, onConfirmed: () -> Unit) {
        AlertDialog.Builder(context)
            .setTitle(context.getString(R.string.confirmation))
            .setMessage(context.getString(R.string.unsaved_changes_exit))
            .setPositiveButton(context.getString(R.string.yes)) { dialog, _ ->
                dialog.dismiss()
                onConfirmed()
            }
            .setNegativeButton(context.getString(R.string.no)) { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }
}
