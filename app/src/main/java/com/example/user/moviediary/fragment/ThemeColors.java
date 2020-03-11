package com.example.user.moviediary.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.support.annotation.ColorInt;
import android.util.Log;

import com.example.user.moviediary.R;

public class ThemeColors {

    private static final String NAME = "ThemeColors", KEY = "color";

    @ColorInt
    private int color;

    public ThemeColors(Context context) {

        SharedPreferences sharedPreferences = context.getSharedPreferences(NAME, Context.MODE_PRIVATE);
        String stringColor = sharedPreferences.getString(KEY, "252525");
        color = Color.parseColor("#" + stringColor);

        if (isLightActionBar()) context.setTheme(R.style.AppTheme);
        context.setTheme(context.getResources().getIdentifier("T_"
                + stringColor, "style", context.getPackageName()));

    }


    public static void setNewThemeColor(Activity activity, int red, int green, int blue) {

        int colorStep = 15;
        red = Math.round(red / colorStep) * colorStep;
        green = Math.round(green / colorStep) * colorStep;
        blue = Math.round(blue / colorStep) * colorStep;

        String stringColor = Integer.toHexString(Color.rgb(red, green, blue)).substring(2);
        SharedPreferences.Editor editor = activity.getSharedPreferences(NAME, Context.MODE_PRIVATE).edit();
        editor.putString(KEY, stringColor);
        editor.apply();


       if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) activity.recreate();

        else {

            Intent i = activity.getPackageManager().getLaunchIntentForPackage(activity.getPackageName());
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            activity.startActivity(i);

        }
    }

    private boolean isLightActionBar() { // Checking if title text color will be black

        int rgb = (Color.red(color) + Color.green(color) + Color.blue(color)) / 3;
        return rgb > 210;

    }


}