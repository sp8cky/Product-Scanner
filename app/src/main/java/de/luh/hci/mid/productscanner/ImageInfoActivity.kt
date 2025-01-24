package de.luh.hci.mid.productscanner

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

class ImageInfoActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val imagePath = intent.getStringExtra("IMAGE_PATH")

        setContent {
            var productName by remember { mutableStateOf("Lade...") }
            var brand by remember { mutableStateOf("Lade...") }
            var ingredients by remember { mutableStateOf("Lade...") }
            var productImageUrl by remember { mutableStateOf(imagePath ?: "") }
            var filters by remember { mutableStateOf<Map<String, Boolean>>(emptyMap()) }
            var isLoading by remember { mutableStateOf(true) }
            var isExpanded by remember { mutableStateOf(false) }
            var errorMessage by remember { mutableStateOf<String?>(null) }
            val scope = rememberCoroutineScope()

            LaunchedEffect(imagePath) {
                imagePath?.let {
                    scope.launch(Dispatchers.IO) {
                        try {
                            Log.d("ImageInfoActivity", "Starting fetchProductDetailsFromImage")
                            val productData = fetchProductDetailsFromImage(it)
                            Log.d("ImageInfoActivity", "Product data fetched: $productData")
                            productName = productData["name"] as? String ?: "Unbekannt"
                            brand = productData["brand"] as? String ?: "Unbekannt"
                            ingredients = productData["ingredients"] as? String ?: "Keine Angaben"
                            filters = productData["filters"] as? Map<String, Boolean> ?: emptyMap()
                        } catch (e: Exception) {
                            Log.e("ImageInfoActivity", "Error fetching product data", e)
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
                productName = productName,
                brand = brand,
                ingredients = ingredients,
                productImageUrl = productImageUrl,
                filters = filters,
                isLoading = isLoading,
                errorMessage = errorMessage,
                isExpanded = isExpanded,
                onExpandToggle = { isExpanded = !isExpanded }
            )
        }
    }

    private suspend fun fetchProductDetailsFromImage(imagePath: String): Map<String, Any> {
        val apiKey = BuildConfig.OPENAI_API_KEY // Lade den API-Schlüssel sicher aus BuildConfig
        val url = "https://api.openai.com/v1/chat/completions"
        val client = OkHttpClient()

        try {
            val file = File(imagePath)

            // Überprüfen, ob die Datei existiert
            if (!file.exists()) {
                Log.e("ImageInfoActivity", "Image file does not exist: $imagePath")
                return mapOf("error" to "Fehler: Bild nicht gefunden")
            }

            Log.d("ImageInfoActivity", "Encoding image to Base64")
            val base64Image = file.inputStream().use {
                android.util.Base64.encodeToString(it.readBytes(), android.util.Base64.DEFAULT)
            }

            Log.d("ImageInfoActivity", "Creating JSON request")
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
                                            put("type", "text");
                                            put("text", "Gebe mir das Produkt (Nahrungsmittel) mit Name, Marke und Zutaten an. Ignoriere Dinge im Hintergrund. Falls Dinge nicht angegeben sind, versuche sie zu vervollständigen durch Recherche.") })
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

            Log.d("ImageInfoActivity", "JSON request created: $jsonRequest")

            val requestBody = jsonRequest.toString().toRequestBody("application/json".toMediaType())

            val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .addHeader("Authorization", "Bearer $apiKey")
                .addHeader("Content-Type", "application/json")
                .build()

            Log.d("ImageInfoActivity", "Sending request to OpenAI API")
            val response = client.newCall(request).execute()

            Log.d("ImageInfoActivity", "Response received: Code ${response.code}")

            if (response.isSuccessful) {
                val responseBody = response.body?.string()
                if (!responseBody.isNullOrEmpty()) {
                    Log.d("ImageInfoActivity", "Response body: $responseBody")
                    val jsonResponse = JSONObject(responseBody)

                    val productName = jsonResponse.optString("name", "Unbekannt")
                    val brand = jsonResponse.optString("brand", "Unbekannt")
                    val ingredients = jsonResponse.optString("ingredients", "Keine Angaben")
                    val filters = mutableMapOf<String, Boolean>()

                    jsonResponse.optJSONObject("filters")?.let { filterObj ->
                        filters["Vegetarisch"] = filterObj.optBoolean("vegetarian", false)
                        filters["Vegan"] = filterObj.optBoolean("vegan", false)
                        filters["Nussfrei"] = filterObj.optBoolean("nut_free", false)
                        filters["Laktosefrei"] = filterObj.optBoolean("lactose_free", false)
                    }

                    return mapOf(
                        "name" to productName,
                        "brand" to brand,
                        "ingredients" to ingredients,
                        "filters" to filters
                    )
                } else {
                    Log.e("ImageInfoActivity", "Empty response body")
                    return mapOf("error" to "Leere Antwort vom Server erhalten.")
                }
            } else {
                Log.e("ImageInfoActivity", "Error response: ${response.code} - ${response.message}")
                return mapOf("error" to "Fehler: ${response.code} - ${response.message}")
            }
        } catch (e: Exception) {
            Log.e("ImageInfoActivity", "Exception during API call: ${e.message}", e)
            return mapOf("error" to "Fehler beim Senden des Bildes: ${e.message}")
        }
    }
}

@Composable
fun ImageProductDetailsScreen(
    productName: String,
    brand: String,
    ingredients: String,
    productImageUrl: String,
    filters: Map<String, Boolean>,
    isLoading: Boolean,
    errorMessage: String?,
    isExpanded: Boolean,
    onExpandToggle: () -> Unit
) {
    if (isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
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
                    text = "Name: $productName",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Marke: $brand",
                    fontSize = 18.sp
                )
            }

            // Zutaten
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onExpandToggle() }
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = if (isExpanded) "Zutaten: $ingredients"
                        else "Zutaten: ${ingredients.take(50)}" +
                                if (!isExpanded && ingredients.length > 50) "..." else "",
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = if (isExpanded) "Einklappen" else "Aufklappen"
                    )
                }
            }

            // Filter
            filters.forEach { (label, isActive) ->
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = label,
                            fontSize = 18.sp
                        )
                        Checkbox(
                            checked = isActive,
                            onCheckedChange = null,
                            enabled = false
                        )
                    }
                }
            }
        }
    }
}