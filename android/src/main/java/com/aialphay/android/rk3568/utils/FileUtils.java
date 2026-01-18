package com.aialphay.android.rk3568.utils;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.os.Environment;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;

/** author : shangrong date : 2019/5/23 2:05 PM description :文件工具类 */
public class FileUtils {
  /** 获取导入图片文件的目录信息 */
  public static File getBatchImportDirectoryatwo() {
    // 获取根目录
    File sdRootFile = getSDRootFile();
    File file = null;
    if (sdRootFile != null && sdRootFile.exists()) {
      file = new File(sdRootFile, "test");
      if (!file.exists()) {
        file.mkdirs();
      }
    }
    return file;
  }

  /**
   * 读取txt文件的内容
   *
   * @param filePath 想要读取的文件对象
   * @return 返回文件内容
   */
  @SuppressLint("NewApi")
  public static String txt2String(String filePath) {
    if (filePath == null) {
      return "";
    }
    File file = new File(filePath);
    if (!file.exists()) {
      return "";
    }

    StringBuilder result = new StringBuilder();
    try {
      // 构造一个BufferedReader类来读取文件
      BufferedReader br = new BufferedReader(new FileReader(file));
      String s = null;
      // 使用readLine方法，一次读一行
      while ((s = br.readLine()) != null) {
        result.append(System.lineSeparator() + s);
      }
      br.close();
    } catch (Exception e) {
      e.printStackTrace();
    }
    return result.toString();
  }

  /** 写入TXT文件 */
  public static boolean writeTxtFile(String content, String filePath) {
    File file = new File(filePath);
    if (!file.exists()) {
      return false;
    }

    RandomAccessFile mm = null;
    boolean flag = false;
    FileOutputStream fileOutputStream = null;
    try {
      fileOutputStream = new FileOutputStream(file);
      fileOutputStream.write(content.getBytes("utf-8"));
      fileOutputStream.close();
      flag = true;
    } catch (Exception e) {
      e.printStackTrace();
    }
    return flag;
  }

  public static String spotString(String str) {
    int n = 0;
    String newStr = "";
    for (int i = 0, k = str.length(); i < k; i++) {
      char item = str.charAt(i);
      if (isChinese(item)) {
        n += 2;
      } else {
        n += 1;
      }

      if (n > 10) {
        newStr += "...";
        return newStr;
      }
      newStr += item;
    }
    return newStr;
  }

  public static boolean isChinese(char c) {

    return c >= 0x4E00 && c <= 0x9FA5; // 根据字节码判断
  }

  /** Checks if is sd card available.检查SD卡是否可用 */
  public static boolean isSdCardAvailable() {
    return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
  }

  /** Gets the SD root file.获取SD卡根目录 */
  public static File getSDRootFile() {
    if (isSdCardAvailable()) {
      return Environment.getExternalStorageDirectory();
    } else {
      return null;
    }
  }

  /** 获取导入图片文件的目录信息 */
  public static File getBatchImportDirectory() {
    // 获取根目录
    File sdRootFile = getSDRootFile();
    File file = null;
    if (sdRootFile != null && sdRootFile.exists()) {
      file = new File(sdRootFile, "test");
      if (!file.exists()) {
        file.mkdirs();
      }
    }
    return file;
  }

  /** 获取导入图片成功的目录信息 */
  public static File getBatchImportSuccessDirectory() {
    File sdRootFile = getSDRootFile();
    File file = null;
    if (sdRootFile != null && sdRootFile.exists()) {
      file = new File(sdRootFile, "Success-Import");
      if (!file.exists()) {
        file.mkdirs();
      }
    }
    return file;
  }

  /** 判断文件是否存在 */
  public static File isFileExist(String fileDirectory, String fileName) {
    File file = new File(fileDirectory + "/" + fileName);
    try {
      if (!file.exists()) {
        return null;
      }
    } catch (Exception e) {
      return null;
    }
    return file;
  }

