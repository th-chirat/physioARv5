// File: ui/screens/TherapyScreen.kt
package com.example.physioarv5.ui.screens

import androidx.camera.core.CameraSelector
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.physioarv5.R
import com.example.physioarv5.navigation.Screen
import com.example.physioarv5.ui.theme.PhysioARTheme
import com.example.physioarv5.utils.LocaleManager
import com.example.physioarv5.utils.TextToSpeechHelper
import com.example.physioarv5.viewmodel.TherapyViewModel
import kotlinx.coroutines.launch
import android.content.Context
import com.example.physioarv5.utils.KeepScreenOn     // ← ใช้กันจอดับ (มีไฟล์นี้แล้วใน utils)

@Composable
fun TherapyScreen(
    navController: NavController,
    bodyPart: String
) {
    val viewModel: TherapyViewModel = viewModel()
    val uiState by viewModel.uiState.collectAsState()
    val scope = rememberCoroutineScope()

    // ---- Text To Speech + ภาษา ----
    val context = LocalContext.current
    val lang by LocaleManager.languageFlow(context).collectAsState(initial = "th")
    var ttsHelper by remember { mutableStateOf<TextToSpeechHelper?>(null) }

    // สร้าง TTS ตามภาษาปัจจุบัน
    LaunchedEffect(Unit) {
        ttsHelper = TextToSpeechHelper(context, initialLangTag = lang)
        viewModel.startTherapy()
    }
    // เปลี่ยนภาษา TTS ระหว่างใช้งาน
    LaunchedEffect(lang) {
        ttsHelper?.updateLanguage(lang)
    }
    // พูดฟีดแบ็ก (แปลงเป็นภาษาที่เลือกก่อน)
    LaunchedEffect(uiState.feedbackMessage, lang) {
        val raw = uiState.feedbackMessage
        if (raw.isNotBlank()) {
            val mapped = mapFeedbackForLocale(context, raw, lang)
            ttsHelper?.speak(mapped)
        }
    }
    DisposableEffect(Unit) { onDispose { ttsHelper?.shutdown() } }

    // ---- UI ----
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(12.dp)
    ) {
        KeepScreenOn()  // ✅ กันจอดับ

        // กล้องหน้า + วิเคราะห์ท่าทาง
        CameraPoseView(
            modifier = Modifier.fillMaxSize(),
            cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA,
            mirrorOverlay = true
        ) { _, _, _ -> /* ใช้ผลลัพธ์ตาม logic ของคุณได้ที่นี่ */ }

        // การ์ดควบคุมลอยด้านล่าง
        Card(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.96f))
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // สถิติย่อ
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    StatItem(
                        title = stringResource(R.string.therapy_reps),
                        value = "${uiState.currentReps}/${uiState.totalReps}"
                    )
                    StatItem(
                        title = stringResource(R.string.therapy_progress),
                        value = "${uiState.progress}%"
                    )
                    StatItem(
                        title = stringResource(R.string.therapy_time),
                        value = uiState.formattedTime
                    )
                }

                // แถบความคืบหน้า
                val animated by animateFloatAsState(
                    targetValue = uiState.progress / 100f,
                    animationSpec = ProgressIndicatorDefaults.ProgressAnimationSpec,
                    label = "progress"
                )
                LinearProgressIndicator(
                    progress = { animated },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .clip(RoundedCornerShape(6.dp))
                )

                // ข้อความแนะนำ — แปลงตามภาษาเดียวกับ TTS
                val feedbackForUi = if (uiState.feedbackMessage.isBlank()) {
                    stringResource(R.string.therapy_hint_default)
                } else {
                    mapFeedbackForLocale(context, uiState.feedbackMessage, lang)
                }
                Text(
                    text = feedbackForUi,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color(0xFF1B5E20)
                )

                // ปุ่มล่าง 3 ปุ่ม (กลับ/หยุดพัก/เสร็จสิ้น)
                TherapyBottomButtons(
                    onBack = {
                        navController.navigate(Screen.Selection.route) {
                            launchSingleTop = true
                            popUpTo(Screen.Selection.route) { inclusive = false }
                        }
                    },
                    onPauseResume = { viewModel.togglePause() },
                    isPaused = uiState.isPaused,
                    onDone = {
                        scope.launch {
                            if (uiState.currentReps > 0) {
                                val speakText = context.getString(R.string.tts_finish, uiState.currentReps)
                                ttsHelper?.speak(speakText)
                            }
                            navController.navigate(Screen.Selection.route) {
                                launchSingleTop = true
                                popUpTo(Screen.Selection.route) { inclusive = false }
                            }
                        }
                    },
                    enabledDone = uiState.currentReps > 0
                )
            }
        }
    }
}

