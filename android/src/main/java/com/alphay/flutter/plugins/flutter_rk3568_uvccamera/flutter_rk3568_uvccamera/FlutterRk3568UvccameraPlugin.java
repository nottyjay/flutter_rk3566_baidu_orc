package com.alphay.flutter.plugins.flutter_rk3568_uvccamera.flutter_rk3568_uvccamera;

import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;

import com.aialphay.android.rk3568.arcface.ArcFaceConstants;
import com.aialphay.android.rk3568.arcface.callback.ArcFaceEngineCallback;
import com.aialphay.android.rk3568.arcface.config.ArcFaceConfig;
import com.aialphay.android.rk3568.arcface.engine.ArcFaceEngine;
import com.aialphay.android.rk3568.arcface.manager.DataBaseManager;
import com.aialphay.android.rk3568.iccard.ICCardReadService;
import com.aialphay.android.rk3568.iccard.callback.ICCardReadCallback;
import com.aialphay.android.rk3568.utils.FileUtils;
import com.alphay.flutter.plugins.flutter_rk3568_uvccamera.flutter_rk3568_uvccamera.callback.Rk3568UvcCameraCallback;
import com.alphay.flutter.plugins.flutter_rk3568_uvccamera.flutter_rk3568_uvccamera.search.SearchUVCCamaraFragment;
import com.alphay.flutter.plugins.flutter_rk3568_uvccamera.flutter_rk3568_uvccamera.utils.ArcFaceUtils;
import com.king.wechat.qrcode.WeChatQRCodeDetector;

import org.opencv.android.OpenCVLoader;

import java.io.File;
import java.util.List;
import java.util.Map;

import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.embedding.engine.plugins.activity.ActivityAware;
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;

