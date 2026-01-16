package com.aialphay.android.rk3568.arcface.callback;

public interface ArcFaceEngineCallback extends ModelInitCallback, DataBaseListener {

  /** 激活成功 */
  void activeLicenseSuccess();

  /**
   * 激活失败
   *
   * @param errorCode
   * @param msg
   */
  void activeLicenseFail(int errorCode, String msg);
}
