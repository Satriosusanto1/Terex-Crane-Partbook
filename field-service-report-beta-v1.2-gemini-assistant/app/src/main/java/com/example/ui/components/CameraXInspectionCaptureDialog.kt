package com.example.ui.components

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.Camera
import androidx.camera.core.CameraControl
import androidx.camera.core.CameraInfo
import androidx.camera.core.CameraSelector
import androidx.camera.core.FocusMeteringAction
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FlashAuto
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.Highlight
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import coil.compose.AsyncImage
import com.example.ui.theme.BentoPrimary
import com.example.ui.theme.IndustrialAmber
import com.example.ui.theme.SafetyGreen
import com.example.ui.theme.SafetyRed
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.util.ImageUtils
import java.io.File
import java.util.concurrent.Executors

@Composable
fun CameraXInspectionCaptureDialog(
    inspectionTitle: String,
    onDismissRequest: () -> Unit,
    onPhotoCaptured: (savedUri: String) -> Unit,
    tagPrefix: String = "camerax"
) {
    val context = LocalContext.current
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
        if (!isGranted) {
            Toast.makeText(context, "Izin kamera diperlukan untuk mengambil foto inspeksi", Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            if (hasCameraPermission) {
                CameraXCaptureContent(
                    inspectionTitle = inspectionTitle,
                    tagPrefix = tagPrefix,
                    onDismissRequest = onDismissRequest,
                    onPhotoConfirmed = { uri ->
                        val finalUri = ImageUtils.saveUriToInternalStorage(context, uri, tagPrefix)
                        if (finalUri != null) {
                            onPhotoCaptured(finalUri)
                        } else {
                            onPhotoCaptured(uri.toString())
                        }
                    }
                )
            } else {
                CameraPermissionRequestContent(
                    onRequestPermission = {
                        permissionLauncher.launch(Manifest.permission.CAMERA)
                    },
                    onDismissRequest = onDismissRequest
                )
            }
        }
    }
}

