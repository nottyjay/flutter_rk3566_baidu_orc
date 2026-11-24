import 'package:flutter/services.dart';

import 'flutter_rk3566_baidu_orc_platform_interface.dart';
import 'flutter_rk3566_baidu_orc.dart';

class MethodChannelFlutterRk3568Uvccamera
    extends FlutterRk3568UvccameraPlatform {
  MethodChannelFlutterRk3568Uvccamera() {
    methodChannel.setMethodCallHandler(_handleMethodCall);
  }

  final MethodChannel methodChannel = const MethodChannel(
    'com.alphay.flutter.plugin/flutter_rk3568_uvc',
  );

  FlutterRk3568UvccameraCallbacks? _callbacks;

  @override
  Future<void> initFaceEngine(Map<String, dynamic> arguments) {
    return methodChannel.invokeMethod<void>('initFaceEngine', arguments);
  }

  @override
  Future<void> initQrcodeEngine() {
    return methodChannel.invokeMethod<void>('initQrcodeEngine');
  }

  @override
  Future<void> addFace(String facePath) {
    return methodChannel.invokeMethod<void>('addFace', <String, dynamic>{
      'facePath': facePath,
    });
  }

  @override
  Future<void> deleteFace(String id) {
    return methodChannel.invokeMethod<void>('deleteFace', <String, dynamic>{
      'id': id,
    });
  }

  @override
  Future<void> clearFace() {
    return methodChannel.invokeMethod<void>('clearFace');
  }

  @override
  Future<void> addFaces(String facePath) {
    return methodChannel.invokeMethod<void>('addFaces', <String, dynamic>{
      'facePath': facePath,
    });
  }

  @override
  Future<void> runDetect() {
    return methodChannel.invokeMethod<void>('runDetect');
  }

  @override
  Future<void> stopDetect() {
    return methodChannel.invokeMethod<void>('stopDetect');
  }

  @override
  Future<String?> getDeviceId() {
    return methodChannel.invokeMethod<String>('getDeviceId');
  }

  @override
  void setCallbacks(FlutterRk3568UvccameraCallbacks callbacks) {
    _callbacks = callbacks;
  }

  Future<dynamic> _handleMethodCall(MethodCall call) async {
    final callbacks = _callbacks;
    if (callbacks == null) {
      return null;
    }
    switch (call.method) {
      case 'activeLicenseSuccess':
        callbacks.onActiveLicenseSuccess?.call();
        break;
      case 'activeLicenseFail':
        final Map<dynamic, dynamic>? data =
            call.arguments as Map<dynamic, dynamic>?;
        final int code = data?['code'] as int? ?? 0;
        final String response = data?['response'] as String? ?? '';
        callbacks.onActiveLicenseFail?.call(code, response);
        break;
      case 'initFullUsersSuccess':
        callbacks.onInitFullUsersSuccess?.call();
        break;
      case 'initModelSuccess':
        callbacks.onInitModelSuccess?.call();
        break;
      case 'initModelFail':
        final Map<dynamic, dynamic>? data =
            call.arguments as Map<dynamic, dynamic>?;
        final int code = data?['code'] as int? ?? 0;
        final String response = data?['response'] as String? ?? '';
        callbacks.onInitModelFail?.call(code, response);
        break;
      case 'addFaceSuccess':
        callbacks.onAddFaceSuccess?.call();
        break;
      case 'addFaceFailed':
        callbacks.onAddFaceFailed?.call();
        break;
      case 'deleteFaceSuccess':
        callbacks.onDeleteFaceSuccess?.call();
        break;
      case 'batchAddFaceSuccess':
        callbacks.onBatchAddFaceSuccess?.call();
        break;
      case 'updateAddFaceProcess':
        final double percent = (call.arguments as num?)?.toDouble() ?? 0.0;
        callbacks.onUpdateAddFaceProcess?.call(percent);
        break;
      case 'findQrCodeSuccess':
        final String qrCode = call.arguments as String? ?? '';
        callbacks.onFindQrCodeSuccess?.call(qrCode);
        break;
      case 'findPersonSuccess':
        final List<dynamic> rawList =
            call.arguments as List<dynamic>? ?? <dynamic>[];
        final List<Map<String, dynamic>> rawPersons = rawList
            .map(
              (dynamic item) => (item as Map<dynamic, dynamic>).map((
                dynamic key,
                dynamic value,
              ) {
                return MapEntry(key.toString(), value);
              }),
            )
            .toList();
        final List<FlutterRk3568UvccameraFindUser> persons = rawPersons
            .map((map) => FlutterRk3568UvccameraFindUser.fromMap(map))
            .toList();
        callbacks.onFindPersonSuccess?.call(persons);
        break;
      case 'onDismiss':
        callbacks.onDismiss?.call();
        break;
    }
    return null;
  }
}
