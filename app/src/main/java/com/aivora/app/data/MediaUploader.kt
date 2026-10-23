package com.aivora.app.data

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import com.aivora.app.data.api.AivoraApi
import com.aivora.app.data.model.AttachmentDto
import com.aivora.app.data.model.MessageDto
import com.aivora.app.data.model.SendMessageBody
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.util.UUID

suspend fun AivoraApi.sendImageMessage(
    context: Context,
    conversationId: String,
    imageUri: Uri,
    caption: String? = null,
    viewOnce: Boolean = false,
): MessageDto = sendAttachmentMessage(
    context = context,
    conversationId = conversationId,
    uri = imageUri,
    caption = caption,
    viewOnce = viewOnce,
    defaultName = "photo.jpg",
    defaultMime = "image/jpeg",
)

suspend fun AivoraApi.sendAttachmentMessage(
    context: Context,
    conversationId: String,
    uri: Uri,
    caption: String? = null,
    defaultName: String = "file.bin",
    defaultMime: String = "application/octet-stream",
    durationSec: Int? = null,
    viewOnce: Boolean = false,
): MessageDto {
    val bytes = withContext(Dispatchers.IO) {
        context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
    } ?: throw IllegalStateException("Could not read file")
    val mime = context.contentResolver.getType(uri) ?: defaultMime
    val name = queryDisplayName(context, uri) ?: defaultName

    val part = okhttp3.MultipartBody.Part.createFormData(
        "file",
        name,
        bytes.toRequestBody(mime.toMediaTypeOrNull()),
    )
    val result = postMediaUpload(part)

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
                name = name,
                duration = durationSec ?: 0,
            ),
            viewOnce = viewOnce,
        ),
    )
}

suspend fun AivoraApi.sendVoiceFile(
    conversationId: String,
    file: File,
    mime: String = "audio/mp4",
    durationSec: Int,
): MessageDto {
    val bytes = withContext(Dispatchers.IO) { file.readBytes() }
    val name = file.name.ifBlank { "voice.m4a" }
    val part = okhttp3.MultipartBody.Part.createFormData(
        "file",
        name,
        bytes.toRequestBody(mime.toMediaTypeOrNull()),
    )
    val result = postMediaUpload(part)
    val url = result.url ?: throw IllegalStateException("Upload missing url")
    val key = result.key ?: throw IllegalStateException("Upload missing key")
    return postConversationsIdMessages(
        conversationId,
        SendMessageBody(
            clientId = UUID.randomUUID().toString(),
            attachment = AttachmentDto(
                key = key,
                url = url,
                mime = result.mime ?: mime,
                size = result.size.takeIf { it > 0 } ?: bytes.size,
                name = name,
                duration = durationSec.coerceAtLeast(1),
            ),
        ),
    )
}

private fun queryDisplayName(context: Context, uri: Uri): String? {
    val cursor = context.contentResolver.query(uri, null, null, null, null) ?: return null
    cursor.use {
        val idx = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        if (idx < 0 || !it.moveToFirst()) return null
        return it.getString(idx)
    }
}
