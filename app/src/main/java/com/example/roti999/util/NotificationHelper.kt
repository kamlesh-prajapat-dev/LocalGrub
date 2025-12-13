package com.example.roti999.util

import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import com.example.roti999.R
import javax.inject.Singleton


class NotificationHelper {

    fun showOrderStatusNotification(context: Context, orderId: String, status: String) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val notification = NotificationCompat.Builder(context, "ORDER_STATUS_CHANNEL")
            .setSmallIcon(R.drawable.ic_cart) // You can use your app's icon here
            .setContentTitle("Order Status Updated")
            .setContentText("Your order #$orderId is now $status")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(orderId.hashCode(), notification)
    }
}
