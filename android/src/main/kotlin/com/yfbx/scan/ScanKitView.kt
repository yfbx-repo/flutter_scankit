package com.yfbx.scan

import android.app.Activity
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentActivity
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import com.kathline.barcode.CameraSourcePreview
import com.kathline.barcode.GraphicOverlay
import com.kathline.barcode.MLKit
import io.flutter.plugin.common.BinaryMessenger
import io.flutter.plugin.common.EventChannel
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.platform.PlatformView

class ScanKitView(
    messenger: BinaryMessenger,
    activity: Activity,
) : PlatformView,
    MethodChannel.MethodCallHandler,
    EventChannel.StreamHandler, MLKit.OnScanListener {

    private val mEvenChannel = EventChannel(messenger, "com.yfbx.scan/event")
    private val mChannel = MethodChannel(messenger, "com.yfbx.scan/widget")
    private var mEvents: EventChannel.EventSink? = null
    private val preview by lazy { CameraSourcePreview(activity, null).matchParent() }
    private var mlKit: MLKit

    init {
        mChannel.setMethodCallHandler(this)
        mEvenChannel.setStreamHandler(this)

        val graphicOverlay = GraphicOverlay(activity, null).matchParent()
        mlKit = MLKit(activity as FragmentActivity, preview, graphicOverlay)
        mlKit.setPlayBeepAndVibrate(false, true)
        mlKit.setOnScanListener(this)

    }


    override fun getView(): View {
        return preview
    }

    override fun onSuccess(
        barcodes: MutableList<Barcode>?,
        graphicOverlay: GraphicOverlay,
        image: InputImage?
    ) {
        if (barcodes.isNullOrEmpty()) return
        mlKit.isAnalyze = false
        val value = barcodes.joinToString { it.rawValue ?: "" }
        mEvents?.success(value)

    }

    override fun onFail(code: Int, e: java.lang.Exception?) {

    }


    override fun onMethodCall(call: MethodCall, result: MethodChannel.Result) {
        when (call.method) {
            "switchLight" -> {
                mlKit.switchLight()
            }
            "setAnalyze" -> {
                mlKit.isAnalyze = call.argument<Boolean>("isAnalyze") ?: false
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

    override fun dispose() {
        preview.release()
        mlKit.onDestroy()
        mChannel.setMethodCallHandler(null)
        mEvenChannel.setStreamHandler(null)
    }
}


fun <T : View> T.matchParent(): T {
    layoutParams = ViewGroup.LayoutParams(
        ViewGroup.LayoutParams.MATCH_PARENT,
        ViewGroup.LayoutParams.MATCH_PARENT
    )
    return this
}