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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path

class InfoActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val barcodeValue = intent.getStringExtra("BARCODE_VALUE") ?: "No barcode found"

        val api = createOpenFoodFactsApi()

        setContent {
            var productName by remember { mutableStateOf("Lade Produkt...") }
            var brand by remember { mutableStateOf("Unbekannte Marke") }
            var ingredients by remember { mutableStateOf("Keine Zutaten verfügbar") }

            LaunchedEffect(barcodeValue) {
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val response = api.getProduct(barcodeValue)
                        if (response.status == 1 && response.product != null) {
                            val product = response.product
                            productName = product.product_name ?: "Unbekannt"
                            brand = product.brands ?: "Unbekannte Marke"
                            ingredients = product.ingredients_text ?: "Keine Zutaten verfügbar"
                        } else {
                            productName = "Produkt nicht gefunden"
                        }
                    } catch (e: Exception) {
                        productName = "Fehler beim Laden"
                    }
                }
            }

            InfoScreen(
                barcode = barcodeValue,
                productName = productName,
                brand = brand,
                ingredients = ingredients
            )
        }
    }

    @Composable
    fun InfoScreen(barcode: String, productName: String, brand: String, ingredients: String) {
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

                    // Produktname und Barcode
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

            // Checkbox (deaktiviert)
            androidx.compose.material3.Checkbox(
                checked = true,
                onCheckedChange = null,
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

    private fun createOpenFoodFactsApi(): OpenFoodFactsApi {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://world.openfoodfacts.org/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        return retrofit.create(OpenFoodFactsApi::class.java)
    }

    interface OpenFoodFactsApi {
        @GET("api/v0/product/{barcode}.json")
        suspend fun getProduct(@Path("barcode") barcode: String): ProductResponse
    }

    data class ProductResponse(
        val status: Int,
        val product: ProductDetails?
    )

    data class ProductDetails(
        val product_name: String?,
        val brands: String?,
        val ingredients_text: String?
    )
}
