package com.example.roti999.util

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.example.roti999.R
import com.example.roti999.ui.activity.MainActivity


class NotificationHelper {

    fun showOrderStatusNotification(context: Context, orderId: String, status: String) {

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            100,                               // requestCode (any unique number)
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, "ORDER_STATUS_CHANNEL")
            .setSmallIcon(R.drawable.ic_cart) // You can use your app's icon here
            .setContentTitle("Order Status Updated")
            .setContentText("Your order #$orderId is now $status")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(orderId.hashCode(), notification)
    }
}
