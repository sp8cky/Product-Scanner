package de.luh.hci.mid.productscanner

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Clear
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.asRequestBody
import org.json.JSONObject
import java.io.File
import java.io.IOException
import de.luh.hci.mid.productscanner.ui.navigationbar.BottomNavigationBar
import de.luh.hci.mid.productscanner.ui.navigationbar.TopNavigationBar
import de.luh.hci.mid.productscanner.ui.theme.Red40

class AddItemActivitiy : ComponentActivity() {
    private var mediaRecorder: MediaRecorder? = null
    private var audioFile: File? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            AddScreen(
                onAddClicked = { productName ->
                    val resultIntent = Intent().apply {
                        putExtra("item_name", productName)
                    }
                    setResult(RESULT_OK, resultIntent)
                    finish()
                },
                onCancelClicked = {
                    setResult(RESULT_CANCELED)
                    finish()
                }
            )
        }
    }
    private fun startRecording() {
        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.RECORD_AUDIO), 200)
            android.util.Log.e("AddItemActivity", "Mikrofon-Berechtigung nicht erteilt.")
            return
        }

        // Sicherstellen, dass die Datei korrekt erstellt wird
        audioFile = File(cacheDir, "speech.mp3")
        android.util.Log.i("AddItemActivity", "Audio-Dateipfad: ${audioFile?.absolutePath}")

        try {
            mediaRecorder = MediaRecorder().apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setOutputFile(audioFile?.absolutePath)
                prepare()
                start()
            }
            Toast.makeText(this, "Aufnahme gestartet", Toast.LENGTH_SHORT).show()
            android.util.Log.i("AddItemActivity", "Aufnahme gestartet, Datei: ${audioFile?.absolutePath}")
        } catch (e: Exception) {
            android.util.Log.e("AddItemActivity", "Fehler beim Starten der Aufnahme: ${e.message}", e)
            Toast.makeText(this, "Fehler beim Starten der Aufnahme", Toast.LENGTH_LONG).show()
        }
    }

    private fun stopRecording(onSuccess: (String) -> Unit) {
        try {
            mediaRecorder?.apply {
                stop()
                release()
                android.util.Log.i("AddItemActivity", "Aufnahme erfolgreich gestoppt.")
            }
            mediaRecorder = null

            // Überprüfen, ob die Datei existiert und gültig ist
            if (audioFile?.exists() == true && audioFile?.length() ?: 0 > 0) {
                android.util.Log.i("AddItemActivity", "Audio-Datei gefunden: ${audioFile?.absolutePath}")
                sendAudioToWhisper(audioFile!!, onSuccess)
            } else {
                android.util.Log.e("AddItemActivity", "Fehler: Keine gültige Audiodatei gefunden.")
                Toast.makeText(this, "Fehler: Keine gültige Audiodatei gefunden.", Toast.LENGTH_LONG).show()
            }
        } catch (e: Exception) {
            android.util.Log.e("AddItemActivity", "Fehler beim Stoppen der Aufnahme: ${e.message}", e)
            Toast.makeText(this, "Fehler beim Stoppen der Aufnahme", Toast.LENGTH_LONG).show()
        }
    }



    private fun sendAudioToWhisper(audioFile: File, onSuccess: (String) -> Unit) {
        val apiKey = BuildConfig.OPENAI_API_KEY
        android.util.Log.i("WhisperAPI", "Sende Datei: ${audioFile.absolutePath}")
        android.util.Log.i("WhisperAPI", "Request wird gesendet: ${audioFile.name}")
        android.util.Log.i("WhisperAPI", "Request-Header: Authorization Bearer $apiKey")

        val client = OkHttpClient()
        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart(
                "file",
                audioFile.name,
                audioFile.asRequestBody("audio/mpeg".toMediaTypeOrNull())
            )
            .addFormDataPart("model", "whisper-1")
            .addFormDataPart("language", "de") // Sprache auf Deutsch setzen
            .build()

        val request = Request.Builder()
            .url("https://api.openai.com/v1/audio/transcriptions")
            .post(requestBody)
            .addHeader("Authorization", "Bearer $apiKey")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                android.util.Log.e("WhisperAPI", "Request fehlgeschlagen: ${e.message}", e)
            }

            override fun onResponse(call: Call, response: Response) {
                android.util.Log.i("WhisperAPI", "Response erhalten: Code ${response.code}")
                if (response.isSuccessful) {
                    val jsonResponse = JSONObject(response.body?.string() ?: "")
                    val transcription = jsonResponse.getString("text")
                    android.util.Log.i("WhisperAPI", "Transkription: $transcription")
                    runOnUiThread {
                        onSuccess(transcription)
                    }
                } else {
                    android.util.Log.e("WhisperAPI", "Fehler in der API-Antwort: ${response.body?.string()}")
                }
            }
        })
    }




    @Composable
    fun AddScreen(
        onAddClicked: (String) -> Unit,
        onCancelClicked: () -> Unit,
        modifier: Modifier = Modifier
    ) {
        var productName by remember { mutableStateOf("") }
        var isRecording by remember { mutableStateOf(false) }

        Scaffold(
            topBar = { TopNavigationBar(title = "Add Item") },
            bottomBar = { BottomNavigationBar(navController = null) }
        ) { paddingValues ->
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .padding(16.dp)
                            .border(1.dp, Color.Gray, shape = CircleShape)
                            .shadow(2.dp, shape = CircleShape)
                            .background(Color.White)
                    ) {
                        BasicTextField(
                            value = productName,
                            onValueChange = { productName = it },
                            textStyle = LocalTextStyle.current.copy(fontSize = 20.sp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            singleLine = true
                        )
                    }

                    Button(
                        onClick = { productName = "" },
                        modifier = Modifier
                            .height(40.dp)
                            .width(40.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Red40),
                        shape = RectangleShape,
                        elevation = ButtonDefaults.buttonElevation(0.dp),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Clear,
                            contentDescription = "Löschen",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Spacer(modifier = Modifier.weight(1f))

                FloatingActionButton(
                    onClick = {
                        if (isRecording) {
                            stopRecording { transcription ->
                                productName = transcription
                                isRecording = false // Status korrekt zurücksetzen
                                android.util.Log.i("AddItemActivity", "Aufnahme gestoppt und verarbeitet.")
                            }
                        } else {
                            startRecording()
                            isRecording = true // Status korrekt setzen
                            android.util.Log.i("AddItemActivity", "Aufnahme gestartet.")
                        }
                    },
                    modifier = Modifier
                        .size(200.dp)
                        .align(Alignment.CenterHorizontally),
                    containerColor = if (isRecording) Color.Gray else Red40,
                    shape = CircleShape
                ) {
                    Text(
                        text = if (isRecording) "⏹" else "🎤",
                        fontSize = 32.sp,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }


                Spacer(modifier = Modifier.weight(1f))

                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = { onAddClicked(productName) },
                        modifier = Modifier
                            .weight(1f)
                            .height(60.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Red40),
                        //shape = RectangleShape,
                        elevation = ButtonDefaults.buttonElevation(0.dp)
                    ) {
                        Text(
                            text = "Hinzufügen",
                            fontSize = 18.sp,
                            color = Color.White,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                    Button(
                        onClick = onCancelClicked,
                        modifier = Modifier
                            .weight(1f)
                            .height(60.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Gray),
                        //shape = RectangleShape,
                        elevation = ButtonDefaults.buttonElevation(0.dp)
                    ) {
                        Text(
                            text = "Abbrechen",
                            fontSize = 18.sp,
                            color = Color.White,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}
