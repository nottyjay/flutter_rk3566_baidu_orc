package com.aialphay.android.rk3568.arcface.engine;

import static com.aialphay.android.rk3568.arcface.ArcFaceConstants.ACTIVE_LICENSE_OFFLINE;
import static com.aialphay.android.rk3568.arcface.ArcFaceConstants.ACTIVE_LICENSE_ONLINE;
import static com.aialphay.android.rk3568.arcface.ArcFaceConstants.FEATURE_SIZE;
import static com.aialphay.android.rk3568.arcface.ArcFaceConstants.SDK_MODEL_LOAD_SUCCESS;
import static com.aialphay.android.rk3568.arcface.ArcFaceConstants.SDK_UNACTIVATION;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import com.aialphay.android.rk3568.arcface.ArcFaceConstants;
import com.aialphay.android.rk3568.arcface.bean.LivenessModel;
import com.aialphay.android.rk3568.arcface.bean.User;
import com.aialphay.android.rk3568.arcface.callback.ArcFaceEngineCallback;
import com.aialphay.android.rk3568.arcface.callback.DataBaseListener;
import com.aialphay.android.rk3568.arcface.callback.FaceDetectCallBack;
import com.aialphay.android.rk3568.arcface.callback.ModelInitCallback;
import com.aialphay.android.rk3568.arcface.config.ArcFaceConfig;
import com.aialphay.android.rk3568.arcface.db.DBManager;
import com.aialphay.android.rk3568.arcface.manager.DataBaseManager;
import com.aialphay.android.rk3568.arcface.manager.ModelManager;
import com.aialphay.android.rk3568.qrcode.QrCodeParser;
import com.baidu.idl.main.facesdk.FaceAuth;
import com.baidu.idl.main.facesdk.FaceInfo;
import com.baidu.idl.main.facesdk.callback.Callback;
import com.baidu.idl.main.facesdk.model.BDFaceDetectListConf;
import com.baidu.idl.main.facesdk.model.BDFaceImageInstance;
import com.baidu.idl.main.facesdk.model.BDFaceOcclusion;
import com.baidu.idl.main.facesdk.model.BDFaceSDKCommon;
import com.baidu.idl.main.facesdk.model.Feature;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

public class ArcFaceEngine {

  private static final String TAG = "ArcFaceEngine";
  private static volatile Context context;

  private ArcFaceEngineCallback arcFaceEngineCallback;

  private FaceAuth faceAuth;
  public static volatile int initStatus = SDK_UNACTIVATION;

  private int mLastFaceId;
  private List<Boolean> mRgbLiveList = new ArrayList<>();
  private List<Boolean> mNirLiveList = new ArrayList<>();
  private float mRgbLiveScore;
  private float mNirLiveScore;

  public String getDeviceId() {
    return faceAuth.getDeviceId(this.context);
  }

  public void userClean() {
    DataBaseManager.getInstance().clearTable();
  }

  public List<User> getAllUserList() {
    return DataBaseManager.getInstance().getAllUserList();
  }

  public void setPush(boolean b) {
    DataBaseManager.getInstance().setPush(b);
  }

  public int getRegisterUserNum() {
    return DataBaseManager.getInstance().getUserCount();
  }

  public boolean registerUserIntoDBmanager(
      String groupId,
      String userId,
      String username,
      String picName,
      String userInfo,
      byte[] faceFeature) {
    boolean isSuccess = false;

    User user = new User();
    user.setGroupId(DBManager.GROUP_ID);
    // 用户id（由数字、字母、下划线组成），长度限制128B
    // uid为用户的id,百度对uid不做限制和处理，应该与您的帐号系统中的用户id对应。
    user.setUserId(userId);
    user.setUserName(username);
    user.setFeature(faceFeature);
    user.setImageName(picName);
    if (userInfo != null) {
      user.setUserInfo(userInfo);
    }
    // 添加用户信息到数据库
    return DataBaseManager.getInstance().userAdd(user);
  }

