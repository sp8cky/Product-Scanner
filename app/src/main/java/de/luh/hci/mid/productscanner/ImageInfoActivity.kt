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
                            val productData = fetchProductDetailsFromImage(it)
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
        // Implementiere die Logik für den API-Aufruf oder Rückgabewerte.
        // Dummy-Wert zurückgeben.
        //TODO
        return mapOf(
            "name" to "Testprodukt",
            "brand" to "Testmarke",
            "ingredients" to "Testzutaten",
            "filters" to mapOf("Vegan" to true, "Vegetarisch" to true)
        )
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
                            .data(File(productImageUrl).takeIf { File(productImageUrl).exists() } ?: productImageUrl)
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
