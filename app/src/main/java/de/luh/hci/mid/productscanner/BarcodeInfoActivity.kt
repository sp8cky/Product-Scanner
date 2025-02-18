package de.luh.hci.mid.productscanner

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.ComponentActivity.RESULT_OK
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import de.luh.hci.mid.productscanner.ui.navigationbar.TopNavigationBar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.net.URL
import de.luh.hci.mid.productscanner.ui.navigationbar.BottomNavigationBar
import de.luh.hci.mid.productscanner.ui.navigationbar.TTSContentProvider
import de.luh.hci.mid.productscanner.ui.theme.Green60
import de.luh.hci.mid.productscanner.ui.navigationbar.TopNavigationBar
import de.luh.hci.mid.productscanner.ui.theme.Red40


class BarcodeInfoActivity : ComponentActivity(), TTSContentProvider {
    private var productName by mutableStateOf("Lade...")
    private var brand by mutableStateOf("Lade...")
    private var ingredients by mutableStateOf("Lade...")
    private var productFilters by mutableStateOf<Map<String, Boolean?>>(emptyMap())
    private var ttsfilterstring = ""

    fun updateActiveFilters() {
        val filterRepository = FilterRepository(this)

        lifecycleScope.launch {
            val filters = filterRepository.getFilters()
            Log.d("BarcodeInfoActivityFilter", "Fetched Filters: $filters") // Log filters

            // Nur Labels mit isActive == true beibehalten
            val activeLabels = filters.filter { it.isActive }.map { it.label }.toSet()

            // Filter productFilters, sodass nur Labels aus activeLabels übrig bleiben
            val filteredProductFilters = productFilters.filterKeys { it in activeLabels }

            val nullFilters = filteredProductFilters.filterValues { it == null }.keys
            Log.d("BarcodeInfoActivityFilter", "Filter mit Status null: $nullFilters")

            val trueFilters = filteredProductFilters.filterValues { it == true }.keys
            Log.d("BarcodeInfoActivityFilter", "Filter mit Status true: $trueFilters")

            val falseFilters = filteredProductFilters.filterValues { it == false }.keys
            Log.d("BarcodeInfoActivityFilter", "Filter mit Status false: $falseFilters")

            val nullFiltertext = if (nullFilters.isEmpty()) {
                ""
            } else {
                "Für folgende Filter gibt es keine Angaben: $nullFilters."
            }
            val trueFilterstext = if (trueFilters.isEmpty()) {
                ""
            } else {
                "Das Produkt ist: $trueFilters."
            }
            val falseFiltertext = if (falseFilters.isEmpty()) {
                ""
            } else {
                "Achtung! Das Produkt ist nicht: $falseFilters."
            }

            ttsfilterstring = trueFilterstext + nullFiltertext + falseFiltertext
            Log.d("BarcodeInfoActivityFilter", "Filtertext: $ttsfilterstring")
        }
    }

    override fun getTTSContent(): String {
        updateActiveFilters() // This is still asynchronous!

        val filterDescriptions = productFilters
        val filterText = if (filterDescriptions.isEmpty()) {
            "Es sind keine Filter aktiv."
        } else {
            ttsfilterstring
        }

        return "Du hast folgendes Produkt gescannt: $productName von $brand. $filterText. Die Zutaten sind $ingredients."
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val barcodeValue = intent.getStringExtra("BARCODE_VALUE")
        val filterRepository = FilterRepository(this)

        setContent {
            var productImageUrl by remember { mutableStateOf("") }
            var activeFilterOptions by remember { mutableStateOf<List<FilterOption>>(emptyList()) }
            var isExpanded by remember { mutableStateOf(false) }
            var isLoading by remember { mutableStateOf(true) }
            val scope = rememberCoroutineScope()

            // Load active filters from DataStore
            LaunchedEffect(Unit) {
                activeFilterOptions = filterRepository.getFilters().filter { it.isActive }
            }

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
                            productFilters = (productData["filters"] as? Map<*, *>)?.mapNotNull { entry ->
                                val key = entry.key as? String
                                val value = entry.value as? Boolean?
                                if (key != null) key to value else null
                            }?.toMap() ?: emptyMap()

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
                            productFilters = emptyMap()
                        } finally {
                            isLoading = false
                        }
                    }
                }
            }

            Scaffold(
                topBar = { TopNavigationBar(title = "Produktdetails") },
                bottomBar = { BottomNavigationBar(navController = null, ttsContentProvider = this@BarcodeInfoActivity) }
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
                        productFilters = productFilters,
                        activeFilterOptions = activeFilterOptions,
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
            val filters = mutableMapOf<String, Boolean?>()

            // Analyze ingredients
            val ingredients = product.optJSONArray("ingredients")
            val allergensTags = product.optJSONArray("allergens_tags") ?: JSONArray()

            // Determine vegetarian and vegan status
            filters["Vegetarisch"] = checkIngredientStatus(ingredients, "vegetarian")
            filters["Vegan"] = checkIngredientStatus(ingredients, "vegan")

            // Check allergens
            filters["Nussfrei"] = !allergensTags.toString().contains("en:nuts")
            filters["Sojafrei"] = !allergensTags.toString().contains("en:soybeans")
            filters["Glutenfrei"] = !allergensTags.toString().contains("en:gluten")
            filters["Milch/Laktosefrei"] = !allergensTags.toString().contains("en:milk")

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

    private fun checkIngredientStatus(ingredients: JSONArray?, statusKey: String): Boolean? {
        if (ingredients == null) return null
        var hasYes = false
        var hasMaybe = false

        for (i in 0 until ingredients.length()) {
            val ingredient = ingredients.optJSONObject(i)
            if (ingredient != null) {
                when (ingredient.optString(statusKey)) {
                    "yes" -> hasYes = true
                    "maybe" -> hasMaybe = true
                }
            }
        }

        return when {
            hasYes && !hasMaybe -> true // All ingredients are "yes"
            hasMaybe -> null           // Uncertainty exists
            else -> false              // At least one ingredient is not "yes"
        }
    }
}

@Composable
fun ProductDetailsScreen(
    productName: String,
    brand: String,
    ingredients: String,
    productImageUrl: String,
    productFilters: Map<String, Boolean?>,
    activeFilterOptions: List<FilterOption>,
    isExpanded: Boolean,
    onExpandToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Product image
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

        // Product details
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

        // Ingredients with toggle
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
                        else "Zutaten: ${ingredients.take(50)}" + if (ingredients.length > 50) "..." else "",
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

        // Display active filters with status
        activeFilterOptions.forEach { filterOption ->
            item {
                val status = productFilters[filterOption.label]
                FilterItem(
                    label = filterOption.label,
                    status = status
                )
            }
        }

        item {
            Button(
                onClick = {
                    val newItem = ShoppingItem(
                        id = ShoppingListManager.shoppingList.size + 1,
                        name = productName
                    )

                    SoundManager.playSound("success")
                    ShoppingListManager.addItem(newItem) // Produkt hinzufügen
                    Toast.makeText(context, "Zur Einkaufsliste hinzugefügt!", Toast.LENGTH_SHORT).show()
                },

                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Green60)
            ) {
                Text(
                    text = "Zur Einkaufsliste hinzufügen",
                    fontSize = 18.sp,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun FilterItem(label: String, status: Boolean?) {
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
        Text(
            text = when (status) {
                true -> "✔"
                null -> "?"
                false -> "❌"
            },
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = when (status) {
                true -> Color.Green
                null -> Color.Gray
                false -> Color.Red
            }
        )
    }
}
