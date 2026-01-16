package com.aialphay.android.rk3568.arcface.manager;

import static com.aialphay.android.rk3568.arcface.ArcFaceConstants.SDK_MODEL_LOAD_SUCCESS;

import android.content.Context;

import com.aialphay.android.rk3568.arcface.ArcFaceConstants;
import com.aialphay.android.rk3568.arcface.callback.ArcFaceEngineCallback;
import com.aialphay.android.rk3568.arcface.callback.ModelInitCallback;
import com.aialphay.android.rk3568.arcface.config.ArcFaceConfig;
import com.aialphay.android.rk3568.arcface.engine.ArcFaceEngine;
import com.baidu.idl.main.facesdk.FaceDetect;
import com.baidu.idl.main.facesdk.FaceFeature;
import com.baidu.idl.main.facesdk.FaceLive;
import com.baidu.idl.main.facesdk.FaceMouthMask;
import com.baidu.idl.main.facesdk.ImageIllum;
import com.baidu.idl.main.facesdk.callback.Callback;
import com.baidu.idl.main.facesdk.model.BDFaceInstance;
import com.baidu.idl.main.facesdk.model.BDFaceSDKCommon;
import com.baidu.idl.main.facesdk.model.BDFaceSDKConfig;

public class ModelManager {

  private static final String TAG = "ModelManager";

  private ArcFaceEngineCallback arcFaceEngineCallback;

  private FaceDetect faceDetect;
  private FaceDetect faceTrackDetect;
  private FaceDetect faceDetectNir;
  private FaceDetect faceDetectPerson;
  private FaceFeature faceFeaturePerson;
  private FaceFeature faceFeature;
  private FaceLive faceLiveness;
  private FaceMouthMask faceMouthMask;
  private ImageIllum imageIllum;

  private static class HolderClass {
    private static final ModelManager MODEL_MANAGER = new ModelManager();
  }

  public static ModelManager getInstance() {
    return ModelManager.HolderClass.MODEL_MANAGER;
  }

  public FaceFeature getFaceFeature() {
    return faceFeature;
  }

  public void setFaceFeature(FaceFeature faceFeature) {
    this.faceFeature = faceFeature;
  }

  public FaceDetect getFaceTrackDetect() {
    return faceTrackDetect;
  }

  public void setFaceTrackDetect(FaceDetect faceTrackDetect) {
    this.faceTrackDetect = faceTrackDetect;
  }

  public FaceDetect getFaceDetect() {
    return faceDetect;
  }

  public void setFaceDetect(FaceDetect faceDetect) {
    this.faceDetect = faceDetect;
  }

  public FaceMouthMask getFaceMouthMask() {
    return faceMouthMask;
  }

  public void setFaceMouthMask(FaceMouthMask faceMouthMask) {
    this.faceMouthMask = faceMouthMask;
  }

  public FaceLive getFaceLiveness() {
    return faceLiveness;
  }

  public void setFaceLiveness(FaceLive faceLiveness) {
    this.faceLiveness = faceLiveness;
  }

  public FaceDetect getFaceDetectNir() {
    return faceDetectNir;
  }

  public void setFaceDetectNir(FaceDetect faceDetectNir) {
    this.faceDetectNir = faceDetectNir;
  }

  public FaceDetect getFaceDetectPerson() {
    return faceDetectPerson;
  }

  public void setFaceDetectPerson(FaceDetect faceDetectPerson) {
    this.faceDetectPerson = faceDetectPerson;
  }

  public FaceFeature getFaceFeaturePerson() {
    return faceFeaturePerson;
  }

  public void setFaceFeaturePerson(FaceFeature faceFeaturePerson) {
    this.faceFeaturePerson = faceFeaturePerson;
  }

  public ImageIllum getImageIllum() {
    return imageIllum;
  }

  public void setImageIllum(ImageIllum imageIllum) {
    this.imageIllum = imageIllum;
  }

