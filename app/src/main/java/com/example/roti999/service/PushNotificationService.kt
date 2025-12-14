package com.example.roti999.service

import android.util.Log
import com.example.roti999.data.local.LocalDatabase
import com.example.roti999.data.model.User
import com.example.roti999.domain.repository.UserRepository
import com.example.roti999.util.NotificationHelper
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class PushNotificationService : FirebaseMessagingService() {

    @Inject
    lateinit var notificationHelper: NotificationHelper

    @Inject
    lateinit var localDatabase: LocalDatabase

    @Inject
    lateinit var userRepository: UserRepository

    companion object {
        private const val TAG = "PushNotificationService"
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        // Check if message contains a notification payload.
        remoteMessage.notification?.let {
            // For simplicity, we'll use the orderId from the data payload
            val orderId = remoteMessage.data["orderId"] ?: "Unknown Order"
            val status = it.body ?: ""
            notificationHelper.showOrderStatusNotification(applicationContext, orderId, status)
        }
    }

    override fun onNewToken(token: String) {
        // This is where you would send the token to your server.
        // For now, we'll just log it.
        Log.d(TAG, "Refreshed token: $token")

        val user = localDatabase.getUser()
        if (user != null) {
            val userWithToken = User(
                uid = user.uid,
                name = user.name,
                phoneNumber = user.phoneNumber,
                address = user.address,
                fcmToken = token
            )
            CoroutineScope(Dispatchers.IO).launch {
                userRepository.saveNewToken(userWithToken) {
                    if (it) {
                        Log.d(TAG, "New token saved successfully")
                    } else {
                        Log.e(TAG, "Failed to save new token")
                    }
                }
            }
        }
    }
}