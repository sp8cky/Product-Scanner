package de.luh.hci.mid.productscanner.ui.navigationbar

import android.content.Intent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import de.luh.hci.mid.productscanner.MainActivity
import de.luh.hci.mid.productscanner.currentRoute

@Composable
fun BottomNavigationBar(
    navController: NavController? = null, // Optionales NavController
    onHomeClicked: (() -> Unit)? = null // Optionaler Callback für den Home-Button
) {
    val context = LocalContext.current
    val items = listOf(
        NavItem("home", "Home", "🏠", MaterialTheme.colorScheme.primary),
        NavItem("tts", "TTS", "🔊", MaterialTheme.colorScheme.primary)
    )

    NavigationBar(
        containerColor = MaterialTheme.colorScheme.background,
        tonalElevation = 8.dp
    ) {
        val currentRoute = navController?.let { currentRoute(it) } ?: "home" // Fallback auf "home"
        items.forEach { item ->
            NavigationBarItem(
                selected = currentRoute == item.route,
                onClick = {
                    if (item.route == "home") {
                        if (navController == null) {
                            // Wenn kein NavController vorhanden, starte MainActivity
                            onHomeClicked?.invoke() ?: run {
                                val intent = Intent(context, MainActivity::class.java)
                                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                                context.startActivity(intent)
                            }
                        } else {
                            // Navigiere im NavController
                            navController.navigate("home") {
                                popUpTo(navController.graph.startDestinationId) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    } else {
                        // Navigiere zu anderen Zielen
                        navController?.navigate(item.route)
                    }
                },
                icon = {
                    Text(
                        text = item.icon,
                        fontSize = 36.sp,
                        color = if (currentRoute == item.route) item.color else MaterialTheme.colorScheme.onBackground
                    )
                },
                label = {
                    Text(
                        text = item.label,
                        fontSize = 14.sp,
                        color = if (currentRoute == item.route) item.color else MaterialTheme.colorScheme.onBackground
                    )
                },
                alwaysShowLabel = true
            )
        }
    }
}
