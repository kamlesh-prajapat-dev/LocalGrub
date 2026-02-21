package com.example.localgrub.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.localgrub.domain.usecase.TokenUseCase
import com.example.localgrub.domain.usecase.UserUseCase
import com.example.localgrub.util.AppLogger
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class FCMTokenWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val tokenUseCase: TokenUseCase,
    private val userUseCase: UserUseCase
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val token = inputData.getString(KEY_FCM_TOKEN)
            ?: return Result.failure()

        return try {
            val user = userUseCase.getLocalUser()
            if (user != null) {
                tokenUseCase.saveUserToken(token = token, userId = user.uid)
            } else {
                AppLogger.e(
                    TAG,
                    "User is null."
                )
            }
            Result.success()
        } catch (e: Exception) {
            AppLogger.e(TAG, "Error saving FCM token", e)

            // Guard against infinite retries
            if (runAttemptCount >= 3) {
                Result.failure()
            } else {
                Result.retry()
            }
        }
    }

    companion object {
        const val KEY_FCM_TOKEN = "FCM_TOKEN"
        private const val TAG = "FCMTokenWorker"
    }
}