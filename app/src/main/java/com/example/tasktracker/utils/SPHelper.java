package com.example.tasktracker.utils;

import android.content.Context;
import android.content.SharedPreferences;
import com.example.tasktracker.models.UserModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SPHelper {
    private static final String PREFERENCES_NAME = "TaskTrackerPrefs";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_USER_NAME = "user_name";
    private static final String KEY_USER_LOGIN = "user_login";
    private static final String KEY_USER_EMAIL = "user_email";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";

    private SharedPreferences settings;
    private SharedPreferences.Editor editor;
    private static SPHelper instance;

    private SPHelper(Context context) {
        settings = context.getApplicationContext().getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);
        editor = settings.edit();
    }

    public static synchronized SPHelper getInstance(Context context) {
        if (instance == null) {
            instance = new SPHelper(context);
        }
        return instance;
    }

    public void saveUser(UserModel user) {
        editor.putString(KEY_USER_ID, user.getId());
        editor.putString(KEY_USER_NAME, user.getName());
        editor.putString(KEY_USER_EMAIL, user.getEmail());
        editor.putString(KEY_USER_LOGIN, user.getLogin());
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.apply();
    }

    public UserModel getUser() {
        String id = settings.getString(KEY_USER_ID, "");
        String name = settings.getString(KEY_USER_NAME, "");
        String login = settings.getString(KEY_USER_LOGIN, "");
        String email = settings.getString(KEY_USER_EMAIL, "");

        if (id.isEmpty()) {
            return null;
        }

        return new UserModel(id, name, email, login, "", Collections.emptyList(), null, null);
    }

    public boolean isLoggedIn() {
        return settings.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    public String getUserId() {
        return settings.getString(KEY_USER_ID, "");
    }

    public void clearUser() {
        editor.clear();
        editor.apply();
    }

    public void logout() {
        editor.putBoolean(KEY_IS_LOGGED_IN, false);
        editor.apply();
    }
}