package com.example.localgrub.data.remote.repository

import com.example.localgrub.data.model.api.request.OtpRequest
import com.example.localgrub.data.model.api.request.ResendOtpRequest
import com.example.localgrub.data.model.api.request.VerifyOtpRequest
import com.example.localgrub.data.remote.api.LoginApiService
import com.example.localgrub.domain.model.failure.GetReqDomainFailure
import com.example.localgrub.domain.model.result.LoginResult
import com.example.localgrub.domain.repository.LoginRepository
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LoginRepositoryImpl @Inject constructor(
    private val apiService: LoginApiService
) : LoginRepository {

    override suspend fun sendOtp(request: OtpRequest): LoginResult {
        return try {
            val response = apiService.sendOtp(request)
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                if (body.success) {
                    LoginResult.SendOtpSuccess(body.data!!)
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

    override suspend fun verifyOtp(request: VerifyOtpRequest): LoginResult {
        return try {
            val response = apiService.verifyOtp(request)
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                if (body.success) {
                    LoginResult.VerifyOtpSuccess(body.data!!)
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

    override suspend fun resendOtp(request: ResendOtpRequest): LoginResult {
        return try {
            val response = apiService.resendOtp(request)
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                if (body.success) {
                    LoginResult.SendOtpSuccess(body.data!!)
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
