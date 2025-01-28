package de.luh.hci.mid.productscanner

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.luh.hci.mid.productscanner.ui.navigationbar.BottomNavigationBar
import de.luh.hci.mid.productscanner.ui.navigationbar.TTSContentProvider
import de.luh.hci.mid.productscanner.ui.navigationbar.TopNavigationBar
import de.luh.hci.mid.productscanner.ui.theme.Blue40

class SettingsActivity : ComponentActivity(), TTSContentProvider {
    private val settingsViewModel: SettingsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SettingsScreen(viewModel = settingsViewModel)
        }
    }

    override fun getTTSContent(): String {
        return "Du befindest dich in den Einstellungen. Du kannst die TTS-Sprache und Lautstärke ändern und die Datenschutzrichtlinien bzw. dir Infos über die App ansehen."
    }
}

@Composable
fun SettingsScreen(viewModel: SettingsViewModel) {
    var showPrivacyDialog by remember { mutableStateOf(false) }
    var showInfoDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = { TopNavigationBar(title = "Einstellungen") },
        bottomBar = { BottomNavigationBar(navController = null, ttsContentProvider = LocalContext.current as TTSContentProvider) }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                VoiceSetting(viewModel)
            }

            item {
                VolumeSetting(viewModel)
            }

            item {
                Button(
                    onClick = { showPrivacyDialog = true
                        SoundManager.playSound("tap")},
                    colors = ButtonDefaults.buttonColors(containerColor = Blue40),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Datenschutzrichtlinie",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }

            item {
                Button(
                    onClick = { showInfoDialog = true
                        SoundManager.playSound("tap")},
                    colors = ButtonDefaults.buttonColors(containerColor = Blue40),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Info über die App",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            if (showPrivacyDialog) {
                item {
                    AlertDialog(
                        onDismissRequest = { showPrivacyDialog = false },
                        confirmButton = {
                            TextButton(onClick = { showPrivacyDialog = false
                                SoundManager.playSound("tap")}) {
                                Text("OK", fontWeight = FontWeight.Bold)
                            }
                        },
                        title = { Text("Datenschutzrichtlinie") },
                        text = {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(max = 300.dp) // Setzt eine maximale Höhe für den scrollbaren Bereich
                            ) {
                                LazyColumn(
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    item {
                                        Text(
                                            "Wir nehmen den Schutz Ihrer Daten ernst. Hier erfahren Sie, wie Ihre Daten in der App verarbeitet werden:\n",
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    }
                                    item {
                                        Text(
                                            "- Barcode-Scanning: Barcodes werden an die Datenbank Open Food Facts gesendet, um Produktinformationen abzurufen. Es werden keine Daten dauerhaft gespeichert.\n",
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    }
                                    item {
                                        Text(
                                            "- Bildersuche: Aufgenommene Bilder werden an OpenAI gesendet, um passende Produkte zu finden. Die Bilder werden nach der Verarbeitung gelöscht.\n",
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    }
                                    item {
                                        Text(
                                            "- Sprachausgabe (TTS): Produktinformationen werden an OpenAI (ChatGPT) gesendet, um Sprache zu generieren. Diese Daten werden nur temporär verarbeitet.\n",
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    }
                                    item {
                                        Text(
                                            "Ihre Daten werden nur für die Bereitstellung der Funktionen genutzt und niemals dauerhaft gespeichert oder ohne Grund an Dritte weitergegeben.",
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    }
                                }
                            }
                        }
                    )
                }
            }

            if (showInfoDialog) {
                item {
                    AlertDialog(
                        onDismissRequest = { showInfoDialog = false },
                        confirmButton = {
                            TextButton(onClick = { showInfoDialog = false
                                SoundManager.playSound("tap")}) {
                                Text("OK", fontWeight = FontWeight.Bold)
                            }
                        },
                        title = { Text("Info über die App") },
                        text = {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(max = 300.dp) // Setzt eine maximale Höhe für den scrollbaren Bereich
                            ) {
                                LazyColumn(
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    item {
                                        Text(
                                            "Produkt Scanner v1.0\n\n",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    item {
                                        Text(
                                            "Entwickler: Emin Bayhan, Julia Hermerding\n",
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    }
                                    item {
                                        Text(
                                            "Funktionen:\n",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    item {
                                        Text(
                                            "- Barcode-Scanner: Echtzeit-Produktsuche mit Inhaltsstoffanalyse.\n" +
                                                    "- Sprachausgabe (TTS): Barrierefreier Zugang zu Produktinformationen.\n" +
                                                    "- Filter: Sortierung nach vegan, vegetarisch, allergenfrei.\n" +
                                                    "- Einkaufsliste: Erstellen von einer Einkaufsliste.",
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    }
                                }
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun VoiceSetting(viewModel: SettingsViewModel) {
    val selectedVoice by viewModel.selectedVoice.collectAsState()

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "Vorlese-Stimme",
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
            color = Color.Black
        )

        DropdownMenuButton(
            options = listOf("Alloy", "Ash", "Coral", "Echo", "Fable", "Onyx", "Nova", "Sage", "Shimmer"),
            selectedOption = selectedVoice,
            onOptionSelected = { viewModel.updateSelectedVoice(it) }
        )
    }
}

@Composable
fun VolumeSetting(viewModel: SettingsViewModel) {
    val volumeLevel by viewModel.volumeLevel.collectAsState()

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Lautstärke",
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
            color = Color.Black
        )

        Slider(
            value = volumeLevel,
            onValueChange = { viewModel.updateVolumeLevel(it) },
            valueRange = 0f..100f,
            colors = SliderDefaults.colors(
                thumbColor = Blue40,
                activeTrackColor = Blue40
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
            onClick = { expanded.value = true
                SoundManager.playSound("tap")},
            colors = ButtonDefaults.buttonColors(containerColor = Blue40),
            shape = RectangleShape
        ) {
            Text(text = selectedOption, fontSize = 16.sp, color = Color.White)
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
                        SoundManager.playSound("tap")
                    },
                    text = { Text(option, fontSize = 16.sp, color = Blue40) }
                )
            }
        }
    }
}
