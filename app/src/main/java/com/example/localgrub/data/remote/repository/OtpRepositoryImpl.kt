package com.example.localgrub.data.remote.repository

import com.example.localgrub.data.remote.api.OtpApiService
import com.example.localgrub.domain.model.result.LoginResult
import com.example.localgrub.domain.repository.OtpRepository
import javax.inject.Inject
import javax.inject.Singleton
import com.example.localgrub.BuildConfig
import com.example.localgrub.data.model.api.request.WidgetOtpRequest
import com.example.localgrub.domain.model.failure.GetReqDomainFailure
import retrofit2.HttpException
import java.io.IOException

@Singleton
class OtpRepositoryImpl @Inject constructor(
    private val otpApiService: OtpApiService
) : OtpRepository {
    override suspend fun sendOtp(phoneNumber: String): LoginResult {
        if (phoneNumber.isBlank()) {
            return LoginResult.Failure(GetReqDomainFailure.InvalidRequest("Phone number cannot be blank."))
        }

        val widgetId = BuildConfig.WIDGET_ID
        val request = WidgetOtpRequest(
            widgetId = widgetId,
            identifier = "91$phoneNumber"
        )

        return try {
            val response = otpApiService.sendOtp(request)
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                if (body.type == "success") {
                    LoginResult.SendOtpSuccess(body)
                } else {
                    LoginResult.Failure(GetReqDomainFailure.InvalidRequest(body.message))
                }
            } else {
                LoginResult.Failure(GetReqDomainFailure.InvalidRequest(response.message()))
            }
        } catch (e: IOException) {
            LoginResult.Failure(GetReqDomainFailure.NoInternet)
        } catch (e: HttpException) {
            LoginResult.Failure(GetReqDomainFailure.InvalidRequest(e.message()))
        } catch (e: Exception) {
            LoginResult.Failure(GetReqDomainFailure.Unknown(e))
        }
    }
}