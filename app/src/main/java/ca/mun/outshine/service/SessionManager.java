package ca.mun.outshine.service;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {

    // Singleton pattern
    private static SessionManager mInstance;
    private static Context mCtx;
    // File name
    private static final String PREF_NAME = "moaa";
    // Keys (make them public if want access from outside)
    private static final String IS_FIRST_TIME = "isFirstTime";
    private static final String IS_LOGGED_IN = "isLoggedIn";
    private static final String USER_ID = "id";
    private static final String USER_NAME = "name";
    private static final String USER_EMAIL = "email";

    private SessionManager(Context context) {
        mCtx = context;
    }

    public static synchronized SessionManager getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new SessionManager(context);
        }
        return mInstance;
    }

    public void setFirstTime(boolean isFirstTime) {
        SharedPreferences pref = mCtx.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putBoolean(IS_FIRST_TIME, isFirstTime);
        editor.apply();
    }

    public void setLoggedIn(boolean isLoggedIn) {
        SharedPreferences pref = mCtx.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putBoolean(IS_LOGGED_IN, isLoggedIn);
        editor.apply();
    }

    public boolean isFirstTime() {
        SharedPreferences pref = mCtx.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return pref.getBoolean(IS_FIRST_TIME, true);
    }

    public boolean isLoggedIn() {
        SharedPreferences pref = mCtx.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return pref.getBoolean(IS_LOGGED_IN, false);
    }

    public void createLoginSession(String id, String email) {
        SharedPreferences pref = mCtx.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putBoolean(IS_LOGGED_IN, true);
        editor.putString(USER_ID, id);
        editor.putString(USER_EMAIL, email);
        editor.apply();
    }

    public void createLoginSession(String id, String email, String name) {
        SharedPreferences pref = mCtx.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putBoolean(IS_LOGGED_IN, true);
        editor.putString(USER_ID, id);
        editor.putString(USER_EMAIL, email);
        editor.putString(USER_NAME, name);
        editor.apply();
    }

    public String getUserId() {
        SharedPreferences pref = mCtx.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return pref.getString(USER_ID, null);
    }

    public String getUserEmail() {
        SharedPreferences pref = mCtx.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return pref.getString(USER_EMAIL, null);
    }

    public String getUserName() {
        SharedPreferences pref = mCtx.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return pref.getString(USER_NAME, null);
    }

    public void logout() {
        SharedPreferences pref = mCtx.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.clear();
        editor.apply();
        // Intent i = new Intent(mCtx, LoginActivity.class);
        // Closing all the Activities
        // i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        // Add new Flag to start new Activity
        // i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        // Staring Login Activity
        // mCtx.startActivity(i);
    }

}
