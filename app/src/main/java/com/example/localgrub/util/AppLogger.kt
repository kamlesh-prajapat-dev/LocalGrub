package com.example.localgrub.util

import android.util.Log
import com.example.localgrub.BuildConfig

object AppLogger {

    private const val DEFAULT_TAG = "AppLogger"

    private fun isLoggable(): Boolean {
        return BuildConfig.DEBUG
    }

    fun d(
        tag: String = DEFAULT_TAG,
        message: String
    ) {
        if (isLoggable()) {
            Log.d(tag, message)
        }
    }

    fun i(
        tag: String = DEFAULT_TAG,
        message: String
    ) {
        if (isLoggable()) {
            Log.i(tag, message)
        }
    }

    fun w(
        tag: String = DEFAULT_TAG,
        message: String,
        throwable: Throwable? = null
    ) {
        if (isLoggable()) {
            Log.w(tag, message, throwable)
        }
    }

    fun e(
        tag: String = DEFAULT_TAG,
        message: String,
        throwable: Throwable? = null
    ) {
        if (isLoggable()) {
            Log.e(tag, message, throwable)
        }
    }

    fun firebase(
        tag: String = DEFAULT_TAG,
        error: Throwable
    ) {
        if (isLoggable()) {
            Log.e(tag, "Firebase error: ${error.message}", error)
        }
        // In future:
        // FirebaseCrashlytics.getInstance().recordException(error)
    }
}
