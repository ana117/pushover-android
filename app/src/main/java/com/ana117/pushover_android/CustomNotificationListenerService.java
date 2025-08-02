package com.ana117.pushover_android;

import android.app.Notification;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Icon;
import android.util.Base64;
import android.util.Log;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.os.Bundle;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class CustomNotificationListenerService extends NotificationListenerService {

    private static final String TAG = "ListenerService";

    private final OkHttpClient client = new OkHttpClient();
    public static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    private static final String PREFS_NAME = "Pushover";
    private static final String PREFS_KEY = "host";

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "NotificationListenerService onCreate");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "NotificationListenerService onDestroy");
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        Log.d(TAG, "Notification Posted: " + sbn.getPackageName());

        String packageName = sbn.getPackageName();
        Notification notification = sbn.getNotification();

        String b64 = null;
        if (notification != null) {
            Bundle extras = notification.extras;
            CharSequence title = extras.getCharSequence(Notification.EXTRA_TITLE);
            CharSequence text = extras.getCharSequence(Notification.EXTRA_TEXT);

            if (text == null || title == null) return;

            Icon icon = notification.getLargeIcon();
            if (icon != null) {
                Drawable drawable = icon.loadDrawable(this);
                Bitmap bitmap = null;
                if (drawable instanceof BitmapDrawable) {
                    BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
                    if(bitmapDrawable.getBitmap() != null) {
                        bitmap = bitmapDrawable.getBitmap();
                    }
                }

                if (bitmap != null) {
                    b64 = bitmapToBase64(bitmap);
                }
            }

            Log.d(TAG, "  Package: " + packageName);
            Log.d(TAG, "  Title: " + title);
            Log.d(TAG, "  Text: " + text);
            Log.d(TAG, "  Icon: " + notification.getLargeIcon());

            sendNotificationDataToEndpoint(
                    packageName,
                    title.toString(),
                    text.toString(),
                    b64 != null ? b64 : ""
            );
        }
    }

    private String getUrl() {
        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getString(PREFS_KEY, null);
    }

    private void sendNotificationDataToEndpoint(String packageName, String title, String text, String icon) {
        String endpointUrl = getUrl();
        if (endpointUrl == null) {
            Log.e(TAG, "URL not set");
            return;
        }

        PackageManager pm = getApplicationContext().getPackageManager();
        ApplicationInfo ai;
        try {
            ai = pm.getApplicationInfo(packageName, 0);
        } catch (PackageManager.NameNotFoundException e) {
            ai = null;
        }
        String applicationName = (String) (ai != null ? pm.getApplicationLabel(ai) : packageName);

        String header = String.format("%s (%s)", title, applicationName);
        String json = String.format(
                "{\"title\": \"%s\", \"message\": \"%s\", \"icon\": \"%s\"}",
                escapeJson(header),
                escapeJson(text),
                escapeJson(icon)
        );

        RequestBody body = RequestBody.create(json, JSON);
        Request request = new Request.Builder()
                .url(endpointUrl)
                .post(body)
                .build();

        new Thread(() -> {
            try (Response response = client.newCall(request).execute()) {
                if (response.isSuccessful()) {
                    Log.d(TAG, "Notification sent successfully: " + response.body().string());
                } else {
                    Log.e(TAG, "Failed to send notification: " + response.code() + " " + response.message());
                    Log.e(TAG, "Response Body: " + response.body().string());
                }
            } catch (IOException e) {
                Log.e(TAG, "Network request failed: " + e.getMessage());
            }
        }).start();
    }

    private String escapeJson(String s) {
        if (s == null) {
            return "";
        }
        return s.replace("\"", "\\\"")
                .replace("\\", "\\\\")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    private String bitmapToBase64(Bitmap bitmap) {
        if (bitmap == null) {
            return null;
        }

        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
            try {
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
                byte[] byteArray = byteArrayOutputStream.toByteArray();
                return Base64.encodeToString(byteArray, Base64.DEFAULT);
            } catch (Exception e) {
                return null;
            }
        } catch (IOException ignored) {
            return null;
        }
    }
}