  /** 删除文件 */
  public static void deleteFile(String filePath) {
    try {
      // 找到文件所在的路径并删除该文件
      File file = new File(filePath);
      file.delete();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /*
   * 获取不带扩展名的文件名
   * */
  public static String getFileNameNoEx(String filename) {
    if ((filename != null) && (filename.length() > 0)) {
      int dot = filename.lastIndexOf('.');
      if ((dot > -1) && (dot < (filename.length()))) {
        return filename.substring(0, dot);
      }
    }
    return filename;
  }

  /** 保存图片 */
  public static boolean saveBitmap(File file, Bitmap bitmap) {
    FileOutputStream out = null;
    try {
      out = new FileOutputStream(file);
      bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
      return true;
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      try {
        if (out != null) {
          out.close();
        }
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    return false;
  }

  public static boolean saveByte2ImageNio(byte[] bytes, String savePath) {
    if (bytes == null || bytes.length == 0 || savePath.isEmpty()) {
      return false;
    }
    FileOutputStream fos = null;
    try {
      File file = new File(savePath);
      if (!file.getParentFile().exists()) {
        file.getParentFile().mkdirs();
      }
      fos = new FileOutputStream(file);
      // NIO 通道写入，效率更高
      fos.getChannel().write(ByteBuffer.wrap(bytes));
      return true;
    } catch (Exception e) {
      e.printStackTrace();
      return false;
    } finally {
      if (fos != null) {
        try {
          fos.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }
  }

  public static boolean copyFile(String oldPath, String newPath) {
    InputStream inStream = null;
    FileOutputStream fs = null;
    boolean result = false;
    try {
      int bytesum = 0;
      int byteread = 0;
      File oldfile = new File(oldPath);
      // 判断目录是否存在
      File newfile = new File(newPath);
      File newFileDir = new File(newfile.getPath().replace(newfile.getName(), ""));
      if (!newFileDir.exists()) {
        newFileDir.mkdirs();
      }
      if (oldfile.exists()) { // 文件存在时
        inStream = new FileInputStream(oldPath); // 读入原文件
        fs = new FileOutputStream(newPath);
        byte[] buffer = new byte[1444];
        int length;
        while ((byteread = inStream.read(buffer)) != -1) {
          bytesum += byteread; // 字节数 文件大小
          System.out.println(bytesum);
          fs.write(buffer, 0, byteread);
        }
        result = true;
      } else {
        result = false;
      }
    } catch (Exception e) {
      System.out.println("复制单个文件操作出错");
      e.printStackTrace();
    } finally {
      if (inStream != null) {
        try {
          inStream.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
      if (fs != null) {
        try {
          fs.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }
    return result;
  }

  /** 获取导入图片文件的目录信息 */
  public static File getBatchImportDirectorya() {
    // 获取根目录
    File sdRootFile = getSDRootFile();
    File file = null;
    if (sdRootFile != null && sdRootFile.exists()) {
      file = new File(sdRootFile, "Face-Import");
      if (!file.exists()) {
        file.mkdirs();
      }
    }
    return file;
  }

  public static void deleteDirAllContent(File dir) {
    // 1. 判断是否是文件夹，且文件夹存在
    if (dir.exists() && dir.isDirectory()) {
      // 2. 获取文件夹下的所有子文件/子文件夹
      File[] files = dir.listFiles();
      if (files != null) {
        for (File file : files) {
          if (file.isFile()) {
            // 3. 如果是文件：立即删除
            file.delete();
            System.out.println("删除文件：" + file.getAbsolutePath());
          } else {
            // 4. 如果是文件夹：递归删除它的所有内容
            deleteDirAllContent(file);
            // 5. 子文件夹清空后，立即删除该空的子文件夹
            file.delete();
            System.out.println("删除空文件夹：" + file.getAbsolutePath());
          }
        }
      }
    }
  }
}
