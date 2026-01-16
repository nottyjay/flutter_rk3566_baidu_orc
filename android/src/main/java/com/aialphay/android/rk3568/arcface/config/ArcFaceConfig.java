package com.aialphay.android.rk3568.arcface.config;

import static com.aialphay.android.rk3568.arcface.ArcFaceConstants.ACTIVE_LICENSE_OFFLINE;

import java.util.Map;

public class ArcFaceConfig {

  // ================== 模型配置 ====================
  private String faceImagesPath;
  private int maxSearchNum = 2;
  private int maxDetectNum = 10;
  // RGB 预览Y轴转向falese为0，true为180
  private Boolean rgbRevert = true;
  // 人脸置信度用于表征被检测到的物体是人脸的概率，该阈值设置越高检测越严格，建议在0.3-0.8区间内调整阈值
  private float faceThreshold = 0.5f;
  // 人脸模糊度
  private float blur = 0.8f;
  // 默认为80px。可传入大于50px的数值，小于此大小的人脸不予检测
  private int minimumFace = 60;
  private boolean qualityControl = true;

  // rbg人脸检测角度 默认为0。可传入0、90、180、270四个选项。
  private int rgbDetectDirection = 0;

  // nir人脸检测角度 默认为0。可传入0、90、180、270四个选项。
  private int nirDetectDirection = 0;

  // rbg人脸检测 0：RGB无镜像，1：有镜像
  private int mirrorDetectRGB = 0;

  // nir人脸检测 0：RGB无镜像，1：有镜像
  private int mirrorDetectNIR = 0;
  // 是否开启best image
  private boolean bestImage = true;
  // RGB活体阀值
  private float rgbLiveScore = 0.80f;
  // NIR活体阀值
  private float nirLiveScore = 0.80f;
  // Depth活体阀值
  private float depthLiveScore = 0.80f;
  // 使用的特征抽取模型默认为生活照：1；证件照：2；RGB+NIR混合模态模型：3；
  private int activeModel = 1;
  // 识别阈值，0-100，默认为80分,需要选择具体模型的阈值。live：80、idcard：80
  private float liveScoreThreshold = 0.76f;
  // 识别阈值，0-100，默认为80分,需要选择具体模型的阈值。
  private float idScoreThreshold = 0.9f;
  // 识别阈值，0-100，默认为80分,需要选择具体模型的阈值。
  private float rgbAndNirScoreThreshold = 0.9f;

  // ==================== 摄像头控制 =====================
  // 获取摄像头类型的方案。【常规 0，1 驱动：0】【使用 usb 设备名称匹配：1】
  private int cameraDriveType = 0;
  // RGB 摄像头名称
  private String rgbCameraName = "";
  // NIR摄像头名称
  private String nirCameraName = "";
  // Depth摄像头名称
  private String depthCameraName = "";
  // false：RGB无镜像，true：有镜像
  private boolean mirrorVideoRGB = false;
  // false：Nir无镜像，true：有镜像
  private boolean mirrorVideoNIR = false;
  // 默认为0。可传入0、90、180、270四个选项。
  private int rgbVideoDirection = 0;
  // 默认为0。可传入0、90、180、270四个选项。
  private int nirVideoDirection = 0;
  // 0:奥比中光海燕、大白（640*400）
  // 1:奥比中光海燕Pro、Atlas（400*640）
  // 2:奥比中光蝴蝶、Astra Pro\Pro S（640*480）
  // 3:舜宇Seeker06
  // 4:螳螂慧视天蝎P1
  // 5:瑞识M720N
  // 6:奥比中光Deeyea(结构光)
  // 7:华捷艾米A100S、A200(结构光)
  // 8:Pico DCAM710(ToF)
  private int cameraType = 0;
  // rgb和nir摄像头宽
  private int rgbAndNirWidth = 640;
  // rgb和nir摄像头高
  private int rgbAndNirHeight = 480;
  // depth摄像头宽
  private int depthWidth = 640;
  // depth摄像头高
  private int depthHeight = 400;
  // 光照设置，默认0.8.取值范围[0~1], 数值越大，光线越强
  private float illumination = 0.8f;

