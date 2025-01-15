package de.luh.hci.mid.productscanner

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.luh.hci.mid.productscanner.ui.navigationbar.BottomNavigationBar
import de.luh.hci.mid.productscanner.ui.navigationbar.TopNavigationBar
import de.luh.hci.mid.productscanner.ui.theme.Blue40
import de.luh.hci.mid.productscanner.ui.theme.Red40

class SettingsActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            SettingsScreen(
                onHomeClicked = { finish() }, // Zurück zur vorherigen Aktivität
                onSpeakerClicked = { /* Action for Lautsprecher */ }
            )
        }
    }

    @Composable
    fun SettingsScreen(
        onHomeClicked: () -> Unit,
        onSpeakerClicked: () -> Unit,
        modifier: Modifier = Modifier
    ) {
        Scaffold(
            topBar = { TopNavigationBar(title = "Einstellungen") },
            bottomBar = { BottomNavigationBar(navController = null) }
        ) { paddingValues ->
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp), // Zusätzliche Polsterung
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                // Liste mit Einstellungen und Toggles
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    SettingItem(label = "Toggle 1")
                    SettingItem(label = "Dark Mode")
                    SettingItem(label = "Toggle 3")

                    // Voice Auswahl
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Voice",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.Black
                        )

                        val selectedOption = remember { mutableStateOf("Option 1") }
                        DropdownMenuButton(
                            options = listOf(
                                "Option 1",
                                "Option 2",
                                "Option 3",
                                "Option 4",
                                "Option 5"
                            ),
                            selectedOption = selectedOption.value,
                            onOptionSelected = { selectedOption.value = it }
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Volume Slider
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Volume",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.Black
                        )

                        val volumeLevel = remember { mutableStateOf(50f) }
                        Slider(
                            value = volumeLevel.value,
                            onValueChange = { volumeLevel.value = it },
                            valueRange = 0f..100f,
                            colors = SliderDefaults.colors(
                                thumbColor = Blue40,
                                activeTrackColor = Blue40
                            )
                        )
                    }
                }

            }
        }
    }

    @Composable
    fun SettingItem(label: String) {
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Black
            )

            val isChecked = remember { mutableStateOf(false) }

            Switch(
                checked = isChecked.value,
                onCheckedChange = { isChecked.value = it },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Blue40,
                    uncheckedThumbColor = Color.Gray
                )
            )
        }
    }

    @Composable
    fun DropdownMenuButton(
        options: List<String>,
        selectedOption: String,
        onOptionSelected: (String) -> Unit
    ) {
        val expanded = remember { mutableStateOf(false) }

        Box {
            Button(
                onClick = { expanded.value = true },
                colors = ButtonDefaults.buttonColors(containerColor = Color.LightGray),
                shape = RectangleShape
            ) {
                Text(text = selectedOption, fontSize = 16.sp)
            }
            DropdownMenu(
                expanded = expanded.value,
                onDismissRequest = { expanded.value = false }
            ) {
                options.forEach { option ->
                    DropdownMenuItem(
                        onClick = {
                            onOptionSelected(option)
                            expanded.value = false
                        },
                        text = { Text(option, fontSize = 16.sp) }
                    )
                }
            }
        }
    }
}
