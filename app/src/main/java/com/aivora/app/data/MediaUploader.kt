package com.aivora.app.data

import android.content.Context
import android.net.Uri
import com.aivora.app.data.api.AivoraApi
import com.aivora.app.data.model.AttachmentDto
import com.aivora.app.data.model.MediaPresignBody
import com.aivora.app.data.model.MessageDto
import com.aivora.app.data.model.SendMessageBody
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.UUID

suspend fun AivoraApi.sendImageMessage(
    context: Context,
    conversationId: String,
    imageUri: Uri,
    caption: String? = null,
): MessageDto {
    val bytes = withContext(Dispatchers.IO) {
        context.contentResolver.openInputStream(imageUri)?.use { it.readBytes() }
    } ?: throw IllegalStateException("Could not read image")
    val mime = context.contentResolver.getType(imageUri) ?: "image/jpeg"

    val uploaded = try {
        val presign = postMediaPresign(
            MediaPresignBody(contentType = mime, filename = "photo.jpg", size = bytes.size),
        )
        val uploadUrl = presign.uploadUrl
        if (!uploadUrl.isNullOrBlank()) {
            putMediaUpload(
                uploadUrl,
                bytes.toRequestBody(mime.toMediaTypeOrNull()),
            )
        } else {
            null
        }
    } catch (_: Exception) {
        null
    }

    val result = uploaded ?: run {
        val part = okhttp3.MultipartBody.Part.createFormData(
            "file",
            "photo.jpg",
            bytes.toRequestBody(mime.toMediaTypeOrNull()),
        )
        postMediaUpload(part)
    }

    val url = result.url ?: throw IllegalStateException("Upload missing url")
    val key = result.key ?: throw IllegalStateException("Upload missing key")

    return postConversationsIdMessages(
        conversationId,
        SendMessageBody(
            text = caption?.takeIf { it.isNotBlank() },
            clientId = UUID.randomUUID().toString(),
            attachment = AttachmentDto(
                key = key,
                url = url,
                mime = result.mime ?: mime,
                size = result.size.takeIf { it > 0 } ?: bytes.size,
                name = "photo.jpg",
            ),
        ),
    )
}
