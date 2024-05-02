package com.example.carebridge.utils

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast

/**
 * openPdfFile opens a PDF file using an intent.
 * @param context the context of the activity or service
 * @param fileUrl the URL of the PDF file
 * @throws ActivityNotFoundException if no PDF viewer app is found
 */
fun openPdfFile(context:Context, fileUrl: String) {
    val intent = Intent(Intent.ACTION_VIEW)
    intent.setDataAndType(Uri.parse(fileUrl), "application/pdf")
    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
    try {
        context.startActivity(intent)
    } catch (e: ActivityNotFoundException) {
        Toast.makeText(context, "No PDF viewer app found", Toast.LENGTH_SHORT).show()
    }
}