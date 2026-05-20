package com.example.movilexplora.data.storage

import android.content.Context
import android.net.Uri
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.example.movilexplora.domain.repository.ImageRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

@Singleton
class CloudinaryImageRepository @Inject constructor(
    @ApplicationContext private val context: Context
) : ImageRepository {

    override suspend fun uploadImage(uri: Uri): String? = suspendCancellableCoroutine { continuation ->
        val fileUri = try {
            if (uri.scheme == "content") {
                val inputStream = context.contentResolver.openInputStream(uri)
                val tempFile = java.io.File.createTempFile("upload_", ".jpg", context.cacheDir)
                inputStream?.use { input ->
                    tempFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
                Uri.fromFile(tempFile)
            } else {
                uri
            }
        } catch (e: Exception) {
            e.printStackTrace()
            uri
        }

        MediaManager.get().upload(fileUri)
            .option("upload_preset", "ml_default")
            .option("folder", "movilexplora/posts")
            .callback(object : UploadCallback {
                override fun onStart(requestId: String?) {}
                override fun onProgress(requestId: String?, bytes: Long, totalBytes: Long) {}
                override fun onSuccess(requestId: String?, resultData: Map<*, *>?) {
                    val url = resultData?.get("secure_url") as? String
                    android.util.Log.d("CloudinaryUpload", "¡Éxito! Imagen subida correctamente a Cloudinary: $url")
                    continuation.resume(url)
                }
                override fun onError(requestId: String?, error: ErrorInfo?) {
                    android.util.Log.e("CloudinaryUpload", "Error al subir a Cloudinary: ${error?.description}")
                    continuation.resume(null)
                }
                override fun onReschedule(requestId: String?, error: ErrorInfo?) {
                    android.util.Log.w("CloudinaryUpload", "Re-programando subida a Cloudinary")
                    continuation.resume(null)
                }
            }).dispatch()
    }
}