  // =================== 姿态控制 ======================
  private float pitch = 20;
  // 平面内旋转角[-180(逆时针), 180(顺时针)]，默认20
  private float roll = 20;
  // 三维旋转之左右旋转角[-90(左), 90(右)]，默认20
  private float yaw = 20;
  // 左眼被遮挡的阈值，默认0.6
  private float leftEye = 0.8f;
  // 右眼被遮挡的阈值，默认0.6
  private float rightEye = 0.8f;
  private float nose = 0.8f;
  // 嘴巴被遮挡的阈值，默认0.7
  private float mouth = 0.8f;
  // 左脸颊被遮挡的阈值，默认0.8
  private float leftCheek = 0.8f;
  // 右脸颊被遮挡的阈值，默认0.8
  private float rightCheek = 0.8f;
  // 下巴被遮挡阈值，默认为0.6
  private float chinContour = 1.0f;

  // =================== SDK 激活配置 ====================
  private String activeLicenseType = ACTIVE_LICENSE_OFFLINE;
  private String licenseKey = "";

  public int getMaxDetectNum() {
    return maxDetectNum;
  }

  public void setMaxDetectNum(int maxDetectNum) {
    this.maxDetectNum = maxDetectNum;
  }

  public Boolean getRgbRevert() {
    return rgbRevert;
  }

  public void setRgbRevert(Boolean rgbRevert) {
    this.rgbRevert = rgbRevert;
  }

  public float getFaceThreshold() {
    return faceThreshold;
  }

  public void setFaceThreshold(float faceThreshold) {
    this.faceThreshold = faceThreshold;
  }

  public float getBlur() {
    return blur;
  }

  public void setBlur(float blur) {
    this.blur = blur;
  }

  public String getActiveLicenseType() {
    return activeLicenseType;
  }

  public void setActiveLicenseType(String activeLicenseType) {
    this.activeLicenseType = activeLicenseType;
  }

  public String getFaceImagesPath() {
    return faceImagesPath;
  }

  public void setFaceImagesPath(String faceImagesPath) {
    this.faceImagesPath = faceImagesPath;
  }

  public String getLicenseKey() {
    return licenseKey;
  }

  public void setLicenseKey(String licenseKey) {
    this.licenseKey = licenseKey;
  }

  public int getMinimumFace() {
    return minimumFace;
  }

  public void setMinimumFace(int minimumFace) {
    this.minimumFace = minimumFace;
  }

  public boolean isQualityControl() {
    return qualityControl;
  }

  public void setQualityControl(boolean qualityControl) {
    this.qualityControl = qualityControl;
  }

  public int getRgbVideoDirection() {
    return rgbVideoDirection;
  }

  public void setRgbVideoDirection(int rgbVideoDirection) {
    this.rgbVideoDirection = rgbVideoDirection;
  }

  public int getNirVideoDirection() {
    return nirVideoDirection;
  }

  public void setNirVideoDirection(int nirVideoDirection) {
    this.nirVideoDirection = nirVideoDirection;
  }

  public int getRgbDetectDirection() {
    return rgbDetectDirection;
  }

  public void setRgbDetectDirection(int rgbDetectDirection) {
    this.rgbDetectDirection = rgbDetectDirection;
  }

  public int getNirDetectDirection() {
    return nirDetectDirection;
  }

  public void setNirDetectDirection(int nirDetectDirection) {
    this.nirDetectDirection = nirDetectDirection;
  }

  public int getMirrorDetectRGB() {
    return mirrorDetectRGB;
  }

  public void setMirrorDetectRGB(int mirrorDetectRGB) {
    this.mirrorDetectRGB = mirrorDetectRGB;
  }

  public int getMirrorDetectNIR() {
    return mirrorDetectNIR;
  }

  public void setMirrorDetectNIR(int mirrorDetectNIR) {
    this.mirrorDetectNIR = mirrorDetectNIR;
  }

  public boolean isBestImage() {
    return bestImage;
  }

  public void setBestImage(boolean bestImage) {
    this.bestImage = bestImage;
  }

  public float getRgbLiveScore() {
    return rgbLiveScore;
  }

  public void setRgbLiveScore(float rgbLiveScore) {
    this.rgbLiveScore = rgbLiveScore;
  }

  public float getNirLiveScore() {
    return nirLiveScore;
  }

  public void setNirLiveScore(float nirLiveScore) {
    this.nirLiveScore = nirLiveScore;
  }

