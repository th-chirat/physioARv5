// File: utils/PoseLandmarkerHelper.kt
package com.example.physioarv5.utils

import android.content.Context
import android.graphics.Bitmap
import com.google.mediapipe.framework.image.BitmapImageBuilder
import com.google.mediapipe.tasks.core.BaseOptions
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarker
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarkerResult

class PoseLandmarkerHelper(
    private val context: Context,
    // ใช้ IMAGE mode เพื่อหลีกเลี่ยงความต่างของ detectAsync ระหว่างเวอร์ชัน
    private val runningMode: RunningMode = RunningMode.IMAGE,
    private val resultListener: (PoseLandmarkerResult, Long) -> Unit
) {
    private var landmarker: PoseLandmarker? = null

    fun setup(modelAssetPath: String = "pose_landmarker_lite.task") {
        val baseOptions = BaseOptions.builder()
            .setModelAssetPath(modelAssetPath)
            .build()

        val options = PoseLandmarker.PoseLandmarkerOptions.builder()
            .setBaseOptions(baseOptions)
            .setRunningMode(runningMode)         // IMAGE
            .setMinPoseDetectionConfidence(0.5f)
            .setMinPosePresenceConfidence(0.5f)
            .setMinTrackingConfidence(0.5f)
            // ⚠️ IMAGE mode ไม่ต้อง setResultListener เลย
            .build()

        landmarker = PoseLandmarker.createFromOptions(context, options)
    }

    fun send(bitmap: Bitmap) {
        val mpImage = BitmapImageBuilder(bitmap).build()
        // IMAGE mode → ใช้ detect() (synchronous) แล้วคืนผลผ่าน callback เอง
        val result = landmarker?.detect(mpImage)
        if (result != null) {
            resultListener(result, System.currentTimeMillis())
        }
    }

    fun close() {
        landmarker?.close()
        landmarker = null
    }
}
