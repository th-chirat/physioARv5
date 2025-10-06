package com.example.physioarv5.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.physioarv5.R
import com.example.physioarv5.navigation.Screen
import com.example.physioarv5.utils.LocaleManager
import kotlinx.coroutines.launch

private val cardGradient = Brush.verticalGradient(
    listOf(Color(0xFF89CFF0), Color(0xFF4F86C6))
)

@Composable
fun SelectionScreen(navController: NavController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val currentLang by LocaleManager.languageFlow(context).collectAsState(initial = "th")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(16.dp)
    ) {
        // หัวข้อ + สวิตช์ภาษา
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.selection_title),
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
            LanguageToggle(
                selected = currentLang,
                onChange = { tag ->
                    scope.launch {
                        // บันทึกภาษา + ใช้กับทั้งแอปทันที
                        LocaleManager.saveLanguage(context, tag)
                        LocaleManager.applyAppLocale(context, tag)
                    }
                }
            )
        }

        Spacer(Modifier.height(16.dp))

        val bodyParts = listOf(
            "knee"     to stringResource(R.string.part_knee),
            "shoulder" to stringResource(R.string.part_shoulder),
            "hand"     to stringResource(R.string.part_hand),
            "back"     to stringResource(R.string.part_back),
            "ankle"    to stringResource(R.string.part_ankle),
            "hip"      to stringResource(R.string.part_hip),
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(bodyParts) { (key, label) ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clickable { navController.navigate(Screen.Ar.createRoute(key)) },
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(cardGradient, RoundedCornerShape(18.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(label, color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
private fun LanguageToggle(
    selected: String,              // "th" | "en"
    onChange: (String) -> Unit
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        SegButton(text = "ไทย", selected = selected == "th") { onChange("th") }
        Spacer(Modifier.width(8.dp))
        SegButton(text = "EN",  selected = selected == "en") { onChange("en") }
    }
}

@Composable
private fun SegButton(text: String, selected: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (selected) Color(0xFF4F86C6) else Color(0xFFE0E7EF),
            contentColor   = if (selected) Color.White       else Color(0xFF2F3B52)
        ),
        shape = RoundedCornerShape(100),
        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
    ) { Text(text) }
}
