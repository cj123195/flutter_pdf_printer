import 'dart:async';

import 'package:flutter/services.dart';

class FlutterPrinter {
  static const MethodChannel _channel =
      const MethodChannel('sample.flutter.io/printer');

  static Future<String> get platformVersion async {
    final String version = await _channel.invokeMethod('getPlatformVersion');
    return version;
  }
}
