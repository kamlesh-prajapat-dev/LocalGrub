package com.example.localgrub.data.model.firebase

import com.example.localgrub.util.OfferStatus
import com.example.localgrub.util.OfferType

data class GetOffer(
    val id: String = "",
    val promoCode: String = "",
    val description: String = "",
    val bannerImageUrl: String = "",
    val discountType: String = OfferType.PERCENTAGE,
    val discountValue: Double = 0.0,
    val minOrderAmount: Double = 0.0,
    val maxDiscountAmount: Double? = 0.0,
    val expiryDate: Long = 0L,
    val offerStatus: String = OfferStatus.ACTIVE,
    val createdAt: Long = System.currentTimeMillis()
)