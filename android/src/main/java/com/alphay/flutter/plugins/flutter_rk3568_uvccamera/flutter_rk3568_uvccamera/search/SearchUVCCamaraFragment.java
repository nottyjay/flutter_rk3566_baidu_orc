package com.alphay.flutter.plugins.flutter_rk3568_uvccamera.flutter_rk3568_uvccamera.search;

import static com.ai.face.faceSearch.search.SearchProcessTipsCode.EMGINE_INITING;
import static com.ai.face.faceSearch.search.SearchProcessTipsCode.FACE_DIR_EMPTY;
import static com.ai.face.faceSearch.search.SearchProcessTipsCode.FACE_SIZE_FIT;
import static com.ai.face.faceSearch.search.SearchProcessTipsCode.FACE_TOO_LARGE;
import static com.ai.face.faceSearch.search.SearchProcessTipsCode.FACE_TOO_SMALL;
import static com.ai.face.faceSearch.search.SearchProcessTipsCode.IR_LIVE_ERROR;
import static com.ai.face.faceSearch.search.SearchProcessTipsCode.MASK_DETECTION;
import static com.ai.face.faceSearch.search.SearchProcessTipsCode.NO_LIVE_FACE;
import static com.ai.face.faceSearch.search.SearchProcessTipsCode.NO_MATCHED;
import static com.ai.face.faceSearch.search.SearchProcessTipsCode.SEARCHING;
import static com.ai.face.faceSearch.search.SearchProcessTipsCode.SEARCH_PREPARED;
import static com.ai.face.faceSearch.search.SearchProcessTipsCode.THRESHOLD_ERROR;
import static com.ai.face.faceSearch.search.SearchProcessTipsCode.TOO_MUCH_FACE;

import android.graphics.Bitmap;
import android.util.Log;
import android.widget.TextView;
import androidx.fragment.app.FragmentActivity;
import com.ai.face.base.utils.BrightnessUtil;
import com.ai.face.faceSearch.search.FaceSearchEngine;

import com.ai.face.faceVerify.verify.FaceVerifyUtils;
import com.aialphay.android.rk3568.arcface.ArcFaceConstants;
import com.aialphay.android.rk3568.arcface.bean.LivenessModel;
import com.aialphay.android.rk3568.arcface.bean.User;
import com.aialphay.android.rk3568.arcface.callback.FaceDetectCallBack;
import com.aialphay.android.rk3568.arcface.config.ArcFaceConfig;
import com.aialphay.android.rk3568.arcface.engine.ArcFaceEngine;
import com.aialphay.android.rk3568.iccard.ICCardReadService;
import com.aialphay.android.rk3568.qrcode.QrCodeParser;
import com.aialphay.android.rk3568.utils.BitmapUtils;
import com.aialphay.android.rk3568.utils.FileUtils;
import com.alphay.flutter.plugins.flutter_rk3568_uvccamera.flutter_rk3568_uvccamera.R;
import com.alphay.flutter.plugins.flutter_rk3568_uvccamera.flutter_rk3568_uvccamera.callback.Rk3568UvcCameraCallback;
import com.king.wechat.qrcode.WeChatQRCodeDetector;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import io.flutter.plugin.common.MethodChannel;

public class SearchUVCCamaraFragment extends AbsSearchUVCCamaraFragment {

  private static final String TAG = "SearchUVCCamaraFragment";

  private FragmentActivity activity;
  private TextView searchTipsTextView;

  private Bitmap irBitmap;
  private Bitmap rgbBitmap;
  private boolean irReady = false;
  private boolean rgbReady = false;
  private Rk3568UvcCameraCallback callback;

  public SearchUVCCamaraFragment(FragmentActivity activity, Rk3568UvcCameraCallback callback) {
    this.activity = activity;
    this.callback = callback;
    Log.d(TAG, "创建一个 View，FlutterSearchUVCCamaraFragment");
  }

  @Override
  public void initViews() {
    super.initViews();
    searchTipsTextView = nativeView.findViewById(R.id.search_tips);
    //    BrightnessUtil.setBrightness(requireActivity(), 1.0f); // 高亮白色背景屏幕光可以当补光灯
  }

  @Override
  void faceSearchSetBitmap(Bitmap bitmap, FaceVerifyUtils.BitmapType type) {
    if (type.equals(FaceVerifyUtils.BitmapType.IR)) {
      irBitmap = bitmap;
      irReady = true;
    } else if (type.equals(FaceVerifyUtils.BitmapType.RGB)) {
      rgbBitmap = bitmap;
      if (ArcFaceConstants.qrCodeFuture == null || ArcFaceConstants.qrCodeFuture.isDone()) {
        ArcFaceConstants.qrCodeFuture =
            ArcFaceConstants.qrCodeExecutorService.submit(
                new Runnable() {
                  @Override
                  public void run() {
                    List<String> contents = WeChatQRCodeDetector.detectAndDecode(bitmap);
                    if (contents != null && contents.size() > 0) {
                      if (irBitmap != null && !irBitmap.isRecycled()) {
                        irBitmap.recycle();
                      }
                      if (rgbBitmap != null && !rgbBitmap.isRecycled()) {
                        rgbBitmap.recycle();
                      }
                      irReady = false;
                      rgbReady = false;
                      if (callback != null) {
                        callback.findQrCodeSuccess(contents.get(0));
                      }
                    }
                  }
                });
      }
      rgbReady = true;
    }

    if (irReady && rgbReady) {
      // 送数据进入SDK
      onFaceCheck(rgbBitmap, irBitmap);
      irReady = false;
      rgbReady = false;
    }
  }

  private void onFaceCheck(Bitmap rgbData, Bitmap nirData) {
    if (ArcFaceConstants.canDetect) {
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
                public void onFaceDetectDarwCallback(LivenessModel livenessModel) {
                  //                  showFrame(livenessModel);
                }
              });
    }
  }

  private void checkResult(LivenessModel livenessModel) {
    activity.runOnUiThread(
        new Runnable() {

          @Override
          public void run() {
            if (livenessModel == null) {
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
    if (ArcFaceConstants.icCardFuture != null && !ArcFaceConstants.icCardFuture.isDone()) {
      ICCardReadService.getInstance().setFinished(true);
      ArcFaceConstants.icCardFuture.cancel(true);
    }
    if (ArcFaceConstants.qrCodeFuture != null && !ArcFaceConstants.qrCodeFuture.isDone()) {
      ArcFaceConstants.qrCodeFuture.cancel(true);
    }
    if (ArcFaceConstants.livenessCheckFuture != null
        && !ArcFaceConstants.livenessCheckFuture.isDone()) {
      ArcFaceConstants.livenessCheckFuture.cancel(true);
    }
    if (ArcFaceConstants.detectCheckFuture != null
        && !ArcFaceConstants.detectCheckFuture.isDone()) {
      ArcFaceConstants.detectCheckFuture.cancel(true);
    }
    if (irBitmap != null && !irBitmap.isRecycled()) {
      irBitmap.recycle();
    }
    if (rgbBitmap != null && !rgbBitmap.isRecycled()) {
      rgbBitmap.recycle();
    }
  }
}
