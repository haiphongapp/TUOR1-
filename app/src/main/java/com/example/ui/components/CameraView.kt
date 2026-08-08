package com.example.ui.components

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import android.view.ViewGroup
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.FallbackStrategy
import androidx.camera.video.FileOutputOptions
import androidx.camera.video.Quality
import androidx.camera.video.QualitySelector
import androidx.camera.video.Recorder
import androidx.camera.video.Recording
import androidx.camera.video.VideoCapture
import androidx.camera.video.VideoRecordEvent
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.ui.CameraState
import com.example.util.BarcodeAnalyzer
import java.io.File
import java.util.concurrent.Executors

@Composable
fun CameraView(
    cameraState: CameraState,
    torchEnabled: Boolean,
    isFrontCamera: Boolean,
    onBarcodeScanned: (String) -> Unit,
    onVideoFileSaved: (File, Boolean) -> Unit,
    modifier: Modifier = Modifier,
    onVideoCaptureReady: ((File) -> Unit, () -> Unit) -> Unit = { _, _ -> }
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val executor = remember { Executors.newSingleThreadExecutor() }
    val mainExecutor = remember { ContextCompat.getMainExecutor(context) }

    var cameraProvider by remember { mutableStateOf<ProcessCameraProvider?>(null) }
    var camera by remember { mutableStateOf<Camera?>(null) }
    var activeRecording by remember { mutableStateOf<Recording?>(null) }

    val barcodeAnalyzer = remember {
        BarcodeAnalyzer { code ->
            onBarcodeScanned(code)
        }
    }

    // Enable / disable scanning based on state
    LaunchedEffect(cameraState) {
        barcodeAnalyzer.setScanningEnabled(cameraState == CameraState.SCANNING)
    }

    // Toggle Torch / Flashlight
    LaunchedEffect(torchEnabled, camera) {
        camera?.cameraControl?.enableTorch(torchEnabled)
    }

    val previewView = remember {
        PreviewView(context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            scaleType = PreviewView.ScaleType.FILL_CENTER
        }
    }

    LaunchedEffect(isFrontCamera) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener({
            try {
                val provider = cameraProviderFuture.get()
                cameraProvider = provider

                val preview = Preview.Builder().build().also {
                    it.surfaceProvider = previewView.surfaceProvider
                }

                val cameraSelector = if (isFrontCamera) {
                    CameraSelector.DEFAULT_FRONT_CAMERA
                } else {
                    CameraSelector.DEFAULT_BACK_CAMERA
                }

                val imageAnalysis = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()
                    .also {
                        it.setAnalyzer(executor, barcodeAnalyzer)
                    }

                val recorder = Recorder.Builder()
                    .setQualitySelector(
                        QualitySelector.from(
                            Quality.SD,
                            FallbackStrategy.lowerQualityOrHigherThan(Quality.SD)
                        )
                    )
                    .build()
                val videoCapture = VideoCapture.withOutput(recorder)

                provider.unbindAll()
                val boundCamera = try {
                    provider.bindToLifecycle(
                        lifecycleOwner,
                        cameraSelector,
                        preview,
                        imageAnalysis,
                        videoCapture
                    )
                } catch (e: Exception) {
                    Log.w("CameraView", "Failed to bind with ImageAnalysis, falling back to preview + videoCapture", e)
                    provider.unbindAll()
                    provider.bindToLifecycle(
                        lifecycleOwner,
                        cameraSelector,
                        preview,
                        videoCapture
                    )
                }
                camera = boundCamera

                // Expose start/stop functions
                onVideoCaptureReady(
                    { outputFile ->
                        val outputOptions = FileOutputOptions.Builder(outputFile).build()
                        val pendingRecording = videoCapture.output.prepareRecording(context, outputOptions)

                        if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                            pendingRecording.withAudioEnabled()
                        }

                        activeRecording = pendingRecording.start(mainExecutor) { event ->
                            if (event is VideoRecordEvent.Finalize) {
                                val success = !event.hasError()
                                onVideoFileSaved(outputFile, success)
                                activeRecording = null
                            }
                        }
                    },
                    {
                        try {
                            activeRecording?.stop()
                        } catch (e: Exception) {
                            Log.e("CameraView", "Error stopping active recording", e)
                        }
                        activeRecording = null
                    }
                )

            } catch (e: Exception) {
                Log.e("CameraView", "Camera binding failed", e)
            }
        }, mainExecutor)
    }

    DisposableEffect(Unit) {
        onDispose {
            try {
                activeRecording?.stop()
            } catch (e: Exception) {
                Log.e("CameraView", "Error stopping recording on dispose", e)
            }
            activeRecording = null
            try {
                cameraProvider?.unbindAll()
            } catch (e: Exception) {
                Log.e("CameraView", "Error unbinding camera", e)
            }
            executor.shutdown()
        }
    }

    AndroidView(
        factory = { previewView },
        modifier = modifier.fillMaxSize()
    )
}
