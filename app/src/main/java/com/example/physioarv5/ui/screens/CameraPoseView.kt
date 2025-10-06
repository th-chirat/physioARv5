// File: ui/screens/CameraPoseView.kt
package com.example.physioarv5.ui.screens

import android.graphics.Bitmap
import android.graphics.Matrix
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.compose.ui.unit.dp
import com.example.physioarv5.utils.PoseLandmarkerHelper
import com.example.physioarv5.utils.YuvToRgbConverter
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarkerResult

@Composable
fun CameraPoseView(
    modifier: Modifier = Modifier,
    cameraSelector: CameraSelector,              // <- เลือกกล้องหน้า/หลังที่นี่
    mirrorOverlay: Boolean = false,              // <- ถ้ากล้องหน้าให้ true เพื่อกลับด้าน overlay ให้ตรง preview
    onResult: (PoseLandmarkerResult, Int, Int) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var lastResult by remember { mutableStateOf<PoseLandmarkerResult?>(null) }
    var frameW by remember { mutableStateOf(0) }
    var frameH by remember { mutableStateOf(0) }

    // MediaPipe helper (โหมด IMAGE → เรียก detect() synchronous)
    val landmarker = remember {
        PoseLandmarkerHelper(
            context = context,
            resultListener = { result, _ ->
                lastResult = result
                if (frameW > 0 && frameH > 0) onResult(result, frameW, frameH)
            }
        ).apply { setup("pose_landmarker_lite.task") }
    }
    DisposableEffect(Unit) { onDispose { landmarker.close() } }

    Box(modifier = modifier) {
        // CameraX Preview
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                PreviewView(ctx).apply { scaleType = PreviewView.ScaleType.FILL_CENTER }
            },
            update = { pv ->
                val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
                val preview = Preview.Builder().build().apply {
                    setSurfaceProvider(pv.surfaceProvider)
                }
                val analysis = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_YUV_420_888)
                    .build()
                    .also { ia ->
                        val yuv = YuvToRgbConverter(context)
                        ia.setAnalyzer(ContextCompat.getMainExecutor(context)) { imageProxy ->
                            try {
                                frameW = imageProxy.width
                                frameH = imageProxy.height
                                val bmp = Bitmap.createBitmap(frameW, frameH, Bitmap.Config.ARGB_8888)
                                yuv.yuvToRgb(imageProxy, bmp)
                                val rotated = rotateIfNeeded(bmp, imageProxy.imageInfo.rotationDegrees)
                                landmarker.send(rotated)
                            } finally {
                                imageProxy.close()
                            }
                        }
                    }

                cameraProviderFuture.addListener({
                    val provider = cameraProviderFuture.get()
                    provider.unbindAll()
                    provider.bindToLifecycle(lifecycleOwner, cameraSelector, preview, analysis)
                }, ContextCompat.getMainExecutor(context))
            }
        )

        // Overlay วาด skeleton
        val result = lastResult
        if (result != null) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height
                val list = result.landmarks()
                if (list.isNotEmpty()) {
                    val pts = list[0]

                    fun X(v: Float) = if (mirrorOverlay) (w - v * w) else (v * w)
                    fun Y(v: Float) = v * h

                    // จุด
                    pts.forEach {
                        drawCircle(
                            color = Color.Green,
                            radius = 4.dp.toPx(),
                            center = androidx.compose.ui.geometry.Offset(X(it.x()), Y(it.y()))
                        )
                    }
                    // เส้นตัวอย่าง
                    fun L(i: Int) = androidx.compose.ui.geometry.Offset(X(pts[i].x()), Y(pts[i].y()))
                    val pairs = listOf(
                        11 to 13, 13 to 15,   // แขนซ้าย
                        12 to 14, 14 to 16,   // แขนขวา
                        23 to 25, 25 to 27,   // ขาซ้าย
                        24 to 26, 26 to 28,   // ขาขวา
                        11 to 12, 11 to 23, 12 to 24 // ลำตัว
                    )
                    pairs.forEach { (a, b) ->
                        drawLine(Color.Cyan, start = L(a), end = L(b), strokeWidth = 3.dp.toPx())
                    }
                }
            }
        }
    }
}

private fun rotateIfNeeded(src: Bitmap, degrees: Int): Bitmap {
    if (degrees == 0) return src
    val m = Matrix().apply { postRotate(degrees.toFloat()) }
    return Bitmap.createBitmap(src, 0, 0, src.width, src.height, m, true)
}
