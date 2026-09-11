package com.jhonmaxdata.authenticator.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.OptIn

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QrScannerScreen(
	onQrCodeScanned: (String) -> Unit,
	onBackClicked: () -> Unit
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

	val launcher = rememberLauncherForActivityResult(
		contract = ActivityResultContracts.RequestPermission()
	) { isGranted ->
		hasCameraPermission = isGranted
	}

	LaunchedEffect(hasCameraPermission) {
		if (!hasCameraPermission) {
			launcher.launch(Manifest.permission.CAMERA)
		}
	}

	Scaffold(
		topBar = {
			TopAppBar(
				title = { Text("Escanear QR Code", color = Color.White) },
				navigationIcon = {
					IconButton(onClick = onBackClicked) {
						Icon(
							imageVector = Icons.AutoMirrored.Filled.ArrowBack,
							contentDescription = "Voltar",
							tint = Color.White
						)
					}
				},
				colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
			)
		}
	) { innerPadding ->
		Box(
			modifier = Modifier
				.fillMaxSize()
				.padding(innerPadding)
		) {
			if (hasCameraPermission) {
				CameraPreviewWithScanner(onQrCodeScanned = onQrCodeScanned)
				ScannerOverlay()
			} else {
				Text(
					text = "A permissão da câmera é necessária para escanear o QR Code.",
					modifier = Modifier.align(Alignment.Center),
					color = Color.White
				)
			}
		}
	}
}

@Composable
private fun CameraPreviewWithScanner(
	onQrCodeScanned: (String) -> Unit
) {
	val context = LocalContext.current
	val lifecycleOwner = LocalLifecycleOwner.current
	val cameraExecutor = remember { Executors.newSingleThreadExecutor() }
	val isScanned = remember { AtomicBoolean(false) }

	AndroidView(
		factory = { ctx ->
			val previewView = PreviewView(ctx)
			val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
			val barcodeScannerOptions = BarcodeScannerOptions.Builder()
				.setBarcodeFormats(Barcode.FORMAT_QR_CODE)
				.build()
			val scanner = BarcodeScanning.getClient(barcodeScannerOptions)

			cameraProviderFuture.addListener({
				val cameraProvider = cameraProviderFuture.get()
				val preview = Preview.Builder().build().also {
					it.setSurfaceProvider(previewView.surfaceProvider)
				}
				val imageAnalysis = ImageAnalysis.Builder()
					.setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
					.build()

				imageAnalysis.setAnalyzer(cameraExecutor) { imageProxy ->
					processImageProxy(
						scanner = scanner,
						imageProxy = imageProxy,
						onSuccess = { rawValue ->
							if (rawValue.startsWith("otpauth://") && isScanned.compareAndSet(false, true)) {
								onQrCodeScanned(rawValue)
							}
						}
					)
				}

				try {
					cameraProvider.unbindAll()
					cameraProvider.bindToLifecycle(
						lifecycleOwner,
						CameraSelector.DEFAULT_BACK_CAMERA,
						preview,
						imageAnalysis
					)
				} catch (exception: Exception) {
					imageAnalysis.clearAnalyzer()
					scanner.close()
					exception.printStackTrace()
				}
			}, ContextCompat.getMainExecutor(ctx))

			previewView
		},
		modifier = Modifier.fillMaxSize()
	)

	androidx.compose.runtime.DisposableEffect(context, lifecycleOwner) {
		onDispose {
			cameraExecutor.shutdown()
		}
	}
}

private fun processImageProxy(
	scanner: BarcodeScanner,
	imageProxy: ImageProxy,
	onSuccess: (String) -> Unit
) {
	val mediaImage = imageProxy.image
	if (mediaImage == null) {
		imageProxy.close()
		return
	}

	val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
	scanner.process(image)
		.addOnSuccessListener { barcodes ->
			barcodes.forEach { barcode ->
				barcode.rawValue?.let(onSuccess)
			}
		}
		.addOnCompleteListener {
			imageProxy.close()
		}
}

@Composable
private fun ScannerOverlay() {
	Box(
		modifier = Modifier.fillMaxSize(),
		contentAlignment = Alignment.Center
	) {
		Canvas(modifier = Modifier.size(260.dp)) {
			val strokeWidth = 4.dp.toPx()
			val dashPathEffect = PathEffect.dashPathEffect(floatArrayOf(20f, 20f), 0f)
			drawRect(
				color = Color.Green,
				style = Stroke(width = strokeWidth, pathEffect = dashPathEffect)
			)
		}
	}
}
