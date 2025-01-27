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
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import coil.size.Scale
import coil.size.Size
import coil.transform.RoundedCornersTransformation
import de.luh.hci.mid.productscanner.ui.navigationbar.BottomNavigationBar
import de.luh.hci.mid.productscanner.ui.navigationbar.TopNavigationBar
import de.luh.hci.mid.productscanner.ui.theme.Blue40
import de.luh.hci.mid.productscanner.ui.theme.Red40

// Datenklasse für einen Listeneintrag
data class ShoppingItem(
    val id: Int,
    val name: String
)

// Singleton-Klasse zur Verwaltung der Einkaufsliste
object ShoppingListManager {
    val shoppingList = mutableStateListOf<ShoppingItem>()

    fun addItem(item: ShoppingItem) {
        shoppingList.add(item)
    }

    fun removeItem(item: ShoppingItem) {
        shoppingList.remove(item)
    }
}

class EinkaufslisteActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            EinkaufslisteScreen(
                shoppingList = ShoppingListManager.shoppingList,
                onItemAdded = { item ->
                    ShoppingListManager.addItem(item)
                }
            )
        }
    }
}

@Composable
fun EinkaufslisteScreen(
    shoppingList: SnapshotStateList<ShoppingItem>,
    onItemAdded: (ShoppingItem) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = { TopNavigationBar(title = "Einkaufsliste") },
        bottomBar = { BottomNavigationBar(navController = null) }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                if (shoppingList.isEmpty()) {
                    // Nachricht anzeigen, wenn keine Einträge vorhanden sind
                    item {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Noch keine Einträge vorhanden",
                                fontSize = 18.sp,
                                color = Color.Gray
                            )
                        }
                    }
                } else {
                    items(shoppingList.size) { index ->
                        val item = shoppingList[index]
                        ShoppingListItem(
                            item = item,
                            onDeleteClicked = {
                                ShoppingListManager.removeItem(item)
                            },
                            onDetailsClicked = {
                                // Details-Logik hier einfügen
                                Log.d("Einkaufsliste", "Details für ${item.name} anzeigen")
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // "+"-Button immer am unteren Ende
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = {
                        val newItem = ShoppingItem(
                            id = shoppingList.size + 1,
                            name = "Produkt ${shoppingList.size + 1}"
                        )
                        onItemAdded(newItem)
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(60.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Red40),
                    shape = RectangleShape,
                    elevation = ButtonDefaults.buttonElevation(0.dp)
                ) {
                    Text(
                        text = "+",
                        fontSize = 24.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}


@Composable
fun ShoppingListItem(
    item: ShoppingItem,
    onDeleteClicked: () -> Unit,
    onDetailsClicked: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            modifier = Modifier
                .weight(1f) // Nimmt den restlichen Platz ein
                .align(Alignment.CenterVertically), // Zentriert die Spalte vertikal
            verticalArrangement = Arrangement.Center
        ) {
            // Eintragsnummer und Name
            Text(
                text = "#${item.id} - ${item.name}",
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium
            )
        }

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
        }
        Spacer(modifier = Modifier.width(8.dp)) // Kleiner Abstand zwischen den Buttons
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Button(
                onClick = onDeleteClicked,
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
}