  public float getDepthLiveScore() {
    return depthLiveScore;
  }

  public void setDepthLiveScore(float depthLiveScore) {
    this.depthLiveScore = depthLiveScore;
  }

  public int getCameraType() {
    return cameraType;
  }

  public void setCameraType(int cameraType) {
    this.cameraType = cameraType;
  }

  public int getRgbAndNirWidth() {
    return rgbAndNirWidth;
  }

  public void setRgbAndNirWidth(int rgbAndNirWidth) {
    this.rgbAndNirWidth = rgbAndNirWidth;
  }

  public int getRgbAndNirHeight() {
    return rgbAndNirHeight;
  }

  public void setRgbAndNirHeight(int rgbAndNirHeight) {
    this.rgbAndNirHeight = rgbAndNirHeight;
  }

  public int getDepthWidth() {
    return depthWidth;
  }

  public void setDepthWidth(int depthWidth) {
    this.depthWidth = depthWidth;
  }

  public int getDepthHeight() {
    return depthHeight;
  }

  public void setDepthHeight(int depthHeight) {
    this.depthHeight = depthHeight;
  }

  public int getActiveModel() {
    return activeModel;
  }

  public void setActiveModel(int activeModel) {
    this.activeModel = activeModel;
  }

  public float getIllumination() {
    return illumination;
  }

  public void setIllumination(float illumination) {
    this.illumination = illumination;
  }

  public float getLiveScoreThreshold() {
    return liveScoreThreshold;
  }

  public void setLiveScoreThreshold(float liveScoreThreshold) {
    this.liveScoreThreshold = liveScoreThreshold;
  }

  public float getIdScoreThreshold() {
    return idScoreThreshold;
  }

  public void setIdScoreThreshold(float idScoreThreshold) {
    this.idScoreThreshold = idScoreThreshold;
  }

  public float getRgbAndNirScoreThreshold() {
    return rgbAndNirScoreThreshold;
  }

  public void setRgbAndNirScoreThreshold(float rgbAndNirScoreThreshold) {
    this.rgbAndNirScoreThreshold = rgbAndNirScoreThreshold;
  }

  public float getPitch() {
    return pitch;
  }

  public void setPitch(float pitch) {
    this.pitch = pitch;
  }

  public float getRoll() {
    return roll;
  }

  public void setRoll(float roll) {
    this.roll = roll;
  }

  public float getYaw() {
    return yaw;
  }

  public void setYaw(float yaw) {
    this.yaw = yaw;
  }

  public float getLeftEye() {
    return leftEye;
  }

  public void setLeftEye(float leftEye) {
    this.leftEye = leftEye;
  }

  public float getRightEye() {
    return rightEye;
  }

  public void setRightEye(float rightEye) {
    this.rightEye = rightEye;
  }

  public float getNose() {
    return nose;
  }

  public void setNose(float nose) {
    this.nose = nose;
  }

  public float getMouth() {
    return mouth;
  }

  public void setMouth(float mouth) {
    this.mouth = mouth;
  }

  public float getLeftCheek() {
    return leftCheek;
  }

  public void setLeftCheek(float leftCheek) {
    this.leftCheek = leftCheek;
  }

  public float getRightCheek() {
    return rightCheek;
  }

  public void setRightCheek(float rightCheek) {
    this.rightCheek = rightCheek;
  }

  public float getChinContour() {
    return chinContour;
  }

  public void setChinContour(float chinContour) {
    this.chinContour = chinContour;
  }

  public int getCameraDriveType() {
    return cameraDriveType;
  }

  public void setCameraDriveType(int cameraDriveType) {
    this.cameraDriveType = cameraDriveType;
  }

  public String getRgbCameraName() {
    return rgbCameraName;
  }

  public void setRgbCameraName(String rgbCameraName) {
    this.rgbCameraName = rgbCameraName;
  }

  public String getNirCameraName() {
    return nirCameraName;
  }

  public void setNirCameraName(String nirCameraName) {
    this.nirCameraName = nirCameraName;
  }

  public String getDepthCameraName() {
    return depthCameraName;
  }

  public void setDepthCameraName(String depthCameraName) {
    this.depthCameraName = depthCameraName;
  }

  public int getMaxSearchNum() {
    return maxSearchNum;
  }

