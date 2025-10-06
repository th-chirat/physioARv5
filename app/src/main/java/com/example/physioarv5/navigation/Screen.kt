// File: navigation/Screen.kt
package com.example.physioarv5.navigation

import android.net.Uri

sealed class Screen(val route: String) {
    object Selection : Screen("selection")
    object Ar : Screen("ar")
    object Therapy : Screen("therapy")

    // ✅ เข้ารหัสพารามิเตอร์ก่อนใส่ในเส้นทาง (รองรับภาษาไทย/ช่องว่าง/อีโมจิ)
    fun createRoute(bodyPart: String): String {
        return "$route/${Uri.encode(bodyPart)}"
    }
}
