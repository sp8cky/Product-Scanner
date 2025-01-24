package de.luh.hci.mid.productscanner

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import de.luh.hci.mid.productscanner.ui.navigationbar.BottomNavigationBar
import de.luh.hci.mid.productscanner.ui.navigationbar.TopNavigationBar
import de.luh.hci.mid.productscanner.ui.theme.Blue40
import de.luh.hci.mid.productscanner.ui.theme.Red40

// Datenklasse für ein gescanntes Produkt
data class ScannedProduct(
    val id: String, // Eindeutiger Identifier, z.B. Barcode
    val name: String,
    val imageUrl: String
)

class DatabaseActivity : ComponentActivity() {

    // Globale Liste für die Scan History
    private val scanHistory = mutableStateListOf<ScannedProduct>() // Mutable State List

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            DatabaseScreen(
                scanHistory = scanHistory,
                onProductScanned = { product ->
                    scanHistory.add(product) // Füge gescanntes Produkt zur History hinzu
                }
            )
        }
    }
}

@Composable
fun DatabaseScreen(
    scanHistory: SnapshotStateList<ScannedProduct>,
    onProductScanned: (ScannedProduct) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = { TopNavigationBar(title = "Scan-History") },
        bottomBar = { BottomNavigationBar(navController = null) }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Anzeige der Scan History
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                items(scanHistory.size) { index ->
                    val product = scanHistory[index]
                    EntryItem(
                        product = product,
                        onDetailsClicked = {
                            // Öffne die Detailansicht
                        },
                        onDeleteClicked = {
                            // Entferne das Produkt aus der History
                            scanHistory.remove(product) // Verwende remove() mit dem Produkt
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun EntryItem(
    product: ScannedProduct,
    onDetailsClicked: () -> Unit,
    onDeleteClicked: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Bild des Produkts
        Image(
            painter = rememberAsyncImagePainter(product.imageUrl),
            contentDescription = "Produktbild",
            modifier = Modifier
                .size(50.dp)
                .align(Alignment.CenterVertically)
        )

        Spacer(modifier = Modifier.width(8.dp))

        // Produktname
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = product.name,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
        }

        // Zwei Buttons
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Button(
                onClick = onDetailsClicked,
                modifier = Modifier
                    .height(40.dp)
                    .width(40.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Blue40),
                shape = RectangleShape,
                elevation = ButtonDefaults.buttonElevation(0.dp)
            ) {
                Text(
                    text = "🔍", // Icon für Details
                    fontSize = 18.sp,
                    color = Color.White
                )
            }

            Button(
                onClick = onDeleteClicked,
                modifier = Modifier
                    .height(40.dp)
                    .width(40.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Red40),
                shape = RectangleShape,
                elevation = ButtonDefaults.buttonElevation(0.dp)
            ) {
                Text(
                    text = "🗑", // Icon für Löschen
                    fontSize = 18.sp,
                    color = Color.White
                )
            }
        }
    }
}
