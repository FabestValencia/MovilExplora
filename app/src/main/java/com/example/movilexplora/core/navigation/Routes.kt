package com.example.movilexplora.core.navigation

import kotlinx.serialization.Serializable

@Serializable
object Home

@Serializable
object Login

@Serializable
object Register

@Serializable
object ForgotPassword

@Serializable
object VerificationCode

@Serializable
object ResetPassword

@Serializable
object Success

@Serializable
object Moderator

@Serializable
object ModeratorFeed

@Serializable
object ModeratorHistory

@Serializable
object Feed

@Serializable
object Events

@Serializable
object MapRoute

@Serializable
object CreatePost

@Serializable
data class CreateEditEvent(val eventId: String? = null)

@Serializable
data class PostDetail(val postId: String)

@Serializable
data class EventDetail(val eventId: String)

@Serializable
object Statistics

@Serializable
object Profile

@Serializable
object EditProfile

@Serializable
object Notifications

@Serializable
object Reputation

@Serializable
object Badges
