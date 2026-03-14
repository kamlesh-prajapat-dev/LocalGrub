package com.example.localgrub.util

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.os.bundleOf
import androidx.navigation.NavDeepLinkBuilder
import com.example.localgrub.R
import com.example.localgrub.ui.activity.MainActivity


class NotificationHelper {

    fun showOrderStatusNotification(
        context: Context,
        orderId: String,
        body: String,
        title: String
    ) {

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val pendingIntent = NavDeepLinkBuilder(context)
            .setGraph(R.navigation.nav_graph)
            .setDestination(R.id.eachOrderStatusFragment)
            .setArguments(bundleOf("orderId" to orderId))
            .createPendingIntent()

        val notification = NotificationCompat.Builder(context, "ORDER_STATUS_CHANNEL")
            .setSmallIcon(R.drawable.ic_cart) // You can use your app's icon here
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(orderId.hashCode(), notification)
    }
}
