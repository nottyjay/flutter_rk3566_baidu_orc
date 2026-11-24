# flutter_rk3566_baidu_orc

基于百度人脸 SDK 的 RK3566/RK3568 UVC 相机人脸识别 Flutter 插件。

提供人脸检测、RGB/NIR 双目活体检测、特征提取与 1:N 人脸检索能力，
配合 UVC 双目摄像头在 RK3566/RK3568 Android 开发板上运行。

## 安装

```yaml
dependencies:
  flutter_rk3566_baidu_orc: ^0.0.1
```

## 快速开始

```dart
import 'package:flutter_rk3566_baidu_orc/flutter_rk3566_baidu_orc.dart';

final plugin = FlutterRk3568Uvccamera.instance;

// 1. 初始化人脸引擎（需提前完成百度授权）
await plugin.initFaceEngine(FlutterRk3568UvccameraConfig(
  rgbCameraName: "CAM1",
  nirCameraName: "CAM2",
  faceImagesPath: "/sdcard/faces",
  liveScoreThreshold: 0.8,
  idScoreThreshold: 0.8,
  activeLicenseType: "offline",
));

// 2. 注册人脸（文件名格式：userId_name.png）
await plugin.addFace('/sdcard/face/1001_zhangsan.png');

// 3. 设置回调并开始 1:N 检索
plugin.setCallbacks(FlutterRk3568UvccameraCallbacks(
  onFindPersonSuccess: (persons) {
    for (final p in persons) {
      print('识别到: ${p.userName}, 得分: ${p.userScore}');
    }
  },
));

await plugin.runDetect();
```

## 人脸模型

模型文件（约 66MB）不随包分发，编译期由 Gradle 任务
`ensureFaceModels` 自动从 [GitHub Release](https://github.com/nottyjay/flutter_rk3566_baidu_orc/releases)
下载并打进 APK（sha256 校验），运行时无需网络、离线可用。

内网/离线构建环境可在宿主 App 的 `gradle.properties` 中覆盖下载源：

```properties
faceModelsUrl=https://内网镜像/face-sdk-models.zip
faceModelsSha256=<镜像包校验和>
```

## 主要 API

| API | 说明 |
| --- | --- |
| `initFaceEngine` | 初始化人脸引擎与授权 |
| `addFace` / `addFaces` | 单张/批量注册人脸 |
| `deleteFace` / `clearFace` | 删除指定/清空人脸库 |
| `runDetect` / `stopDetect` | 开始/停止 1:N 检索 |
| `getDeviceId` | 获取设备授权 ID |
| `setCallbacks` | 注册识别、注册结果等回调 |

## 使用前提

- RK3566/RK3568 Android 开发板（arm64），已接入 UVC 双目摄像头
- 已获取百度人脸 SDK 授权（`activeLicenseType` 对应的激活方式）
- 首次编译需可访问 GitHub Release（或配置内网镜像）
