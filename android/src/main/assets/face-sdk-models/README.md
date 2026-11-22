# face-sdk 模型文件

本目录下的 RKNN 模型文件（约 66MB）**不随 git 仓库分发**，
由 `android/build.gradle` 中的 Gradle 任务在编译期自动管理。

## 工作机制

- `ensureFaceModels`（编译期自动执行，挂载于 `preBuild`）：
  检查本地模型是否齐全，缺失时从 Release 下载 `face-sdk-models.zip`，
  sha256 校验通过后解压到本目录。模型最终打进 APK assets，运行时无需网络。
- `packFaceModels`（维护者使用）：将本目录模型打包为
  `android/build/face-sdk-cache/face-sdk-models.zip` 并输出 sha256，用于上传 Release。

## 维护者：更新模型

1. 修改/替换本目录下的模型文件；
2. 执行打包并记录校验和：

   ```bash
   cd android && ANDROID_HOME=<sdk路径> ./gradlew packFaceModels
   ```

3. 将生成的 zip 作为附件上传到仓库 Release（tag 建议 `models-vX.Y.Z`）；
4. 更新 `android/build.gradle` 中的默认 `faceModelsUrl`（指向新 tag）与 `faceModelsSha256`。

## 使用方：离线/内网构建

构建宿主 App 的 `gradle.properties`（或 gradle -P 参数）可覆盖下载源：

```properties
faceModelsUrl=https://内网镜像/face-sdk-models.zip
faceModelsSha256=<镜像包校验和>
```
