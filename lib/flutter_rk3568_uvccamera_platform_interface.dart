import 'package:plugin_platform_interface/plugin_platform_interface.dart';

import 'flutter_rk3568_uvccamera_method_channel.dart';
import 'flutter_rk3568_uvccamera.dart';

class FlutterRk3568UvccameraCallbacks {
  FlutterRk3568UvccameraCallbacks({
    this.onActiveLicenseSuccess,
    this.onActiveLicenseFail,
    this.onInitFullUsersSuccess,
    this.onInitModelSuccess,
    this.onInitModelFail,
    this.onAddFaceSuccess,
    this.onAddFaceFailed,
    this.onDeleteFaceSuccess,
    this.onBatchAddFaceSuccess,
    this.onUpdateAddFaceProcess,
    this.onFindQrCodeSuccess,
    this.onFindPersonSuccess,
    this.onDismiss,
  });

  final void Function()? onActiveLicenseSuccess;
  final void Function(int code, String response)? onActiveLicenseFail;
  final void Function()? onInitFullUsersSuccess;
  final void Function()? onInitModelSuccess;
  final void Function(int code, String response)? onInitModelFail;
  final void Function()? onAddFaceSuccess;
  final void Function()? onAddFaceFailed;
  final void Function()? onDeleteFaceSuccess;
  final void Function()? onBatchAddFaceSuccess;
  final void Function(double percent)? onUpdateAddFaceProcess;
  final void Function(String qrCode)? onFindQrCodeSuccess;
  final void Function(List<FlutterRk3568UvccameraFindUser> persons)? onFindPersonSuccess;
  final void Function()? onDismiss;
}

abstract class FlutterRk3568UvccameraPlatform extends PlatformInterface {
  FlutterRk3568UvccameraPlatform() : super(token: _token);

  static final Object _token = Object();

  static FlutterRk3568UvccameraPlatform _instance =
      MethodChannelFlutterRk3568Uvccamera();

  static FlutterRk3568UvccameraPlatform get instance => _instance;

  static set instance(FlutterRk3568UvccameraPlatform instance) {
    PlatformInterface.verifyToken(instance, _token);
    _instance = instance;
  }

  Future<void> initFaceEngine(Map<String, dynamic> arguments) {
    throw UnimplementedError('initFaceEngine() has not been implemented.');
  }

  Future<void> initQrcodeEngine() {
    throw UnimplementedError('initQrcodeEngine() has not been implemented.');
  }


  Future<void> addFace(String facePath) {
    throw UnimplementedError('addFace() has not been implemented.');
  }

  Future<void> deleteFace(String id) {
    throw UnimplementedError('deleteFace() has not been implemented.');
  }

  Future<void> clearFace() {
    throw UnimplementedError('clearFace() has not been implemented.');
  }

  Future<void> addFaces(String facePath) {
    throw UnimplementedError('addFaces() has not been implemented.');
  }

  Future<void> runDetect() {
    throw UnimplementedError('runDetect() has not been implemented.');
  }

  @override
  Future<void> stopDetect() {
    throw UnimplementedError('stopDetect() has not been implemented.');
  }

  Future<String?> getDeviceId() {
    throw UnimplementedError('getDeviceId() has not been implemented.');
  }

  void setCallbacks(FlutterRk3568UvccameraCallbacks callbacks) {
    throw UnimplementedError('setCallbacks() has not been implemented.');
  }
}
