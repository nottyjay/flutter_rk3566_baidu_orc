package com.aialphay.android.rk3568.arcface;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class ArcFaceConstants {

  public static final String RGB_TYPE = "RGB_TYPE";
  public static final String IR_TYPE = "IR_TYPE";

  // 在线激活
  public static final String ACTIVE_LICENSE_ONLINE = "online";
  // 离线激活
  public static final String ACTIVE_LICENSE_OFFLINE = "offline";

  public static final String ACTIVE_LICENSE_BATCHONLINE = "batchOnline";

  // SDK 加载成功
  public static final int SDK_MODEL_LOAD_SUCCESS = 0;
  // SDK 未激活
  public static final int SDK_UNACTIVATION = 1;

  // ====================== 模型定义 ======================
  public static final String PATH = "";
  public static final String MOUTH_MASK_MODEL =
      PATH + "face-sdk-models/mouth_mask/mouthmask_v8.0.rknn.encrypted";
  public static final String DETECT_VIS_MODEL =
      PATH + "face-sdk-models/detect/detect_V192x192_v8.0.rknn.encrypted";
  public static final String DETECT_PRIOR_BOX =
      PATH + "face-sdk-models/detect/detect_mbox_192x192_priorbox.bin";
  public static final String DETECT_NIR_MODE =
      PATH + "face-sdk-models/detect/detect_V192x192_v8.0.rknn.encrypted";
  public static final String ALIGN_RGB_MODEL =
      PATH + "face-sdk-models/align/align_v7.0.rknn.encrypted";
  public static final String ALIGN_NIR_MODEL =
      PATH + "face-sdk-models/align/align_v7.0.rknn.encrypted";
  public static final String ALIGN_TRACK_MODEL =
      PATH + "face-sdk-models/align/align_v7.0.rknn.encrypted";
  public static final String LIVE_VIS_MODEL =
      PATH + "face-sdk-models/silent_live/liveness_rgb_fp16_v8.0.rknn.encrypted";
  public static final String LIVE_NIR_MODEL =
      PATH + "face-sdk-models/silent_live/liveness_nir_fp16_v8.0.rknn.encrypted";
  public static final String BLUR_MODEL = PATH + "face-sdk-models/blur/blur_v7.0_u8.rknn.encrypted";
  public static final String OCCLUSION_MODEL =
      PATH + "face-sdk-models/occlusion/occlusion_fp16_v7.0.rknn.encrypted";
  public static final String BEST_IMAGE =
      PATH + "face-sdk-models/best_image/best_image-customized-pa-rk3568.model.int8-2.0.1.1";
  public static final String ATTRIBUTE_MODEL =
      PATH + "face-sdk-models/attribute/attribute-customized-pa-rk3568.model.int8-1.0.1.1";
  public static final String EMOTION_MODEL = PATH;
  public static final String LIVE_2DMASK_MODEL =
      PATH + "face-sdk-models/silent_live_multi_factor/mask2d_v7.6.rknn.encrypted";
  public static final String LIVE_HAND_MODEL =
      PATH + "face-sdk-models/silent_live_multi_factor/handcheck_v7.6.rknn.encrypted";
  public static final String LIVE_REFLECTION_MODEL =
      PATH + "face-sdk-models/silent_live_multi_factor/reflection_v7.6.rknn.encrypted";

  public static final String RECOGNIZE_VIS_MODEL =
      PATH + "face-sdk-models/feature/feature_live_v8.5.rknn.encrypted";
  public static final String RECOGNIZE_IDPHOTO_MODEL =
      PATH + "face-sdk-models/feature/feature_id-customized-pa-rk3568.model.int8-1.0.3.1";
  public static final String RECOGNIZE_NIR_MODEL =
      PATH + "face-sdk-models/feature/feature_nir-customized-pa-rk3568.model.int8-1.0.5.1";

  public static final int FEATURE_SIZE = 1024;

  public static ExecutorService detectCheckExecutorService = Executors.newSingleThreadExecutor();
  public static Future detectCheckFuture;

  public static ExecutorService livenessCheckExecutorService = Executors.newSingleThreadExecutor();
  public static Future livenessCheckFuture;

  public static ExecutorService qrCodeExecutorService = Executors.newSingleThreadExecutor();
  public static Future qrCodeFuture;

  public static ExecutorService icCardExecutorService = Executors.newSingleThreadExecutor();
  public static Future icCardFuture;
  public static boolean canDetect = false;
}
