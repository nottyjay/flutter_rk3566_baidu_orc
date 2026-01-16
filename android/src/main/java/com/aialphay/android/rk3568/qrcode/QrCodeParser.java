package com.aialphay.android.rk3568.qrcode;

import android.graphics.Bitmap;

public class QrCodeParser {

  private static final String TAG = "QrCodeParser";

  public String decode(Bitmap rgbData) {
    return null;
  }

  private static class HolderClass {
    private static final QrCodeParser QR_CODE_PARSER = new QrCodeParser();
  }

  public static QrCodeParser getInstance() {
    return QrCodeParser.HolderClass.QR_CODE_PARSER;
  }
}