  public float personDetect(Bitmap bitmap, byte[] feature) {
    BDFaceImageInstance rgbInstance = new BDFaceImageInstance(bitmap);
    float ret = -1;
    //        FaceInfo[] faceInfos = FaceSDKManager.getInstance().getFaceDetect()
    //                .detect(BDFaceSDKCommon.DetectType.DETECT_VIS, rgbInstance);
    FaceInfo[] faceInfos =
        ModelManager.getInstance()
            .getFaceDetectPerson()
            .detect(BDFaceSDKCommon.DetectType.DETECT_VIS, rgbInstance);

    if (faceInfos != null && faceInfos.length > 0) {
      // 判断质量检测，针对模糊度、遮挡、
      ret =
          ModelManager.getInstance()
              .getFaceFeaturePerson()
              .feature(
                  BDFaceSDKCommon.FeatureType.BDFACE_FEATURE_TYPE_LIVE_PHOTO,
                  rgbInstance,
                  faceInfos[0].landmarks,
                  feature);
    } else {
      rgbInstance.destory();
      return -1;
    }
    rgbInstance.destory();
    return ret;
  }

  public void initConfig(Map<String, Object> arguments) {
    ArcFaceConfig.getInstance().initConfig(arguments);
  }

  private static class HolderClass {
    private static final ArcFaceEngine ARC_FACE_ENGINE = new ArcFaceEngine();
  }

  public static ArcFaceEngine getInstance() {
    return ArcFaceEngine.HolderClass.ARC_FACE_ENGINE;
  }

  public void initEngine(Context context, ArcFaceEngineCallback callback) {
    this.context = context;
    this.arcFaceEngineCallback = callback;
    // 初始化百度 FaceAuth
    faceAuth = new FaceAuth();
    initLicense();
  }

  /**
   * 检测-活体-特征-人脸检索流程
   *
   * @param rgbData            可见光YUV 数据流
   * @param irData             红外YUV 数据流
   * @param depthData          深度depth 数据流
   * @param width              可见光YUV 数据流-宽度
   * @param height             可见光YUV 数据流-高度
   * @param liveCheckMode      活体检测类型【不使用活体：0】；【RGB活体：1】；【RGB+NIR活体：2】；【RGB+Depth活体：3】；【RGB+NIR+Depth活体：4】
   * @param faceDetectCallBack
   */
  public void searchPerson(
      Bitmap rgbData,
      Bitmap irData,
      Bitmap depthData,
      int width,
      int height,
      int liveCheckMode,
      FaceDetectCallBack faceDetectCallBack) {
    detectCheck(rgbData, irData, depthData, width, height, liveCheckMode, 3, faceDetectCallBack);
  }

  /**
   * 初始化授权
   */
  private void initLicense() {
    if (ACTIVE_LICENSE_ONLINE.equals(ArcFaceConfig.getInstance().getActiveLicenseType())) {
      faceAuth.initLicenseOnLine(
          context,
          ArcFaceConfig.getInstance().getLicenseKey(),
          new Callback() {
            @Override
            public void onResponse(int i, String s) {
              checkActiveLicenseResponse(i, s);
            }
          });
    } else if (ACTIVE_LICENSE_OFFLINE.equals(ArcFaceConfig.getInstance().getActiveLicenseType())) {
      faceAuth.initLicenseOffLine(
          context,
          new Callback() {
            @Override
            public void onResponse(int i, String s) {
              checkActiveLicenseResponse(i, s);
            }
          });
    } else if (ACTIVE_LICENSE_OFFLINE.equals(ArcFaceConfig.getInstance().getActiveLicenseType())) {
      faceAuth.initLicenseBatchLine(
          context,
          ArcFaceConfig.getInstance().getLicenseKey(),
          new Callback() {
            @Override
            public void onResponse(int i, String s) {
              checkActiveLicenseResponse(i, s);
            }

            ;
          });
    }
  }

  /**
   * 检测激活授权结果
   *
   * @param i 激活授权结果码
   * @param s 激活授权结果信息
   */
  private void checkActiveLicenseResponse(int i, String s) {
    if (i == 0) {
      // 激活成功
      arcFaceEngineCallback.activeLicenseSuccess();
      initModel();
    } else {
      // 激活失败
      arcFaceEngineCallback.activeLicenseFail(i, s);
    }
  }

  /**
   * 初始化模型
   */
  private void initModel() {
    if (initStatus != SDK_MODEL_LOAD_SUCCESS) { // 模型尚未初始化时，初始化模型
      ModelManager.getInstance()
          .initModel(
              context,
              new ModelInitCallback() {
                @Override
                public void initModelSuccess() {
                  arcFaceEngineCallback.initModelSuccess();
                  DataBaseManager.getInstance()
                      .initDatabases(
                          ArcFaceEngine.context,
                          new DataBaseListener() {
                            @Override
                            public void initFullUsersSuccess() {
                              arcFaceEngineCallback.initFullUsersSuccess();
                            }
                          });
                }

                @Override
                public void initModelFail(int code, String response) {
                  arcFaceEngineCallback.initModelFail(code, response);
                }
              });
    }
  }

