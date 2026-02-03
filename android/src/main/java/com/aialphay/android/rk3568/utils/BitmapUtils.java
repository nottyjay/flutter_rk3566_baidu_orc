package com.aialphay.android.rk3568.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.baidu.idl.main.facesdk.model.BDFaceImageInstance;
import com.baidu.idl.main.facesdk.model.BDFaceSDKCommon;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;

public class BitmapUtils {

  /**
   * 检测 Bitmap 是否有效
   *
   * @param bitmap 待检测的 Bitmap
   * @return true=有效，false=无效
   */
  public static boolean isBitmapValid(Bitmap bitmap) {
    // 1. 先判断是否为 null（最基础）
    if (bitmap == null) {
      return false;
    }

    // 2. 判断是否已被回收（recycle() 后 Bitmap 内部资源释放，不可再使用）
    if (bitmap.isRecycled()) {
      return false;
    }

    // 3. 判断宽高是否有效（宽或高为 0 的 Bitmap 无实际使用价值）
    if (bitmap.getWidth() <= 0 || bitmap.getHeight() <= 0) {
      return false;
    }

    // 4. 可选：额外判断是否为可变位图（如需修改 Bitmap 像素时添加，仅读取可省略）
    // 若不需要修改 Bitmap，此判断可删除
    // if (!bitmap.isMutable()) {
    //     return false;
    // }

    // 所有条件满足，视为有效 Bitmap
    return true;
  }

  public static byte[] bitmap2BytesNoCompress(Bitmap bitmap) {
    if (bitmap == null || bitmap.isRecycled()) {
      return null;
    }
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    // PNG是无损格式，quality写100即可实现无压缩转换
    bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
    return baos.toByteArray();
  }

  /**
   * 写法1【标准转回Bitmap 推荐99%场景使用】 直接将图片字节数组解析为Bitmap，适配 JPEG/PNG/WEBP 所有格式字节数组
   *
   * @param bytes 图片字节数组
   * @return Bitmap对象，失败返回null
   */
  public static Bitmap bytes2Bitmap(byte[] bytes) {
    if (bytes == null || bytes.length == 0) {
      return null;
    }
    // BitmapFactory原生解析方法，高效稳定，自动兼容所有图片格式字节流
    return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
  }

  public static Bitmap getInstaceBmp(BDFaceImageInstance newInstance) {
    Bitmap transBmp = null;
    if (newInstance.imageType == BDFaceSDKCommon.BDFaceImageType.BDFACE_IMAGE_TYPE_RGBA) {
      transBmp =
          Bitmap.createBitmap(newInstance.width, newInstance.height, Bitmap.Config.ARGB_8888);
      transBmp.copyPixelsFromBuffer(ByteBuffer.wrap(newInstance.data));
    } else if (newInstance.imageType == BDFaceSDKCommon.BDFaceImageType.BDFACE_IMAGE_TYPE_BGR) {
      transBmp = BitmapUtils.rgb2Bitmap(newInstance.data, newInstance.width, newInstance.height);
    } else if (newInstance.imageType
        == BDFaceSDKCommon.BDFaceImageType.BDFACE_IMAGE_TYPE_YUV_NV21) {
      transBmp = BitmapUtils.yuv2Bitmap(newInstance.data, newInstance.width, newInstance.height);
    } else if (newInstance.imageType == BDFaceSDKCommon.BDFaceImageType.BDFACE_IMAGE_TYPE_GRAY) {
      transBmp = depth2Bitmap(newInstance.data, newInstance.width, newInstance.height);
    }
    return transBmp;
  }

  public static Bitmap rgb2Bitmap(byte[] bytes, int width, int height) {
    // use Bitmap.Config.ARGB_8888 instead of type is OK
    Bitmap stitchBmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
    byte[] rgba = new byte[width * height * 4];
    for (int i = 0; i < width * height; i++) {
      byte b1 = bytes[i * 3 + 0];
      byte b2 = bytes[i * 3 + 1];
      byte b3 = bytes[i * 3 + 2];
      // set value
      rgba[i * 4 + 0] = b3;
      rgba[i * 4 + 1] = b2;
      rgba[i * 4 + 2] = b1;
      rgba[i * 4 + 3] = (byte) 255;
    }
    stitchBmp.copyPixelsFromBuffer(ByteBuffer.wrap(rgba));
    return stitchBmp;
  }

  public static Bitmap yuv2Bitmap(byte[] data, int width, int height) {
    int frameSize = width * height;
    int[] rgba = new int[frameSize];

    for (int i = 0; i < height; i++) {
      for (int j = 0; j < width; j++) {
        int y = (0xff & ((int) data[i * width + j]));
        int u = (0xff & ((int) data[frameSize + (i >> 1) * width + (j & ~1) + 0]));
        int v = (0xff & ((int) data[frameSize + (i >> 1) * width + (j & ~1) + 1]));
        y = y < 16 ? 16 : y;

        int r = Math.round(1.164f * (y - 16) + 1.596f * (v - 128));
        int g = Math.round(1.164f * (y - 16) - 0.813f * (v - 128) - 0.391f * (u - 128));
        int b = Math.round(1.164f * (y - 16) + 2.018f * (u - 128));

        r = r < 0 ? 0 : (r > 255 ? 255 : r);
        g = g < 0 ? 0 : (g > 255 ? 255 : g);
        b = b < 0 ? 0 : (b > 255 ? 255 : b);

        rgba[i * width + j] = 0xff000000 + (b << 16) + (g << 8) + r;
      }
    }

    Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
    bmp.setPixels(rgba, 0, width, 0, 0, width, height);
    return bmp;
  }

  public static Bitmap depth2Bitmap(byte[] depthBytes, int width, int height) {
    Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
    int[] argbData = new int[width * height];
    for (int i = 0; i < width * height; i++) {
      argbData[i] =
          (((int) depthBytes[i * 2] + depthBytes[i * 2 + 1] * 256) / 10 & 0x000000ff)
              | ((((int) depthBytes[i * 2] + depthBytes[i * 2 + 1] * 256) / 10) & 0x000000ff) << 8
              | ((((int) depthBytes[i * 2] + depthBytes[i * 2 + 1] * 256) / 10) & 0x000000ff) << 16
              | 0xff000000;
    }
    bitmap.setPixels(argbData, 0, width, 0, 0, width, height);
    return bitmap;
  }
}
