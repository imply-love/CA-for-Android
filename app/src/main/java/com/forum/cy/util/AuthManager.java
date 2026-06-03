package com.forum.cy.util;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * 简单的登录状态管理工具类，使用 SharedPreferences 持久化登录信息。
 * 仅在本地保存用户名和登录标记，满足本项目“无后端”需求。
 */
public class AuthManager {
    private static final String PREF_NAME = "auth_prefs";
    private static final String KEY_LOGGED_IN = "logged_in";
    private static final String KEY_USERNAME = "username";

    private SharedPreferences prefs;

    public AuthManager(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    /** 登录成功后调用 */
    public void login(String username) {
        prefs.edit()
                .putBoolean(KEY_LOGGED_IN, true)
                .putString(KEY_USERNAME, username)
                .apply();
    }

    /** 登出 */
    public void logout() {
        prefs.edit()
                .putBoolean(KEY_LOGGED_IN, false)
                .remove(KEY_USERNAME)
                .apply();
    }

    /** 是否已登录 */
    public boolean isLoggedIn() {
        return prefs.getBoolean(KEY_LOGGED_IN, false);
    }

    /** 获取当前登录的用户名（未登录返回 null） */
    public String getUsername() {
        return prefs.getString(KEY_USERNAME, null);
    }
}
