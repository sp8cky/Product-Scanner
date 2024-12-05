package de.luh.hci.mid.productscanner

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage

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

    @Composable
    fun CameraPreviewScreen(onBarcodeScanned: (String) -> Unit) {
        val context = LocalContext.current
        val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
        val scanner: BarcodeScanner = BarcodeScanning.getClient()
        var hasCameraPermission by remember { mutableStateOf(false) }

        // Launcher zur Anforderung von Kamera-Berechtigungen
        val cameraPermissionLauncher = rememberLauncherForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            hasCameraPermission = isGranted
        }

        // Kamera-Berechtigung überprüfen
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

        if (hasCameraPermission) {
            AndroidView(
                factory = { ctx ->
                    PreviewView(ctx).apply {
                        val cameraProvider = cameraProviderFuture.get()
                        // Kameravorschau einrichten
                        val preview = androidx.camera.core.Preview.Builder().build()
                        val cameraSelector = androidx.camera.core.CameraSelector.DEFAULT_BACK_CAMERA
                        val imageAnalyzer = androidx.camera.core.ImageAnalysis.Builder()
                            .build()
                            .apply {
                                setAnalyzer(
                                    ContextCompat.getMainExecutor(ctx),
                                    { imageProxy ->
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
                                )
                            }

                        cameraProvider.bindToLifecycle(
                            this@ScanActivity,
                            cameraSelector,
                            preview,
                            imageAnalyzer
                        )
                        preview.setSurfaceProvider(this.surfaceProvider)
                    }
                }
            )
        }
    }
}