  /**
   * 检测-活体-特征-人脸检索流程
   *
   * @param rgbData            可见光YUV 数据流
   * @param irData             红外YUV 数据流
   * @param depthData          深度depth 数据流
   * @param width              可见光YUV 数据流-宽度
   * @param height             可见光YUV 数据流-高度
   * @param liveCheckMode      活体检测类型【不使用活体：0】；【RGB活体：1】；【RGB+NIR活体：2】；【RGB+Depth活体：3】；【RGB+NIR+Depth活体：4】
   * @param featureCheckMode   特征抽取模式【不提取特征：1】；【提取特征：2】；【提取特征+1：N检索：3】；
   * @param faceDetectCallBack
   */
  public void detectCheck(
      final Bitmap rgbData,
      final Bitmap irData,
      final Bitmap depthData,
      final int width,
      final int height,
      final int liveCheckMode,
      final int featureCheckMode,
      final FaceDetectCallBack faceDetectCallBack) {
    if (rgbData == null
        || rgbData.isRecycled()
        || (ArcFaceConstants.detectCheckFuture != null
        && !ArcFaceConstants.detectCheckFuture.isDone())) {
      return;
    }
    ArcFaceConstants.detectCheckFuture =
        ArcFaceConstants.detectCheckExecutorService.submit(
            new Runnable() {
              @Override
              public void run() {
                // 创建检测结果存储数据
                LivenessModel livenessModel = new LivenessModel();
                // 创建检测对象，如果原始数据为 YUV，转换算法为检测的图片 BGR
                BDFaceImageInstance rgbInstance;
                rgbInstance = new BDFaceImageInstance(rgbData);

                // 获取送检图片
                livenessModel.setBdFaceImageInstance(rgbInstance.getImage());

                // 快速检测获取人脸信息
                FaceInfo[] faceInfos =
                    ModelManager.getInstance()
                        .getFaceTrackDetect()
                        .track(
                            BDFaceSDKCommon.DetectType.DETECT_VIS,
                            BDFaceSDKCommon.AlignType.BDFACE_ALIGN_TYPE_RGB_FAST,
                            rgbInstance);
                if (faceInfos != null && faceInfos.length > 0) {
                  livenessModel.setTrackFaceInfo(faceInfos);
                  // TODO 检测到多个人脸, 获取多个人脸中最大的那张人脸（faceinfo.width * faceinfo.height
                  FaceInfo bestFaceInfo = faceInfos[0];
                  if (faceInfos.length > 1) {
                    for (int i = 1; i < faceInfos.length; i++) {
                      float currentArea = faceInfos[i].width * faceInfos[i].height;
                      float bestArea = bestFaceInfo.width * bestFaceInfo.height;
                      if (currentArea > bestArea) {
                        bestFaceInfo = faceInfos[i];
                      }
                    }
                  }

                  livenessModel.setFaceInfo(bestFaceInfo);
                  livenessModel.setTrackLandmarks(bestFaceInfo.landmarks);
                  livenessModel.setTrackStatus(1);
                  livenessModel.setLandmarks(bestFaceInfo.landmarks);
                  if (faceDetectCallBack != null) {
                    faceDetectCallBack.onFaceDetectDarwCallback(livenessModel);
                  }
                  // 活体检测
                  onLivenessCheck(
                      rgbInstance,
                      irData,
                      depthData,
                      width,
                      height,
                      livenessModel.getLandmarks(),
                      livenessModel,
                      liveCheckMode,
                      featureCheckMode,
                      faceDetectCallBack,
                      faceInfos);
                } else {
                  rgbInstance.destory();
                  if (faceDetectCallBack != null) {
                    faceDetectCallBack.onFaceDetectCallback(null);
                    faceDetectCallBack.onFaceDetectDarwCallback(null);
                    faceDetectCallBack.onTip(0, "未检测到人脸");
                  }
                }
              }
            });
  }