@Composable
private fun CameraPermissionRequestContent(
    onRequestPermission: () -> Unit,
    onDismissRequest: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            shape = CircleShape,
            color = Color(0xFF2A233A),
            modifier = Modifier.size(72.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    Icons.Default.Security,
                    contentDescription = null,
                    tint = BentoPrimary,
                    modifier = Modifier.size(36.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            text = "Izin Kamera Dibutuhkan",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = "Aplikasi memerlukan akses kamera untuk mengambil foto dokumentasi inspeksi alat berat berkualitas tinggi dengan CameraX.",
            style = MaterialTheme.typography.bodyMedium,
            color = Slate400,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        Spacer(modifier = Modifier.height(28.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onDismissRequest,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
            ) {
                Text("Batal")
            }
            Button(
                onClick = onRequestPermission,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BentoPrimary)
            ) {
                Text("Izinkan Kamera", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun CameraXCaptureContent(
    inspectionTitle: String,
    tagPrefix: String,
    onDismissRequest: () -> Unit,
    onPhotoConfirmed: (Uri) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var lensFacing by remember { mutableStateOf(CameraSelector.LENS_FACING_BACK) }
    var flashMode by remember { mutableStateOf(ImageCapture.FLASH_MODE_AUTO) }
    var isTorchEnabled by remember { mutableStateOf(false) }
    var showGrid by remember { mutableStateOf(true) }
    var zoomRatio by remember { mutableFloatStateOf(1f) }
    var maxZoomRatio by remember { mutableFloatStateOf(8f) }

    var isCapturing by remember { mutableStateOf(false) }
    var capturedPhotoUri by remember { mutableStateOf<Uri?>(null) }
    var capturedTempFile by remember { mutableStateOf<File?>(null) }

    var cameraControl by remember { mutableStateOf<CameraControl?>(null) }
    var cameraInfo by remember { mutableStateOf<CameraInfo?>(null) }
    var imageCapture by remember { mutableStateOf<ImageCapture?>(null) }
    var previewViewInstance by remember { mutableStateOf<PreviewView?>(null) }

    var focusPoint by remember { mutableStateOf<Offset?>(null) }
    var showFocusRing by remember { mutableStateOf(false) }

    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }

    DisposableEffect(lifecycleOwner) {
        onDispose {
            try {
                val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
                if (cameraProviderFuture.isDone) {
                    cameraProviderFuture.get().unbindAll()
                }
            } catch (e: Exception) {
                // ignore
            }
            cameraExecutor.shutdown()
        }
    }

    // Main Camera View or Captured Photo Review
    if (capturedPhotoUri != null) {
        PhotoReviewScreen(
            photoUri = capturedPhotoUri!!,
            inspectionTitle = inspectionTitle,
            onRetake = {
                capturedTempFile?.delete()
                capturedPhotoUri = null
                capturedTempFile = null
            },
            onConfirm = {
                onPhotoConfirmed(capturedPhotoUri!!)
            }
        )
    } else {
        Box(modifier = Modifier.fillMaxSize()) {
            // Live Camera Viewfinder
            AndroidView(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectTransformGestures { _, _, zoom, _ ->
                            val currentZoom = zoomRatio
                            val newZoom = (currentZoom * zoom).coerceIn(1f, maxZoomRatio)
                            zoomRatio = newZoom
                            cameraControl?.setZoomRatio(newZoom)
                        }
                    }
                    .pointerInput(Unit) {
                        detectTapGestures { offset ->
                            focusPoint = offset
                            showFocusRing = true
                            previewViewInstance?.let { pView ->
                                val factory = pView.meteringPointFactory
                                val point = factory.createPoint(offset.x, offset.y)
                                val action = FocusMeteringAction.Builder(point).build()
                                cameraControl?.startFocusAndMetering(action)
                            }
                        }
                    },
                factory = { ctx ->
                    PreviewView(ctx).apply {
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                        implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                        scaleType = PreviewView.ScaleType.FILL_CENTER
                        previewViewInstance = this

                        val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                        cameraProviderFuture.addListener({
                            val cameraProvider = cameraProviderFuture.get()

                            val preview = Preview.Builder()
                                .build()
                                .also {
                                    it.setSurfaceProvider(this.surfaceProvider)
                                }

                            val capture = ImageCapture.Builder()
                                .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
                                .setFlashMode(flashMode)
                                .build()

                            imageCapture = capture

                            val cameraSelector = CameraSelector.Builder()
                                .requireLensFacing(lensFacing)
                                .build()

                            try {
                                cameraProvider.unbindAll()
                                val camera = cameraProvider.bindToLifecycle(
                                    lifecycleOwner,
                                    cameraSelector,
                                    preview,
                                    capture
                                )
                                cameraControl = camera.cameraControl
                                cameraInfo = camera.cameraInfo
                                camera.cameraInfo.zoomState.observe(lifecycleOwner) { zoomState ->
                                    zoomState?.let {
                                        zoomRatio = it.zoomRatio
                                        maxZoomRatio = it.maxZoomRatio.coerceAtMost(8f)
                                    }
                                }
                            } catch (exc: Exception) {
                                exc.printStackTrace()
                            }
                        }, ContextCompat.getMainExecutor(ctx))
                    }
                },
                update = { pView ->
                    previewViewInstance = pView
                    // Update flash & torch settings
                    imageCapture?.flashMode = flashMode
                    cameraControl?.enableTorch(isTorchEnabled)
                }
            )

            // Inspection Grid Overlay
            if (showGrid) {
                InspectionGridOverlay()
            }

            // Tap Focus Indicator Ring
            if (showFocusRing && focusPoint != null) {
                FocusRingIndicator(
                    offset = focusPoint!!,
                    onDismiss = { showFocusRing = false }
                )
            }

            // Top Control Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Close button
                FilledIconButton(
                    onClick = onDismissRequest,
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = Color.Black.copy(alpha = 0.5f),
                        contentColor = Color.White
                    ),
                    modifier = Modifier.size(42.dp)
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Tutup Kamera")
                }

                // Title Pill
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color.Black.copy(alpha = 0.6f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.2f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(IndustrialAmber)
                        )
                        Text(
                            text = inspectionTitle,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }

                // Right action buttons (Flash, Torch, Grid)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Flash Mode Toggle
                    FilledIconButton(
                        onClick = {
                            flashMode = when (flashMode) {
                                ImageCapture.FLASH_MODE_AUTO -> ImageCapture.FLASH_MODE_ON
                                ImageCapture.FLASH_MODE_ON -> ImageCapture.FLASH_MODE_OFF
                                else -> ImageCapture.FLASH_MODE_AUTO
                            }
                            imageCapture?.flashMode = flashMode
                        },
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = Color.Black.copy(alpha = 0.5f),
                            contentColor = when (flashMode) {
                                ImageCapture.FLASH_MODE_ON -> IndustrialAmber
                                ImageCapture.FLASH_MODE_AUTO -> BentoPrimary
                                else -> Color.White
                            }
                        ),
                        modifier = Modifier.size(42.dp)
                    ) {
                        Icon(
                            imageVector = when (flashMode) {
                                ImageCapture.FLASH_MODE_ON -> Icons.Default.FlashOn
                                ImageCapture.FLASH_MODE_AUTO -> Icons.Default.FlashAuto
                                else -> Icons.Default.FlashOff
                            },
                            contentDescription = "Mode Flash"
                        )
                    }

                    // Torch (Senter) Toggle for dark engine bays
                    FilledIconButton(
                        onClick = {
                            isTorchEnabled = !isTorchEnabled
                            cameraControl?.enableTorch(isTorchEnabled)
                        },
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = if (isTorchEnabled) IndustrialAmber else Color.Black.copy(alpha = 0.5f),
                            contentColor = if (isTorchEnabled) Color.Black else Color.White
                        ),
                        modifier = Modifier.size(42.dp)
                    ) {
                        Icon(Icons.Default.Highlight, contentDescription = "Senter / Torch")
                    }

                    // Grid Toggle
                    FilledIconButton(
                        onClick = { showGrid = !showGrid },
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = if (showGrid) BentoPrimary else Color.Black.copy(alpha = 0.5f),
                            contentColor = Color.White
                        ),
                        modifier = Modifier.size(42.dp)
                    ) {
                        Icon(Icons.Default.GridOn, contentDescription = "Grid Inspeksi")
                    }
                }
            }

            // Bottom Shutter & Controls Panel
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(bottom = 56.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Zoom Quick Select Chips
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier
                        .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    listOf(1.0f, 2.0f, 4.0f).forEach { targetZoom ->
                        val isSelected = kotlin.math.abs(zoomRatio - targetZoom) < 0.3f
                        Surface(
                            shape = CircleShape,
                            color = if (isSelected) IndustrialAmber else Color.Transparent,
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape),
                            onClick = {
                                zoomRatio = targetZoom
                                cameraControl?.setZoomRatio(targetZoom)
                            }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = "${targetZoom.toInt()}x",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Color.Black else Color.White
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Shutter Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 32.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Empty left spacer for alignment
                    Spacer(modifier = Modifier.size(48.dp))

                    // Shutter Button
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .border(4.dp, Color.White, CircleShape)
                            .padding(6.dp)
                            .clip(CircleShape)
                            .background(if (isCapturing) IndustrialAmber else Color.White)
                            .testTag("${tagPrefix}_shutter_btn")
                            .pointerInput(isCapturing) {
                                if (!isCapturing) {
                                    detectTapGestures {
                                        val captureUseCase = imageCapture ?: return@detectTapGestures
                                        isCapturing = true

                                        val (tempUri, tempFile) = ImageUtils.createTempImageUri(context)
                                        val outputOptions = ImageCapture.OutputFileOptions.Builder(tempFile).build()

                                        captureUseCase.takePicture(
                                            outputOptions,
                                            cameraExecutor,
                                            object : ImageCapture.OnImageSavedCallback {
                                                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                                                    ContextCompat.getMainExecutor(context).execute {
                                                        isCapturing = false
                                                        capturedTempFile = tempFile
                                                        capturedPhotoUri = tempUri
                                                    }
                                                }

                                                override fun onError(exception: ImageCaptureException) {
                                                    ContextCompat.getMainExecutor(context).execute {
                                                        isCapturing = false
                                                        Toast.makeText(
                                                            context,
                                                            "Gagal mengambil foto: ${exception.message}",
                                                            Toast.LENGTH_SHORT
                                                        ).show()
                                                    }
                                                }
                                            }
                                        )
                                    }
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (isCapturing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(32.dp),
                                color = Color.Black,
                                strokeWidth = 3.dp
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(CircleShape)
                                    .background(Color.White)
                            )
                        }
                    }

                    // Switch Camera (Front/Back)
                    FilledIconButton(
                        onClick = {
                            lensFacing = if (lensFacing == CameraSelector.LENS_FACING_BACK) {
                                CameraSelector.LENS_FACING_FRONT
                            } else {
                                CameraSelector.LENS_FACING_BACK
                            }
                            val cameraProvider = ProcessCameraProvider.getInstance(context).get()
                            val preview = Preview.Builder().build().also {
                                it.setSurfaceProvider(previewViewInstance?.surfaceProvider)
                            }
                            val capture = ImageCapture.Builder()
                                .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
                                .setFlashMode(flashMode)
                                .build()
                            imageCapture = capture

                            try {
                                cameraProvider.unbindAll()
                                val camera = cameraProvider.bindToLifecycle(
                                    lifecycleOwner,
                                    CameraSelector.Builder().requireLensFacing(lensFacing).build(),
                                    preview,
                                    capture
                                )
                                cameraControl = camera.cameraControl
                                cameraInfo = camera.cameraInfo
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        },
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = Color.Black.copy(alpha = 0.5f),
                            contentColor = Color.White
                        ),
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(Icons.Default.Cameraswitch, contentDescription = "Ganti Kamera")
                    }
                }
            }
        }
    }
}

