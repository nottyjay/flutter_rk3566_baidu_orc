import 'dart:async';
import 'package:flutter/material.dart';
import 'package:flutter_rk3568_uvccamera/flutter_rk3568_uvccamera.dart';

class UvcDemoPage extends StatefulWidget {
  const UvcDemoPage({super.key});

  @override
  State<UvcDemoPage> createState() => _UvcDemoPageState();
}

class _UvcDemoPageState extends State<UvcDemoPage> {
  double _importPercent = 0.0;
  bool _isImporting = false;

  FlutterRk3568Uvccamera get plugin => FlutterRk3568Uvccamera.instance;

  @override
  void initState() {
    super.initState();
    _setupCallbacks();
    _initEngines();
  }

  void _setupCallbacks() {
    plugin.setCallbacks(
      FlutterRk3568UvccameraCallbacks(
        onAddFaceSuccess: () {
          print("添加人脸成功");
        },
        onAddFaceFailed: () {
          print("添加人脸失败");
        },
        onActiveLicenseFail: (int code, String message) {
          print("人脸激活失败。Code" + code.toString() + ", msg: " + message);
        },
        onActiveLicenseSuccess: () {
          print("人脸激活成功");
        },
        onFindPersonSuccess: (List<Map<String, dynamic>> persons) {
          for(int i = 0; i < persons.length; i++){
            Map<String, dynamic> person = persons[i];
            print("找到人脸：${person['name']}, 得分：${person['score']}");
          };
        },
        onFindQrCodeSuccess: (String qrCode) {
          print("找到二维码：" + qrCode);
        },
        onUpdateAddFaceProcess: (double percent) {
          setState(() {
            _isImporting = true;
            _importPercent = percent / 100.0;
          });
        },
        onBatchAddFaceSuccess: () {
          setState(() {
            _isImporting = false;
            _importPercent = 0.0;
          });
        },
        onReadICCardIdSuccess: (cardId) {
          print("id 卡卡号：$cardId");
        },
      ),
    );
  }

  Future<void> _initEngines() async {
    FlutterRk3568UvccameraConfig config = FlutterRk3568UvccameraConfig(
      rgbCameraName: "CAM1", 
      nirCameraName: "CAM2", 
      nirVideoDirection: 180, 
      rgbVideoDirection: 180, 
      rgbAndNirWidth: 640, 
      rgbAndNirHeight: 480,
      maxSearchNum: 2,
      mirrorVideoRGB: true,
      mirrorVideoNIR: true,
      faceImagesPath: "/sdcard/faces",
      liveScoreThreshold: 0.8,
      idScoreThreshold: 0.8,
      rgbAndNirScoreThreshold: 0.8);
    await plugin.initFaceEngine(config);
    await plugin.initQrcodeEngine();
    await plugin.initICCardReader();
  }

  Future<void> _onInitFaces() async {
    setState(() {
      _isImporting = true;
      _importPercent = 0.0;
    });
    await plugin.addFaces('/sdcard/face_test');
  }

  Future<void> _onStartDetect() async {
    await plugin.runDetect();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('flutter_rk3568_uvccamera example')),
      body: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.stretch,
          children: <Widget>[
            ElevatedButton(
              onPressed: _onInitFaces,
              child: const Text('初始化人脸库'),
            ),
            const SizedBox(height: 8),
            ElevatedButton(
              onPressed: _onStartDetect,
              child: const Text('开始检测'),
            ),
            const SizedBox(height: 24),
            if (_isImporting)
              Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: <Widget>[
                  const Text('人脸导入进度'),
                  const SizedBox(height: 8),
                  LinearProgressIndicator(value: _importPercent),
                  const SizedBox(height: 4),
                  Text('${(_importPercent * 100).toStringAsFixed(0)}%'),
                ],
              ),
          ],
        ),
      ),
    );
  }
}