  /**
   * 活体-特征-人脸检索全流程
   *
   * @param rgbInstance        可见光底层送检对象
   * @param irData             红外YUV 数据流
   * @param depthData          深度depth 数据流
   * @param width              可见光YUV 数据流-宽度
   * @param height             可见光YUV 数据流-高度
   * @param landmarks          检测眼睛，嘴巴，鼻子，72个关键点
   * @param livenessModel      检测结果数据集合
   * @param liveCheckMode      活体检测模式【不使用活体：0】；【RGB活体：1】；【RGB+NIR活体：2】；【RGB+Depth活体：3】；【RGB+NIR+Depth活体：4】
   * @param featureCheckMode   特征抽取模式【不提取特征：1】；【提取特征：2】；【提取特征+1：N检索：3】；
   * @param faceDetectCallBack
   * @param fastFaceInfos      人脸信息
   */
  public void onLivenessCheck(
      BDFaceImageInstance rgbInstance,
      Bitmap irData,
      Bitmap depthData,
      int width,
      int height,
      float[] landmarks,
      LivenessModel livenessModel,
      int liveCheckMode,
      int featureCheckMode,
      FaceDetectCallBack faceDetectCallBack,
      FaceInfo[] fastFaceInfos) {
    if (ArcFaceConstants.livenessCheckFuture != null
        && !ArcFaceConstants.livenessCheckFuture.isDone()) {
      rgbInstance.destory();
      return;
    }
    ArcFaceConstants.livenessCheckFuture =
        ArcFaceConstants.livenessCheckExecutorService.submit(
            new Runnable() {
              @Override
              public void run() {
                BDFaceDetectListConf bdFaceDetectListConfig = new BDFaceDetectListConf();
                bdFaceDetectListConfig.usingQuality =
                    bdFaceDetectListConfig.usingHeadPose =
                        ArcFaceConfig.getInstance().isQualityControl();
                bdFaceDetectListConfig.usingBestImage = ArcFaceConfig.getInstance().isBestImage();
                FaceInfo[] faceInfos =
                    ModelManager.getInstance()
                        .getFaceDetect()
                        .detect(
                            BDFaceSDKCommon.DetectType.DETECT_VIS,
                            BDFaceSDKCommon.AlignType.BDFACE_ALIGN_TYPE_RGB_ACCURATE,
                            rgbInstance,
                            fastFaceInfos,
                            bdFaceDetectListConfig);

                // 重新赋予详细人脸信息
                if (faceInfos != null && faceInfos.length > 0) {
                  faceInfos[0].faceID = livenessModel.getFaceInfo().faceID;

                  livenessModel.setFaceInfo(faceInfos[0]);
                  livenessModel.setTrackStatus(2);
                  livenessModel.setLandmarks(faceInfos[0].landmarks);
                  if (mLastFaceId != fastFaceInfos[0].faceID) {
                    mLastFaceId = fastFaceInfos[0].faceID;
                    mRgbLiveList.clear();
                    mNirLiveList.clear();
                  }
                } else {
                  rgbInstance.destory();
                  if (faceDetectCallBack != null) {
                    faceDetectCallBack.onFaceDetectCallback(livenessModel);
                  }
                  return;
                }

                // 最优人脸控制
                if (!onBestImageCheck(livenessModel, faceDetectCallBack)) {
                  livenessModel.setQualityCheck(false);
                  rgbInstance.destory();
                  if (faceDetectCallBack != null) {
                    faceDetectCallBack.onFaceDetectCallback(livenessModel);
                  }
                  return;
                }
                float[] scores =
                    ModelManager.getInstance().getFaceMouthMask().checkMask(rgbInstance, faceInfos);
                if (scores != null && scores.length > 0) {
                  livenessModel.setMaskScore(scores[0]);
                }
                // 质量检测未通过，销毁BDFaceImageInstance，结束函数
                if (!onQualityCheck(livenessModel, faceDetectCallBack)) {
                  livenessModel.setQualityCheck(false);
                  rgbInstance.destory();
                  if (faceDetectCallBack != null) {
                    faceDetectCallBack.onFaceDetectCallback(livenessModel);
                  }
                  return;
                }
                livenessModel.setQualityCheck(true);
                // 活体检测
                float rgbScore = -1;
                if (liveCheckMode != 0) {
                  rgbScore =
                      ModelManager.getInstance()
                          .getFaceLiveness()
                          .silentLive(
                              BDFaceSDKCommon.LiveType.BDFACE_SILENT_LIVE_TYPE_RGB,
                              rgbInstance,
                              faceInfos[0].landmarks);
                  mRgbLiveList.add(rgbScore > mRgbLiveScore);
                  while (mRgbLiveList.size() > 6) {
                    mRgbLiveList.remove(0);
                  }
                  if (mRgbLiveList.size() > 2) {
                    int rgbSum = 0;
                    for (Boolean b : mRgbLiveList) {
                      if (b) {
                        rgbSum++;
                      }
                    }
                    if (1.0 * rgbSum / mRgbLiveList.size() > 0.6) {
                      if (rgbScore < mRgbLiveScore) {
                        rgbScore = mRgbLiveScore + (1 - mRgbLiveScore) * new Random().nextFloat();
                      }
                    } else {
                      if (rgbScore > mRgbLiveScore) {
                        rgbScore = new Random().nextFloat() * mRgbLiveScore;
                      }
                    }
                  }
                  livenessModel.setRgbLivenessScore(rgbScore);

                  // ir摄像头 人体活体检测
                  float nirScore = -1;
                  FaceInfo[] faceInfosIr = null;
                  BDFaceImageInstance nirInstance = null;
                  if (liveCheckMode == 2 || liveCheckMode == 4 && irData != null) {
                    nirInstance = new BDFaceImageInstance(irData);
                    livenessModel.setBdNirFaceImageInstance(nirInstance.getImage());

                    BDFaceDetectListConf bdFaceDetectListConf = new BDFaceDetectListConf();
                    bdFaceDetectListConf.usingDetect = true;
                    faceInfosIr =
                        ModelManager.getInstance()
                            .getFaceDetectNir()
                            .detect(
                                BDFaceSDKCommon.DetectType.DETECT_NIR,
                                BDFaceSDKCommon.AlignType.BDFACE_ALIGN_TYPE_NIR_ACCURATE,
                                nirInstance,
                                null,
                                bdFaceDetectListConf);
                    bdFaceDetectListConf.usingDetect = false;

                    if (faceInfosIr != null && faceInfosIr.length > 0) {
                      long startNirTime = System.currentTimeMillis();
                      FaceInfo faceInfoIr = faceInfosIr[0];
                      nirScore =
                          ModelManager.getInstance()
                              .getFaceLiveness()
                              .silentLive(
                                  BDFaceSDKCommon.LiveType.BDFACE_SILENT_LIVE_TYPE_NIR,
                                  nirInstance,
                                  faceInfoIr.landmarks);
                      mNirLiveList.add(nirScore > mNirLiveScore);
                      while (mNirLiveList.size() > 6) {
                        mNirLiveList.remove(0);
                      }
                      if (mNirLiveList.size() > 2) {
                        int nirSum = 0;
                        for (Boolean b : mNirLiveList) {
                          if (b) {
                            nirSum++;
                          }
                        }
                        if (1.0f * nirSum / mNirLiveList.size() > 0.6) {
                          if (nirScore < mNirLiveScore) {
                            nirScore =
                                mNirLiveScore + new Random().nextFloat() * (1 - mNirLiveScore);
                          }
                        } else {
                          if (nirScore > mNirLiveScore) {
                            nirScore = new Random().nextFloat() * mNirLiveScore;
                          }
                        }
                      }
                      livenessModel.setIrLivenessScore(nirScore);
                    }
                  }

                  // depth摄像头 人体活体检测
                  float depthScore = -1;
                  if (liveCheckMode == 3 || liveCheckMode == 4 && depthData != null) {
                    // TODO: 用户调整旋转角度和是否镜像，适配Atlas 镜头，目前宽和高400*640，其他摄像头需要动态调整,人脸72 个关键点x
                    // 坐标向左移动80个像素点
                    float[] depthLandmark = new float[faceInfos[0].landmarks.length];
                    BDFaceImageInstance depthInstance;
                    if (ArcFaceConfig.getInstance().getCameraType() == 1) {
                      System.arraycopy(
                          faceInfos[0].landmarks,
                          0,
                          depthLandmark,
                          0,
                          faceInfos[0].landmarks.length);
                      if (ArcFaceConfig.getInstance().getCameraType() == 1) {
                        for (int i = 0; i < 144; i = i + 2) {
                          depthLandmark[i] -= 80;
                        }
                      }
                      depthInstance = new BDFaceImageInstance(depthData);
                    } else {
                      depthInstance = new BDFaceImageInstance(depthData);
                    }

                    livenessModel.setBdDepthFaceImageInstance(depthInstance.getImage());
                    // 创建检测对象，如果原始数据Depth
                    if (ArcFaceConfig.getInstance().getCameraType() == 1) {
                      depthScore =
                          ModelManager.getInstance()
                              .getFaceLiveness()
                              .silentLive(
                                  BDFaceSDKCommon.LiveType.BDFACE_SILENT_LIVE_TYPE_DEPTH,
                                  depthInstance,
                                  depthLandmark);
                    } else {
                      depthScore =
                          ModelManager.getInstance()
                              .getFaceLiveness()
                              .silentLive(
                                  BDFaceSDKCommon.LiveType.BDFACE_SILENT_LIVE_TYPE_DEPTH,
                                  depthInstance,
                                  faceInfos[0].landmarks);
                    }
                    livenessModel.setDepthLivenessScore(depthScore);
                    depthInstance.destory();
                  }

                  // 特征提取+人脸搜索
                  if (liveCheckMode == 0) {
                    // TODO 特征提取+人脸检索
                    onFeatureCheck(
                        rgbInstance,
                        faceInfos[0].landmarks,
                        faceInfosIr,
                        nirInstance,
                        livenessModel,
                        featureCheckMode,
                        ArcFaceConfig.getInstance().getActiveModel());
                  } else {
                    if (liveCheckMode == 1
                        && rgbScore > ArcFaceConfig.getInstance().getRgbLiveScore()) {
                      onFeatureCheck(
                          rgbInstance,
                          faceInfos[0].landmarks,
                          faceInfosIr,
                          nirInstance,
                          livenessModel,
                          featureCheckMode,
                          1);
                    } else if (liveCheckMode == 2
                        && rgbScore > ArcFaceConfig.getInstance().getRgbLiveScore()
                        && nirScore > ArcFaceConfig.getInstance().getNirLiveScore()) {
                      onFeatureCheck(
                          rgbInstance,
                          faceInfos[0].landmarks,
                          faceInfosIr,
                          nirInstance,
                          livenessModel,
                          featureCheckMode,
                          ArcFaceConfig.getInstance().getActiveModel());
                    } else if (liveCheckMode == 3
                        && rgbScore > ArcFaceConfig.getInstance().getRgbLiveScore()
                        && nirScore > ArcFaceConfig.getInstance().getNirLiveScore()
                        && depthScore > ArcFaceConfig.getInstance().getDepthLiveScore()) {
                      onFeatureCheck(
                          rgbInstance,
                          faceInfos[0].landmarks,
                          faceInfosIr,
                          nirInstance,
                          livenessModel,
                          featureCheckMode,
                          1);
                    } else if (liveCheckMode == 4
                        && rgbScore > ArcFaceConfig.getInstance().getRgbLiveScore()
                        && nirScore > ArcFaceConfig.getInstance().getNirLiveScore()
                        && depthScore > ArcFaceConfig.getInstance().getDepthLiveScore()) {
                      onFeatureCheck(
                          rgbInstance,
                          faceInfos[0].landmarks,
                          faceInfosIr,
                          nirInstance,
                          livenessModel,
                          featureCheckMode,
                          ArcFaceConfig.getInstance().getActiveModel());
                    }
                  }
                  if (faceDetectCallBack != null) {
                    faceDetectCallBack.onFaceDetectCallback(livenessModel);
                  }
                }
              }
            });
  }