  public void initModel(Context context, ModelInitCallback callback) {
    // 默认检测
    BDFaceInstance bdFaceInstance = new BDFaceInstance();
    bdFaceInstance.creatInstance();
    faceDetect = new FaceDetect(bdFaceInstance);
    BDFaceInstance bdFaceTrackInstance = new BDFaceInstance();
    bdFaceTrackInstance.creatInstance();
    faceTrackDetect = new FaceDetect(bdFaceTrackInstance);

    // 红外检测
    BDFaceInstance irBdFaceInstance = new BDFaceInstance();
    irBdFaceInstance.creatInstance();
    faceDetectNir = new FaceDetect(irBdFaceInstance);
    BDFaceInstance faceInstancePerson = new BDFaceInstance();
    faceInstancePerson.creatInstance();
    faceDetectPerson = new FaceDetect(faceInstancePerson);

    initConfig();

    // 认证识别
    faceFeaturePerson = new FaceFeature(faceInstancePerson);
    // 默认识别
    faceFeature = new FaceFeature();
    // 活体检测
    faceLiveness = new FaceLive();

    faceMouthMask = new FaceMouthMask();

    imageIllum = new ImageIllum();

    faceMouthMask.initModel(
        context,
        ArcFaceConstants.MOUTH_MASK_MODEL,
        new Callback() {
          @Override
          public void onResponse(int i, String s) {
            if (i != 0 && callback != null) {
              callback.initModelFail(i, s);
            }
          }
        });

    faceDetectPerson.initModel(
        context,
        ArcFaceConstants.DETECT_VIS_MODEL,
        ArcFaceConstants.DETECT_PRIOR_BOX,
        ArcFaceConstants.ALIGN_RGB_MODEL,
        BDFaceSDKCommon.DetectType.DETECT_VIS,
        BDFaceSDKCommon.AlignType.BDFACE_ALIGN_TYPE_RGB_ACCURATE,
        new Callback() {
          @Override
          public void onResponse(int i, String s) {
            if (i != 0 && callback != null) {
              callback.initModelFail(i, s);
            }
          }
        });

    faceDetectPerson.initQuality(
        context,
        ArcFaceConstants.BLUR_MODEL,
        ArcFaceConstants.OCCLUSION_MODEL,
        new Callback() {
          @Override
          public void onResponse(int code, String response) {
            if (code != 0 && callback != null) {
              callback.initModelFail(code, response);
            }
          }
        });
    faceTrackDetect.initModel(
        context,
        ArcFaceConstants.DETECT_VIS_MODEL,
        ArcFaceConstants.DETECT_PRIOR_BOX,
        ArcFaceConstants.ALIGN_TRACK_MODEL,
        BDFaceSDKCommon.DetectType.DETECT_VIS,
        BDFaceSDKCommon.AlignType.BDFACE_ALIGN_TYPE_RGB_FAST,
        new Callback() {
          @Override
          public void onResponse(int code, String response) {
            if (code != 0 && callback != null) {
              callback.initModelFail(code, response);
            }
          }
        });

    faceDetect.initModel(
        context,
        ArcFaceConstants.DETECT_VIS_MODEL,
        ArcFaceConstants.DETECT_PRIOR_BOX,
        ArcFaceConstants.ALIGN_RGB_MODEL,
        BDFaceSDKCommon.DetectType.DETECT_VIS,
        BDFaceSDKCommon.AlignType.BDFACE_ALIGN_TYPE_RGB_ACCURATE,
        new Callback() {
          @Override
          public void onResponse(int code, String response) {
            //  ToastUtils.toast(context, code + "  " + response);
            if (code != 0 && callback != null) {
              callback.initModelFail(code, response);
            }
          }
        });

    faceDetectNir.initModel(
        context,
        ArcFaceConstants.DETECT_NIR_MODE,
        ArcFaceConstants.DETECT_PRIOR_BOX,
        ArcFaceConstants.ALIGN_NIR_MODEL,
        BDFaceSDKCommon.DetectType.DETECT_NIR,
        BDFaceSDKCommon.AlignType.BDFACE_ALIGN_TYPE_NIR_ACCURATE,
        new Callback() {
          @Override
          public void onResponse(int code, String response) {
            //  ToastUtils.toast(context, code + "  " + response);
            if (code != 0 && callback != null) {
              callback.initModelFail(code, response);
            }
          }
        });

    faceDetect.initQuality(
        context,
        ArcFaceConstants.BLUR_MODEL,
        ArcFaceConstants.OCCLUSION_MODEL,
        new Callback() {
          @Override
          public void onResponse(int code, String response) {
            if (code != 0 && callback != null) {
              callback.initModelFail(code, response);
            }
          }
        });

    faceDetect.initBestImage(
        context,
        ArcFaceConstants.BEST_IMAGE,
        new Callback() {
          @Override
          public void onResponse(int code, String response) {
            if (code != 0 && callback != null) {
              callback.initModelFail(code, response);
            }
          }
        });

    faceDetect.initAttrEmo(
        context,
        ArcFaceConstants.ATTRIBUTE_MODEL,
        ArcFaceConstants.EMOTION_MODEL,
        new Callback() {
          @Override
          public void onResponse(int code, String response) {
            if (code != 0 && callback != null) {
              callback.initModelFail(code, response);
            }
          }
        });

    faceLiveness.initModel(
        context,
        ArcFaceConstants.LIVE_VIS_MODEL,
        ArcFaceConstants.LIVE_2DMASK_MODEL,
        ArcFaceConstants.LIVE_HAND_MODEL,
        ArcFaceConstants.LIVE_REFLECTION_MODEL,
        ArcFaceConstants.LIVE_NIR_MODEL,
        "",
        new Callback() {
          @Override
          public void onResponse(int code, String response) {
            //  ToastUtils.toast(context, code + "  " + response);
            if (code != 0 && callback != null) {
              callback.initModelFail(code, response);
            }
          }
        });
    faceFeaturePerson.initModel(
        context,
        ArcFaceConstants.RECOGNIZE_IDPHOTO_MODEL,
        ArcFaceConstants.RECOGNIZE_VIS_MODEL,
        ArcFaceConstants.RECOGNIZE_NIR_MODEL,
        ArcFaceConstants.RECOGNIZE_VIS_MODEL,
        new Callback() {
          @Override
          public void onResponse(int code, String response) {
            if (code != 0 && callback != null) {
              callback.initModelFail(code, response);
            }
          }
        });
    // 初始化特征提取模型
    faceFeature.initModel(
        context,
        ArcFaceConstants.RECOGNIZE_IDPHOTO_MODEL,
        ArcFaceConstants.RECOGNIZE_VIS_MODEL,
        ArcFaceConstants.RECOGNIZE_NIR_MODEL,
        ArcFaceConstants.RECOGNIZE_VIS_MODEL,
        new Callback() {
          @Override
          public void onResponse(int code, String response) {
            if (code != 0) {
              if (callback != null) {
                callback.initModelFail(code, response);
              }
            } else {
              ArcFaceEngine.initStatus = SDK_MODEL_LOAD_SUCCESS;
              if (callback != null) {
                callback.initModelSuccess();
              }
            }
          }
        });
  }

