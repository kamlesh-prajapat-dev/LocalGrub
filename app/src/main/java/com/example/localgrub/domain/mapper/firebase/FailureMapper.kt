package com.example.localgrub.domain.mapper.firebase

import com.example.localgrub.util.AppLogger
import com.example.localgrub.util.DataNotFoundException
import com.example.localgrub.util.DataParsingException
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.database.DatabaseException
import java.io.IOException
import kotlin.coroutines.cancellation.CancellationException
import kotlin.text.contains

fun <T>Throwable.toGetReqDomainFailure(data: T): GetReqDomainFailure {
    return when (this) {
        is SecurityException -> {
            AppLogger.e(
                tag = "FirebasePermission",
                message = "Permission denied for bookingId=$data",
                throwable = this
            )
            GetReqDomainFailure.PermissionDenied(this.message ?: "")
        }
        is IOException -> GetReqDomainFailure.Network
        is DataNotFoundException -> GetReqDomainFailure.DataNotFount(this.message ?: "The requested data was not found.")
        is DataParsingException -> {
            AppLogger.e(
                tag = "ObserveBookings",
                message = "Unexpected error while parsing data $data",
                throwable = this
            )
            GetReqDomainFailure.InvalidData(this.message ?: "")
        }
        else -> {
            AppLogger.e(
                tag = "UnknownFailure",
                message = "Unexpected error in cancelBooking, data is $data",
                throwable = this
            )
            GetReqDomainFailure.Unknown(this)
        }
    }
}

fun <T> Throwable.toWriteReqDomainFailure(data: T): WriteReqDomainFailure {
    return when (this) {
        is FirebaseNetworkException -> {
            WriteReqDomainFailure.NoInternet
        }

        is DatabaseException -> {
            if (this.message?.contains("Permission denied", true) == true) {
                AppLogger.e(
                    tag = "FirebasePermission",
                    message = "Permission denied for bookingId=$data",
                    throwable = this
                )
                WriteReqDomainFailure.PermissionDenied(this.message ?: "Permission denied")
            } else {
                WriteReqDomainFailure.Unknown(this)
            }
        }

        is DataNotFoundException -> {
            WriteReqDomainFailure.DataNotFound(this.message ?: "The requested data was not found.")
        }

        is IllegalArgumentException -> {
            WriteReqDomainFailure.ValidationError(this.message ?: "Invalid input")
        }

        is CancellationException -> {
            WriteReqDomainFailure.Cancelled(this.message ?: "Cancelled")
        }

        is IllegalStateException -> {
            AppLogger.e(
                tag = "FirebaseIllegalStateException",
                message = "${this.message} for user: $data",
                throwable = this
            )
            WriteReqDomainFailure.Unknown(this)
        }

        else -> {
            AppLogger.e(
                tag = "UnknownFailure",
                message = "Unexpected error in cancelBooking",
                throwable = this
            )
            WriteReqDomainFailure.Unknown(this)
        }
    }
}
