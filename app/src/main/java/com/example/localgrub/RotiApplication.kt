package com.example.localgrub

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.google.firebase.database.FirebaseDatabase
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class RotiApplication : Application(), Configuration.Provider {
    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override fun getWorkManagerConfiguration(): Configuration {
        return Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
    }

    override fun onCreate() {
        super.onCreate()

        initFirebase()
        createNotificationChannel()
    }

    private fun initFirebase() {
        try {
            FirebaseDatabase.getInstance().setPersistenceEnabled(true)
        } catch (e: Exception) {
            // Persistence already enabled or Firebase initialized elsewhere
        }
    }

    private fun createNotificationChannel() {

        val channel = NotificationChannel(
            ORDER_STATUS_CHANNEL_ID,
            ORDER_STATUS_CHANNEL_NAME,
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = ORDER_STATUS_CHANNEL_DESCRIPTION
        }

        val notificationManager =
            getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        notificationManager.createNotificationChannel(channel)
    }

    companion object {
        const val ORDER_STATUS_CHANNEL_ID = "order_status_channel"
        private const val ORDER_STATUS_CHANNEL_NAME = "Order Status"
        private const val ORDER_STATUS_CHANNEL_DESCRIPTION =
            "Notifications related to order updates and status changes"
    }
}