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
import androidx.navigation.NavController
import de.luh.hci.mid.productscanner.MainActivity
import de.luh.hci.mid.productscanner.currentRoute
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.File

@Composable
fun BottomNavigationBar(
    navController: NavController? = null,
    onHomeClicked: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val items = listOf(
        NavItem("home", "Home", "🏠", MaterialTheme.colorScheme.primary),
        NavItem("tts", "TTS", "🔊", MaterialTheme.colorScheme.primary)
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
                        scope.launch(Dispatchers.IO) {
                            val textToSpeak = when (currentRoute) {
                                "home" -> "Willkommen auf der Startseite. Bitte wählen Sie eine Aktion."
                                "scan" -> "Hier können Sie einen Barcode scannen."
                                "tts" -> "Text-to-Speech aktiviert."
                                else -> "Kein Text verfügbar."
                            }
                            fetchTTSFromOpenAI(textToSpeak)
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

suspend fun fetchTTSFromOpenAI(text: String) {
    val apiKey = "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\n".trim() // Sicherstellen, dass keine Leerzeichen vorhanden sind
    val url = "https://api.openai.com/v1/audio/speech"
    val client = OkHttpClient()

    // JSON-Objekt für die Anfrage erstellen
    val jsonRequest = JSONObject()
    jsonRequest.put("input", text)
    jsonRequest.put("voice", "alloy") // Beispiel-Stimme
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

                // Nutze MediaPlayer, um den Audio-Stream abzuspielen
                try {
                    val tempFile = File.createTempFile("tts_audio", ".mp3")
                    tempFile.outputStream().use { output ->
                        responseBody.copyTo(output)
                    }

                    val mediaPlayer = MediaPlayer().apply {
                        setDataSource(tempFile.absolutePath)
                        prepare()
                        start()
                    }

                    mediaPlayer.setOnCompletionListener {
                        mediaPlayer.release()
                        tempFile.delete() // Lösche die temporäre Datei nach Wiedergabe
                    }
                } catch (e: Exception) {
                    Log.e("OpenAI TTS", "Fehler beim Abspielen des Audios: ${e.message}", e)
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
