package com.aialphay.android.rk3568.iccard;

import android.content.Context;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Handler;
import android.os.Looper;

import com.aialphay.android.rk3568.arcface.ArcFaceConstants;
import com.aialphay.android.rk3568.iccard.callback.ICCardReadCallback;
import com.mwcardx9.ReaderAndroidUsb;

import java.util.Iterator;

public class ICCardReadService {

  private static final String ACTION_USB_PERMISSION = "MWCARD_USE_PERMISSION";
  private UsbDevice targetDevice;
  private UsbManager usbManager;
  private ReaderAndroidUsb reader;
  private Boolean isFinished;
  private Handler mainHandler = new Handler(Looper.getMainLooper());

  public void setFinished(Boolean finished) {
    isFinished = finished;
  }

  public void initICCardReadService(Context context) {
    usbManager = (UsbManager) context.getSystemService(Context.USB_SERVICE);
    Iterator<UsbDevice> deviceList = usbManager.getDeviceList().values().iterator();
    while (deviceList.hasNext()) {
      UsbDevice device = deviceList.next();
      if (!ReaderAndroidUsb.isSupported(device)) {
        continue;
      }
      reader = new ReaderAndroidUsb(usbManager);
      int ret = reader.openReader(device);
      if (ret >= 0) {
        targetDevice = device;
      }
    }
  }

  public void startReadICCardID(final ICCardReadCallback callback) {
    if (checkDevice()) {
      ArcFaceConstants.icCardFuture =
          ArcFaceConstants.icCardExecutorService.submit(
              new Runnable() {
                @Override
                public void run() {
                  try {
                    while (!isFinished) {
                      int cardType = -1;
                      try {
                        cardType = reader.request(2);
                      } catch (Exception e) {
                        Thread.sleep(50);
                        continue;
                      }
                      if (cardType >= 0) {
                        reader.beep(1, 1, 2);
                        String cardSerialHex = null;
                        try {
                          cardSerialHex = reader.openCard(1);
                          callback.onReaCardIDSuccess(cardSerialHex);
                        } catch (Exception e) {
                          throw new RuntimeException(e);
                        }
                        isFinished = true;
                      } else {
                        isFinished = true;
                      }
                    }
                  } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    isFinished = true;
                  }
                }
              });
    }
  }

  private boolean checkDevice() {
    if (targetDevice == null || reader == null) {
      return false;
    }
    if (ArcFaceConstants.icCardFuture != null && !ArcFaceConstants.icCardFuture.isDone()) {
      ICCardReadService.getInstance().setFinished(true);
      ArcFaceConstants.icCardFuture.cancel(true);
    }
    isFinished = false;
    return true;
  }

  private static class HolderClass {
    private static final ICCardReadService IC_CARD_READ_SERVICE = new ICCardReadService();
  }

  public static ICCardReadService getInstance() {
    return ICCardReadService.HolderClass.IC_CARD_READ_SERVICE;
  }
}
