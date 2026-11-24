# flutter_rk3566_baidu_orc

基于百度人脸 SDK 的 RK3566/RK3568 UVC 相机人脸识别 Flutter 插件。

在 RK3566/RK3568 Android 开发板（arm64）上，配合 UVC RGB/NIR 双目摄像头，提供：

- 人脸检测与跟踪
- RGB / NIR 双目活体检测
- 人脸特征提取（512 维向量）
- 1:N 人脸检索（内置 SQLite 人脸库）
- 人脸库管理（单张/批量注册、删除、清空）

## 环境要求

- RK3566 / RK3568 Android 开发板（arm64-v8a）
- 已接入 UVC RGB/NIR 双目摄像头
- 已获取百度人脸 SDK 授权（license）

## 安装

```yaml
dependencies:
  flutter_rk3566_baidu_orc: ^0.0.1
```

## 快速开始

```dart
import 'package:flutter_rk3566_baidu_orc/flutter_rk3566_baidu_orc.dart';

final plugin = FlutterRk3568Uvccamera.instance;

// 1. 注册回调
plugin.setCallbacks(FlutterRk3568UvccameraCallbacks(
  onActiveLicenseSuccess: () => print('人脸激活成功'),
  onActiveLicenseFail: (code, msg) => print('激活失败: $code $msg'),
  onAddFaceSuccess: () => print('注册成功'),
  onAddFaceFailed: () => print('注册失败（未检出人脸/文件名不符合规则等）'),
  onUpdateAddFaceProcess: (percent) => print('批量导入进度: $percent%'),
  onBatchAddFaceSuccess: () => print('批量导入完成'),
  onInitFullUsersSuccess: () => print('人脸库加载完成'),
  onFindPersonSuccess: (persons) {
    for (final p in persons) {
      print('识别到: ${p.userName}, 得分: ${p.userScore}');
    }
  },
  onDismiss: () => print('检测窗口已关闭'),
));

// 2. 初始化引擎并激活授权
await plugin.initFaceEngine(FlutterRk3568UvccameraConfig(
  rgbCameraName: "CAM1",
  nirCameraName: "CAM2",
  rgbAndNirWidth: 640,
  rgbAndNirHeight: 480,
  faceImagesPath: "/sdcard/faces",   // 人脸图片存放目录
  liveScoreThreshold: 0.8,           // 活体阈值
  idScoreThreshold: 0.8,             // 识别阈值
  activeLicenseType: "offline",      // offline / online / batchOnline
));

// 3. 开始 1:N 检索（弹出检测窗口）
await plugin.runDetect();

// 4. 停止检索
await plugin.stopDetect();
```

## 人脸库管理

### 注册人脸（增）

**单张注册 `addFace`**：传入图片的绝对路径。

```dart
await plugin.addFace('/sdcard/face/1001_zhangsan.png');
```

- 文件名**必须**为 `userId_name.png` 格式（`_` 分割后两段，`userId` 为用户 ID、`name` 为姓名）
- 未检出人脸 / 文件名不符合规则时回调 `onAddFaceFailed`，不会写入人脸库
- 对同一 `userId` 重复注册时，会先删除旧记录再写入新特征

**批量导入 `addFaces`**：传入**目录**路径，遍历其中所有图片。

```dart
await plugin.addFaces('/sdcard/face');
```

- 命名规则同上；检出人脸的入库并把图片拷贝到 `faceImagesPath`
- 未能注册的图片会被拷贝到同级 `<目录>_fail` 目录（如 `/sdcard/face_fail`），便于排查
- 进度通过 `onUpdateAddFaceProcess` 回调（0~100），全部结束回调 `onBatchAddFaceSuccess`

### 删除人脸（减）

**按 userId 删除 `deleteFace`**：删除数据库记录、同步清理内存特征库，
并自动删除 `faceImagesPath` 下该用户的人脸图片。

```dart
await plugin.deleteFace('1001');
```

**清空人脸库 `clearFace`**：清空数据库全部记录与内存特征库，
并清空 `faceImagesPath` 目录下的所有文件。

```dart
await plugin.clearFace();
```

## 自定义数据库地址

人脸库默认存储在应用内部目录：`/data/data/<应用包名>/databases/face.db`。

通过 `initFaceEngine` 的配置项 `dbPath` 可将数据库**外置**到任意可写绝对路径
（如外置存储），便于备份或多个应用共享排查：

```dart
await plugin.initFaceEngine(FlutterRk3568UvccameraConfig(
  // ...其他配置...
  dbPath: "/sdcard/faces/face.db",
));
```

行为说明：

- `dbPath` 为空或不传时，使用内部默认路径
- `dbPath` 为绝对路径时，数据库文件使用该路径；首次运行若检测到内部旧库
  `databases/face.db` 存在，会**自动迁移**（拷贝校验后删除内部源文件），老数据不丢失
- 外置到 `/sdcard` 时需确保应用已具备存储权限
- 目录不存在时会自动创建

## 模型文件

人脸模型（约 66MB）不随插件包分发。编译期由 Gradle 任务 `ensureFaceModels`
自动从 [GitHub Release](https://github.com/nottyjay/flutter_rk3566_baidu_orc/releases)
下载（sha256 校验）并打进 APK，**运行时无需网络、完全离线可用**。

内网 / 无外网构建环境，可在宿主 App 的 `gradle.properties` 中覆盖下载源：

```properties
faceModelsUrl=https://内网镜像/face-sdk-models.zip
faceModelsSha256=<镜像包校验和>
```

## API 一览

| API | 说明 |
| --- | --- |
| `initFaceEngine(config)` | 初始化引擎、读取配置并激活授权 |
| `addFace(facePath)` | 单张注册人脸 |
| `addFaces(dirPath)` | 批量导入目录下的人脸 |
| `deleteFace(userId)` | 删除指定用户 |
| `clearFace()` | 清空人脸库 |
| `runDetect()` | 开始 1:N 检索（弹出检测窗口）|
| `stopDetect()` | 停止检索 |
| `getDeviceId()` | 获取设备授权 ID |
| `setCallbacks(callbacks)` | 注册各类回调 |

## 常见问题

**注册纯白图 / 无人脸图片会崩溃吗？**

不会。图片在人脸检测阶段即被拦截（检测不到人脸直接返回失败），
回调 `onAddFaceFailed`，不会写入人脸库，已有数据不受影响。

**face.db 在哪？**

默认在 `/data/data/<应用包名>/databases/face.db`；配置 `dbPath` 后位于指定路径。
