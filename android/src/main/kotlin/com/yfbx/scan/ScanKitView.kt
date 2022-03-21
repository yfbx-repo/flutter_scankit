package com.yfbx.scan

import android.annotation.SuppressLint
import android.app.Activity
import android.view.View
import android.view.ViewGroup
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.google.mlkit.vision.barcode.Barcode
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import io.flutter.plugin.common.BinaryMessenger
import io.flutter.plugin.common.EventChannel
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.platform.PlatformView
import java.util.concurrent.Executors

class ScanKitView(
    messenger: BinaryMessenger,
    private val activity: Activity,
    private val lifecycle: Lifecycle
) : PlatformView, LifecycleEventObserver,
    MethodChannel.MethodCallHandler,
    EventChannel.StreamHandler, LifecycleOwner {

    companion object {
        const val EVENT_SCAN_RESULT = 0
    }


    private val scanner = BarcodeScanning.getClient(
        BarcodeScannerOptions.Builder()
            .setBarcodeFormats(Barcode.FORMAT_ALL_FORMATS)
            .build()
    )

    private val mEvenChannel = EventChannel(messenger, "com.yfbx.scan/event")
    private val mChannel = MethodChannel(messenger, "com.yfbx.scan/widget")
    private var mEvents: EventChannel.EventSink? = null
    private val previewView: PreviewView
    private val cameraExecutor = Executors.newSingleThreadExecutor()
    private var camera: Camera? = null
    private var enableTorch = false

    init {
        mChannel.setMethodCallHandler(this)
        mEvenChannel.setStreamHandler(this)
        lifecycle.addObserver(this)

        previewView = createCameraView()
        previewView.startCamera(this, this::analyzeBitmap)
    }


    private fun createCameraView(): PreviewView {
        return PreviewView(activity).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }
    }

    /**
     * 分析图片
     */
    @SuppressLint("UnsafeOptInUsageError")
    private fun analyzeBitmap(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image ?: return
        val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)

        scanner.process(image)
            .addOnSuccessListener { result ->
                if (result.isNullOrEmpty()) return@addOnSuccessListener
                scanner.close()
                val data = result.map { it.displayValue!! }
                onScanResult(data)
            }

            .addOnFailureListener {
                // Do nothing
            }
            .addOnCompleteListener {
                imageProxy.close()
            }
    }

    /**
     * 启动相机
     */
    private fun PreviewView.startCamera(
        lifecycleOwner: LifecycleOwner,
        analyzer: ImageAnalysis.Analyzer
    ) {
        val imageAnalysis = ImageAnalysis.Builder().build()
        imageAnalysis.setAnalyzer(cameraExecutor, analyzer)

        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener({
            val preview = Preview.Builder().build()
            preview.setSurfaceProvider(surfaceProvider)
            val cameraProvider = cameraProviderFuture.get()
            cameraProvider.unbindAll()
            camera = cameraProvider.bindToLifecycle(
                lifecycleOwner,
                CameraSelector.DEFAULT_BACK_CAMERA,
                preview,
                imageAnalysis,
            )
        }, ContextCompat.getMainExecutor(context))
    }


    override fun getView(): View {
        return previewView
    }


    override fun onMethodCall(call: MethodCall, result: MethodChannel.Result) {
        when (call.method) {
            "switchLight" -> {
                camera?.cameraControl?.enableTorch(!enableTorch)
            }
            else -> result.notImplemented()
        }
    }

    override fun onListen(arguments: Any?, events: EventChannel.EventSink?) {
        mEvents = events
    }

    override fun onCancel(arguments: Any?) {
        mEvents = null
    }

    private fun onScanResult(value: List<String>) {
        mEvents?.success(hashMapOf("event" to EVENT_SCAN_RESULT, "value" to value))
    }

    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        when (event) {
            Lifecycle.Event.ON_CREATE -> {
            }
            Lifecycle.Event.ON_START -> {
            }
            Lifecycle.Event.ON_RESUME -> {
            }
            Lifecycle.Event.ON_PAUSE -> {
            }
            Lifecycle.Event.ON_STOP -> {
            }
            Lifecycle.Event.ON_DESTROY -> {
            }
            else -> {
            }
        }
    }

    override fun dispose() {
        cameraExecutor.shutdown()
        mChannel.setMethodCallHandler(null)
        mEvenChannel.setStreamHandler(null)
    }

    override fun getLifecycle(): Lifecycle {
        return lifecycle
    }

}
