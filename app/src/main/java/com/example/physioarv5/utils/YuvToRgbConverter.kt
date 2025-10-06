package com.example.physioarv5.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageFormat
import android.graphics.PixelFormat
import android.media.Image
import androidx.camera.core.ImageProxy
import android.renderscript.*
import android.os.Build

// เวอร์ชันเรียบง่าย (ไม่ใช้ RS) ใช้บน Android 12+ ได้
class YuvToRgbConverter(private val context: Context) {
    fun yuvToRgb(image: ImageProxy, output: Bitmap) {
        val yBuffer = image.planes[0].buffer // Y
        val uBuffer = image.planes[1].buffer // U
        val vBuffer = image.planes[2].buffer // V

        val ySize = yBuffer.remaining()
        val uSize = uBuffer.remaining()
        val vSize = vBuffer.remaining()

        val nv21 = ByteArray(ySize + uSize + vSize)
        yBuffer.get(nv21, 0, ySize)
        vBuffer.get(nv21, ySize, vSize)
        uBuffer.get(nv21, ySize + vSize, uSize)

        val yuvImage = android.graphics.YuvImage(
            nv21, ImageFormat.NV21, image.width, image.height, null
        )
        val out = java.io.ByteArrayOutputStream()
        yuvImage.compressToJpeg(
            android.graphics.Rect(0, 0, image.width, image.height), 90, out
        )
        val jpegBytes = out.toByteArray()
        val b = android.graphics.BitmapFactory.decodeByteArray(jpegBytes, 0, jpegBytes.size)
        android.graphics.Canvas(output).drawBitmap(b, 0f, 0f, null)
    }
}
