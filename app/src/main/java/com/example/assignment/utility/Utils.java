package com.example.assignment.utility;

import android.content.Context;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.ColorRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.example.assignment.R;
import com.google.android.material.snackbar.Snackbar;

public class Utils {

    private static Snackbar snackbar;

    private static Utils instance = null;

    public static Utils getInstance() {
        if (instance == null) {
            instance = new Utils();
        }
        return instance;
    }

    public static void showToastMessage(@NonNull View rootLayout, String message) {
        showToastMessage(rootLayout, message, Snackbar.LENGTH_LONG,
                null, null, null);
    }

    public static void showShortToastMessage(@NonNull View rootLayout, String message) {
        showToastMessage(rootLayout, message, Snackbar.LENGTH_LONG,
                null, null, null);
    }

    public static void showToastMessage(@NonNull View rootLayout, String message, @ColorRes int backgroundColor) {
        showToastMessage(rootLayout, message, Snackbar.LENGTH_LONG,
                null, null, backgroundColor);
    }

    public static void showToastMessage(@NonNull View rootLayout, String message, String actionButtonText,
                                        View.OnClickListener actionButtonClickListener) {
        showToastMessage(rootLayout, message, Snackbar.LENGTH_LONG, actionButtonText,
                actionButtonClickListener, null);
    }

    public static void showToastMessage(@NonNull View rootLayout, String message, int duration,
                                        String actionButtonText, View.OnClickListener actionButtonClickListener) {
        showToastMessage(rootLayout, message, duration, actionButtonText, actionButtonClickListener, null);
    }

    private static void showToastMessage(@NonNull View rootLayout,
                                         String message,
                                         Integer duration,
                                         @Nullable String actionButtonText,
                                         @Nullable View.OnClickListener actionButtonClickListener,
                                         @Nullable @ColorRes Integer backgroundColor) {
        snackbar = Snackbar.make(rootLayout, message, duration);
        snackbar.setAnimationMode(Snackbar.ANIMATION_MODE_SLIDE);

        View snackBarView = snackbar.getView();

        TextView tv = snackBarView.findViewById(R.id.snackbar_text);
        tv.setTextColor(Color.WHITE);
        tv.setMaxLines(5);

        if (backgroundColor != null) {
            snackBarView.setBackgroundColor(ContextCompat.getColor(rootLayout.getContext(),
                    backgroundColor));
        }

        if (actionButtonText != null && actionButtonClickListener != null) {
            snackbar.setAction(actionButtonText, actionButtonClickListener);
        }

        snackbar.show();
    }

    public static Snackbar getSnackbar() {
        return snackbar;
    }

    public boolean isNetworkAvailable(Context context) {
        boolean nwFlag = false;
        try {
            ConnectivityManager connMgr = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = null;
            if (connMgr != null) {
                networkInfo = connMgr.getActiveNetworkInfo();
            }
            if (networkInfo != null && networkInfo.isConnected()) {
                nwFlag = true;
            }
        } catch (Exception e) {
        }

        return nwFlag;
    }
}
