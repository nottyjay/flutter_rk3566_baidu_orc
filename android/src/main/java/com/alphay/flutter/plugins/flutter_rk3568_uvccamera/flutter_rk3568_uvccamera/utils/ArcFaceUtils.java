package com.alphay.flutter.plugins.flutter_rk3568_uvccamera.flutter_rk3568_uvccamera.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.aialphay.android.rk3568.arcface.bean.User;
import com.aialphay.android.rk3568.arcface.config.ArcFaceConfig;
import com.aialphay.android.rk3568.arcface.db.DBManager;
import com.aialphay.android.rk3568.arcface.engine.ArcFaceEngine;
import com.aialphay.android.rk3568.arcface.manager.DataBaseManager;
import com.aialphay.android.rk3568.arcface.manager.ModelManager;
import com.aialphay.android.rk3568.utils.FileUtils;
import com.alphay.flutter.plugins.flutter_rk3568_uvccamera.flutter_rk3568_uvccamera.callback.Rk3568UvcCameraCallback;

import java.io.File;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

public class ArcFaceUtils {

  private static ExecutorService executorService = null;

  public static void addFace(String path, Rk3568UvcCameraCallback callback) {
    File faceImgFile = new File(path);
    File faceFileSuccess;
    if (ArcFaceConfig.getInstance().getFaceImagesPath() != null
        && ArcFaceConfig.getInstance().getFaceImagesPath().trim() != "") {
      faceFileSuccess = new File(ArcFaceConfig.getInstance().getFaceImagesPath());
      if (!faceFileSuccess.exists()) {
        faceFileSuccess.mkdirs();
      }
    } else {
      faceFileSuccess = null;
    }
    String picName = faceImgFile.getName();
    String username = FileUtils.getFileNameNoEx(picName);
    Bitmap bitmap = BitmapFactory.decodeFile(faceImgFile.getAbsolutePath());
    byte[] bytes = new byte[1024];
    float ret = ArcFaceEngine.getInstance().personDetect(bitmap, bytes);
    if (bitmap != null && !bitmap.isRecycled()) {
      bitmap = null;
    }
    if (ret == 256) {
      String[] users = username.split("_");
      if (users.length == 2) {
        User user = DataBaseManager.getInstance().getUserByUserId(users[0]);
        if (user != null) {
          DataBaseManager.getInstance().deleteFace(users[0]);
          FileUtils.deleteFile(faceFileSuccess.getAbsolutePath() + "/" + user.getImageName());
        }
        boolean importDBSuccess =
            ArcFaceEngine.getInstance()
                .registerUserIntoDBmanager(
                    DBManager.GROUP_ID, users[0], users[1], picName, null, bytes);
        if (importDBSuccess) {
          if (faceFileSuccess != null) {
            FileUtils.copyFile(
                faceImgFile.getAbsolutePath(), faceFileSuccess.getAbsolutePath() + "/" + picName);
          }
          synchronized (DataBaseManager.getInstance().getAllUserList()) {
            ModelManager.getInstance()
                .getFaceFeature()
                .featurePush(DataBaseManager.getInstance().getAllUserList());
            ArcFaceEngine.getInstance().setPush(true);
          }
          callback.addFaceSuccess();
        } else {
          callback.addFaceFailed();
        }
      } else {
        callback.addFaceFailed();
      }
    } else {
      callback.addFaceFailed();
    }
  }

  public static void reAddFace(String path, Rk3568UvcCameraCallback callback) {
    if (executorService != null) {
      return;
    }
    executorService = Executors.newFixedThreadPool(4);
    File faceFile = new File(path);
    File faceFileFail = new File(path + "_fail");
    if (!faceFileFail.exists()) {
      faceFileFail.mkdirs();
    }
    File faceFileSuccess;
    if (ArcFaceConfig.getInstance().getFaceImagesPath() != null
        && ArcFaceConfig.getInstance().getFaceImagesPath().trim() != "") {
      faceFileSuccess = new File(ArcFaceConfig.getInstance().getFaceImagesPath());
      if (!faceFileSuccess.exists()) {
        faceFileSuccess.mkdirs();
      }
    } else {
      faceFileSuccess = null;
    }
    File[] files = faceFile.listFiles();
    if (files != null && files.length > 0) {
      ArcFaceEngine.getInstance().userClean();
      // 使用CountDownLatch来等待所有任务完成
      CountDownLatch latch = new CountDownLatch(files.length);
      // 创建一个原子整数，用于记录已经完成的任务数
      AtomicInteger count = new AtomicInteger(0);
      for (int i = 0; i < files.length; i++) {
        File file = files[i];

        // 提交任务到线程池
        executorService.submit(
            () -> {
              try {
                String picName = file.getName();
                String username = FileUtils.getFileNameNoEx(picName);
                Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
                byte[] bytes = new byte[1024];
                float ret = ArcFaceEngine.getInstance().personDetect(bitmap, bytes);
                if (bitmap != null && !bitmap.isRecycled()) {
                  bitmap = null;
                }
                count.incrementAndGet();
                if (ret == 256) {
                  String[] users = username.split("_");
                  if (users.length == 2) {
                    boolean importDBSuccess =
                        ArcFaceEngine.getInstance()
                            .registerUserIntoDBmanager(
                                DBManager.GROUP_ID, users[0], users[1], picName, null, bytes);
                    if (faceFileSuccess != null) {
                      FileUtils.copyFile(
                          file.getAbsolutePath(),
                          faceFileSuccess.getAbsolutePath() + "/" + picName);
                    }
                  }
                } else {
                  FileUtils.copyFile(
                      file.getAbsolutePath(), faceFileFail.getAbsolutePath() + "/" + picName);
                }
                callback.updateAddFaceProcess(count.get() * 100.0f / files.length);
              } finally {
                latch.countDown(); // 任务完成，计数器减1
              }
            });
      }

      // 在单独的线程中等待所有任务完成
      new Thread(
          () -> {
            try {
              // 等待所有任务完成
              latch.await();
              synchronized (ArcFaceEngine.getInstance().getAllUserList()) {
                ModelManager.getInstance()
                    .getFaceFeature()
                    .featurePush(ArcFaceEngine.getInstance().getAllUserList());
                ArcFaceEngine.getInstance().setPush(true);
              }
              // 人脸导入成功回调
              callback.batchAddFaceSuccess();
            } catch (InterruptedException e) {
              e.printStackTrace();
            } finally {
              // 关闭线程池
              executorService.shutdown();
              executorService = null;
            }
          })
          .start();
    } else {
      // 人脸导入成功回调
      callback.batchAddFaceSuccess();
      executorService.shutdown();
      executorService = null;
    }
  }
}
