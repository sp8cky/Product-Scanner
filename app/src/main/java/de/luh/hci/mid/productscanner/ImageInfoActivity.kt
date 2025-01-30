package de.luh.hci.mid.productscanner

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.File
import de.luh.hci.mid.productscanner.ui.navigationbar.BottomNavigationBar
import de.luh.hci.mid.productscanner.ui.navigationbar.TTSContentProvider
import de.luh.hci.mid.productscanner.ui.navigationbar.TopNavigationBar

class ImageInfoActivity : ComponentActivity() , TTSContentProvider{
    private var productName: String = "Lade..."

    override fun getTTSContent(): String {
        return productName
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val imagePath = intent.getStringExtra("IMAGE_PATH")
        setContent {
            var productNameState by remember { mutableStateOf("Lade...") }
            val productImageUrl by remember { mutableStateOf(imagePath ?: "") }
            var isLoading by remember { mutableStateOf(true) }
            var errorMessage by remember { mutableStateOf<String?>(null) }
            val scope = rememberCoroutineScope()

            LaunchedEffect(imagePath) {
                imagePath?.let {
                    scope.launch(Dispatchers.IO) {
                        try {
                            Log.d("ImageInfoActivity1", "Starting fetchProductDetailsFromImage")
                            val productData = fetchProductDetailsFromImage(it)
                            Log.d("ImageInfoActivity1", "Product data fetched: $productData")
                            productNameState = productData["response"] as? String ?: "Unbekannt"
                            productName = productNameState // Set the instance variable
                        } catch (e: Exception) {
                            Log.e("ImageInfoActivity1", "Error fetching product data", e)
                            errorMessage = "Fehler beim Abrufen der Daten"
                        } finally {
                            isLoading = false
                        }
                    }
                } ?: run {
                    errorMessage = "Kein Bildpfad angegeben"
                    isLoading = false
                }
            }

            ImageProductDetailsScreen(
                productNameState = productName,
                productImageUrl = productImageUrl,
                isLoading = isLoading,
                errorMessage = errorMessage
            )
        }
    }

    private fun fetchProductDetailsFromImage(imagePath: String): Map<String, Any> {
        val apiKey = BuildConfig.OPENAI_API_KEY // Lade den API-Schlüssel sicher aus BuildConfig
        val url = "https://api.openai.com/v1/chat/completions"
        val client = OkHttpClient.Builder()
            .connectTimeout(60, java.util.concurrent.TimeUnit.SECONDS)  // Verbindungstimeout
            .readTimeout(60, java.util.concurrent.TimeUnit.SECONDS)     // Lese-Timeout
            .writeTimeout(60, java.util.concurrent.TimeUnit.SECONDS)    // Schreib-Timeout
            .build()

        try {
            val file = File(imagePath)

            // Überprüfen, ob die Datei existiert
            if (!file.exists()) {
                Log.e("ImageInfoActivity1", "Image file does not exist: $imagePath")
                return mapOf("error" to "ImageInfoActivity1: Bild nicht gefunden")
            }

            Log.d("ImageInfoActivity1", "Encoding image to Base64")
            val base64Image = file.inputStream().use {
                android.util.Base64.encodeToString(it.readBytes(), android.util.Base64.DEFAULT)
            }

            Log.d("ImageInfoActivity1", "Creating JSON request")
            val jsonRequest = JSONObject().apply {
                put("model", "gpt-4o-mini")
                put("messages", JSONArray().apply {
                    put(
                        JSONObject().apply {
                            put("role", "user")
                            put(
                                "content",
                                JSONArray().apply {
                                    put(
                                        JSONObject().apply {
                                            put("type", "text")
                                            put("text", "Zeige mir das Produkt mit Name, Marke und Zutaten an. Ignoriere Dinge im Hintergrund. Falls Dinge nicht angegeben sind, versuche sie zu vervollständigen durch Recherche.") })
                                    put(
                                        JSONObject().apply {
                                            put("type", "image_url")
                                            put("image_url", JSONObject().apply {
                                                put("url", "data:image/jpeg;base64,$base64Image")
                                            })
                                        }
                                    )
                                }
                            )
                        }
                    )
                })
            }

            Log.d("ImageInfoActivity1", "JSON request created: $jsonRequest")

            val requestBody = jsonRequest.toString().toRequestBody("application/json".toMediaType())

            val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .addHeader("Authorization", "Bearer $apiKey")
                .addHeader("Content-Type", "application/json")
                .build()

            Log.d("ImageInfoActivity1", "Sending request to OpenAI API")
            val response = client.newCall(request).execute()

            Log.d("ImageInfoActivity1", "Response received: Code ${response.code}")

            if (response.isSuccessful) {
                val responseBody = response.body?.string()
                if (!responseBody.isNullOrEmpty()) {
                    try {
                        // JSON in JSONObject umwandeln
                        val jsonObject = JSONObject(responseBody)

                        // Die "choices" Liste extrahieren
                        val choices = jsonObject.getJSONArray("choices")

                        // Die erste Wahl (choice) extrahieren
                        val firstChoice = choices.getJSONObject(0)

                        // Den Inhalt der Nachricht extrahieren
                        val messageString = firstChoice.getJSONObject("message").getString("content")

                        // Loggen oder weiterverwenden
                        Log.d("ImageInfoActivity1", "Message content: $messageString")

                        return mapOf("response" to messageString)
                    } catch (e: JSONException) {
                        Log.e("ImageInfoActivity1", "Fehler beim Parsen des JSON: ${e.message}")
                        return mapOf("error" to "Fehler beim Verarbeiten der Antwort.")
                    }
                } else {
                    Log.e("ImageInfoActivity1", "Leere Antwort vom Server erhalten.")
                    return mapOf("error" to "Leere Antwort vom Server erhalten.")
                }

        } else {
                Log.e("ImageInfoActivity1", "Error response: ${response.code} - ${response.message}")
                return mapOf("error" to "Fehler: ${response.code} - ${response.message}")
            }
        } catch (e: Exception) {
            Log.e("ImageInfoActivity1", "Exception during API call: ${e.message}", e)
            return mapOf("error" to "Fehler beim Senden des Bildes: ${e.message}")
        }
    }
}


@Composable
fun ImageProductDetailsScreen(
    productNameState: String,
    productImageUrl: String,
    isLoading: Boolean,
    errorMessage: String?,
) {
    Scaffold(
        topBar = { TopNavigationBar(title = "Produktdetails") },
        bottomBar = { BottomNavigationBar(navController = null, ttsContentProvider = LocalContext.current as TTSContentProvider) }
    ){paddingValues ->
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(16.dp)) // Abstand zwischen Kreis und Text
                    Text(
                        text = "Bitte Warten\nDieser Prozess kann ein wenig dauern",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else if (!errorMessage.isNullOrEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Produktbild
                item {
                    Image(
                        painter = rememberAsyncImagePainter(
                            ImageRequest.Builder(LocalContext.current)
                                .data(File(productImageUrl).takeIf { File(productImageUrl).exists() }
                                    ?: productImageUrl)
                                .apply { crossfade(true) }
                                .build()
                        ),
                        contentDescription = "Produktbild",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                    )
                }

                // Produktdetails
                item {
                    Text(
                        text = productNameState,
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}
