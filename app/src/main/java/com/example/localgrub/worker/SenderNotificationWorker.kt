package com.example.localgrub.worker

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.localgrub.data.model.api.request.NotificationRequest
import com.example.localgrub.domain.model.result.NotificationResult
import com.example.localgrub.domain.repository.NotificationRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class SenderNotificationWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val notificationRepository: NotificationRepository
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val orderId = inputData.getString("ORDER_ID")
        val userId = inputData.getString("USER_ID")
        val status = inputData.getString("STATUS")
        val userName = inputData.getString("USER_NAME")

        if (orderId == null || userId == null || status == null || userName == null) {
            Log.e(
                "SenderNotificationWorker",
                "Invalid input data for SenderNotificationWorker: OrderId: $orderId, UserId: $userId, Status: $status, UserName: $userName"
            )
            return Result.failure()
        }
        Log.d("SenderNotificationWorker", "Sending notification for orderId: $orderId")

        return when (val result = notificationRepository.sendNotification(
            NotificationRequest(
                userId = userId,
                orderId = orderId,
                status = status,
                userName = userName
            )
        )) {
            is NotificationResult.Success -> {
                Log.d("SenderNotificationWorker", result.message)
                Result.success()
            }

            is NotificationResult.Failure -> {
                Log.e("SenderNotificationWorker", result.exception.message, result.exception)
                Result.retry()
            }
        }
    }
}
