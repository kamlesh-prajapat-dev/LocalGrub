package com.example.localgrub.domain.model.failure

sealed interface GetReqDomainFailure {
    object NoInternet : GetReqDomainFailure

    data class PermissionDenied(val message: String) : GetReqDomainFailure

    data class DataNotFound(val message: String) : GetReqDomainFailure

    data class InvalidRequest(val message: String) : GetReqDomainFailure

    object Cancelled : GetReqDomainFailure

    data class Unknown(
        val cause: Throwable
    ) : GetReqDomainFailure
}
