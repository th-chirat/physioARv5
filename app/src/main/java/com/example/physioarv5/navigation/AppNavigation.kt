// File: navigation/AppNavigation.kt
package com.example.physioarv5.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.physioarv5.ui.screens.ArScreen
import com.example.physioarv5.ui.screens.SelectionScreen
import com.example.physioarv5.ui.screens.TherapyScreen

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screen.Selection.route
    ) {
        composable(Screen.Selection.route) {
            SelectionScreen(navController)
        }

        // ✅ ระบุ argument ให้ชัด และรองรับค่าที่ถูก encode
        composable(
            route = "${Screen.Ar.route}/{bodyPart}",
            arguments = listOf(navArgument("bodyPart") { type = NavType.StringType })
        ) { backStackEntry ->
            val bodyPart = backStackEntry.arguments?.getString("bodyPart") ?: ""
            ArScreen(navController, bodyPart)
        }

        composable(
            route = "${Screen.Therapy.route}/{bodyPart}",
            arguments = listOf(navArgument("bodyPart") { type = NavType.StringType })
        ) { backStackEntry ->
            val bodyPart = backStackEntry.arguments?.getString("bodyPart") ?: ""
            TherapyScreen(navController, bodyPart)
        }
    }
}
