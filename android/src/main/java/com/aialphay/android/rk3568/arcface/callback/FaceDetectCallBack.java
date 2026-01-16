package com.aialphay.android.rk3568.arcface.callback;

import com.aialphay.android.rk3568.arcface.bean.LivenessModel;

/** 人脸检测回调接口。 @Time: 2019/1/25 @Author: v_chaixiaogang */
public interface FaceDetectCallBack {

  /**
   * 人脸检测回调接口。
   *
   * @param livenessModel
   */
  void onFaceDetectCallback(LivenessModel livenessModel);

  /**
   * 提示信息回调接口。
   *
   * @param code
   * @param msg
   */
  void onTip(int code, String msg);

  /**
   * 人脸识别结果绘制回调接口。
   *
   * @param livenessModel
   */
  void onFaceDetectDarwCallback(LivenessModel livenessModel);
}
