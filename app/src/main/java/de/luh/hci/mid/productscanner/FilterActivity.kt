package de.luh.hci.mid.productscanner

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
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
import de.luh.hci.mid.productscanner.ui.navigationbar.BottomNavigationBar
import de.luh.hci.mid.productscanner.ui.navigationbar.TopNavigationBar
import de.luh.hci.mid.productscanner.ui.theme.Blue40
import de.luh.hci.mid.productscanner.ui.theme.Red40

class FilterActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            FilterScreen(
                onHomeClicked = { finish() }, // Zurück zur vorherigen Aktivität
                onSpeakerClicked = { /* Action for Lautsprecher */ }
            )
        }
    }

    @Composable
    fun FilterScreen(
        onHomeClicked: () -> Unit,
        onSpeakerClicked: () -> Unit,
        modifier: Modifier = Modifier
    ) {
        Scaffold(
            topBar = { TopNavigationBar(title = "Filter") },
            bottomBar = { BottomNavigationBar(navController = null) }
        ) { paddingValues ->
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Liste mit Filtern und Toggles
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    SettingItem(label = "Filter 1")
                    SettingItem(label = "Filter 2")
                    SettingItem(label = "Filter 3")
                }
            }
        }
    }

    @Composable
    fun SettingItem(label: String) {
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Black
            )

            val isChecked = remember { mutableStateOf(false) }

            Switch(
                checked = isChecked.value,
                onCheckedChange = { isChecked.value = it },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Blue40,
                    uncheckedThumbColor = Color.Gray
                )
            )
        }
    }
}
