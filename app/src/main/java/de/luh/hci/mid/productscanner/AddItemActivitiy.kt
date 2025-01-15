package de.luh.hci.mid.productscanner

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.luh.hci.mid.productscanner.ui.navigationbar.BottomNavigationBar
import de.luh.hci.mid.productscanner.ui.navigationbar.TopNavigationBar
import de.luh.hci.mid.productscanner.ui.theme.Blue40
import de.luh.hci.mid.productscanner.ui.theme.PurpleGrey40
import de.luh.hci.mid.productscanner.ui.theme.Red40

class AddItemActivitiy : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            AddScreen()
        }
    }

    @Composable
    fun AddScreen(modifier: Modifier = Modifier) {
        var productName by remember { mutableStateOf("Produktname") } // Standardname

        Scaffold(
            topBar = { TopNavigationBar(title = "Add Item") },
            bottomBar = { BottomNavigationBar(navController = null) }
        ) { paddingValues ->
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
            ) {
                // Textfeld für Produktname mit X-Button zum Löschen
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .padding(16.dp)
                            .border(1.dp, Color.Gray, shape = CircleShape) // Border um das Textfeld
                            .shadow(2.dp, shape = CircleShape) // Optionaler Shadow-Effekt
                            .background(Color.White) // Hintergrundfarbe des Textfelds
                    ) {
                        BasicTextField(
                            value = productName,
                            onValueChange = { productName = it },
                            textStyle = LocalTextStyle.current.copy(fontSize = 20.sp),
                            modifier = Modifier
                                .fillMaxWidth() // Füllt den gesamten Bereich der Box aus
                                .padding(8.dp), // Innenabstand des Textfelds
                            singleLine = true // Für eine einzeilige Eingabe
                        )
                    }

                    // X-Button, um den Inhalt des Textfeldes zu löschen
                    Button(
                        onClick = { productName = "" },
                        modifier = Modifier.size(32.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PurpleGrey40),
                        shape = CircleShape,
                        elevation = ButtonDefaults.buttonElevation(0.dp)
                    ) {
                        Text(
                            text = "X",
                            fontSize = 18.sp,
                            color = Color.Red,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Großer runder Mikrofon-Button am unteren Ende
                Spacer(modifier = Modifier.weight(1f)) // Platzierung des Buttons am unteren Ende

                FloatingActionButton(
                    onClick = { /* Mikrofon Aktion */ },
                    modifier = Modifier
                        .size(200.dp)
                        .align(Alignment.CenterHorizontally),
                    containerColor = Red40,
                    shape = CircleShape
                ) {
                    Text(
                        text = "🎤",
                        fontSize = 32.sp,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp) // horizontalArrangement für Row
                ) {
                    Button(
                        onClick = {
                            //TODO
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
}
