package de.luh.hci.mid.productscanner

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
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
    private lateinit var addItemLauncher: ActivityResultLauncher<Intent>
    private lateinit var editItemLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Launcher zur Handhabung des Ergebnisses der AddItemActivity
        addItemLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val newItemName = result.data?.getStringExtra("item_name") ?: return@registerForActivityResult
                val newItem = ShoppingItem(
                    id = ShoppingListManager.shoppingList.size + 1,
                    name = newItemName
                )
                ShoppingListManager.addItem(newItem)
            }
        }

        // Launcher zur Handhabung des Ergebnisses der EditItemActivity
        editItemLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val updatedName = result.data?.getStringExtra("updated_name") ?: return@registerForActivityResult
                val itemId = result.data?.getIntExtra("item_id", -1) ?: -1
                if (itemId != -1) {
                    val index = ShoppingListManager.shoppingList.indexOfFirst { it.id == itemId }
                    if (index != -1) {
                        val updatedItem = ShoppingListManager.shoppingList[index].copy(name = updatedName)
                        ShoppingListManager.shoppingList[index] = updatedItem
                    }
                }
            }
        }

        setContent {
            EinkaufslisteScreen(
                shoppingList = ShoppingListManager.shoppingList,
                onAddItemClicked = {
                    val intent = Intent(this, AddItemActivitiy::class.java)
                    addItemLauncher.launch(intent)
                },
                onEditItemClicked = { item ->
                    val intent = Intent(this, EditItemActivity::class.java).apply {
                        putExtra("item_id", item.id)
                        putExtra("item_name", item.name)
                    }
                    editItemLauncher.launch(intent)
                }
            )
        }
    }
}


@Composable
fun EinkaufslisteScreen(
    shoppingList: SnapshotStateList<ShoppingItem>,
    onAddItemClicked: () -> Unit,
    onEditItemClicked: (ShoppingItem) -> Unit, // Neue Callback-Funktion
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
            // Scrollable Liste mit Overlay-Indikatoren
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                OverlayScrollIndicator(
                    shoppingList = shoppingList,
                    onDeleteClicked = { item -> ShoppingListManager.removeItem(item) },
                    onDetailsClicked = { onEditItemClicked(it) } // Edit-Funktion aufrufen
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // "+"-Button am unteren Ende
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onAddItemClicked,
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
                .weight(1f)
                .align(Alignment.CenterVertically),
            verticalArrangement = Arrangement.Center
        ) {
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

        Spacer(modifier = Modifier.width(8.dp))

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

@Composable
fun OverlayScrollIndicator(
    shoppingList: SnapshotStateList<ShoppingItem>,
    onDeleteClicked: (ShoppingItem) -> Unit,
    onDetailsClicked: (ShoppingItem) -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()

    Box(modifier = modifier.fillMaxSize()) {
        if (shoppingList.isEmpty()) {
            // Nachricht anzeigen, wenn keine Einträge vorhanden sind
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
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize()
            ) {
                items(shoppingList.size) { index ->
                    val item = shoppingList[index]
                    ShoppingListItem(
                        item = item,
                        onDeleteClicked = { onDeleteClicked(item) },
                        onDetailsClicked = { onDetailsClicked(item) }
                    )
                }
            }

            if (listState.canScrollForward) {
                Text(
                    text = "Scroll for more",
                    color = Color.Gray,
                    fontSize = 12.sp,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 16.dp)
                )
            }

            if (listState.canScrollBackward) {
                Text(
                    text = "Scroll up",
                    color = Color.Gray,
                    fontSize = 12.sp,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 16.dp)
                )
            }
        }
    }
}
