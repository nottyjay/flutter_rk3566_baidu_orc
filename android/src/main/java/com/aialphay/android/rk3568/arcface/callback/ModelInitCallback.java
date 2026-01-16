package com.aialphay.android.rk3568.arcface.callback;

public interface ModelInitCallback {

  /** 初始化模型成功 */
  void initModelSuccess();

  /**
   * 初始化模型失败
   *
   * @param code
   * @param response
   */
  void initModelFail(int code, String response);
}
