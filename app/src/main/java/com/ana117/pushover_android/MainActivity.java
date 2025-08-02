package com.ana117.pushover_android;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.provider.Settings;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    private static final String ENABLED_NOTIFICATION_LISTENERS = "enabled_notification_listeners";
    private static final String ACTION_NOTIFICATION_LISTENER_SETTINGS = "android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS";


    private TextView displayTextView;
    private EditText editText;
    private Button saveButton;

    private static final String PREFS_NAME = "Pushover";
    private static final String PREFS_KEY = "host";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        displayTextView = findViewById(R.id.statusTextView);
        editText = findViewById(R.id.endpointUrlEditText);
        saveButton = findViewById(R.id.saveUrlButton);
        saveButton.setOnClickListener(v -> saveButtonClick());
        loadUrl();

        if (!isNotificationServiceEnabled()){
            AlertDialog enableNotificationListenerAlertDialog = buildNotificationServiceAlertDialog();
            enableNotificationListenerAlertDialog.show();
        } else {
            hostRunningInit();
        }
    }

    private void saveButtonClick() {
        String url = editText.getText().toString();
        saveUrl(url);
    }

    private void saveUrl(String url) {
        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        OkHttpClient client = new OkHttpClient();
        new Thread(() -> {
            try (Response response = client.newCall(request).execute()) {
                if (response.isSuccessful()) {
                    SharedPreferences sp = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sp.edit();

                    editor.putString(PREFS_KEY, url);
                    editor.apply();

                    runOnUiThread(() -> {
                        displayTextView.setText("Server is running!");
                        Toast.makeText(this, "URL saved!", Toast.LENGTH_SHORT).show();
                    });
                } else {
                    Log.e("MAIN", "Response unsuccesfull: " + response.body().string());
                }
            } catch (IOException e) {
                Log.e("MAIN", "Network request failed: " + e.getMessage());
            }
        }).start();
    }

    private void loadUrl() {
        SharedPreferences sp = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String url = sp.getString(PREFS_KEY, null);
        if (url != null) {
            editText.setText(url);
        }
    }

    private String getUrl() {
        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getString(PREFS_KEY, null);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private boolean isNotificationServiceEnabled(){
        String pkgName = getPackageName();
        final String flat = Settings.Secure.getString(getContentResolver(),
                ENABLED_NOTIFICATION_LISTENERS);
        if (!TextUtils.isEmpty(flat)) {
            final String[] names = flat.split(":");
            for (String name : names) {
                final ComponentName cn = ComponentName.unflattenFromString(name);
                if (cn != null) {
                    if (TextUtils.equals(pkgName, cn.getPackageName())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private AlertDialog buildNotificationServiceAlertDialog(){
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle(R.string.notification_listener_service);
        alertDialogBuilder.setMessage(R.string.notification_listener_service_explanation);
        alertDialogBuilder.setPositiveButton(R.string.yes,
                (dialog, id) -> startActivity(new Intent(ACTION_NOTIFICATION_LISTENER_SETTINGS)));
        alertDialogBuilder.setNegativeButton(R.string.no,
                (dialog, id) -> {
                });
        return(alertDialogBuilder.create());
    }

    private void hostRunningInit() {
        String url = getUrl();
        if (url != null)  {
            saveUrl(url);
        }
    }
}