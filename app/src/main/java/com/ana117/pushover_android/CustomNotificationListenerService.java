package com.ana117.pushover_android;

import android.app.Notification;
import android.util.Log;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.os.Bundle;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import java.io.IOException;

public class CustomNotificationListenerService extends NotificationListenerService {

    private static final String TAG = "ListenerService";

    private final OkHttpClient client = new OkHttpClient();
    public static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

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

        if (notification != null) {
            Bundle extras = notification.extras;
            CharSequence title = extras.getCharSequence(Notification.EXTRA_TITLE);
            CharSequence text = extras.getCharSequence(Notification.EXTRA_TEXT);

            Log.d(TAG, "  Package: " + packageName);
            Log.d(TAG, "  Title: " + (title != null ? title.toString() : "N/A"));
            Log.d(TAG, "  Text: " + (text != null ? text.toString() : "N/A"));

            sendNotificationDataToEndpoint(packageName, title != null ? title.toString() : "", text != null ? text.toString() : "");
        }
    }

    private void sendNotificationDataToEndpoint(String packageName, String title, String text) {
        String endpointUrl = "http://192.168.0.125:3333/alert";

        String message = String.format("%s\n%s", title, text);
        String json = String.format(
                "{\"title\": \"%s\", \"message\": \"%s\"}",
                escapeJson(packageName),
                escapeJson(message)
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
}