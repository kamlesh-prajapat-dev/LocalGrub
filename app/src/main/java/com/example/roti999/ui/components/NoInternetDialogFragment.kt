package com.example.roti999.ui.components

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.example.roti999.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class NoInternetDialogFragment : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.no_internet_connection)
            .setMessage(R.string.check_internet_connection)
            .setPositiveButton(R.string.ok) { dialog, _ -> dialog.dismiss() }
            .setCancelable(false)
            .create()
    }
}