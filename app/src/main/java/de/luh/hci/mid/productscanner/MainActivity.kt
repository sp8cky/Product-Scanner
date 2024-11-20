package de.luh.hci.mid.productscanner

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import de.luh.hci.mid.productscanner.ui.theme.ProductscannerTheme
import android.content.Intent
import androidx.compose.ui.platform.LocalContext


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ProductscannerTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    MainScreen(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun MainScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current // Hole den Kontext hier, innerhalb einer @Composable-Funktion

    Column(
        modifier = modifier.fillMaxSize()
    ) {
        // Scan Button (Blue, 1/3 of the screen height)
        Button(
            onClick = {
                val intent = Intent(context, ScanActivity::class.java)
                context.startActivity(intent) // Starte ScanActivity
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 50.dp)
                .padding(horizontal = 30.dp)
                .height(300.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Blue) // Setze die Hintergrundfarbe
        ) {
            Text(text = "Scan", color = Color.White)
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Green Buttons (take up half the screen width)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 30.dp)
        ) {
            Button(
                onClick = { /* Action for Einkaufsliste */ },
                modifier = Modifier
                    .weight(1f)
                    .height(150.dp)
                    .padding(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Green) // Hintergrundfarbe Grün
            ) {
                Text(text = "Einkaufsliste", color = Color.White)
            }
            Button(
                onClick = { /* Action for Datenbank */ },
                modifier = Modifier
                    .weight(1f)
                    .height(150.dp)
                    .padding(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Green) // Hintergrundfarbe Grün
            ) {
                Text(text = "Datenbank", color = Color.White)
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Red Buttons (take up half the screen width)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 30.dp)
        ) {
            Button(
                onClick = { /* Action for Filter */ },
                modifier = Modifier
                    .weight(1f)
                    .height(150.dp)
                    .padding(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red) // Hintergrundfarbe Rot
            ) {
                Text(text = "Filter", color = Color.White)
            }
            Button(
                onClick = { /* Action for Einstellungen */ },
                modifier = Modifier
                    .weight(1f)
                    .height(150.dp)
                    .padding(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red) // Hintergrundfarbe Rot
            ) {
                Text(text = "Einstellungen", color = Color.White)
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    ProductscannerTheme {
        MainScreen()
    }
}
