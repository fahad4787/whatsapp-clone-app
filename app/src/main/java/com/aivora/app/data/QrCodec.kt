package com.aivora.app.data

import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import com.google.zxing.BarcodeFormat
import com.google.zxing.BinaryBitmap
import com.google.zxing.DecodeHintType
import com.google.zxing.MultiFormatReader
import com.google.zxing.RGBLuminanceSource
import com.google.zxing.common.HybridBinarizer
import com.google.zxing.qrcode.QRCodeWriter

object QrCodec {
    const val SchemeHostUser = "aivora://user/"
    const val SchemeHostLink = "aivora://link/"
    const val HttpsPrefix = "https://whatsapp-clone-backend-hep1.onrender.com/u/"

    fun profilePayload(userId: String): String = "$SchemeHostUser$userId"

    /** Companion device pairing code (Wave 56). */
    fun linkPayload(code: String): String = "$SchemeHostLink${code.trim().uppercase()}"

    fun encodeBitmap(payload: String, size: Int = 720): Bitmap {
        val bits = QRCodeWriter().encode(payload, BarcodeFormat.QR_CODE, size, size)
        val bmp = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        for (x in 0 until size) {
            for (y in 0 until size) {
                bmp.setPixel(x, y, if (bits[x, y]) Color.BLACK else Color.WHITE)
            }
        }
        return bmp
    }

    fun decodeBitmap(bitmap: Bitmap): String? {
        val w = bitmap.width
        val h = bitmap.height
        val pixels = IntArray(w * h)
        bitmap.getPixels(pixels, 0, w, 0, 0, w, h)
        val source = RGBLuminanceSource(w, h, pixels)
        return decodeBinary(BinaryBitmap(HybridBinarizer(source)))
    }

    /** Y-plane from CameraX [ImageAnalysis] (YUV_420_888). */
    fun decodeLuma(yPlane: ByteArray, width: Int, height: Int): String? {
        if (width <= 0 || height <= 0 || yPlane.size < width * height) return null
        val source = com.google.zxing.PlanarYUVLuminanceSource(
            yPlane,
            width,
            height,
            0,
            0,
            width,
            height,
            false,
        )
        return decodeBinary(BinaryBitmap(HybridBinarizer(source)))
    }

    private fun decodeBinary(binary: BinaryBitmap): String? =
        runCatching {
            MultiFormatReader().decode(
                binary,
                mapOf(DecodeHintType.POSSIBLE_FORMATS to listOf(BarcodeFormat.QR_CODE)),
            ).text
        }.getOrNull()

    fun userIdFromPayload(raw: String?): String? {
        if (raw.isNullOrBlank()) return null
        if (linkCodeFromPayload(raw) != null) return null
        val uri = runCatching { Uri.parse(raw.trim()) }.getOrNull() ?: return null
        if (uri.scheme == "aivora" && uri.host == "user") {
            return uri.pathSegments?.firstOrNull()?.trim()?.takeIf { it.isNotEmpty() }
        }
        if (uri.scheme == "https" &&
            uri.host == "whatsapp-clone-backend-hep1.onrender.com" &&
            uri.pathSegments?.firstOrNull() == "u"
        ) {
            return uri.pathSegments.getOrNull(1)?.trim()?.takeIf { it.isNotEmpty() }
        }
        // Bare user id pasted from share sheet
        if (raw.matches(Regex("^[a-zA-Z0-9_-]{8,64}$"))) return raw.trim()
        return null
    }

    fun linkCodeFromPayload(raw: String?): String? {
        if (raw.isNullOrBlank()) return null
        val trimmed = raw.trim()
        val uri = runCatching { Uri.parse(trimmed) }.getOrNull()
        if (uri?.scheme == "aivora" && uri.host == "link") {
            val code = uri.pathSegments?.firstOrNull()?.trim()?.uppercase().orEmpty()
            if (code.matches(LINK_CODE_RE)) return code
        }
        // Bare pairing code only (backend alphabet; avoids stealing profile user ids)
        val bare = trimmed.uppercase()
        if (bare.matches(LINK_CODE_RE)) return bare
        return null
    }

    private val LINK_CODE_RE = Regex("^[ABCDEFGHJKLMNPQRSTUVWXYZ23456789]{8}$")
}
