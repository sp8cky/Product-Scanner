package de.luh.hci.mid.productscanner

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import de.luh.hci.mid.productscanner.ui.theme.ProductscannerTheme
import de.luh.hci.mid.productscanner.ui.navigationbar.BottomNavigationBar
import de.luh.hci.mid.productscanner.ui.navigationbar.TTSContentProvider
import de.luh.hci.mid.productscanner.ui.navigationbar.TopNavigationBar

class MainActivity : ComponentActivity(), TTSContentProvider {
    override fun getTTSContent(): String {
        return "Willkommen auf der Startseite. Bitte wählen Sie eine Aktion aus."
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ProductscannerTheme {
                MainScreen()
            }
        }
        SoundManager.initialize(this)

    }

}

@Composable
fun MainScreen() {
    val navController = rememberNavController()

    Scaffold(
        topBar = {
            val currentRoute = currentRoute(navController)
            TopNavigationBar(title = when (currentRoute) {
                "home" -> "Home"
                "tts" -> "TTS"
                "scan" -> "Scan"
                else -> "App"
            })
        },

        bottomBar = { BottomNavigationBar(navController = null, ttsContentProvider = LocalContext.current as TTSContentProvider) }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("home") { HomeScreen(navController) }
            composable("tts") { TTSScreen() }
            composable("scan") { ScanScreen(navController) }
        }
    }
}

@Composable
fun HomeScreen(navController: NavController) {
    val context = LocalContext.current // Kontext wird benötigt, um die Activity zu starten

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp) // Seitliche Ränder für ein konsistentes Layout
    ) {
        // Scan Button (größer in der Höhe)
        Button(
            onClick = {
                // Startet die ScanActivity

                val intent = Intent(context, ScanActivity::class.java)
                SoundManager.playSound("tap")
                context.startActivity(intent)
            },
            modifier = Modifier
                .fillMaxWidth()
                .weight(1.2f)
                .border(2.dp, Color.Black, shape = RectangleShape), // Schwarze Umrandung
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF007BFF)), // Blau für primäre Aktion
            shape = RectangleShape
        ) {
            Text(
                text = "SCAN",
                fontSize = 48.sp,
                color = Color.White,
                textAlign = TextAlign.Center
            )
        }

        // Spacer zwischen Scan-Button und erster Reihe
        Spacer(modifier = Modifier.height(8.dp)) // Lücke zwischen Scan und der nächsten Reihe

        // Erste Reihe: Verlauf und Einkaufsliste
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f), // Nutzt weniger Platz als der Scan-Button
            horizontalArrangement = Arrangement.spacedBy(10.dp) // Gleichmäßiger Abstand zwischen Buttons
        ) {
            Button(
                onClick = {

                    val intent = Intent(context, DatabaseActivity::class.java)
                    SoundManager.playSound("tap")
                    context.startActivity(intent)
                },
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(1f)
                    .border(2.dp, Color.Black),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD97706)),
                shape = RectangleShape
            ) {
                Text(
                    text = "VERLAUF",
                    fontSize = 22.sp,
                    color = Color.Black, // Weißer Text für maximalen Kontrast
                    textAlign = TextAlign.Center
                )
            }

            Button(
                onClick = {
                    val intent = Intent(context, EinkaufslisteActivity::class.java)
                    SoundManager.playSound("tap")
                    context.startActivity(intent)
                },
                modifier = Modifier.fillMaxHeight().weight(1f).border(2.dp, Color.Black),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD97706)),
                shape = RectangleShape
            ) {
                Text(
                    text = "EINKAUFS\nLISTE",
                    fontSize = 22.sp,
                    color = Color.Black,
                    textAlign = TextAlign.Center
                )
            }
        }

        // Spacer zwischen der ersten und zweiten Reihe
        Spacer(modifier = Modifier.height(8.dp)) // Lücke zwischen erster und zweiter Reihe

        // Zweite Reihe: Filter und Einstellungen
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f), // Nutzt weniger Platz als der Scan-Button
            horizontalArrangement = Arrangement.spacedBy(10.dp) // Gleichmäßiger Abstand zwischen Buttons
        ) {
            Button(
                onClick = {
                    val intent = Intent(context, FilterActivity::class.java)
                    context.startActivity(intent)
                    SoundManager.playSound("tap")
                },
                modifier = Modifier.fillMaxHeight().weight(1f).border(2.dp, Color.Black),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF228B22)),
                shape = RectangleShape
            ) {
                Text(
                    text = "FILTER",
                    fontSize = 22.sp,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
            }

            Button(
                onClick = {
                    val intent = Intent(context, SettingsActivity::class.java)
                    context.startActivity(intent)
                    SoundManager.playSound("tap")
                },
                modifier = Modifier.fillMaxHeight().weight(1f).border(2.dp, Color.Black),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF228B22)),
                shape = RectangleShape
            ) {
                Text(
                    text = "EINSTELLUNGEN",
                    fontSize = 22.sp,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}


@Composable
fun TTSScreen() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = "TTS-Bildschirm", fontSize = 24.sp, textAlign = TextAlign.Center)
    }
}

@Composable
fun ScanScreen(navController: NavController) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Barcode-Scanner wird hier integriert",
                fontSize = 18.sp,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(20.dp))
            Button(
                onClick = {
                    navController.navigate("home")
                },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text(text = "Zurück zu Home", color = MaterialTheme.colorScheme.onError)
            }
        }
    }
}

@Composable
fun currentRoute(navController: NavController): String? {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    return navBackStackEntry?.destination?.route
}
