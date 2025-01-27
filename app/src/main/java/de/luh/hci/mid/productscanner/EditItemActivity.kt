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
import de.luh.hci.mid.productscanner.ui.navigationbar.BottomNavigationBar
import de.luh.hci.mid.productscanner.ui.navigationbar.TopNavigationBar
import de.luh.hci.mid.productscanner.ui.theme.Red40
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.asRequestBody
import org.json.JSONObject
import java.io.File
import java.io.IOException

class EditItemActivity : ComponentActivity() {
    private var mediaRecorder: MediaRecorder? = null
    private var audioFile: File? = null
    private var isRecording = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val itemId = intent.getIntExtra("item_id", -1)
        val itemName = intent.getStringExtra("item_name") ?: ""

        setContent {
            EditScreen(
                initialName = itemName,
                onSaveClicked = { updatedName ->
                    val resultIntent = Intent().apply {
                        putExtra("item_id", itemId)
                        putExtra("updated_name", updatedName)
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
            return
        }

        audioFile = File(cacheDir, "speech_edit.mp3")
        mediaRecorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setOutputFile(audioFile?.absolutePath)
            prepare()
            start()
        }
        isRecording = true
        Toast.makeText(this, "Aufnahme gestartet", Toast.LENGTH_SHORT).show()
    }

    private fun stopRecording(onSuccess: (String) -> Unit) {
        mediaRecorder?.apply {
            stop()
            release()
        }
        isRecording = false
        mediaRecorder = null

        audioFile?.let { file ->
            sendAudioToWhisper(file, onSuccess)
        } ?: Toast.makeText(this, "Keine gültige Audiodatei gefunden", Toast.LENGTH_LONG).show()
    }

    private fun sendAudioToWhisper(audioFile: File, onSuccess: (String) -> Unit) {
        val apiKey = BuildConfig.OPENAI_API_KEY
        val client = OkHttpClient()
        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("file", audioFile.name, audioFile.asRequestBody("audio/mpeg".toMediaTypeOrNull()))
            .addFormDataPart("model", "whisper-1")
            .addFormDataPart("language", "de")
            .build()

        val request = Request.Builder()
            .url("https://api.openai.com/v1/audio/transcriptions")
            .post(requestBody)
            .addHeader("Authorization", "Bearer $apiKey")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread { Toast.makeText(this@EditItemActivity, "Fehler bei der Transkription", Toast.LENGTH_SHORT).show() }
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val jsonResponse = JSONObject(response.body?.string() ?: "")
                    val transcription = jsonResponse.getString("text")
                    runOnUiThread { onSuccess(transcription) }
                } else {
                    runOnUiThread { Toast.makeText(this@EditItemActivity, "Fehler: ${response.message}", Toast.LENGTH_SHORT).show() }
                }
            }
        })
    }

    @Composable
    fun EditScreen(
        initialName: String,
        onSaveClicked: (String) -> Unit,
        onCancelClicked: () -> Unit,
        modifier: Modifier = Modifier
    ) {
        var productName by remember { mutableStateOf(initialName) }

        Scaffold(
            topBar = { TopNavigationBar(title = "Edit Item") },
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
                }

                Spacer(modifier = Modifier.height(16.dp))

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
                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = { onSaveClicked(productName) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Red40)
                    ) {
                        Text("Speichern", color = Color.White)
                    }
                    Button(
                        onClick = onCancelClicked,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)
                    ) {
                        Text("Abbrechen", color = Color.White)
                    }
                }
            }
        }
    }
}
