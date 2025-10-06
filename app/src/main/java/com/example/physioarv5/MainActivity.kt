// MainActivity.kt
package com.example.physioarv5

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.physioarv5.navigation.AppNavigation
import com.example.physioarv5.ui.theme.PhysioARTheme
import com.example.physioarv5.utils.LocaleManager
import com.example.physioarv5.utils.rememberCameraPermissionState

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {

        // ✅ 1) Apply locale ที่เคยบันทึกไว้ ก่อนสร้าง UI
        LocaleManager.applySavedLocaleBlocking(this)

        super.onCreate(savedInstanceState)
        setContent {
            // ✅ 2) ถ้าภาษาถูกเปลี่ยนในแอประหว่างใช้งาน ให้ apply ซ้ำด้วย context ของ Activity
            val lang by LocaleManager.languageFlow(this).collectAsState(initial = "th")
            LaunchedEffect(lang) { LocaleManager.applyAppLocale(this@MainActivity, lang) }

            PhysioARTheme {
                val hasCameraPermission by rememberCameraPermissionState()
                if (hasCameraPermission) {
                    AppNavigation()
                } else {
                    Surface(color = MaterialTheme.colorScheme.background) {
                        Text(
                            text = stringResource(R.string.camera_perm_wait),
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
            }
        }
    }
}
