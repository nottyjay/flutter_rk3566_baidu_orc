import 'flutter_rk3568_uvccamera_platform_interface.dart';

export 'flutter_rk3568_uvccamera_platform_interface.dart'
    show FlutterRk3568UvccameraCallbacks;

class FlutterRk3568UvccameraConfig {
  FlutterRk3568UvccameraConfig({
    this.rgbCameraName,
    this.nirCameraName,
    this.depthCameraName,
    this.cameraDriveType,
    this.rgbAndNirWidth,
    this.rgbAndNirHeight,
    this.depthWidth,
    this.depthHeight,
    this.activeModel,
    this.liveScoreThreshold,
    this.idScoreThreshold,
    this.rgbAndNirScoreThreshold,
    this.maxSearchNum,
    this.mirrorVideoRGB,
    this.mirrorVideoNIR,
    this.rgbVideoDirection,
    this.nirVideoDirection,
    this.faceImagesPath,
  });

  final String? rgbCameraName;
  final String? nirCameraName;
  final String? depthCameraName;
  final int? cameraDriveType;
  final int? rgbAndNirWidth;
  final int? rgbAndNirHeight;
  final int? depthWidth;
  final int? depthHeight;
  final int? activeModel;
  final double? liveScoreThreshold;
  final double? idScoreThreshold;
  final double? rgbAndNirScoreThreshold;
  final int? maxSearchNum;
  final bool? mirrorVideoRGB;
  final bool? mirrorVideoNIR;
  final int? rgbVideoDirection;
  final int? nirVideoDirection;
  final String? faceImagesPath;

  Map<String, Object?> toMap() {
    return <String, Object?>{
      'rgbCameraName': rgbCameraName,
      'nirCameraName': nirCameraName,
      'depthCameraName': depthCameraName,
      'cameraDriveType': cameraDriveType ?? 0,
      'rgbAndNirWidth': rgbAndNirWidth ?? 0,
      'rgbAndNirHeight': rgbAndNirHeight ?? 0,
      'depthWidth': depthWidth ?? 0,
      'depthHeight': depthHeight ?? 0,
      'activeModel': activeModel ?? 0,
      'liveScoreThreshold': liveScoreThreshold ?? 0.0,
      'idScoreThreshold': idScoreThreshold ?? 0.0,
      'rgbAndNirScoreThreshold': rgbAndNirScoreThreshold ?? 0.0,
      'maxSearchNum': maxSearchNum ?? 0,
      'mirrorVideoRGB': mirrorVideoRGB,
      'mirrorVideoNIR': mirrorVideoNIR,
      'rgbVideoDirection': rgbVideoDirection ?? 0,
      'nirVideoDirection': nirVideoDirection ?? 0,
      'faceImagesPath': faceImagesPath,
    };
  }
}

class FlutterRk3568Uvccamera {
  FlutterRk3568Uvccamera._();

  static final FlutterRk3568Uvccamera instance = FlutterRk3568Uvccamera._();

  Future<void> initFaceEngine(FlutterRk3568UvccameraConfig config) {
    return FlutterRk3568UvccameraPlatform.instance.initFaceEngine(
      config.toMap(),
    );
  }

  Future<void> initQrcodeEngine() {
    return FlutterRk3568UvccameraPlatform.instance.initQrcodeEngine();
  }

  Future<void> initICCardReader() {
    return FlutterRk3568UvccameraPlatform.instance.initICCardReader();
  }

  Future<void> addFace(String facePath) {
    return FlutterRk3568UvccameraPlatform.instance.addFace(facePath);
  }

  Future<void> deleteFace(String id) {
    return FlutterRk3568UvccameraPlatform.instance.deleteFace(id);
  }

  Future<void> clearFace() {
    return FlutterRk3568UvccameraPlatform.instance.clearFace();
  }

  Future<void> addFaces(String facePath) {
    return FlutterRk3568UvccameraPlatform.instance.addFaces(facePath);
  }

  Future<void> runDetect() {
    return FlutterRk3568UvccameraPlatform.instance.runDetect();
  }

  void setCallbacks(FlutterRk3568UvccameraCallbacks callbacks) {
    FlutterRk3568UvccameraPlatform.instance.setCallbacks(callbacks);
  }
}
