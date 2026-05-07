package com.dev.uasist.utils

import android.graphics.Bitmap
import android.graphics.Color
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.MultiFormatWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel

object QrGenerator {
    /**
     * Genera un Bitmap de un código QR a partir de un contenido string.
     */
    fun generarBitmapQR(contenido: String, size: Int = 512): Bitmap? {
        if (contenido.isEmpty()) return null
        return try {
            val hints = mapOf(
                EncodeHintType.CHARACTER_SET to "UTF-8",
                EncodeHintType.MARGIN to 1,
                EncodeHintType.ERROR_CORRECTION to ErrorCorrectionLevel.M
            )

            val matrix = MultiFormatWriter().encode(
                contenido, BarcodeFormat.QR_CODE, size, size, hints
            )

            val pixels = IntArray(size * size)
            for (y in 0 until size) {
                val offset = y * size
                for (x in 0 until size) {
                    pixels[offset + x] = if (matrix.get(x, y)) Color.BLACK else Color.WHITE
                }
            }

            val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
            bitmap.setPixels(pixels, 0, size, 0, 0, size, size)
            bitmap
        } catch (e: Throwable) {
            e.printStackTrace()
            null
        }
    }
}