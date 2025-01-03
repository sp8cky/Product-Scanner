package de.luh.hci.mid.productscanner

import android.os.Bundle
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
import de.luh.hci.mid.productscanner.ui.navigationbar.BottomNavigationBar
import de.luh.hci.mid.productscanner.ui.navigationbar.TopNavigationBar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.io.File
import java.net.URL

class InfoActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val barcodeValue = intent.getStringExtra("BARCODE_VALUE")
        val imagePath = intent.getStringExtra("IMAGE_PATH") // Vom Kamerabutton übergeben

        setContent {
            var productName by remember { mutableStateOf("Lade...") }
            var brand by remember { mutableStateOf("Lade...") }
            var ingredients by remember { mutableStateOf("Lade...") }
            var productImageUrl by remember { mutableStateOf("") }
            var filters by remember { mutableStateOf<Map<String, Boolean>>(emptyMap()) }
            var isExpanded by remember { mutableStateOf(false) } // Einheitliche Expansion-Logik

            val scope = rememberCoroutineScope()

            // API-Anfrage nur ausführen, wenn ein Barcode verfügbar ist
            LaunchedEffect(barcodeValue) {
                barcodeValue?.let {
                    scope.launch(Dispatchers.IO) {
                        try {
                            val productData = fetchProductData(it)
                            productName = productData["name"] as? String ?: "Unbekannt"
                            brand = productData["brand"] as? String ?: "Unbekannt"
                            ingredients = productData["ingredients"] as? String ?: "Keine Angaben"
                            productImageUrl =
                                productData["image_url"] as? String ?: ""
                            filters = productData["filters"] as? Map<String, Boolean>
                                ?: emptyMap()
                        } catch (e: Exception) {
                            productName = "Fehler beim Laden"
                            brand = "Fehler"
                            ingredients = "Fehler"
                            productImageUrl = ""
                            filters = emptyMap()
                            e.printStackTrace()
                        }
                    }
                }
            }

            Scaffold(
                topBar = { TopNavigationBar(title = "Info") },
                bottomBar = { BottomNavigationBar(navController = null) }
            ) { padding ->
                InfoScreen(
                    barcode = barcodeValue,
                    imagePath = imagePath,
                    productName = productName,
                    brand = brand,
                    ingredients = ingredients,
                    productImageUrl = productImageUrl,
                    filters = filters,
                    isExpanded = isExpanded,
                    onExpandToggle = { isExpanded = !isExpanded },
                    modifier = Modifier.padding(padding)
                )
            }
        }
    }

    private fun fetchProductData(barcode: String): Map<String, Any> {
        return try {
            val url = "https://world.openfoodfacts.org/api/v0/product/$barcode.json"
            val response = URL(url).readText()
            val json = JSONObject(response)

            if (!json.has("product") || json.optJSONObject("product") == null) {
                return emptyMap()
            }

            val product = json.getJSONObject("product")
            val filters = mutableMapOf<String, Boolean>()

            product.optJSONArray("ingredients_analysis_tags")?.let { tags ->
                filters["Vegetarisch"] = tags.toString().contains("en:vegetarian")
                filters["Vegan"] = tags.toString().contains("en:vegan")
            }
            product.optJSONArray("allergens_tags")?.let { tags ->
                filters["Nussfrei"] = !tags.toString().contains("en:nuts")
                filters["Laktosefrei"] = !tags.toString().contains("en:milk")
            }

            mapOf(
                "name" to product.optString("product_name", "Unbekannt"),
                "brand" to product.optString("brands", "Unbekannt"),
                "ingredients" to product.optString("ingredients_text_de", "Keine Angaben"),
                "image_url" to product.optString("image_url", ""),
                "filters" to filters
            )
        } catch (e: Exception) {
            e.printStackTrace()
            emptyMap()
        }
    }

    @Composable
    fun InfoScreen(
        barcode: String?,
        imagePath: String?,
        productName: String,
        brand: String,
        ingredients: String,
        productImageUrl: String,
        filters: Map<String, Boolean>,
        isExpanded: Boolean,
        onExpandToggle: () -> Unit,
        modifier: Modifier = Modifier
    ) {
        val isLoading = productName == "Lade..." || brand == "Lade..." || ingredients == "Lade..."

        if (isLoading && imagePath == null) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Zeige Bild, wenn ein Bildpfad übergeben wurde
                imagePath?.let {
                    item {
                        Image(
                            painter = rememberAsyncImagePainter(File(it)),
                            contentDescription = "Aufgenommenes Bild",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                        )
                    }
                }

                // Produktinformationen anzeigen
                barcode?.let {
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Image(
                                painter = rememberAsyncImagePainter(
                                    ImageRequest.Builder(LocalContext.current)
                                        .data(data = productImageUrl.ifEmpty { "https://via.placeholder.com/150" })
                                        .apply { crossfade(true) }.build()
                                ),
                                contentDescription = "Produktbild",
                                modifier = Modifier
                                    .size(100.dp)
                                    .padding(end = 16.dp)
                            )

                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = "Name: $productName",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Marke: $brand",
                                    fontSize = 18.sp
                                )
                                Text(
                                    text = "Barcode: $barcode",
                                    fontSize = 16.sp
                                )
                            }
                        }
                    }
                }

                // Aufklappbare Zutatenliste
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onExpandToggle() }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isExpanded) "Zutaten: $ingredients"
                                else "Zutaten: ${ingredients.take(50)}" +
                                        if (!isExpanded && ingredients.length > 50) "..." else "",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Normal,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Icon(
                            imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = if (isExpanded) "Einklappen" else "Aufklappen",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                // Dynamische Filteranzeige
                filters.forEach { (label, isActive) ->
                    item {
                        FilterItem(label = label, isActive = isActive)
                    }
                }
            }
        }
    }

    @Composable
    fun FilterItem(label: String, isActive: Boolean) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium
            )
            Checkbox(
                checked = isActive,
                onCheckedChange = null,
                enabled = false
            )
        }
    }
}
