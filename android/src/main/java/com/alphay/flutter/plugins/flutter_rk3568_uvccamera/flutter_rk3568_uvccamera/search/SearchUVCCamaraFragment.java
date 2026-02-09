package com.alphay.flutter.plugins.flutter_rk3568_uvccamera.flutter_rk3568_uvccamera.search;

import android.graphics.Bitmap;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.fragment.app.FragmentActivity;

import com.ai.face.faceVerify.verify.FaceVerifyUtils;
import com.aialphay.android.rk3568.arcface.ArcFaceConstants;
import com.aialphay.android.rk3568.arcface.bean.LivenessModel;
import com.aialphay.android.rk3568.arcface.callback.FaceDetectCallBack;
import com.aialphay.android.rk3568.arcface.config.ArcFaceConfig;
import com.aialphay.android.rk3568.arcface.engine.ArcFaceEngine;
import com.aialphay.android.rk3568.arcface.manager.DataBaseManager;
import com.aialphay.android.rk3568.utils.BitmapUtils;
import com.aialphay.android.rk3568.utils.FileUtils;
import com.alphay.flutter.plugins.flutter_rk3568_uvccamera.flutter_rk3568_uvccamera.R;
import com.alphay.flutter.plugins.flutter_rk3568_uvccamera.flutter_rk3568_uvccamera.callback.Rk3568UvcCameraCallback;
import com.king.wechat.qrcode.WeChatQRCodeDetector;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class SearchUVCCamaraFragment extends AbsSearchUVCCamaraFragment {

  private static final String TAG = "SearchUVCCamaraFragment";

  private FragmentActivity activity;
  private TextView searchTipsTextView;
  private ImageButton closeButton;
  private Button cancelPaymentButton;

  private Bitmap irBitmap;
  private Bitmap rgbBitmap;
  private boolean irReady = false;
  private boolean rgbReady = false;
  private volatile boolean isFindQR = false;
  private Rk3568UvcCameraCallback callback;

  public SearchUVCCamaraFragment(FragmentActivity activity, Rk3568UvcCameraCallback callback) {
    this.activity = activity;
    this.callback = callback;
    Log.d(TAG, "创建一个 View，FlutterSearchUVCCamaraFragment");
    ArcFaceEngine.getInstance().clearFeatureMap();
  }

  @Override
  public void initViews() {
    super.initViews();
    searchTipsTextView = nativeView.findViewById(R.id.search_tips);
    closeButton = nativeView.findViewById(R.id.close_button);
    cancelPaymentButton = nativeView.findViewById(R.id.cancel_payment_button);
    closeButton.setOnClickListener(
        v -> {
          // 关闭当前fragment
          this.dismissAllowingStateLoss();
          if (activity != null && !activity.isFinishing()) {
            // activity.moveTaskToBack(false);
          }
        });
    cancelPaymentButton.setOnClickListener(
        v -> {
          // 取消支付按钮，效果与关闭窗口按钮相同
          this.dismissAllowingStateLoss();
          if (activity != null && !activity.isFinishing()) {
            // activity.moveTaskToBack(false);
          }
        });
    // BrightnessUtil.setBrightness(requireActivity(), 1.0f); // 高亮白色背景屏幕光可以当补光灯
  }

  @Override
  synchronized void faceSearchSetBitmap(Bitmap bitmap, FaceVerifyUtils.BitmapType type) {
    if (isFindQR || isScaningFace()) return;

    if (type.equals(FaceVerifyUtils.BitmapType.IR)) {
      if (irBitmap != null && !irBitmap.isRecycled()) {
        irBitmap.recycle();
      }
      irBitmap = bitmap;
      irReady = true;
    } else if (type.equals(FaceVerifyUtils.BitmapType.RGB)) {
      if (rgbBitmap != null && !rgbBitmap.isRecycled()) {
        rgbBitmap.recycle();
      }
      rgbBitmap = bitmap;
      rgbReady = true;
      try {
        if (bitmap != null && !bitmap.isRecycled()) {
          Bitmap qrCodeBitmap = bitmap.copy(bitmap.getConfig(), true);
          List<String> contents = WeChatQRCodeDetector.detectAndDecode(qrCodeBitmap);
          qrCodeBitmap = null;
          if (contents != null && contents.size() > 0 && !isFindQR) {
            isFindQR = true;
            if (callback != null) {
              callback.findQrCodeSuccess(contents.get(0));
            }
            dismissAllowingStateLoss();
          }
        }
      } catch (Exception e) {
        Log.e(TAG, "QRCode detect failed", e);
      }
    }

    if (irReady && rgbReady && !isFindQR) {
      if (rgbBitmap != null
          && !rgbBitmap.isRecycled()
          && irBitmap != null
          && !irBitmap.isRecycled()) {
        try {
          // 送数据进入SDK
          onFaceCheck(
              rgbBitmap.copy(Bitmap.Config.ARGB_8888, false),
              irBitmap.copy(Bitmap.Config.ARGB_8888, false));
        } catch (Exception e) {
          Log.e(TAG, "onFaceCheck copy failed", e);
        }
        irReady = false;
        rgbReady = false;
      }
    }
  }

  private boolean isScaningFace() {
    boolean result =
        !((ArcFaceConstants.detectCheckFuture == null
                || ArcFaceConstants.detectCheckFuture.isDone())
            || (ArcFaceConstants.livenessCheckFuture == null
                || ArcFaceConstants.livenessCheckFuture.isDone()));
    return result;
  }

  private void onFaceCheck(Bitmap rgbData, Bitmap nirData) {
    if (DataBaseManager.getInstance().isPush()) {
      if (rgbData != null && !rgbData.isRecycled() && nirData != null && !nirData.isRecycled()) {
        ArcFaceEngine.getInstance()
            .searchPerson(
                rgbData,
                nirData,
                null,
                ArcFaceConfig.getInstance().getRgbAndNirWidth(),
                ArcFaceConfig.getInstance().getRgbAndNirHeight(),
                2,
                new FaceDetectCallBack() {

                  @Override
                  public void onFaceDetectCallback(LivenessModel livenessModel) {
                    checkResult(livenessModel);
                  }

                  @Override
                  public void onTip(int code, String msg) {
                    searchTipsTextView.setText(msg);
                  }

                  @Override
                  public void onFaceDetectDarwCallback(LivenessModel livenessModel) {}
                });
      }
    }
  }

  private void checkResult(LivenessModel livenessModel) {
    activity.runOnUiThread(
        new Runnable() {

          @Override
          public void run() {
            if (livenessModel == null || isFindQR) {
              return;
            }
            if (!livenessModel.isQualityCheck()) {
              searchTipsTextView.setText("请正视摄像头或将二维码置于摄像头前 30cm 处");
            } else if (livenessModel.getRgbLivenessScore()
                    < ArcFaceConfig.getInstance().getRgbLiveScore()
                || livenessModel.getIrLivenessScore()
                    < ArcFaceConfig.getInstance().getNirLiveScore()) {
              searchTipsTextView.setText("请正视摄像头或将二维码置于摄像头前 30cm 处");
            } else if (livenessModel.getUsers() == null || livenessModel.getUsers().isEmpty()) {
              searchTipsTextView.setText("请正视摄像头或将二维码置于摄像头前 30cm 处");
            } else {
              searchTipsTextView.setText("");
              // 获取用户信息
              List<Map<String, Object>> userImages =
                  livenessModel.getUsers().stream()
                      .map(
                          user -> {
                            Map<String, Object> map = new HashMap<>();
                            map.put("userId", user.getUserId());
                            map.put("name", user.getUserName());
                            map.put("path", user.getImageName());
                            map.put("score", user.getFeatureScore());
                            String file =
                                ArcFaceConfig.getInstance().getFaceImagesPath()
                                    + "/search/"
                                    + System.currentTimeMillis()
                                    + ".jpg";
                            File saveFile = new File(file);
                            if (!saveFile.getParentFile().exists()) {
                              saveFile.getParentFile().mkdirs();
                            }
                            FileUtils.saveBitmap(
                                new File(file),
                                BitmapUtils.getInstaceBmp(livenessModel.getBdFaceImageInstance()));
                            map.put("file", file);
                            return map;
                          })
                      .collect(Collectors.toList());
              if (callback != null) {
                isFindQR = true;
                callback.findPersonSuccess(userImages);
              }
            }
          }
        });
  }

  @Override
  public void onDestroyView() {
    Log.d(TAG, "销毁 View，SearchUVCCamaraFragment");
    super.onDestroyView();
    if (callback != null) {
      callback.onDismiss();
    }
    irReady = false;
    rgbReady = false;
    if (irBitmap != null && !irBitmap.isRecycled()) {
      irBitmap = null;
    }
    if (rgbBitmap != null && !rgbBitmap.isRecycled()) {
      rgbBitmap = null;
    }

    System.gc();
  }
}
