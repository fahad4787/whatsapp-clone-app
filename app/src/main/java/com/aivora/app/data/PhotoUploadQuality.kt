package com.aivora.app.data

import android.content.Context
import android.graphics.Bitmap
import com.aivora.app.data.local.AutoDownloadPrefs
import java.io.ByteArrayOutputStream

/** Recompress chat photos before upload (Wave 58). GIF and non-images pass through. */
object PhotoUploadQuality {
    data class Encoded(val bytes: ByteArray, val mime: String, val name: String)

    fun prepare(context: Context, bytes: ByteArray, mime: String, name: String): Encoded {
        if (!mime.startsWith("image/") || mime.contains("gif")) {
            return Encoded(bytes, mime, name)
        }
        val qualityKey = AutoDownloadPrefs.load(context).photoUploadQuality
        if (qualityKey == "high") return Encoded(bytes, mime, name)
        val (edge, jpegQ) = if (qualityKey == "data_saver") 1280 to 70 else 1920 to 85
        // Re-encoding drops the camera rotation tag, so the pixels must be turned upright here.
        val upright = decodeUprightBitmap(bytes, edge) ?: return Encoded(bytes, mime, name)
        val bos = ByteArrayOutputStream()
        val ok = upright.compress(Bitmap.CompressFormat.JPEG, jpegQ, bos)
        upright.recycle()
        if (!ok) return Encoded(bytes, mime, name)
        val jpgName = name.substringBeforeLast('.').ifBlank { "photo" } + ".jpg"
        return Encoded(bos.toByteArray(), "image/jpeg", jpgName)
    }
}
