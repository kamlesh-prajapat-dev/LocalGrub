package com.example.localgrub.domain.usecase

import com.example.localgrub.domain.mapper.firebase.toGetReqDomainFailure
import com.example.localgrub.domain.model.result.OfferResult
import com.example.localgrub.domain.repository.OfferRepository
import com.example.localgrub.ui.screens.home.HomeUIState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OfferUseCase @Inject constructor(
    private val offerRepository: OfferRepository
) {

    fun getOffer(): Flow<HomeUIState> {
        return offerRepository.getOffer()
            .map { result ->
                when(result) {
                    is OfferResult.GetSuccess -> HomeUIState.OfferGetSuccess(result.offers)

                    is OfferResult.Failure ->
                        HomeUIState.FirebaseGetFailure(result.failure.toGetReqDomainFailure("Offers Data."))


                }
            }.catch {
                emit(HomeUIState.FirebaseGetFailure(it.toGetReqDomainFailure("Offers Data.")))

            }
    }
}