  /**
   * 初始化配置
   *
   * @return
   */
  public boolean initConfig() {
    if (faceDetect != null && faceTrackDetect != null && faceDetectPerson != null) {
      BDFaceSDKConfig config = new BDFaceSDKConfig();
      // 最小人脸个数检查，默认设置为1,用户根据自己需求调整
      config.maxDetectNum = ArcFaceConfig.getInstance().getMaxDetectNum();
      // 默认为80px。可传入大于30px的数值，小于此大小的人脸不予检测，生效时间第一次加载模型
      config.minFaceSize = ArcFaceConfig.getInstance().getMinimumFace();

      // 默认为0.5。可传入大于0.3的数值
      config.notRGBFaceThreshold = ArcFaceConfig.getInstance().getFaceThreshold();
      config.notNIRFaceThreshold = ArcFaceConfig.getInstance().getFaceThreshold();

      // 模糊，遮挡，光照三个质量检测和姿态角查默认关闭，如果要开启，设置页启动
      config.isCheckBlur =
          config.isOcclusion =
              config.isIllumination =
                  config.isHeadPose = ArcFaceConfig.getInstance().isQualityControl();

      faceDetect.loadConfig(config);
      faceTrackDetect.loadConfig(config);
      faceDetectPerson.loadConfig(config);
      return true;
    }
    return false;
  }
}
