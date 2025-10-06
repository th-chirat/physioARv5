package com.example.physioarv5.viewmodel
/// File: viewmodel/TherapyViewModel.kt
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class TherapyUiState(
    val currentReps: Int = 0,
    val totalReps: Int = 10,
    val progress: Int = 0,          // 0..100
    val formattedTime: String = "00:00",
    val feedbackMessage: String = "",
    val isPaused: Boolean = false
)

class TherapyViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(TherapyUiState())
    val uiState = _uiState.asStateFlow()

    private var started = false
    private var seconds = 0

    fun startTherapy() {
        if (started) return
        started = true

        viewModelScope.launch {
            while (true) {
                delay(1000)
                if (_uiState.value.isPaused) continue

                // อัพเดตเวลา
                seconds += 1
                val mm = seconds / 60
                val ss = seconds % 60
                val time = "%02d:%02d".format(mm, ss)

                // จำลองความคืบหน้า/นับครั้ง
                var progress = _uiState.value.progress + 10
                var reps = _uiState.value.currentReps
                var msg = ""

                if (progress >= 100) {
                    progress = 0
                    reps += 1
                    msg = if (reps % 2 == 0) "ยอดไปเลยค่ะ!" else "เก่งมากค่ะ!"
                } else {
                    msg = if (progress >= 60) "อีกนิดเดียวค่ะ" else "รักษาทรงให้ตรงค่ะ"
                }

                _uiState.value = _uiState.value.copy(
                    currentReps = reps,
                    progress = progress,
                    formattedTime = time,
                    feedbackMessage = msg
                )
            }
        }
    }

    fun togglePause() {
        _uiState.value = _uiState.value.copy(isPaused = !_uiState.value.isPaused)
    }
}

