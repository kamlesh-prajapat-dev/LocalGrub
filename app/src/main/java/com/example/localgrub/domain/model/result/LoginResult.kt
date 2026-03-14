package com.example.localgrub.domain.model.result

import com.example.localgrub.data.model.api.response.OtpResponse
import com.example.localgrub.data.model.api.response.VerifyOtpResponse
import com.example.localgrub.domain.model.failure.GetReqDomainFailure

sealed interface LoginResult {
    data class SendOtpSuccess(val response: OtpResponse) : LoginResult
    data class VerifyOtpSuccess(val response: VerifyOtpResponse) : LoginResult
    data class Failure(val failure: GetReqDomainFailure) : LoginResult
}