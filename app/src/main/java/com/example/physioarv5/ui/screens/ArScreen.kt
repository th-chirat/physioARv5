package com.example.physioarv5.ui.screens

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController

// SceneView / AR
import io.github.sceneview.ar.ArSceneView
import io.github.sceneview.ar.node.ArModelNode
import io.github.sceneview.ar.node.PlacementMode
import io.github.sceneview.math.Position
import io.github.sceneview.math.Scale

@Composable
fun ArScreen(navController: NavController, bodyPart: String) {
    Box(Modifier.fillMaxSize()) {

        val context = LocalContext.current
        var arView by remember { mutableStateOf<ArSceneView?>(null) }
        var modelNode by remember { mutableStateOf<ArModelNode?>(null) }

        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                ArSceneView(ctx).apply {
                    // เปิดให้แสดง plane ของ AR
                    planeRenderer.isEnabled = true
                    // เก็บไว้เพื่อใช้ตอน update/onRelease
                    arView = this
                }
            },
            update = { view ->
                // โหลดโมเดลครั้งเดียว
                if (modelNode == null) {
                    val node = ArModelNode(view.engine, view.modelLoader).apply {
                        placementMode = PlacementMode.PLANE_HORIZONTAL
                        // scale/position ปรับได้ตามต้องการ
                        scale = Scale(0.4f)
                        position = Position(0.0f, 0.0f, -0.5f)
                        // โหลด GLB จาก assets (path ต้องตรง)
                        loadModelGlbAsync(
                            context = context,
                            glbFileLocation = "models/Duck.glb"
                        ) {
                            // callback หลังโหลดเสร็จ
                        }
                    }
                    modelNode = node
                    view.addChild(node)
                }
            },
            onRelease = { view ->
                // เก็บกวาดเมื่อคอมโพสออกจากคอมโพสทรี
                try {
                    modelNode?.destroy()
                } catch (_: Throwable) {}
                modelNode = null
                try {
                    view.destroy()
                } catch (_: Throwable) {}
                arView = null
            }
        )

        // ปุ่มลอย/ส่วน UI อื่น ๆ ใช้ของเดิมที่คุณมีอยู่แล้ว
        // (GradientSelectionButton, ปุ่มเริ่ม, ฯลฯ)
    }
}