/** แมปประโยคฟีดแบ็กจากฝั่ง ViewModel ให้เป็นภาษาปัจจุบัน (ใช้ทั้ง UI และ TTS) */
private fun mapFeedbackForLocale(ctx: Context, raw: String, lang: String): String {
    // ใส่เคสที่คุณใช้จริงใน ViewModel ตรงนี้ (เท่าที่เราเคยเห็นตัวอย่าง)
    return when (raw.trim()) {
        // TH → ใช้ resource ที่มีคำแปล EN ให้อัตโนมัติ
        "รักษาทรงให้ตรงค่ะ", "รักษาทรงให้ตรงครับ" -> ctx.getString(R.string.fb_keep_straight)
        "อีกนิดเดียวค่ะ", "อีกนิดเดียวครับ"         -> ctx.getString(R.string.fb_one_more)
        "ดีมากค่ะ", "ดีมากครับ", "เยี่ยมมาก!"        -> ctx.getString(R.string.fb_good_job)
        "ขยับช้าลงค่ะ", "ขยับช้าลงครับ"             -> ctx.getString(R.string.fb_move_slowly)
        "ยืดอกค่ะ", "ยืดอกครับ"                      -> ctx.getString(R.string.fb_open_chest)
        "ลดไหล่ลงค่ะ", "ลดไหล่ลงครับ"               -> ctx.getString(R.string.fb_drop_shoulders)

        // ถ้า ViewModel ส่ง EN มาอยู่แล้ว ให้คงเดิม
        "Keep your posture straight." -> ctx.getString(R.string.fb_keep_straight)
        "Almost there!"               -> ctx.getString(R.string.fb_one_more)
        "Great job!"                  -> ctx.getString(R.string.fb_good_job)
        "Move more slowly."           -> ctx.getString(R.string.fb_move_slowly)
        "Open your chest."            -> ctx.getString(R.string.fb_open_chest)
        "Drop your shoulders."        -> ctx.getString(R.string.fb_drop_shoulders)

        else -> raw // ไม่รู้จักข้อความ—แสดงตามเดิม
    }
}

/** ปุ่มล่างสามปุ่ม: กลับ / หยุดพัก / เสร็จสิ้น  (สีอ่อน + ขนาดเท่ากัน + ไม่มีอิโมจิ) */
@Composable
private fun TherapyBottomButtons(
    onBack: () -> Unit,
    onPauseResume: () -> Unit,
    isPaused: Boolean,
    onDone: () -> Unit,
    enabledDone: Boolean
) {
    val backColor  = Color(0xFFB3E5FC) // ฟ้าอ่อน
    val pauseColor = Color(0xFFFFCDD2) // แดงอ่อน
    val doneColor  = Color(0xFFC8E6C9) // เขียวอ่อน

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Button(
            onClick = onBack,
            modifier = Modifier
                .weight(1f)
                .height(56.dp),
            shape = MaterialTheme.shapes.extraLarge,
            colors = ButtonDefaults.buttonColors(
                containerColor = backColor,
                contentColor = Color(0xFF0D47A1)
            )
        ) { Text(stringResource(R.string.btn_back)) }

        Button(
            onClick = onPauseResume,
            modifier = Modifier
                .weight(1f)
                .height(56.dp),
            shape = MaterialTheme.shapes.extraLarge,
            colors = ButtonDefaults.buttonColors(
                containerColor = pauseColor,
                contentColor = Color(0xFFB71C1C)
            )
        ) { Text(if (isPaused) stringResource(R.string.btn_resume) else stringResource(R.string.btn_pause)) }

        Button(
            onClick = onDone,
            enabled = enabledDone,
            modifier = Modifier
                .weight(1f)
                .height(56.dp),
            shape = MaterialTheme.shapes.extraLarge,
            colors = ButtonDefaults.buttonColors(
                containerColor = doneColor,
                contentColor = Color(0xFF1B5E20),
                disabledContainerColor = doneColor.copy(alpha = 0.6f),
                disabledContentColor = Color(0xFF1B5E20).copy(alpha = 0.5f)
            )
        ) { Text(stringResource(R.string.btn_finish)) }
    }
}

// --------- ของเดิมสำหรับกรอบพัลส์ (ไม่เปลี่ยน) ----------
@Composable private fun StatItem(title: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontWeight = FontWeight.Bold, color = Color(0xFF1565C0))
        Text(title, color = Color.Gray, textAlign = TextAlign.Center)
    }
}

@Preview(showBackground = true)
@Composable
private fun TherapyPreview() {
    PhysioARTheme {
        TherapyScreen(navController = rememberNavController(), bodyPart = "ข้อเข่า")
    }
}
