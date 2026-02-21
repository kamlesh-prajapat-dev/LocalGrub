package com.example.localgrub.worker

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.localgrub.data.model.NotificationRequest
import com.example.localgrub.domain.model.result.TokenResult
import com.example.localgrub.domain.repository.NotificationRepository
import com.example.localgrub.domain.repository.TokenRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class SenderNotificationWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val tokenRepository: TokenRepository,
    private val notificationRepository: NotificationRepository
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val orderId = inputData.getString("ORDER_ID") ?: return Result.failure()

        return try {
            when(val result = tokenRepository.getToken(orderId)) {
                is TokenResult.TokenGetSuccess -> {
                    val token = result.tokenData.token
                    if (token.isBlank()) return Result.failure()

                    notificationRepository.sendNotification(
                        NotificationRequest(
                            token = token,
                            title = "New Order Received",
                            body = "You have a new order! Order ID: $orderId",
                            orderId = orderId
                        )
                    )
                    Result.success()
                }

                is TokenResult.Failure -> {
                    return Result.failure()
                }

                else -> return Result.failure()
            }
        } catch (e: Exception) {
            Log.e("SenderNotificationWorker", "Error sending notification", e)
            Result.retry()
        }
    }
}