public class FlutterRk3568UvccameraPlugin
    implements FlutterPlugin, MethodCallHandler, ActivityAware {
  private MethodChannel channel;

  private ActivityPluginBinding activityBinding;

  private SearchUVCCamaraFragment fragment;

  @Override
  public void onAttachedToEngine(@NonNull FlutterPluginBinding flutterPluginBinding) {
    channel =
        new MethodChannel(
            flutterPluginBinding.getBinaryMessenger(),
            "com.alphay.flutter.plugin/flutter_rk3568_uvc");
    channel.setMethodCallHandler(this);
  }

  @Override
  public void onMethodCall(@NonNull MethodCall call, @NonNull Result result) {

    if (call.method.equals("initFaceEngine")) {
      initFaceEngine((Map<String, Object>) call.arguments);
      result.success(true);
    } else if (call.method.equals("initQrcodeEngine")) {
      OpenCVLoader.initLocal();
      WeChatQRCodeDetector.init(this.activityBinding.getActivity());
      result.success(true);
    } else if (call.method.equals("initICCardReader")) {
      ICCardReadService.getInstance().initICCardReadService(this.activityBinding.getActivity());
      result.success(true);
    } else if (call.method.equals("addFace")) {
      String facePath = call.argument("facePath");
      ArcFaceUtils.addFace(facePath, rk3568UvcCameraCallback);
      result.success(true);
    } else if (call.method.equals("deleteFace")) {
      String id = call.argument("id");
      DataBaseManager.getInstance().deleteFace(id);
      rk3568UvcCameraCallback.deleteFaceSuccess();
      result.success(true);
    } else if (call.method.equals("clearFace")) {
      ArcFaceEngine.getInstance().userClean();
        if(ArcFaceConfig.getInstance().getFaceImagesPath() != null && ArcFaceConfig.getInstance().getFaceImagesPath().trim() != ""){
            FileUtils.deleteDirAllContent(new File(ArcFaceConfig.getInstance().getFaceImagesPath()));
        }
      rk3568UvcCameraCallback.deleteFaceSuccess();
      result.success(true);
    } else if (call.method.equals("addFaces")) {
      String facePath = call.argument("facePath");
      ArcFaceUtils.reAddFace(facePath, rk3568UvcCameraCallback);
      result.success(true);
    } else if (call.method.equals("runDetect")) {
      // 同时打开摄像头进行人脸识别、二维码识别、IC 卡读卡
      runReadListener();
      result.success(true);
    } else {
      result.notImplemented();
    }
  }

  private void runReadListener() {
    // 运行 IC 卡监听
    ICCardReadService.getInstance()
        .startReadICCardID(
            new ICCardReadCallback() {
              @Override
              public void onReaCardIDSuccess(String cardId) {
                rk3568UvcCameraCallback.readICCardIdSuccess(cardId);
              }
            });
    runDetect();
  }

  private void runDetect() {
    FragmentActivity activity = (FragmentActivity) activityBinding.getActivity();
    FragmentManager fragmentManager = activity.getSupportFragmentManager();
    fragment = new SearchUVCCamaraFragment(activity, rk3568UvcCameraCallback);
    fragment.show(fragmentManager, "search_uvc");
  }

  private void initFaceEngine(Map<String, Object> arguments) {
    // arguments转换为 ArcFaceConfig
    ArcFaceEngine.getInstance().initConfig(arguments);

    ArcFaceEngine.getInstance()
        .initEngine(
            this.activityBinding.getActivity(),
            new ArcFaceEngineCallback() {
              @Override
              public void activeLicenseSuccess() {
                activityBinding
                    .getActivity()
                    .runOnUiThread(
                        () -> {
                          channel.invokeMethod("activeLicenseSuccess", null);
                        });
              }

              @Override
              public void activeLicenseFail(int code, String response) {
                activityBinding
                    .getActivity()
                    .runOnUiThread(
                        () -> {
                          channel.invokeMethod(
                              "activeLicenseFail", Map.of("code", code, "response", response));
                        });
              }

              @Override
              public void initFullUsersSuccess() {
                activityBinding
                    .getActivity()
                    .runOnUiThread(
                        () -> {
                          channel.invokeMethod("initFullUsersSuccess", null);
                        });
              }

              @Override
              public void initModelSuccess() {
                activityBinding
                    .getActivity()
                    .runOnUiThread(
                        () -> {
                          channel.invokeMethod("initModelSuccess", null);
                        });
              }

              @Override
              public void initModelFail(int code, String response) {
                activityBinding
                    .getActivity()
                    .runOnUiThread(
                        () -> {
                          channel.invokeMethod(
                              "initModelFail", Map.of("code", code, "response", response));
                        });
              }
            });
  }

  private Rk3568UvcCameraCallback rk3568UvcCameraCallback =
      new Rk3568UvcCameraCallback() {
        @Override
        public void addFaceSuccess() {
          activityBinding
              .getActivity()
              .runOnUiThread(
                  () -> {
                    channel.invokeMethod("addFaceSuccess", null);
                  });
        }

        @Override
        public void addFaceFailed() {
          activityBinding
              .getActivity()
              .runOnUiThread(
                  () -> {
                    channel.invokeMethod("addFaceFailed", null);
                  });
        }

        @Override
        public void deleteFaceSuccess() {
          activityBinding
              .getActivity()
              .runOnUiThread(
                  () -> {
                    channel.invokeMethod("deleteFaceSuccess", null);
                  });
        }

        @Override
        public void batchAddFaceSuccess() {
          activityBinding
              .getActivity()
              .runOnUiThread(
                  () -> {
                    channel.invokeMethod("batchAddFaceSuccess", null);
                  });
        }

        @Override
        public void updateAddFaceProcess(float percent) {
          activityBinding
              .getActivity()
              .runOnUiThread(
                  () -> {
                    channel.invokeMethod("updateAddFaceProcess", percent);
                  });
        }

        @Override
        public void findQrCodeSuccess(String qrCode) {
          // 有结果，暂停其他 Future
          if (ArcFaceConstants.detectCheckFuture != null
              && !ArcFaceConstants.detectCheckFuture.isDone()) {
            ArcFaceConstants.detectCheckFuture.cancel(true);
          }
          if (ArcFaceConstants.livenessCheckFuture != null
              && !ArcFaceConstants.livenessCheckFuture.isDone()) {
            ArcFaceConstants.livenessCheckFuture.cancel(true);
          }
          if (ArcFaceConstants.icCardFuture != null && !ArcFaceConstants.icCardFuture.isDone()) {
            ICCardReadService.getInstance().setFinished(true);
            ArcFaceConstants.icCardFuture.cancel(true);
          }
          activityBinding
              .getActivity()
              .runOnUiThread(
                  () -> {
                    if (fragment != null) {
                      fragment.dismissAllowingStateLoss();
                    }
                    channel.invokeMethod("findQrCodeSuccess", qrCode);
                  });
        }

        @Override
        public void findPersonSuccess(List<Map<String, Object>> persons) {
          if (ArcFaceConstants.qrCodeFuture != null && !ArcFaceConstants.qrCodeFuture.isDone()) {
            ArcFaceConstants.qrCodeFuture.cancel(true);
          }
          if (ArcFaceConstants.icCardFuture != null && !ArcFaceConstants.icCardFuture.isDone()) {
            ICCardReadService.getInstance().setFinished(true);
            ArcFaceConstants.icCardFuture.cancel(true);
          }
          activityBinding
              .getActivity()
              .runOnUiThread(
                  () -> {
                    if (fragment != null) {
                      fragment.dismissAllowingStateLoss();
                    }
                    channel.invokeMethod("findPersonSuccess", persons);
                  });
        }

        @Override
        public void readICCardIdSuccess(String cardId) {
          if (ArcFaceConstants.detectCheckFuture != null
              && !ArcFaceConstants.detectCheckFuture.isDone()) {
            ArcFaceConstants.detectCheckFuture.cancel(true);
          }
          if (ArcFaceConstants.livenessCheckFuture != null
              && !ArcFaceConstants.livenessCheckFuture.isDone()) {
            ArcFaceConstants.livenessCheckFuture.cancel(true);
          }
          if (ArcFaceConstants.qrCodeFuture != null && !ArcFaceConstants.qrCodeFuture.isDone()) {
            ArcFaceConstants.qrCodeFuture.cancel(true);
          }
          activityBinding
              .getActivity()
              .runOnUiThread(
                  () -> {
                    if (fragment != null) {
                      fragment.dismissAllowingStateLoss();
                    }
                    channel.invokeMethod("readICCardIdSuccess", cardId);
                  });
        }
      };

  @Override
  public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {
    channel.setMethodCallHandler(null);
    if (ArcFaceConstants.qrCodeFuture != null && !ArcFaceConstants.qrCodeFuture.isDone()) {
      ArcFaceConstants.qrCodeFuture.cancel(true);
    }
    if (ArcFaceConstants.detectCheckFuture != null
        && !ArcFaceConstants.detectCheckFuture.isDone()) {
      ArcFaceConstants.detectCheckFuture.cancel(true);
    }
    if (ArcFaceConstants.livenessCheckFuture != null
        && !ArcFaceConstants.livenessCheckFuture.isDone()) {
      ArcFaceConstants.livenessCheckFuture.cancel(true);
    }
    if (ArcFaceConstants.icCardFuture != null && !ArcFaceConstants.icCardFuture.isDone()) {
      ICCardReadService.getInstance().setFinished(true);
      ArcFaceConstants.icCardFuture.cancel(true);
    }
  }

  @Override
  public void onAttachedToActivity(@NonNull ActivityPluginBinding binding) {
    activityBinding = binding;
  }

  @Override
  public void onDetachedFromActivityForConfigChanges() {}

  @Override
  public void onReattachedToActivityForConfigChanges(@NonNull ActivityPluginBinding binding) {
    activityBinding = binding;
  }

  @Override
  public void onDetachedFromActivity() {
    activityBinding = null;
  }
}
