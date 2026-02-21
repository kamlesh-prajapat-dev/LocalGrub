package com.example.localgrub.domain.mapper.firestore

import com.google.firebase.FirebaseNetworkException
import com.google.firebase.firestore.FirebaseFirestoreException
import com.example.localgrub.domain.model.failure.GetReqDomainFailure
import com.example.localgrub.util.AppLogger
import com.example.localgrub.util.DataNotFoundException
import kotlin.coroutines.cancellation.CancellationException

object FirestoreFailureMapper {

    fun <T> map(throwable: Throwable, data: T): GetReqDomainFailure {

        return when (throwable) {

            is CancellationException -> {
                GetReqDomainFailure.Cancelled
            }

            is FirebaseNetworkException -> {
                GetReqDomainFailure.NoInternet
            }

            is FirebaseFirestoreException -> {
                mapFirestoreException(throwable, data)
            }

            is IllegalArgumentException -> {
                GetReqDomainFailure.InvalidRequest(throwable.message ?: "Invalid request")
            }

            is DataNotFoundException -> {
                GetReqDomainFailure.DataNotFound(throwable.message ?: "Data not found")
            }

            else -> {
                AppLogger.e(
                    tag = "UnknownFailure",
                    message = "Unexpected error in cancelBooking, data is $data",
                    throwable = throwable
                )
                GetReqDomainFailure.Unknown(throwable)
            }
        }
    }

    private fun <T> mapFirestoreException(
        exception: FirebaseFirestoreException,
        data: T
    ): GetReqDomainFailure {

        return when (exception.code) {

            FirebaseFirestoreException.Code.PERMISSION_DENIED -> {
                AppLogger.e(
                    tag = "FirebasePermission",
                    message = "Permission denied for bookingId=$data",
                    throwable = exception
                )
                GetReqDomainFailure.PermissionDenied(exception.message ?: "Permission Denied.")
            }

            FirebaseFirestoreException.Code.NOT_FOUND -> {
                GetReqDomainFailure.DataNotFound(exception.message ?: "Data not found")
            }

            FirebaseFirestoreException.Code.UNAVAILABLE -> {
                GetReqDomainFailure.NoInternet
            }

            FirebaseFirestoreException.Code.FAILED_PRECONDITION -> {
                // Mostly index missing
                GetReqDomainFailure.InvalidRequest(exception.message ?: "Invalid Request.")
            }

            else -> {
                AppLogger.e(
                    tag = "UnknownFailure",
                    message = "Unexpected error in cancelBooking, data is $data",
                    throwable = exception
                )
                GetReqDomainFailure.Unknown(exception)
            }
        }
    }
}