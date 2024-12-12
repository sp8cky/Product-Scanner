package de.luh.hci.mid.productscanner

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.luh.hci.mid.productscanner.ui.theme.ProductscannerTheme
import de.luh.hci.mid.productscanner.ui.theme.Blue40
import de.luh.hci.mid.productscanner.ui.theme.Red40

class DatabaseActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            DatabaseScreen(
                onHomeClicked = { finish() }, // Zurück zur vorherigen Aktivität
                onSpeakerClicked = { /* Action for Lautsprecher */ }
            )
        }
    }

    @Composable
    fun DatabaseScreen(
        onHomeClicked: () -> Unit,
        onSpeakerClicked: () -> Unit,
        modifier: Modifier = Modifier
    ) {
        Scaffold(
            content = { paddingValues ->
                Column(
                    modifier = modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(16.dp), // Zusätzliche Polsterung
                ) {
                    // Header
                    Text(
                        text = "Scan-History",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // LazyColumn für die Liste der Einträge
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f) // Füllt den verbleibenden Raum
                            .padding(bottom = 80.dp) // Platz für die Buttons am unteren Rand
                    ) {
                        items(10) { index -> // Beispiel: 10 Einträge
                            EntryItem(entryNumber = index + 1, productName = "Produkt $index")
                        }
                    }

                    // Footer Row für Home und Lautsprecher Buttons (Immer sichtbar)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp), // Padding am unteren Rand
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = { onHomeClicked() },
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
                            onClick = { onSpeakerClicked() },
                            modifier = Modifier
                                .weight(1f)
                                .height(60.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Blue40),
                            shape = RectangleShape,
                            elevation = ButtonDefaults.buttonElevation(0.dp)
                        ) {
                            Text(
                                text = "🔊", // Speaker Icon
                                fontSize = 24.sp,
                                color = Color.White,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        )
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

            // Bild des Produkts (Platzhalter für später)
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .background(Color.Gray)
                    .align(Alignment.CenterVertically)
            )

            Spacer(modifier = Modifier.width(8.dp))

            // Produktname
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = productName,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            // Zwei Buttons übereinander
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Button(
                    onClick = { /* Action für Button 1 */ },
                    modifier = Modifier
                        .height(40.dp)
                        .width(40.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Blue40),
                    shape = RectangleShape,
                    elevation = ButtonDefaults.buttonElevation(0.dp)
                ) {
                    Text(
                        text = "🔍", // Beispiel-Icon für Button 1
                        fontSize = 18.sp,
                        color = Color.White
                    )
                }

                Button(
                    onClick = { /* Action für Button 2 */ },
                    modifier = Modifier
                        .height(40.dp)
                        .width(40.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Red40),
                    shape = RectangleShape,
                    elevation = ButtonDefaults.buttonElevation(0.dp)
                ) {
                    Text(
                        text = "🛒", // Beispiel-Icon für Button 2
                        fontSize = 18.sp,
                        color = Color.White
                    )
                }
            }
        }
    }


}