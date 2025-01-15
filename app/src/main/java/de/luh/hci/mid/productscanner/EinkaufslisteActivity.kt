package de.luh.hci.mid.productscanner

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
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
import de.luh.hci.mid.productscanner.ui.navigationbar.BottomNavigationBar
import de.luh.hci.mid.productscanner.ui.navigationbar.TopNavigationBar
import de.luh.hci.mid.productscanner.ui.theme.Blue40
import de.luh.hci.mid.productscanner.ui.theme.Red40

class EinkaufslisteActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            EinkaufslisteScreen()
        }
    }

    @Composable
    fun EinkaufslisteScreen(modifier: Modifier = Modifier) {
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
                // LazyColumn für die Liste der Einträge
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f) // Füllt den verbleibenden Raum
                ) {
                    items(10) { index -> // Beispiel: 10 Einträge
                        EntryItem(entryNumber = index + 1, productName = "Produkt $index")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // + Button am unteren Ende
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp) // horizontalArrangement für Row
                ) {
                    Button(
                        onClick = {
                            // Context korrekt übergeben, falls es zu Fehlern kommt
                            val intent = Intent(this@EinkaufslisteActivity, AddItemActivitiy::class.java)
                            startActivity(intent)
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(60.dp), // Einheitliche Höhe der Buttons
                        colors = ButtonDefaults.buttonColors(containerColor = Red40),
                        shape = RectangleShape,
                        elevation = ButtonDefaults.buttonElevation(0.dp)
                    ) {
                        Text(
                            text = "+",
                            fontSize = 18.sp,
                            color = Color.White,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
            }
        }
    }

    @Composable
    fun EntryItem(entryNumber: Int, productName: String) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)

        ) {
            // Eintragsnummer
            Text(
                text = "#$entryNumber",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.CenterVertically)
            )

            Spacer(modifier = Modifier.width(8.dp))

            // Produktname
            Text(
                text = productName,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f).align(Alignment.CenterVertically)
            )

            // Zwei kleine runde Buttons nebeneinander
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Button 1
                Button(
                    onClick = {
                        val intent = Intent(this@EinkaufslisteActivity, EditItemActivity::class.java)
                        startActivity(intent)
                    },

                    modifier = Modifier
                        .size(40.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Blue40),
                    shape = CircleShape,
                    elevation = ButtonDefaults.buttonElevation(0.dp)
                ) {
                    Text(
                        text = "🔍", // Beispiel-Icon für Button 1
                        fontSize = 18.sp,
                        color = Color.White
                    )
                }

                // Button 2
                Button(
                    onClick = {
                    },
                    modifier = Modifier
                        .size(40.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Red40),
                    shape = CircleShape,
                    elevation = ButtonDefaults.buttonElevation(0.dp)
                ) {
                    Text(
                        text = "Add Item", // Beispiel-Icon für Button 2
                        fontSize = 18.sp,
                        color = Color.White
                    )
                }
            }
        }
    }
}
