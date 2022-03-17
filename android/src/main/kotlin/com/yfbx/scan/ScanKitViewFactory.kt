package com.yfbx.scan

import android.content.Context
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.embedding.engine.plugins.lifecycle.HiddenLifecycleReference
import io.flutter.plugin.common.BinaryMessenger
import io.flutter.plugin.common.StandardMessageCodec
import io.flutter.plugin.platform.PlatformView
import io.flutter.plugin.platform.PlatformViewFactory

class ScanKitViewFactory(
    private val messenger: BinaryMessenger,
    private val binding: ActivityPluginBinding
) : PlatformViewFactory(StandardMessageCodec.INSTANCE) {


    override fun create(context: Context?, viewId: Int, args: Any?): PlatformView {
        val reference = binding.lifecycle as HiddenLifecycleReference
        val lifecycle = reference.lifecycle
        return ScanKitView(messenger, binding.activity, lifecycle)
    }
}
