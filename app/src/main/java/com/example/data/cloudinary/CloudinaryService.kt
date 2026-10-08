package com.example.data.cloudinary

import android.content.Context
import android.net.Uri
import android.util.Log
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.InputStream
import java.util.concurrent.TimeUnit

object CloudinaryConfig {
    val cloudName: String
        get() = try {
            BuildConfig.CLOUDINARY_CLOUD_NAME.ifEmpty { "glqwxbbu" }
        } catch (_: Exception) {
            "glqwxbbu"
        }

    val apiKey: String
        get() = try {
            BuildConfig.CLOUDINARY_API_KEY.ifEmpty { "379859691332813" }
        } catch (_: Exception) {
            "379859691332813"
        }

    val uploadPreset: String
        get() = try {
            BuildConfig.CLOUDINARY_UPLOAD_PRESET.ifEmpty { "ml_default" }
        } catch (_: Exception) {
            "ml_default"
        }

    val folder: String
        get() = try {
            BuildConfig.CLOUDINARY_FOLDER.ifEmpty { "root_dr" }
        } catch (_: Exception) {
            "root_dr"
        }

    fun getOptimizedUrl(publicId: String, width: Int = 1080, quality: String = "auto"): String {
        return "https://res.cloudinary.com/$cloudName/image/upload/c_limit,w_$width,q_$quality,f_auto/$publicId.jpg"
    }

    fun getThumbnailUrl(publicId: String, width: Int = 500, height: Int = 900): String {
        return "https://res.cloudinary.com/$cloudName/image/upload/c_fill,w_$width,h_$height,g_auto,q_auto,f_auto/$publicId.jpg"
    }
}

data class CloudinaryUploadResult(
    val url: String,
    val secureUrl: String,
    val publicId: String,
    val width: Int,
    val height: Int,
    val format: String
)

class CloudinaryUploader(
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()
) {
    suspend fun uploadImageUri(
        context: Context,
        imageUri: Uri,
        tags: List<String> = emptyList()
    ): Result<CloudinaryUploadResult> = withContext(Dispatchers.IO) {
        try {
            val contentResolver = context.contentResolver
            val inputStream: InputStream? = contentResolver.openInputStream(imageUri)
            val bytes = inputStream?.use { it.readBytes() }
                ?: return@withContext Result.failure(Exception("Cannot read image bytes from URI"))

            val mimeType = contentResolver.getType(imageUri) ?: "image/jpeg"
            uploadBytes(bytes, mimeType, tags)
        } catch (e: Exception) {
            Log.e("CloudinaryUploader", "URI upload failed", e)
            Result.failure(e)
        }
    }

    suspend fun uploadFromWebUrl(
        imageUrl: String,
        tags: List<String> = emptyList()
    ): Result<CloudinaryUploadResult> = withContext(Dispatchers.IO) {
        try {
            val uploadUrl = "https://api.cloudinary.com/v1_1/${CloudinaryConfig.cloudName}/image/upload"
            val builder = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", imageUrl)
                .addFormDataPart("upload_preset", CloudinaryConfig.uploadPreset)
                .addFormDataPart("folder", CloudinaryConfig.folder)

            if (tags.isNotEmpty()) {
                builder.addFormDataPart("tags", tags.joinToString(","))
            }

            val request = Request.Builder()
                .url(uploadUrl)
                .post(builder.build())
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string().orEmpty()

            if (!response.isSuccessful) {
                return@withContext Result.failure(Exception("Cloudinary error: ${response.code} - $responseBody"))
            }

            val json = JSONObject(responseBody)
            val result = CloudinaryUploadResult(
                url = json.optString("url"),
                secureUrl = json.optString("secure_url"),
                publicId = json.optString("public_id"),
                width = json.optInt("width", 1080),
                height = json.optInt("height", 1920),
                format = json.optString("format", "jpg")
            )
            Result.success(result)
        } catch (e: Exception) {
            Log.e("CloudinaryUploader", "Web URL upload failed", e)
            Result.failure(e)
        }
    }

    suspend fun uploadBytes(
        bytes: ByteArray,
        mimeType: String = "image/jpeg",
        tags: List<String> = emptyList()
    ): Result<CloudinaryUploadResult> = withContext(Dispatchers.IO) {
        try {
            val uploadUrl = "https://api.cloudinary.com/v1_1/${CloudinaryConfig.cloudName}/image/upload"
            val mediaType = mimeType.toMediaTypeOrNull()

            val builder = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", "wallpaper_${System.currentTimeMillis()}.jpg", bytes.toRequestBody(mediaType))
                .addFormDataPart("upload_preset", CloudinaryConfig.uploadPreset)
                .addFormDataPart("folder", CloudinaryConfig.folder)

            if (tags.isNotEmpty()) {
                builder.addFormDataPart("tags", tags.joinToString(","))
            }

            val request = Request.Builder()
                .url(uploadUrl)
                .post(builder.build())
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string().orEmpty()

            if (!response.isSuccessful) {
                return@withContext Result.failure(Exception("Cloudinary upload failed: ${response.code} $responseBody"))
            }

            val json = JSONObject(responseBody)
            val result = CloudinaryUploadResult(
                url = json.optString("url"),
                secureUrl = json.optString("secure_url"),
                publicId = json.optString("public_id"),
                width = json.optInt("width", 1080),
                height = json.optInt("height", 1920),
                format = json.optString("format", "jpg")
            )
            Result.success(result)
        } catch (e: Exception) {
            Log.e("CloudinaryUploader", "Byte upload failed", e)
            Result.failure(e)
        }
    }
}