  /**
   * 特征提取-人脸识别比对
   *
   * @param rgbInstance      可见光底层送检对象
   * @param landmark         检测眼睛，嘴巴，鼻子，72个关键点
   * @param faceInfos        nir人脸数据
   * @param nirInstance      nir 图像句柄
   * @param livenessModel    检测结果数据集合
   * @param featureCheckMode 特征抽取模式【不提取特征：1】；【提取特征：2】；【提取特征+1：N检索：3】；
   * @param featureType      特征抽取模态执行 【生活照：1】；【证件照：2】；【混合模态：3】；
   */
  public void onFeatureCheck(
      final BDFaceImageInstance rgbInstance,
      final float[] landmark,
      final FaceInfo[] faceInfos,
      final BDFaceImageInstance nirInstance,
      final LivenessModel livenessModel,
      final int featureCheckMode,
      final int featureType) {
    if (featureCheckMode == 1) {
      return;
    }
    byte[] feature;
    if (featureType == 3) {
      AtomicInteger atomicInteger = new AtomicInteger();
      ModelManager.getInstance().getImageIllum().imageIllum(rgbInstance, atomicInteger);
      int illumScore = atomicInteger.get();
      if (illumScore < ArcFaceConfig.getInstance().getIllumination()) {
        feature = new byte[512];
        if (faceInfos != null && nirInstance != null) {
          float featureSize =
              ModelManager.getInstance()
                  .getFaceFeature()
                  .feature(
                      BDFaceSDKCommon.FeatureType.BDFACE_FEATURE_TYPE_NIR,
                      nirInstance,
                      faceInfos[0].landmarks,
                      feature);
          livenessModel.setFeature(feature);
          featureSearch(
              featureCheckMode,
              livenessModel,
              feature,
              featureSize,
              FEATURE_SIZE / 8,
              BDFaceSDKCommon.FeatureType.BDFACE_FEATURE_TYPE_NIR);
        }
      } else {
        feature = new byte[1024];
        float featureSize =
            ModelManager.getInstance()
                .getFaceFeature()
                .feature(
                    BDFaceSDKCommon.FeatureType.BDFACE_FEATURE_TYPE_LIVE_PHOTO,
                    rgbInstance,
                    landmark,
                    feature);
        featureSearch(
            featureCheckMode,
            livenessModel,
            feature,
            featureSize,
            FEATURE_SIZE / 4,
            BDFaceSDKCommon.FeatureType.BDFACE_FEATURE_TYPE_LIVE_PHOTO);
      }
    } else {
      feature = new byte[1024];
      float featureSize =
          ModelManager.getInstance()
              .getFaceFeature()
              .feature(
                  BDFaceSDKCommon.FeatureType.BDFACE_FEATURE_TYPE_LIVE_PHOTO,
                  rgbInstance,
                  landmark,
                  feature);
      livenessModel.setFeature(feature);
      // 人脸检索
      featureSearch(
          featureCheckMode,
          livenessModel,
          feature,
          featureSize,
          FEATURE_SIZE / 4,
          BDFaceSDKCommon.FeatureType.BDFACE_FEATURE_TYPE_LIVE_PHOTO);
    }
  }

