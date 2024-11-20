package de.luh.hci.mid.productscanner.ui.preview


import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.util.Size
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import de.luh.hci.mid.productscanner.ProductScanningApp
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

// ViewModel for the preview screen.
class PreviewViewModel(
    app: ProductScanningApp,
) : ViewModel() {

    // Camera operations are executed on a separate thread (because they are expensive).
    // An executor executes Runnable tasks on a specific thread.
    private val cameraExecutor: ExecutorService = Executors.newSingleThreadExecutor()

    // A ProcessCameraProvider is used to bind the lifecycle of a camera to a LifecycleOwner.
    private val cameraProviderFuture = ProcessCameraProvider.getInstance(app)

    // The barcode scanning engine.
    private val barcodeScanner: BarcodeScanner

    var barcodeValue by mutableStateOf("")
        private set

    var barcodeFormat by mutableStateOf("")
        private set

    var barcodeType by mutableStateOf("")
        private set

    // Observable string for message output.
    var message by mutableStateOf<String?>(null)
        private set

    init {
        log("init")
        val options = BarcodeScannerOptions.Builder()
            .setBarcodeFormats(
                Barcode.FORMAT_QR_CODE,
                Barcode.FORMAT_EAN_13, Barcode.FORMAT_EAN_8,
                Barcode.FORMAT_UPC_A, Barcode.FORMAT_UPC_E
            )
            .build()
        barcodeScanner = BarcodeScanning.getClient(options)
    }

    fun setCamera(
        previewView: PreviewView,
        lifecycleOwner: LifecycleOwner,
        context: Context
    ) {
        cameraProviderFuture.addListener(
            {
                bindPreview(previewView, lifecycleOwner)
            },
            ContextCompat.getMainExecutor(context)
        )
    }

    private fun bindPreview(
        previewView: PreviewView,
        lifecycleOwner: LifecycleOwner
    ) {
        // The camera provider (one per Android process) binds the lifecycle of cameras
        // (open, started, stopped, closed) to the chosen lifecycle. Also allows
        // to specify "use cases" (e.g., preview, photo capture, image analysis).
        val cameraProvider = cameraProviderFuture.get()

        // Use case: preview (live viewfinder)
        val preview: Preview = Preview.Builder().build()
        // where to show the preview
        preview.surfaceProvider = previewView.surfaceProvider

        // Use case: barcode scanning
        val ba = BarcodeAnalyzer()
        val barcodeAnalysis = ImageAnalysis.Builder()
            //.setTargetResolution(Size(480, 640))
            .setTargetResolution(Size(768, 1024))
            .build()
        barcodeAnalysis.setAnalyzer(cameraExecutor, ba)

        // Select the back camera
        val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

        try {
            // Unbind existing use cases (if any), before rebinding
            cameraProvider.unbindAll()

            // Bind the specified use cases of the camera to the provided lifecycle
            cameraProvider.bindToLifecycle(
                lifecycleOwner, cameraSelector, preview, barcodeAnalysis
            )
        } catch (ex: Exception) {
            log("Use case binding failed: $ex")
        }
    }


    private inner class BarcodeAnalyzer : ImageAnalysis.Analyzer {
        @SuppressLint("UnsafeOptInUsageError")
        override fun analyze(imageProxy: ImageProxy) {
            val mediaImage = imageProxy.image
            if (mediaImage != null) {
                val image: InputImage =
                    InputImage.fromMediaImage(
                        mediaImage,
                        imageProxy.imageInfo.rotationDegrees
                    )
                barcodeScanner.process(image)
                    .addOnSuccessListener { barcodes: List<Barcode> ->
                        for (bc: Barcode in barcodes) {
                            val value = bc.displayValue ?: "(null)"
                            val format = when (bc.format) {
                                Barcode.FORMAT_QR_CODE -> "QR_CODE"
                                Barcode.FORMAT_EAN_13 -> "EAN_13"
                                Barcode.FORMAT_EAN_8 -> "EAN_8"
                                Barcode.FORMAT_UPC_A -> "UPC_A"
                                Barcode.FORMAT_UPC_E -> "UPC_E"
                                else -> "unknown format"
                            }
                            val valueType = when (bc.valueType) {
                                Barcode.TYPE_PRODUCT -> "product"
                                Barcode.TYPE_ISBN -> "ISBN"
                                Barcode.TYPE_URL -> "URL"
                                Barcode.TYPE_TEXT -> "text"
                                else -> "unknown type"
                            }
                            log("barcode: $value $format, $valueType")
                            if (value.isNotEmpty()) {
                                barcodeValue = value
                                barcodeFormat = format
                                barcodeType = valueType
                            }
                        }
                        // it is very important to close the image
                        // once this image is closed, the next one will be delivered
                        imageProxy.close()
                    }
                    .addOnFailureListener { ex: Exception ->
                        Log.d(javaClass.simpleName, "BarcodeScanner failed", ex)
                        // it is very important to close the image
                        // once this image is closed, the next one will be delivered
                        imageProxy.close()
                    }
            } else {
                imageProxy.close()
            }
        }
    }


    // Called by the view to indicate that the message has been shown.
    fun messageShown() {
        message = null
    }

    // Called when this ViewModel is no longer used and will be destroyed. Can be used for cleanup.
    override fun onCleared() {
        log("onCleared")
        barcodeScanner.close()
        cameraExecutor.shutdown()
    }

    // Logs a debug message.
    private fun log(msg: String) {
        Log.d(javaClass.simpleName, msg)
    }

    companion object {
        // Companion object for creating the view model in the right lifecycle scope.
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as ProductScanningApp
                PreviewViewModel(app)
            }
        }
    }

}
