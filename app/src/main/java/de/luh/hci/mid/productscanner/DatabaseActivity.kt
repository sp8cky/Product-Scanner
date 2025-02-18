package de.luh.hci.mid.productscanner

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import coil.size.Scale
import coil.size.Size
import coil.transform.RoundedCornersTransformation
import de.luh.hci.mid.productscanner.ui.navigationbar.BottomNavigationBar
import de.luh.hci.mid.productscanner.ui.navigationbar.TTSContentProvider
import de.luh.hci.mid.productscanner.ui.navigationbar.TopNavigationBar
import de.luh.hci.mid.productscanner.ui.theme.Blue40
import de.luh.hci.mid.productscanner.ui.theme.Red40

// Datenklasse für ein gescanntes Produkt
data class ScannedProduct(
    val id: String, // Eindeutiger Identifier, z.B. Barcode
    val name: String,
    val imageUrl: String
)

// Singleton-Klasse zur Verwaltung der Scan-History
object ScanHistoryManager {
    val scanHistory = mutableStateListOf<ScannedProduct>()

    fun addProduct(product: ScannedProduct) {
        // Überprüfen, ob ein Produkt mit derselben ID bereits existiert
        if (scanHistory.none { it.id == product.id }) {
            scanHistory.add(product)
        }
    }

    fun removeProduct(product: ScannedProduct) {
        SoundManager.playSound("delete")
        scanHistory.remove(product)
    }
}

class DatabaseActivity : ComponentActivity(), TTSContentProvider {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            DatabaseScreen(
                scanHistory = ScanHistoryManager.scanHistory,
                onProductScanned = { product ->
                    ScanHistoryManager.addProduct(product)
                }
            )
        }
    }

    override fun getTTSContent(): String {
        val recentProducts = ScanHistoryManager.scanHistory.takeLast(5).map { it.name }

        return if (recentProducts.isEmpty()) {
            "Du befindest dich im Scanverlauf. Es wurden noch keine Produkte gescannt. " +
                    "Scanne ein Produkt, um es hier anzuzeigen."
        } else {
            val productList = when (recentProducts.size) {
                1 -> recentProducts.first() // Nur ein Produkt
                2 -> recentProducts.joinToString(" und ") // Zwei Produkte
                else -> recentProducts.dropLast(1).joinToString(", ") + " und " + recentProducts.last() // Mehr als zwei Produkte
            }

            val productCount = recentProducts.size // Korrekte Anzahl der Produkte
            "Du befindest dich im Scanverlauf. Die letzten $productCount gescannten Produkte waren: $productList. " +
                    "Über das blaue Lupensymbol kannst du weitere Produktinfos aufrufen, über das rote Löschsymbol kannst du das Produkt aus der Liste löschen."
        }
    }


}


@Composable
fun DatabaseScreen(
    scanHistory: SnapshotStateList<ScannedProduct>,
    onProductScanned: (ScannedProduct) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current // Lokaler Kontext für den Intent

    Scaffold(
        topBar = { TopNavigationBar(title = "Scan-History") },
        bottomBar = { BottomNavigationBar(navController = null, ttsContentProvider = LocalContext.current as TTSContentProvider) }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            if (scanHistory.isEmpty()) {
                // Nachricht anzeigen, wenn keine Einträge vorhanden sind
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Noch keine Einträge gescannt",
                        fontSize = 18.sp,
                        color = Color.Gray
                    )
                }
            } else {
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
                                SoundManager.playSound("tap")
                                // Intent starten mit Barcode
                                val intent = Intent(context, BarcodeInfoActivity::class.java).apply {
                                    putExtra("BARCODE_VALUE", product.id) // Barcode des Produkts
                                }
                                context.startActivity(intent)
                            },
                            onDeleteClicked = {
                                ScanHistoryManager.removeProduct(product)
                            }
                        )
                    }
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
    // Zustand für den Bestätigungsdialog
    val showConfirmationDialog = remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Produktbild
        val painter = rememberAsyncImagePainter(
            model = ImageRequest.Builder(LocalContext.current)
                .data(product.imageUrl)
                .crossfade(true)
                .scale(Scale.FILL)
                .size(Size.ORIGINAL)
                .transformations(RoundedCornersTransformation(8f))
                .listener(
                    onStart = { Log.d("Coil", "Bildladen gestartet: ${product.imageUrl}") },
                    onSuccess = { _, _ -> Log.d("Coil", "Bild erfolgreich geladen: ${product.imageUrl}") },
                    onError = { _, result -> Log.e("Coil", "Fehler beim Laden des Bildes: ${result.throwable}") }
                )
                .build()
        )

        Image(
            painter = painter,
            contentDescription = "Produktbild",
            modifier = Modifier
                .size(50.dp)
                .align(Alignment.CenterVertically)
        )

        Spacer(modifier = Modifier.width(2.dp))

        // Produktname und Barcode
        Column(
            modifier = Modifier
                .weight(1f)
                .align(Alignment.CenterVertically),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = product.name,
                fontSize = 16.sp,
                modifier = Modifier.padding(bottom = 2.dp)
            )
            Text(
                text = "Barcode: ${product.id}",
                fontSize = 14.sp,
                color = Color.Gray
            )
        }

        // Buttons
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
                elevation = ButtonDefaults.buttonElevation(0.dp),
                contentPadding = PaddingValues(0.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Search,
                    contentDescription = "Details anzeigen",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }

            Button(
                onClick = {
                    SoundManager.playSound("tap")
                    showConfirmationDialog.value = true
                          },
                modifier = Modifier
                    .height(40.dp)
                    .width(40.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Red40),
                shape = RectangleShape,
                elevation = ButtonDefaults.buttonElevation(0.dp),
                contentPadding = PaddingValues(0.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Delete,
                    contentDescription = "Löschen",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }

    // Bestätigungsdialog
    if (showConfirmationDialog.value) {
        AlertDialog(
            onDismissRequest = { showConfirmationDialog.value = false },
            confirmButton = {
                TextButton(onClick = {
                    onDeleteClicked() // Produkt löschen
                    showConfirmationDialog.value = false
                }) {
                    Text("Ja", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    SoundManager.playSound("tap")
                    showConfirmationDialog.value = false }) {
                    Text("Nein")
                }
            },
            title = { Text("Produkt entfernen") },
            text = { Text("Möchtest Du dieses Produkt wirklich aus der Liste entfernen?") }
        )
    }
}