@Composable
private fun InspectionGridOverlay() {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height

        val gridColor = Color.White.copy(alpha = 0.25f)
        val strokeWidth = 1.dp.toPx()

        // Vertical lines (Rule of thirds)
        drawLine(
            color = gridColor,
            start = Offset(width / 3f, 0f),
            end = Offset(width / 3f, height),
            strokeWidth = strokeWidth
        )
        drawLine(
            color = gridColor,
            start = Offset(2 * width / 3f, 0f),
            end = Offset(2 * width / 3f, height),
            strokeWidth = strokeWidth
        )

        // Horizontal lines (Rule of thirds)
        drawLine(
            color = gridColor,
            start = Offset(0f, height / 3f),
            end = Offset(width, height / 3f),
            strokeWidth = strokeWidth
        )
        drawLine(
            color = gridColor,
            start = Offset(0f, 2 * height / 3f),
            end = Offset(width, 2 * height / 3f),
            strokeWidth = strokeWidth
        )

        // Center reticle (crosshair target for part inspection)
        val centerX = width / 2f
        val centerY = height / 2f
        val reticleSize = 24.dp.toPx()
        val reticleColor = IndustrialAmber.copy(alpha = 0.6f)

        drawLine(
            color = reticleColor,
            start = Offset(centerX - reticleSize, centerY),
            end = Offset(centerX + reticleSize, centerY),
            strokeWidth = 1.5.dp.toPx()
        )
        drawLine(
            color = reticleColor,
            start = Offset(centerX, centerY - reticleSize),
            end = Offset(centerX, centerY + reticleSize),
            strokeWidth = 1.5.dp.toPx()
        )
    }
}

