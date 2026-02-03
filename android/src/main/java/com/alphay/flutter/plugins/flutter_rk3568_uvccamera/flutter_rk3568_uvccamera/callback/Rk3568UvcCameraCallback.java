package com.alphay.flutter.plugins.flutter_rk3568_uvccamera.flutter_rk3568_uvccamera.callback;

import java.util.List;
import java.util.Map;

public interface Rk3568UvcCameraCallback {

  void addFaceSuccess();

  void addFaceFailed();

  void deleteFaceSuccess();

  void batchAddFaceSuccess();

  void updateAddFaceProcess(float percent);

  void findQrCodeSuccess(String qrCode);

  void findPersonSuccess(List<Map<String, Object>> imgPaths);

  void readICCardIdSuccess(String cardId);

  void onDismiss();
}