  /**
   * 人脸库检索
   *
   * @param featureCheckMode 特征抽取模式【不提取特征：1】；【提取特征：2】；【提取特征+1：N检索：3】；
   * @param livenessModel    检测结果数据集合
   * @param feature          特征点
   * @param featureSize      特征点的size
   * @param type             特征提取类型
   */
  private void featureSearch(
      final int featureCheckMode,
      final LivenessModel livenessModel,
      final byte[] feature,
      final float featureSize,
      float ret,
      BDFaceSDKCommon.FeatureType type) {
    if (featureCheckMode == 2) {
      livenessModel.setFeatureCode(featureSize);
      return;
    }
    if (featureSize == ret) {
      List<Feature> featureResult =
          ModelManager.getInstance()
              .getFaceFeature()
              .featureSearch(feature, type, ArcFaceConfig.getInstance().getMaxSearchNum(), false);
      if (featureResult != null && featureResult.size() > 0) {
        List<User> users = new ArrayList<>();
        float threholdScore = 0;
        if (ArcFaceConfig.getInstance().getActiveModel() == 1) {
          threholdScore = ArcFaceConfig.getInstance().getLiveScoreThreshold();
        } else if (ArcFaceConfig.getInstance().getActiveModel() == 2) {
          threholdScore = ArcFaceConfig.getInstance().getIdScoreThreshold();
        } else if (ArcFaceConfig.getInstance().getActiveModel() == 3) {
          threholdScore = ArcFaceConfig.getInstance().getRgbAndNirScoreThreshold();
        }
        for (Feature topFeature : featureResult) {
          User tmpUser = DataBaseManager.getInstance().getUserById(topFeature.getId());
          Log.d(TAG, "搜索疑似人员ID: " + tmpUser.getId() + ", 姓名：" + tmpUser.getUserName() + ", 得分：" + topFeature.getScore());
          if (topFeature != null && topFeature.getScore() > threholdScore) {
            User user = DataBaseManager.getInstance().getUserById(topFeature.getId());
            if (user != null) {
              user.setFeatureScore(topFeature.getScore());
              users.add(user);
            }
          }
        }
        livenessModel.setUsers(users);
      }
    }
  }

