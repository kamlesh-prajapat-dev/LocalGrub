package com.example.localgrub.util

object AppConstant {
    const val LOCAL_DATABASE_NAME = "LocalGrubPrefs"
    const val OTP_VALIDITY_MS = 60_000L // 60 seconds
}


object UserRepositoryConstant {
    const val USERS_COLLECTION_NAME = "users"
    const val PHONE_NUMBER = "phoneNumber"
}

object TokenRepositoryConstant {
    const val TOKENS_COLLECTION_NAME = "tokens"
    const val ADMINS_COLLECTION_NAME = "admins"
}
object OrderStatus {
    const val PLACED = "Placed"
    const val CONFIRMED = "Confirmed"
    const val PREPARING = "Preparing"
    const val OUT_FOR_DELIVERY = "Out for Delivery"
    const val DELIVERED = "Delivered"
    const val CANCELLED = "Cancelled"
}

object DishFields {
    const val COLLECTION = "dishes"
    const val IN_STOCK = "available"
}

object OrderFields {
    const val COLLECTION = "orders"
    const val STATUS = "status"
    const val PREVIOUS_STATUS = "previousStatus"
    const val USER_ID = "userId"
    const val CANCELLED_AT = "cancelledAt"
}

object ShopOwnerFields {
    const val COLLECTION = "owners"
    const val TOKEN = "token"
}

object UserFields {
    const val COLLECTION = "users"
    const val TOKEN = "fcmToken"
}

object OfferType {
    const val PERCENTAGE = "Percentage"
    const val FIXED = "Fixed"
}

object OfferStatus {
    const val ACTIVE = "Active"
    const val INACTIVE = "Inactive"
    const val EXPIRED = "Expired"
}

object OfferConstant {
    const val COLLECTION_NAME = "offers"
    const val OFFER_STATUS = "offerStatus"
}