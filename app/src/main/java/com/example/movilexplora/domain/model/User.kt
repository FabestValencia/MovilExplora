package com.example.movilexplora.domain.model

import com.example.movilexplora.domain.model.enum.UserRole
import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
data class User(
    var id: String = "",
    var name: String = "",
    var email: String = "",
    var password: String? = null,
    var city: String = "",
    var address: String = "",
    var profilePictureUrl: String = "",
    var role: String = "EXPLORER",
    var points: Long = 0,
    var latitude: Double = 0.0,
    var longitude: Double = 0.0,
    var isVerified: Boolean = false,
    var verificationCode: String = ""
) {
    // Constructor sin argumentos para Firestore
    constructor() : this("", "", "", null, "", "", "", "EXPLORER", 0, 0.0, 0.0, false, "")

    val userRole: UserRole
        get() = try {
            UserRole.valueOf(role.trim().uppercase())
        } catch (e: Exception) {
            UserRole.EXPLORER
        }
}
