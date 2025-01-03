package de.luh.hci.mid.productscanner.ui.navigationbar

import androidx.compose.ui.graphics.Color

data class NavItem(
    val route: String,
    val label: String,
    val icon: String, // Unicode-Symbol als String
    val color: Color
)
