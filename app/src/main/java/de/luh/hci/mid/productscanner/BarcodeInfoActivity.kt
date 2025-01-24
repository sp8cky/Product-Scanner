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
import de.luh.hci.mid.productscanner.ui.navigationbar.BottomNavigationBar
import de.luh.hci.mid.productscanner.ui.navigationbar.TopNavigationBar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.net.URL

class BarcodeInfoActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val barcodeValue = intent.getStringExtra("BARCODE_VALUE")

        setContent {
            var productName by remember { mutableStateOf("Lade...") }
            var brand by remember { mutableStateOf("Lade...") }
            var ingredients by remember { mutableStateOf("Lade...") }
            var productImageUrl by remember { mutableStateOf("") }
            var filters by remember { mutableStateOf<Map<String, Boolean>>(emptyMap()) }
            var isExpanded by remember { mutableStateOf(false) }
            var isLoading by remember { mutableStateOf(true) }
            val scope = rememberCoroutineScope()

            // Fetch product data
            LaunchedEffect(barcodeValue) {
                barcodeValue?.let {
                    scope.launch(Dispatchers.IO) {
                        try {
                            val productData = fetchProductData(it)
                            productName = productData["name"] as? String ?: "Unbekannt"
                            brand = productData["brand"] as? String ?: "Unbekannt"
                            ingredients = productData["ingredients"] as? String ?: "Keine Angaben"
                            productImageUrl = productData["image_url"] as? String ?: ""
                            filters = productData["filters"] as? Map<String, Boolean> ?: emptyMap()

                            // Produkt zur Scan-History hinzufügen
                            val newProduct = ScannedProduct(
                                id = it,
                                name = productName,
                                imageUrl = productImageUrl
                            )
                            ScanHistoryManager.addProduct(newProduct)
                        } catch (e: Exception) {
                            Log.e("BarcodeInfoActivity", "Error fetching product data", e)
                            productName = "Fehler beim Laden"
                            brand = "Fehler"
                            ingredients = "Fehler"
                            productImageUrl = ""
                            filters = emptyMap()
                        } finally {
                            isLoading = false
                        }
                    }
                }
            }


            Scaffold(
                topBar = { TopNavigationBar(title = "Produktdetails") },
                bottomBar = { BottomNavigationBar(navController = null) }
            ) { padding ->
                if (isLoading) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                } else {
                    ProductDetailsScreen(
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
            Log.e("BarcodeInfoActivity", "Error fetching product data", e)
            emptyMap()
        }
    }
}

@Composable
fun ProductDetailsScreen(
    productName: String,
    brand: String,
    ingredients: String,
    productImageUrl: String,
    filters: Map<String, Boolean>,
    isExpanded: Boolean,
    onExpandToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Produktbild
        item {
            Image(
                painter = rememberAsyncImagePainter(
                    ImageRequest.Builder(LocalContext.current)
                        .data(productImageUrl.ifEmpty { "https://via.placeholder.com/150" })
                        .apply { crossfade(true) }.build()
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

        // Zutaten mit Pfeilen
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

        // Filteranzeige
        filters.forEach { (label, isActive) ->
            item {
                FilterItem(label = label, isActive = isActive)
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