  /**
   * 质量检测结果过滤，如果需要质量检测， 需要调用 ArcFaceConfig.getInstance().setQualityControl(true);设置为true， 再调用
   * FaceSDKManager.getInstance().initConfig() 加载到底层配置项中
   *
   * @param livenessModel
   * @param faceDetectCallBack
   * @return
   */
  public boolean onQualityCheck(
      final LivenessModel livenessModel, final FaceDetectCallBack faceDetectCallBack) {

    if (!ArcFaceConfig.getInstance().isQualityControl()) {
      return true;
    }

    if (livenessModel != null && livenessModel.getFaceInfo() != null) {

      // 角度过滤
      if (Math.abs(livenessModel.getFaceInfo().yaw) > ArcFaceConfig.getInstance().getYaw()) {
        faceDetectCallBack.onTip(-1, "人脸左右偏转角超出限制");
        return false;
      } else if (Math.abs(livenessModel.getFaceInfo().roll)
          > ArcFaceConfig.getInstance().getRoll()) {
        faceDetectCallBack.onTip(-1, "人脸平行平面内的头部旋转角超出限制");
        return false;
      } else if (Math.abs(livenessModel.getFaceInfo().pitch)
          > ArcFaceConfig.getInstance().getPitch()) {
        faceDetectCallBack.onTip(-1, "人脸上下偏转角超出限制");
        return false;
      }

      // 模糊结果过滤
      float blur = livenessModel.getFaceInfo().bluriness;
      if (blur > ArcFaceConfig.getInstance().getBlur()) {
        faceDetectCallBack.onTip(-1, "图片模糊");
        return false;
      }

      // 光照结果过滤
      float illum = livenessModel.getFaceInfo().illum;
      Log.e("illum", "illum = " + illum);
      if (illum < ArcFaceConfig.getInstance().getIllumination()) {
        faceDetectCallBack.onTip(-1, "图片光照不通过");
        return false;
      }

      // 遮挡结果过滤
      if (livenessModel.getFaceInfo().occlusion != null) {
        BDFaceOcclusion occlusion = livenessModel.getFaceInfo().occlusion;

        if (occlusion.leftEye > ArcFaceConfig.getInstance().getLeftEye()) {
          // 左眼遮挡置信度
          faceDetectCallBack.onTip(-1, "左眼遮挡");
        } else if (occlusion.rightEye > ArcFaceConfig.getInstance().getRightEye()) {
          // 右眼遮挡置信度
          faceDetectCallBack.onTip(-1, "右眼遮挡");
        } else if (occlusion.nose > ArcFaceConfig.getInstance().getNose()) {
          // 鼻子遮挡置信度
          faceDetectCallBack.onTip(-1, "鼻子遮挡");
        } else if (occlusion.mouth > ArcFaceConfig.getInstance().getMouth()) {
          // 嘴巴遮挡置信度
          faceDetectCallBack.onTip(-1, "嘴巴遮挡");
        } else if (occlusion.leftCheek > ArcFaceConfig.getInstance().getLeftCheek()) {
          // 左脸遮挡置信度
          faceDetectCallBack.onTip(-1, "左脸遮挡");
        } else if (occlusion.rightCheek > ArcFaceConfig.getInstance().getRightCheek()) {
          // 右脸遮挡置信度
          faceDetectCallBack.onTip(-1, "右脸遮挡");
        } else if (occlusion.chin > ArcFaceConfig.getInstance().getChinContour()) {
          // 下巴遮挡置信度
          faceDetectCallBack.onTip(-1, "下巴遮挡");
        } else {
          return true;
        }
      }
    }
    return false;
  }

  /**
   * 最优人脸控制
   *
   * @param livenessModel
   * @param faceDetectCallBack
   * @return
   */
  public boolean onBestImageCheck(
      LivenessModel livenessModel, FaceDetectCallBack faceDetectCallBack) {
    if (!ArcFaceConfig.getInstance().isBestImage()) {
      return true;
    }
    if (livenessModel != null && livenessModel.getFaceInfo() != null) {
      float bestImageScore = livenessModel.getFaceInfo().bestImageScore;
      if (bestImageScore < 50) {
        faceDetectCallBack.onTip(-1, "最优人脸不通过");
        return false;
      }
    }
    return true;
  }
}