@Composable
private fun FocusRingIndicator(
    offset: Offset,
    onDismiss: () -> Unit
) {
    LaunchedEffect(offset) {
        kotlinx.coroutines.delay(1200)
        onDismiss()
    }

    Box(
        modifier = Modifier
            .offset { IntOffset(offset.x.toInt() - 36, offset.y.toInt() - 36) }
            .size(72.dp)
            .border(2.dp, IndustrialAmber, RoundedCornerShape(12.dp))
    )
}

@Composable
private fun PhotoReviewScreen(
    photoUri: Uri,
    inspectionTitle: String,
    onRetake: () -> Unit,
    onConfirm: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F0E17))
    ) {
        // Photo Image Preview
        AsyncImage(
            model = photoUri,
            contentDescription = "Hasil Foto Inspeksi",
            contentScale = ContentScale.Fit,
            modifier = Modifier.fillMaxSize()
        )

        // Top Inspection Badge
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = Color.Black.copy(alpha = 0.7f),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.2f)),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding()
                .padding(top = 16.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Default.Check,
                    contentDescription = null,
                    tint = SafetyGreen,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = "Pratinjau Hasil Foto: $inspectionTitle",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }

        // Bottom Action Bar
        Surface(
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            color = Color.Black.copy(alpha = 0.85f),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 48.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Pastikan nomor seri, kondisi komponen, atau meteran terbaca jelas.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Slate200,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onRetake,
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color.White
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp)
                            .testTag("camerax_retake_btn")
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Ambil Ulang", fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = onConfirm,
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = BentoPrimary
                        ),
                        modifier = Modifier
                            .weight(1.3f)
                            .height(52.dp)
                            .testTag("camerax_confirm_btn")
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Gunakan Foto", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
