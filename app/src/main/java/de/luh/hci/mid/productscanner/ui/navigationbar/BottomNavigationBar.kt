package de.luh.hci.mid.productscanner.ui.navigationbar

import android.content.Intent
import android.media.MediaPlayer
import android.util.Log
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import de.luh.hci.mid.productscanner.MainActivity
import de.luh.hci.mid.productscanner.SettingsViewModel
import de.luh.hci.mid.productscanner.currentRoute
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.File
import de.luh.hci.mid.productscanner.BuildConfig

private var mediaPlayer: MediaPlayer? = null // Globaler MediaPlayer
@Composable
fun BottomNavigationBar(
    navController: NavController? = null,
    onHomeClicked: (() -> Unit)? = null,
    settingsViewModel: SettingsViewModel = viewModel(),
    ttsContentProvider: TTSContentProvider // Neuer Parameter für TTS-Daten
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val selectedVoice by settingsViewModel.selectedVoice.collectAsState()
    val volumeLevel by settingsViewModel.volumeLevel.collectAsState()

    val items = listOf(
        NavItem("home", "Home", "\uD83C\uDFE0", MaterialTheme.colorScheme.primary),
        NavItem("tts", "TTS", "\uD83D\uDD0A", MaterialTheme.colorScheme.primary)
    )

    NavigationBar(
        containerColor = MaterialTheme.colorScheme.background,
        tonalElevation = 8.dp
    ) {
        val currentRoute = navController?.let { currentRoute(it) } ?: "home"
        items.forEach { item ->
            NavigationBarItem(
                selected = currentRoute == item.route,
                onClick = {
                    if (item.route == "home") {
                        // Navigation zur Home-Activity
                        if (navController == null) {
                            onHomeClicked?.invoke() ?: run {
                                val intent = Intent(context, MainActivity::class.java)
                                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                                context.startActivity(intent)
                            }
                        } else {
                            navController.navigate("home") {
                                popUpTo(navController.graph.startDestinationId) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    } else if (item.route == "tts") {
                        // Dynamischer TTS-Text von der aktuellen Activity
                        val ttsContent = ttsContentProvider.getTTSContent()
                        scope.launch(Dispatchers.IO) {
                            fetchTTSFromOpenAI(
                                text = ttsContent,
                                voice = selectedVoice,
                                volume = volumeLevel / 100f
                            )
                        }
                    }
                },
                icon = {
                    Text(
                        text = item.icon,
                        fontSize = 36.sp,
                        color = if (currentRoute == item.route) item.color else MaterialTheme.colorScheme.onBackground
                    )
                },
                label = {
                    Text(
                        text = item.label,
                        fontSize = 14.sp,
                        color = if (currentRoute == item.route) item.color else MaterialTheme.colorScheme.onBackground
                    )
                },
                alwaysShowLabel = true
            )
        }
    }
}


suspend fun fetchTTSFromOpenAI(text: String, voice: String, volume: Float) {
    val apiKey = BuildConfig.OPENAI_API_KEY // Lade den API-Key aus den BuildConfig-Variablen
    val url = "https://api.openai.com/v1/audio/speech"
    val client = OkHttpClient()

    // JSON-Objekt für die Anfrage erstellen
    val jsonRequest = JSONObject()
    jsonRequest.put("input", text)
    jsonRequest.put("voice", voice.lowercase()) // Dynamisch ausgewählte Stimme
    jsonRequest.put("model", "tts-1")

    val requestBody = jsonRequest.toString().toRequestBody("application/json".toMediaType())

    val request = Request.Builder()
        .url(url)
        .post(requestBody)
        .addHeader("Authorization", "Bearer $apiKey")
        .addHeader("Content-Type", "application/json")
        .build()

    try {
        val response = client.newCall(request).execute()
        if (response.isSuccessful) {
            val responseBody = response.body?.byteStream()
            if (responseBody != null) {
                Log.d("OpenAI TTS", "Audio erfolgreich generiert")

                // Falls ein MediaPlayer bereits aktiv ist, stoppen und freigeben
                mediaPlayer?.apply {
                    if (isPlaying) {
                        Log.d("MediaPlayer", "Vorherige TTS-Ausgabe wird gestoppt.")
                        stop()
                    }
                    reset()
                    release()
                }

                mediaPlayer = MediaPlayer().apply {
                    val tempFile = File.createTempFile("tts_audio", ".mp3")
                    tempFile.outputStream().use { output ->
                        responseBody.copyTo(output)
                    }

                    setDataSource(tempFile.absolutePath)
                    prepare()
                    setVolume(volume, volume) // Lautstärke einstellen
                    start()

                    setOnCompletionListener {
                        Log.d("MediaPlayer", "TTS-Wiedergabe abgeschlossen.")
                        release()
                        mediaPlayer = null // MediaPlayer zurücksetzen
                        tempFile.delete() // Temporäre Datei löschen
                    }

                    setOnErrorListener { _, what, extra ->
                        Log.e("MediaPlayer", "Fehler während der Wiedergabe: what=$what, extra=$extra")
                        release()
                        mediaPlayer = null
                        tempFile.delete()
                        true
                    }
                }
            } else {
                Log.e("OpenAI TTS", "Leere Antwort erhalten")
            }
        } else {
            Log.e("OpenAI TTS", "Fehler: ${response.code} - ${response.message}")
        }
    } catch (e: Exception) {
        Log.e("OpenAI TTS", "Fehler beim Abrufen der TTS-Daten: ${e.message}", e)
    }
}

