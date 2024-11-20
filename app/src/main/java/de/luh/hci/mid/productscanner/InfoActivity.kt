package de.luh.hci.mid.productscanner

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

class InfoActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val barcodeValue = intent.getStringExtra("BARCODE_VALUE") ?: "No barcode found"

        setContent {
            InfoScreen(barcodeValue)
        }
    }

    @Composable
    fun InfoScreen(barcode: String) {
        Text(text = "Scanned Barcode: $barcode")
    }
}
