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
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.google.common.util.concurrent.ListenableFuture
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import de.luh.hci.mid.productscanner.ui.navigationbar.BottomNavigationBar
import de.luh.hci.mid.productscanner.ui.navigationbar.TTSContentProvider
import de.luh.hci.mid.productscanner.ui.navigationbar.TopNavigationBar
import java.io.File

class ScanActivity : ComponentActivity(), TTSContentProvider {
    private var isInfoActivityOpened = false // Flag, um doppelte Navigation zu vermeiden
    private lateinit var cameraProvider: ProcessCameraProvider

    override fun getTTSContent(): String {
        return "Richten Sie die Kamera auf einen Barcode, um ihn automatisch zu scannen. Alternativ können Sie ein Foto aufnehmen, um zusätzliche Informationen aus dem Bild zu extrahieren."
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            ScanScreen(
                onBarcodeScanned = { barcode ->
                    if (!isInfoActivityOpened) {
                        isInfoActivityOpened = true
                        releaseCamera()
                        val intent = Intent(this, BarcodeInfoActivity::class.java).apply {
                            putExtra("BARCODE_VALUE", barcode)
                            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                        startActivity(intent)
                        finish()
                    }
                },
                onImageCaptured = { imagePath ->
                    if (!isInfoActivityOpened) {
                        isInfoActivityOpened = true
                        releaseCamera()
                        val intent = Intent(this, ImageInfoActivity::class.java).apply {
                            putExtra("IMAGE_PATH", imagePath)
                            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                        startActivity(intent)
                        finish()
                    }
                }
            )
        }

        SoundManager.initialize(this)
    }

    private fun releaseCamera() {
        ProcessCameraProvider.getInstance(this).get().unbindAll()
    }
}


@OptIn(ExperimentalGetImage::class)
@Composable
fun ScanScreen(
    onBarcodeScanned: (String) -> Unit,
    onImageCaptured: (String) -> Unit
) {
    val context = LocalContext.current
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    val scanner: BarcodeScanner = BarcodeScanning.getClient()
    var hasCameraPermission by remember { mutableStateOf(false) }
    var imageCapture: ImageCapture? by remember { mutableStateOf(null) }

    // Permission Launcher
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted -> hasCameraPermission = isGranted }

    LaunchedEffect(Unit) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        } else {
            hasCameraPermission = true
        }
    }

    Scaffold(
        topBar = { TopNavigationBar(title = "Scan") },
        bottomBar = { BottomNavigationBar(navController = null, ttsContentProvider = LocalContext.current as TTSContentProvider) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Kamera-Preview
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f), // Dynamische Größe für die Kamera-Ansicht
                contentAlignment = Alignment.Center
            ) {
                if (hasCameraPermission) {
                    CameraPreview(
                        cameraProviderFuture = cameraProviderFuture,
                        scanner = scanner,
                        onBarcodeScanned = onBarcodeScanned,
                        onImageCaptureReady = { imageCapture = it } // ImageCapture-Objekt abrufen
                    )
                } else {
                    Text(
                        text = "Kameraerlaubnis erforderlich",
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(120.dp)) // Platzhalter unter der Kamera

            // Kamera-Button
            Button(
                onClick = {
                    imageCapture?.let { capture ->
                        val photoFile = File(context.filesDir, "captured_image.jpg")
                        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

                        capture.takePicture(
                            outputOptions,
                            ContextCompat.getMainExecutor(context),
                            object : ImageCapture.OnImageSavedCallback {
                                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                                    onImageCaptured(photoFile.absolutePath) // Aufruf der Callback-Funktion
                                }

                                override fun onError(exception: ImageCaptureException) {
                                    exception.printStackTrace()
                                }
                            }
                        )
                    }
                    SoundManager.playSound("tap")
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .height(60.dp), // Feste Höhe für den Button
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                shape = MaterialTheme.shapes.medium
            ) {
                Text(
                    text = "\uD83D\uDCF7", // Kamerasymbol
                    fontSize = 24.sp,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    }
}

@OptIn(ExperimentalGetImage::class)
@Composable
fun CameraPreview(
    cameraProviderFuture: ListenableFuture<ProcessCameraProvider>,
    scanner: BarcodeScanner,
    onBarcodeScanned: (String) -> Unit,
    onImageCaptureReady: (ImageCapture) -> Unit
) {
    val context = LocalContext.current
    var cameraProvider by remember { mutableStateOf<ProcessCameraProvider?>(null) }

    LaunchedEffect(cameraProviderFuture) {
        cameraProvider = cameraProviderFuture.get()
    }

    if (cameraProvider != null) {
        AndroidView(
            factory = { ctx ->
                PreviewView(ctx).apply {
                    val preview = Preview.Builder().build()
                    val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
                    val imageCapture = ImageCapture.Builder().build()
                    onImageCaptureReady(imageCapture)

                    val imageAnalyzer = ImageAnalysis.Builder().build().apply {
                        setAnalyzer(ContextCompat.getMainExecutor(ctx)) { imageProxy ->
                            val image = InputImage.fromMediaImage(
                                imageProxy.image!!,
                                imageProxy.imageInfo.rotationDegrees
                            )
                            scanner.process(image)
                                .addOnSuccessListener { barcodes ->
                                    for (barcode in barcodes) {
                                        barcode.rawValue?.let { value ->
                                            imageProxy.close()
                                            SoundManager.playSound("success")
                                            onBarcodeScanned(value)
                                            return@addOnSuccessListener
                                        }
                                    }
                                    imageProxy.close()
                                }
                                .addOnFailureListener {
                                    imageProxy.close()
                                }
                        }
                    }

                    cameraProvider!!.bindToLifecycle(
                        context as LifecycleOwner,
                        cameraSelector,
                        preview,
                        imageAnalyzer,
                        imageCapture
                    )

                    preview.setSurfaceProvider(this.surfaceProvider)
                }
            },
            modifier = Modifier
                .fillMaxSize()
        )
    } else {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    }
}
