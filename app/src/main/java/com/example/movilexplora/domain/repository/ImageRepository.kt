package com.example.movilexplora.domain.repository

import android.net.Uri

interface ImageRepository {
    suspend fun uploadImage(uri: Uri): String?
}
