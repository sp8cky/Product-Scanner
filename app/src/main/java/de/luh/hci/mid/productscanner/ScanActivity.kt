package de.luh.hci.mid.productscanner

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
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
import androidx.compose.ui.text.font.FontWeight
import de.luh.hci.mid.productscanner.ui.theme.ProductscannerTheme
import de.luh.hci.mid.productscanner.ui.theme.*

class ScanActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            CameraPreviewScreen(
                onBarcodeScanned = { barcode ->
                    // Wenn der Barcode gescannt wurde, weiter zur InfoActivity
                    //val intent = Intent(this, InfoActivity::class.java)
                    val intent = Intent(this, InfoActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
                    }
                    intent.putExtra("BARCODE_VALUE", barcode)
                    startActivity(intent)
                    finish()


                }
            )
        }
    }

    @OptIn(ExperimentalGetImage::class)
    @Composable
    fun CameraPreviewScreen(onBarcodeScanned: (String) -> Unit, modifier: Modifier = Modifier) {

        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(16.dp), // Dynamisches Padding für alle Geräte
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Text(
                text = "SCAN",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            // Kamera-Vorschau (nimmt flexiblen Platz ein)
            val context = LocalContext.current
            val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
            val scanner: BarcodeScanner = BarcodeScanning.getClient()
            var hasCameraPermission by remember { mutableStateOf(false) }

            val cameraPermissionLauncher = rememberLauncherForActivityResult(
                ActivityResultContracts.RequestPermission()
            ) { isGranted ->
                hasCameraPermission = isGranted
            }

            LaunchedEffect(Unit) {
                when {
                    ContextCompat.checkSelfPermission(
                        context, Manifest.permission.CAMERA
                    ) == PackageManager.PERMISSION_GRANTED -> {
                        hasCameraPermission = true
                    }

                    else -> {
                        cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                    }
                }
            }

            AndroidView(
                factory = { ctx ->
                    PreviewView(ctx).apply {
                        val cameraProvider = cameraProviderFuture.get()
                        val preview = androidx.camera.core.Preview.Builder().build()
                        val cameraSelector = androidx.camera.core.CameraSelector.DEFAULT_BACK_CAMERA
                        val imageAnalyzer = androidx.camera.core.ImageAnalysis.Builder()
                            .build()
                            .apply {
                                setAnalyzer(
                                    ContextCompat.getMainExecutor(ctx)
                                ) { imageProxy ->
                                    val image = InputImage.fromMediaImage(
                                        imageProxy.image!!,
                                        imageProxy.imageInfo.rotationDegrees
                                    )
                                    scanner.process(image)
                                        .addOnSuccessListener { barcodes ->
                                            for (barcode in barcodes) {
                                                barcode.rawValue?.let { value ->
                                                    onBarcodeScanned(value)
                                                }
                                            }
                                        }
                                        .addOnCompleteListener {
                                            imageProxy.close()
                                        }
                                }
                            }

                        cameraProvider.bindToLifecycle(
                            this@ScanActivity,
                            cameraSelector,
                            preview,
                            imageAnalyzer
                        )
                        preview.setSurfaceProvider(this.surfaceProvider)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f) // Dynamisch verfügbaren Platz einnehmen
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Untere Buttons (Kamera-Button)
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = { /* Action for Kamera */ },
                    modifier = Modifier
                        .weight(1f)
                        .height(60.dp), // Einheitliche Höhe der Buttons
                    colors = ButtonDefaults.buttonColors(containerColor = Red40),
                    shape = RectangleShape,
                    elevation = ButtonDefaults.buttonElevation(0.dp)
                ) {
                    Text(
                        text = "\uD83D\uDCF7",
                        fontSize = 18.sp,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Footer Row für Lautsprecher und zukünftige Buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = { /* Action for HomeButton */ },
                    modifier = Modifier
                        .weight(1f)
                        .height(60.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Red40),
                    shape = RectangleShape,
                    elevation = ButtonDefaults.buttonElevation(0.dp)
                ) {
                    Text(
                        text = "\uD83C\uDFE0",
                        fontSize = 24.sp,
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )
                }
                Button(
                    onClick = { /* Action for Lautsprecher */ },
                    modifier = Modifier
                        .weight(1f)
                        .height(60.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Blue40),
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
}