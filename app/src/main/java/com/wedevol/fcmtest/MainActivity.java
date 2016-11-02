package com.wedevol.fcmtest;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.RemoteMessage;

import java.io.IOException;
import java.util.Random;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

/**
 * Main Activity
 */
public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    public static final String FCM_PROJECT_SENDER_ID = "431269160141";
    public static final String FCM_SERVER_CONNECTION = "@gcm.googleapis.com";
    public static final String BACKEND_SERVER_IP = "10.0.2.2";
    public static final String BACKEND_URL_BASE = "http://" + BACKEND_SERVER_IP;
    public static final String BACKEND_ACTION_MESSAGE = "MESSAGE";
    public static final String BACKEND_ACTION_ECHO = "ECHO";
    public static final Random RANDOM = new Random();


    private EditText editTextEcho;
    private TextView deviceText;
    private Button buttonUpstreamEcho;

    private String message;
    private String token = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        deviceText = (TextView) findViewById(R.id.deviceText);
        editTextEcho = (EditText) findViewById(R.id.editTextEcho);
        buttonUpstreamEcho = (Button) findViewById(R.id.buttonUpstreamEcho);

        Log.d(TAG, "On create logic");
        FirebaseMessaging.getInstance().subscribeToTopic("test");
        token = FirebaseInstanceId.getInstance().getToken();
        Log.d(TAG, "Token: " + token);
        deviceText.setText("Device: " + token);

        new AsyncTask() {

            @Override
            protected Object doInBackground(Object[] params) {

                registerTokenInDB(token);
                return null;
            }
        }.execute(null, null, null);

        buttonUpstreamEcho.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Log.d(TAG, "Echo Upstream message logic");
                message = editTextEcho.getText().toString();
                Log.d(TAG, "Message: " + message + ", recipient: " + token);
                FirebaseMessaging.getInstance().send(new RemoteMessage.Builder(FCM_PROJECT_SENDER_ID + FCM_SERVER_CONNECTION)
                        .setMessageId(Integer.toString(RANDOM.nextInt()))
                        .addData("message", message)
                        .addData("action", BACKEND_ACTION_ECHO)
                        .build());
                // To send a message to other device through the XMPP Server, you should add the
                // receiverId and change the action name to BACKEND_ACTION_MESSAGE in the data
            }
        });

    }

    public static void registerTokenInDB(String token) {
        // The call should have a back off strategy

        OkHttpClient client = new OkHttpClient();
        String url = MainActivity.BACKEND_URL_BASE + "/fcmtest/register.php";
        RequestBody body = new FormBody.Builder()
                .add("token", token)
                .build();

        Log.d(TAG, url);
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();

        try {
            client.newCall(request).execute();
        } catch (IOException e) {
            Log.d(TAG, "Error trying to register the token in the DB");
        }
    }

}
