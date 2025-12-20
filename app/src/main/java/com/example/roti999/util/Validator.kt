package com.example.roti999.util

class Validator {

    fun validatePhoneNumber(phoneNumber: String): String? {
        if (phoneNumber.isBlank()) {
            return "Phone number must not be null"
        }

        if (phoneNumber.length != 10) {
            return "Please enter a valid 10-digit phone number"
        }

        return null
    }

    fun validateOtp(otp: String): String? {
        if (otp.isBlank()) {
            return "OTP must not be null"
        }

        if (otp.length != 6) {
            return "Please enter a valid 6-digit OTP"
        }

        return null
    }
}