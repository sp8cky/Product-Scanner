package de.luh.hci.mid.productscanner

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberImagePainter
import de.luh.hci.mid.productscanner.ui.theme.Blue40
import de.luh.hci.mid.productscanner.ui.theme.Red40
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.net.URL

class InfoActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val barcodeValue = intent.getStringExtra("BARCODE_VALUE") ?: "Kein Barcode gefunden"

        setContent {
            var productName by remember { mutableStateOf("Lade...") }
            var brand by remember { mutableStateOf("Lade...") }
            var ingredients by remember { mutableStateOf("Lade...") }
            var productImageUrl by remember { mutableStateOf("") }

            val scope = rememberCoroutineScope()

            LaunchedEffect(barcodeValue) {
                scope.launch(Dispatchers.IO) {
                    val productData = fetchProductData(barcodeValue)
                    productName = productData["name"] ?: "Unbekannt"
                    brand = productData["brand"] ?: "Unbekannt"
                    ingredients = productData["ingredients"] ?: "Keine Angaben"
                    productImageUrl = productData["image_url"] ?: ""
                }
            }

            InfoScreen(
                barcode = barcodeValue,
                productName = productName,
                brand = brand,
                ingredients = ingredients,
                productImageUrl = productImageUrl
            )
        }
    }

    private fun fetchProductData(barcode: String): Map<String, String> {
        return try {
            val url = "https://world.openfoodfacts.org/api/v0/product/$barcode.json"
            val response = URL(url).readText()
            val json = JSONObject(response)
            if (!json.has("product")) return emptyMap()

            val product = json.getJSONObject("product")
            mapOf(
                "name" to product.optString("product_name", "Unbekannt"),
                "brand" to product.optString("brands", "Unbekannt"),
                "ingredients" to product.optString("ingredients_text_de", "Keine Angaben"),
                "image_url" to product.optString("image_url", "")
            )
        } catch (e: Exception) {
            e.printStackTrace()
            emptyMap()
        }
    }

    @Composable
    fun InfoScreen(
        barcode: String,
        productName: String,
        brand: String,
        ingredients: String,
        productImageUrl: String
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            content = { paddingValues ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Header
                    Text(
                        text = "Info",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Produktbild und Basisinformationen
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Produktbild
                        Image(
                            painter = rememberImagePainter(
                                data = productImageUrl.ifEmpty { "https://via.placeholder.com/150" },
                                builder = {
                                    crossfade(true)
                                    placeholder(R.drawable.nutella) // Platzhalterbild
                                }
                            ),
                            contentDescription = "Produktbild",
                            modifier = Modifier
                                .size(100.dp) // Quadratische Größe
                                .padding(end = 16.dp)
                        )

                        // Produktdetails
                        Column(
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "Name: $productName",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
                            Text(
                                text = "Marke: $brand",
                                fontSize = 18.sp,
                                color = Color.Gray
                            )
                            Text(
                                text = "Barcode: $barcode",
                                fontSize = 16.sp,
                                color = Color.Gray
                            )
                        }
                    }

                    // Zutaten
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Zutaten: $ingredients",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Normal,
                        color = Color.Black
                    )

                    // Filter mit Checkboxen
                    Spacer(modifier = Modifier.height(24.dp))
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        FilterItem(label = "Vegetarisch")
                        FilterItem(label = "Vegan")
                        FilterItem(label = "Nussfrei")
                    }
                }
            },
            bottomBar = {
                BottomBar()
            }
        )
    }

    @Composable
    fun FilterItem(label: String) {
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Black
            )
            androidx.compose.material3.Checkbox(
                checked = true, // Immer aktiv
                onCheckedChange = null, // Keine Interaktion möglich
                enabled = false
            )
        }
    }

    @Composable
    fun BottomBar() {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick = { finish() },
                modifier = Modifier
                    .weight(1f)
                    .height(60.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Red40),
                shape = RectangleShape,
                elevation = ButtonDefaults.buttonElevation(0.dp)
            ) {
                Text(
                    text = "\uD83C\uDFE0",
                    fontSize = 24.sp,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
            }

            Button(
                onClick = { /* Lautsprecher Action */ },
                modifier = Modifier
                    .weight(1f)
                    .height(60.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Blue40),
                shape = RectangleShape,
                elevation = ButtonDefaults.buttonElevation(0.dp)
            ) {
                Text(
                    text = "🔊",
                    fontSize = 24.sp,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
