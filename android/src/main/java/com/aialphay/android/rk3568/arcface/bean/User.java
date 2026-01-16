/*
 * Copyright (C) 2018 Baidu, Inc. All Rights Reserved.
 */
package com.aialphay.android.rk3568.arcface.bean;

import com.baidu.idl.main.facesdk.model.Feature;

/** 用户实体类 */
public class User extends Feature {

  private String userInfo = "";
  private float featureScore = 0;
  private int userIndex = -1;

  public int getUserIndex() {
    return userIndex;
  }

  public void setUserIndex(int userIndex) {
    this.userIndex = userIndex;
  }

  public User() {}

  public String getUserInfo() {
    return userInfo;
  }

  public void setUserInfo(String userInfo) {
    this.userInfo = userInfo;
  }

  public float getFeatureScore() {
    return featureScore;
  }

  public void setFeatureScore(float featureScore) {
    this.featureScore = featureScore;
  }
}
