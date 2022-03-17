package com.yfbx.scan

import android.app.Activity
import android.content.Intent
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.plugin.common.EventChannel
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import io.flutter.plugin.platform.PlatformViewFactory

/** FlutterScanKitPlugin */
class FlutterScanKitPlugin : FlutterPlugin, ActivityAware {

    private var mActivity: Activity? = null
    private var mPluginBinding: FlutterPlugin.FlutterPluginBinding? = null

    override fun onAttachedToEngine(binding: FlutterPlugin.FlutterPluginBinding) {
        mPluginBinding = binding
    }


    override fun onDetachedFromEngine(binding: FlutterPlugin.FlutterPluginBinding) {
        mPluginBinding = null
    }


    override fun onAttachedToActivity(binding: ActivityPluginBinding) {
        mActivity = binding.activity
        mPluginBinding?.let {
            it.platformViewRegistry.registerViewFactory(
                "ScanKitWidgetType", ScanKitViewFactory(it.binaryMessenger, binding)
            )
        }
    }

    override fun onReattachedToActivityForConfigChanges(binding: ActivityPluginBinding) {
        onAttachedToActivity(binding)
    }

    override fun onDetachedFromActivityForConfigChanges() {
        mActivity = null
    }

    override fun onDetachedFromActivity() {
        mActivity = null
    }
}