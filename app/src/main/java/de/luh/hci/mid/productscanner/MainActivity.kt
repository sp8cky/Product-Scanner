package de.luh.hci.mid.productscanner

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.RectangleShape
import de.luh.hci.mid.productscanner.ui.theme.ProductscannerTheme

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
    val context = LocalContext.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp), // Dynamisches Padding für alle Geräte
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header
        Text(
            text = "HOME",
            fontSize = 32.sp,
            color = Color.Black,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        // Scan Button (oben)
        Button(
            onClick = {
                val intent = Intent(context, ScanActivity::class.java)
                context.startActivity(intent)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp), // Höhe explizit definiert
            colors = ButtonDefaults.buttonColors(containerColor = Color.Blue),
            shape = RectangleShape, // Ecken nicht abrunden
            elevation = ButtonDefaults.buttonElevation(0.dp) // Schatten entfernen
        ) {
            Text(
                text = "SCAN",
                fontSize = 48.sp,
                color = Color.White,
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Mittlere Buttons (Scan-Verlauf und Einkaufsliste)
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp) // Abstand zwischen Buttons
        ) {
            Button(
                onClick = { /* Action for Scan-Verlauf */ },
                modifier = Modifier
                    .weight(1f) // Gleichmäßiges Ausfüllen
                    .height(150.dp), // Höhe direkt auf den Button angewendet
                colors = ButtonDefaults.buttonColors(containerColor = Color.Green),
                shape = RectangleShape,
                elevation = ButtonDefaults.buttonElevation(0.dp)
            ) {
                Text(text = "SCAN-VERLAUF", fontSize = 16.sp, color = Color.White)
            }
            Button(
                onClick = { /* Action for Einkaufsliste */ },
                modifier = Modifier
                    .weight(1f) // Gleichmäßiges Ausfüllen
                    .height(150.dp), // Höhe direkt auf den Button angewendet
                colors = ButtonDefaults.buttonColors(containerColor = Color.Green),
                shape = RectangleShape,
                elevation = ButtonDefaults.buttonElevation(0.dp)
            ) {
                Text(text = "EINKAUFSLISTE", fontSize = 16.sp, color = Color.White)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Untere Buttons (Filter und Einstellungen)
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp) // Abstand zwischen Buttons
        ) {
            Button(
                onClick = { /* Action for Filter */ },
                modifier = Modifier
                    .weight(1f) // Gleichmäßiges Ausfüllen
                    .height(150.dp), // Höhe direkt auf den Button angewendet
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                shape = RectangleShape,
                elevation = ButtonDefaults.buttonElevation(0.dp)
            ) {
                Text(text = "FILTER", fontSize = 16.sp, color = Color.White)
            }
            Button(
                onClick = { /* Action for Einstellungen */ },
                modifier = Modifier
                    .weight(1f) // Gleichmäßiges Ausfüllen
                    .height(150.dp), // Höhe direkt auf den Button angewendet
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                shape = RectangleShape,
                elevation = ButtonDefaults.buttonElevation(0.dp)
            ) {
                Text(text = "EINSTELLUNGEN", fontSize = 16.sp, color = Color.White)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Footer Row für Lautsprecher und zukünftige Buttons
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Spacer(modifier = Modifier.weight(1f)) // Gleicher Abstand wie links bei den unteren Buttons
            Button(
                onClick = { /* Action for Lautsprecher */ },
                modifier = Modifier
                    .weight(1f) // Exakt gleiche Breite wie "Einstellungen"
                    .height(150.dp), // Exakt gleiche Höhe wie die anderen Buttons
                colors = ButtonDefaults.buttonColors(containerColor = Color.Blue),
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
}