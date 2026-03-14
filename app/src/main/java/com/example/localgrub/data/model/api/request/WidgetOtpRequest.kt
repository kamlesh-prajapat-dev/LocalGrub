package com.example.localgrub.data.model.api.request

import kotlinx.serialization.Serializable

@Serializable
data class WidgetOtpRequest(
    val widgetId: String,
    val identifier: String
)
