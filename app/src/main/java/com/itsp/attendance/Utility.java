package com.itsp.attendance;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Utility
{
    private static final String TAG = Utility.class.getName();

    public static void ASSERT(boolean condition)
    {
        if(BuildConfig.DEBUG && !condition)
        {
            throw new AssertionError("Assertion!");
        }
    }

    public static void debugToast(Activity activity, String message)
    {
        /*
        if(com.itsp.attendance.BuildConfig.DEBUG)
        {
            LayoutInflater inflater = LayoutInflater.from(activity);
            View layout = inflater.inflate(R.layout.toast_debug,
                    (ViewGroup) activity.findViewById(R.id.toast_debug_container));

            TextView textview = (TextView) layout.findViewById(R.id.toast_debug_text);
            textview.setText("DEBUG: " + message);

            Toast toast = new Toast(activity.getApplicationContext());
            toast.setGravity(Gravity.TOP, 0, 100);
            toast.setDuration(Toast.LENGTH_LONG);
            toast.setView(layout);
            toast.show();
        }
         */
    }

    public static String loadRawResourceKey(Context context, int path, String key)
    {
        String result = null;

        try
        {
            Resources resources = context.getResources();
            InputStream rawResource = resources.openRawResource(path);
            Properties properties = new Properties();
            properties.load(rawResource);
            result = properties.getProperty(key);

        } catch (Resources.NotFoundException e)
        {
            Log.e(TAG, "Unable to find the file specified: " + e.getMessage());
        } catch (IOException e)
        {
            Log.e(TAG, "Failed to open the file: " + e.getMessage());
        }
        return result;
    }
}