  public void setMaxSearchNum(int maxSearchNum) {
    this.maxSearchNum = maxSearchNum;
  }

  public boolean isMirrorVideoRGB() {
    return mirrorVideoRGB;
  }

  public boolean isMirrorVideoNIR() {
    return mirrorVideoNIR;
  }

  public void setMirrorVideoRGB(boolean mirrorVideoRGB) {
    this.mirrorVideoRGB = mirrorVideoRGB;
  }

  public void setMirrorVideoNIR(boolean mirrorVideoNIR) {
    this.mirrorVideoNIR = mirrorVideoNIR;
  }

  public void initConfig(Map<String, Object> arguments) {
    String rgbCameraName = (String) arguments.get("rgbCameraName");
    if (rgbCameraName != null) {
      this.rgbCameraName = rgbCameraName;
    }
    String nirCameraName = (String) arguments.get("nirCameraName");
    if (nirCameraName != null) {
      this.nirCameraName = nirCameraName;
    }
    String depthCameraName = (String) arguments.get("depthCameraName");
    if (depthCameraName != null) {
      this.depthCameraName = depthCameraName;
    }
    int cameraDriveType = (int) arguments.get("cameraDriveType");
    if (cameraDriveType != 0) {
      this.cameraDriveType = cameraDriveType;
    }
    int rgbAndNirWidth = (int) arguments.get("rgbAndNirWidth");
    if (rgbAndNirWidth != 0) {
      this.rgbAndNirWidth = rgbAndNirWidth;
    }
    int rgbAndNirHeight = (int) arguments.get("rgbAndNirHeight");
    if (rgbAndNirHeight != 0) {
      this.rgbAndNirHeight = rgbAndNirHeight;
    }
    int depthWidth = (int) arguments.get("depthWidth");
    if (depthWidth != 0) {
      this.depthWidth = depthWidth;
    }
    int depthHeight = (int) arguments.get("depthHeight");
    if (depthHeight != 0) {
      this.depthHeight = depthHeight;
    }
    int activeModel = (int) arguments.get("activeModel");
    if (activeModel != 0) {
      this.activeModel = activeModel;
    }
    float liveScoreThreshold = ((Double) arguments.get("liveScoreThreshold")).floatValue();
    if (liveScoreThreshold != 0) {
      this.liveScoreThreshold = liveScoreThreshold;
    }
    float idScoreThreshold = ((Double) arguments.get("idScoreThreshold")).floatValue();
    if (idScoreThreshold != 0) {
      this.idScoreThreshold = idScoreThreshold;
    }
    float rgbAndNirScoreThreshold = ((Double) arguments.get("rgbAndNirScoreThreshold")).floatValue();
    if (rgbAndNirScoreThreshold != 0) {
      this.rgbAndNirScoreThreshold = rgbAndNirScoreThreshold;
    }
    int maxSearchNum = (int) arguments.get("maxSearchNum");
    if (maxSearchNum != 0) {
      this.maxSearchNum = maxSearchNum;
    }
    Boolean mirrorVideoRGB = (Boolean) arguments.get("mirrorVideoRGB");
    if (mirrorVideoRGB != null) {
      this.mirrorVideoRGB = mirrorVideoRGB;
    }
    Boolean mirrorVideoNIR = (Boolean) arguments.get("mirrorVideoNIR");
    if (mirrorVideoNIR != null) {
      this.mirrorVideoNIR = mirrorVideoNIR;
    }
    int rgbVideoDirection = (int) arguments.get("rgbVideoDirection");
    if (rgbVideoDirection != 0) {
      this.rgbVideoDirection = rgbVideoDirection;
    }
    int nirVideoDirection = (int) arguments.get("nirVideoDirection");
    if (nirVideoDirection != 0) {
      this.nirVideoDirection = nirVideoDirection;
    }
    String faceImagesPath = (String) arguments.get("faceImagesPath");
    if (faceImagesPath != null) {
      this.faceImagesPath = faceImagesPath;
    }
  }

  private static class HolderClass {
    private static final ArcFaceConfig ARC_FACE_CONFIG = new ArcFaceConfig();
  }

  public static ArcFaceConfig getInstance() {
    return HolderClass.ARC_FACE_CONFIG;
  }
}
