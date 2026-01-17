package com.aialphay.android.rk3568.arcface.manager;

import android.content.Context;
import android.text.TextUtils;

import com.aialphay.android.rk3568.arcface.bean.User;
import com.aialphay.android.rk3568.arcface.callback.DataBaseListener;
import com.aialphay.android.rk3568.arcface.db.DBManager;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DataBaseManager {

  private boolean isPush = false;
  private ExecutorService executorService = Executors.newSingleThreadExecutor();
  private Future initPushFuture;

  private List<User> users;

  public void clearTable() {
    users = new ArrayList<>();
    DBManager.getInstance().clearTable();
  }

  public boolean isPush() {
    return isPush;
  }

  public void setPush(boolean b) {
    this.isPush = b;
  }

  public int getUserCount() {
    return users == null ? 0 : users.size();
  }

  public boolean userAdd(User user) {
    if (user == null || TextUtils.isEmpty(user.getGroupId())) {
      return false;
    }
    Pattern pattern = Pattern.compile("^[0-9a-zA-Z_-]{1,}$");
    Matcher matcher = pattern.matcher(user.getUserId());
    if (!matcher.matches()) {
      return false;
    }
    boolean ret = DBManager.getInstance().addUser(user);
    User newUser = DBManager.getInstance().queryUserByUserNameItem(user.getUserName());
    users.add(0, newUser);
    return ret;
  }

  public void deleteFace(String userId) {
    DBManager.getInstance().deleteUser(userId);
  }

  private static class HolderClass {
    private static final DataBaseManager DATA_BASE_MANAGER = new DataBaseManager();
  }

  public static DataBaseManager getInstance() {
    return DataBaseManager.HolderClass.DATA_BASE_MANAGER;
  }

  public void initDatabases(Context context, DataBaseListener listener) {
    isPush = false;
    DBManager.getInstance().init(context);
    initPush(listener);
  }

  public List<User> getAllUserList() {
    if (users == null || users.size() == 0) {
      users = DBManager.getInstance().queryAllUsers();
    }
    return users;
  }

  public User getUserById(int id) {
    if (id < 0) {
      return null;
    }
    List<User> userList = DBManager.getInstance().queryUserById(id);
    if (userList != null && userList.size() > 0) {
      return userList.get(0);
    }
    return null;
  }

  private void initPush(DataBaseListener listener) {
    if (initPushFuture != null && !initPushFuture.isDone()) {
      initPushFuture.cancel(true);
    }

    initPushFuture =
        executorService.submit(
            new Runnable() {
              @Override
              public void run() {
                synchronized (getAllUserList()) {
                  ModelManager.getInstance().getFaceFeature().featurePush(getAllUserList());
                  if (users != null && users.size() > 0) {
                    if (listener != null) {
                      listener.initFullUsersSuccess();
                    }
                    isPush = true;
                  }
                }
              }
            });
  }
}
