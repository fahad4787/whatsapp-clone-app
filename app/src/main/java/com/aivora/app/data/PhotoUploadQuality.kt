package com.aivora.app.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.aivora.app.data.local.AutoDownloadPrefs
import java.io.ByteArrayOutputStream
import kotlin.math.max
import kotlin.math.roundToInt

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
        val decoded = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            ?: return Encoded(bytes, mime, name)
        val longest = max(decoded.width, decoded.height).coerceAtLeast(1)
        val scale = (edge.toFloat() / longest).coerceAtMost(1f)
        val outBmp = if (scale < 0.999f) {
            Bitmap.createScaledBitmap(
                decoded,
                (decoded.width * scale).roundToInt().coerceAtLeast(1),
                (decoded.height * scale).roundToInt().coerceAtLeast(1),
                true,
            )
        } else {
            decoded
        }
        val bos = ByteArrayOutputStream()
        val ok = outBmp.compress(Bitmap.CompressFormat.JPEG, jpegQ, bos)
        if (outBmp !== decoded) outBmp.recycle()
        decoded.recycle()
        if (!ok) return Encoded(bytes, mime, name)
        val jpgName = name.substringBeforeLast('.').ifBlank { "photo" } + ".jpg"
        return Encoded(bos.toByteArray(), "image/jpeg", jpgName)
    }
}
