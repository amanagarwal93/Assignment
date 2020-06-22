package com.example.assignment.utility;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import androidx.annotation.NonNull;

public class DataStorage {

    private static String MY_PREFERENCES = "preferences";

    private static final String NAME = "name";

    private static DataStorage instance = null;

    private final SharedPreferences defaultSharedPref;
    private final SharedPreferences appSharedPref;

    private DataStorage(@NonNull Context context) {
        this.defaultSharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        this.appSharedPref = context.getSharedPreferences(MY_PREFERENCES, Context.MODE_PRIVATE);
    }

    public static DataStorage getInstance(Context context) {
        if (instance == null) {
            instance = new DataStorage(context.getApplicationContext());
        }
        return instance;
    }

    public void clearStorage(){
        SharedPreferences.Editor editor = defaultSharedPref.edit();

        editor.clear();
        editor.apply();
    }

    private void editDefaultSharedPref(@NonNull String key, @NonNull Object value) {
        SharedPreferences.Editor editor = defaultSharedPref.edit();

        if (value instanceof String) {
            editor.putString(key, String.valueOf(value));
        } else if (value instanceof Integer) {
            editor.putInt(key, (Integer) value);
        } else if (value instanceof Boolean) {
            editor.putBoolean(key, (Boolean) value);
        }

        editor.apply();
    }

    public String getName() {
        return defaultSharedPref.getString(NAME, "");
    }

    public void setName(String name) {
        editDefaultSharedPref(NAME, name);
    }

}
