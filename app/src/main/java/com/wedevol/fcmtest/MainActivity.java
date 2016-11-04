package com.wedevol.fcmtest;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.RemoteMessage;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;


/**
 * Main Activity
 */
public class MainActivity extends AppCompatActivity implements IRequestListener {

    private static final String TAG = "MainActivity";
    public static final String FCM_PROJECT_SENDER_ID = "431269160141";
    public static final String FCM_SERVER_CONNECTION = "@gcm.googleapis.com";
    public static final String BACKEND_ACTION_MESSAGE = "MESSAGE";
    public static final String BACKEND_ACTION_ECHO = "com.wedevol.ECHO";
    public static final Random RANDOM = new Random();


    private EditText editTextEcho;
    private TextView deviceText;
    private Button buttonUpstreamEcho;
    private TokenService tokenService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d(TAG, "FCM Token create method");

        // Get variables reference
        deviceText = (TextView) findViewById(R.id.deviceText);
        editTextEcho = (EditText) findViewById(R.id.editTextEcho);
        buttonUpstreamEcho = (Button) findViewById(R.id.buttonUpstreamEcho);

        //Get token from Firebase
        FirebaseMessaging.getInstance().subscribeToTopic("test");
        final String token = FirebaseInstanceId.getInstance().getToken();
        Log.d(TAG, "Token: " + token);
        deviceText.setText(token);

        //Call the token service to save the token in the database
        tokenService = new TokenService(this, this);
        tokenService.execute(token);

        buttonUpstreamEcho.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Log.d(TAG, "Echo Upstream message logic");
                String message = editTextEcho.getText().toString();
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


    @Override
    public void onComplete() {
        //success
    }

    @Override
    public void onError(int code, String message) {
    //error
    }
}
