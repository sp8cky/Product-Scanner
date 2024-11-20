package de.luh.hci.mid.productscanner.ui.preview

import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.LinearLayout
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView

@Composable
fun PreviewScreen(
    viewModel: PreviewViewModel,
) {
    // used to show a message
    val snackbarHostState = remember {
        SnackbarHostState()
    }

    // If message is not null, then show it.
    // https://developer.android.com/topic/architecture/ui-layer/events#compose_3
    viewModel.message?.let { message ->
        LaunchedEffect(message) {
            snackbarHostState.showSnackbar(message)
            viewModel.messageShown()
        }
    }

    val lifecycleOwner = LocalLifecycleOwner.current

    // Provides a "scaffold" ("Gerüst") for typical parts of an application screen,
    // such as a floating action button, a bottom bar, and the main content.
    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        modifier = Modifier.fillMaxSize(),
//        floatingActionButton = {
//            FloatingActionButton(onClick = { }) {
//                Icon(
//                    imageVector = Icons.Default.Check,
//                    contentDescription = "Take Photo"
//                )
//            }
//        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Composable function "AndroidView" wraps an android.view.View. Here, it wraps the
            // PreviewView, which is a custom view that displays the camera's live preview.
            AndroidView(
                factory = { context ->
                    PreviewView(context).apply {
                        setBackgroundColor(android.graphics.Color.WHITE)
                        layoutParams = LinearLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT)
                        scaleType = PreviewView.ScaleType.FILL_START
                        implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                        viewModel.setCamera(this, lifecycleOwner, context)
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
            // The content of the column is shown on top of the camera preview in the background.
            Column(
                horizontalAlignment = Alignment.Start,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp)
            ) {
                Text(
                    text = "Barcode: ${viewModel.barcodeValue}",
                    modifier = Modifier
                        .background(Color(0x77FFFFFF)) // semitransparent white background
                        .padding(8.dp),
                    fontSize = 24.sp
                )
                Text(
                    text = "${viewModel.barcodeFormat} ${viewModel.barcodeType}",
                    modifier = Modifier
                        .background(Color(0x77FFFFFF)) // semitransparent white background
                        .padding(8.dp),
                    fontSize = 16.sp
                )
            }
        }
    }
}
