import 'dart:async';

import 'package:flutter/foundation.dart';
import 'package:flutter/services.dart';
import 'package:flutter/widgets.dart';

typedef ScanKitCallback = void Function(ScanKitController);

const _viewType = 'ScanKitWidgetType';

class ScanKitWidget extends StatelessWidget {
  final ScanKitCallback callback;

  ScanKitWidget({
    required this.callback,
  });

  @override
  Widget build(BuildContext context) {
    switch (defaultTargetPlatform) {
      case TargetPlatform.android:
        return AndroidView(
          onPlatformViewCreated: (id) {
            callback(ScanKitController());
          },
          creationParamsCodec: const StandardMessageCodec(),
          viewType: _viewType,
        );
      case TargetPlatform.iOS:
        return UiKitView(
          onPlatformViewCreated: (id) {
            callback(ScanKitController());
          },
          creationParamsCodec: const StandardMessageCodec(),
          viewType: _viewType,
        );
      default:
        throw UnsupportedError("Not supported on the current platform!");
    }
  }
}

///
/// ScanKitController
///
class ScanKitController {
  final _channel = const MethodChannel('com.yfbx.scan/widget');
  final _eventChannel = EventChannel('com.yfbx.scan/event');

  final StreamController<List<String>> _broadcast =
      StreamController.broadcast();

  late StreamSubscription _eventSubscription;

  ScanKitController() {
    _eventSubscription = _eventChannel.receiveBroadcastStream().listen(
          _eventHandler,
          cancelOnError: false,
        );
  }

  ///
  /// 获取扫码结果
  ///
  Stream<List<String>> get onResult => _broadcast.stream;

  ///
  /// 事件处理
  ///
  void _eventHandler(event) {
    if (event == null) return;
    final Map<dynamic, dynamic> map = event;
    switch (map["event"]) {
      case _ScanEvent.result:
        _broadcast.add(map['value'].cast<String>());
        break;
      default:
    }
  }

  ///
  /// 闪光灯
  ///
  Future<void> switchLight() async {
    return await _channel.invokeMethod('switchLight');
  }

  void dispose() {
    _eventSubscription.cancel();
  }
}

class _ScanEvent {
  static const result = 0;
  static const lightVisible = 1;
}
