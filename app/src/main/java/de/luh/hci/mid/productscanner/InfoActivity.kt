package de.luh.hci.mid.productscanner

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicText
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
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

class InfoActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val barcodeValue = intent.getStringExtra("BARCODE_VALUE") ?: "No barcode found"

        setContent {
            InfoScreen(barcodeValue)
        }
    }

    @Composable
    fun InfoScreen(barcode: String) {
        val productImageUrl = "https://example.com/product_image.jpg" // Beispiel-URL für Produktbild

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

                    // Produktbild und Name
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Produktbild mit Platzhalter
                        Image(
                            painter = rememberImagePainter(
                                productImageUrl,
                                builder = {
                                    crossfade(true)
                                    placeholder(R.drawable.nutella)  // Platzhalter Bild
                                }
                            ),
                            contentDescription = "Product Image",
                            modifier = Modifier
                                .size(100.dp)
                                .padding(end = 16.dp)
                        )

                        // Produktname und Barcode
                        Column(
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "Product Name",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
                            Text(
                                text = "Barcode: $barcode",
                                fontSize = 16.sp,
                                color = Color.Gray
                            )
                        }
                    }

                    // Zutaten nebeneinander
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp)
                    ) {
                        Text(
                            text = "Zutaten: Zucker, Palmöl, HASELNÜSSE (13 %), MAGERMILCHPULVER (8,7 %), fettarmer Kakao (7,4%), Emulgator Lecithine (SOJA), Vanillin.Zucker, Palmöl, HASELNÜSSE (13 %), MAGERMILCHPULVER (8,7 %), fettarmer Kakao (7,4%), Emulgator Lecithine (SOJA), Vanillin.Zucker, Palmöl, HASELNÜSSE (13 %), MAGERMILCHPULVER (8,7 %), fettarmer Kakao (7,4%), Emulgator Lecithine (SOJA), Vanillin.", // Zutaten nebeneinander
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Normal,
                            color = Color.Black
                        )
                    }

                    // Spacer zwischen Zutaten und Filter
                    Spacer(modifier = Modifier.height(24.dp))

                    // Filter mit Checkboxen (deaktiviert)
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
            bottomBar = { // Buttons am unteren Rand fixieren
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

            // Checkbox (deaktiviert, nicht änderbar)
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
                onClick = { finish() }, // Zurück zur vorherigen Aktivität
                modifier = Modifier
                    .weight(1f)
                    .height(60.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Red40),
                shape = RectangleShape,
                elevation = ButtonDefaults.buttonElevation(0.dp)
            ) {
                Text(
                    text = "\uD83C\uDFE0", // Home Icon
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
                    text = "🔊", // Lautsprecher Icon
                    fontSize = 24.sp,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
