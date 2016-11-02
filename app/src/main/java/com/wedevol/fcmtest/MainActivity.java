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
public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    public static final String FCM_PROJECT_SENDER_ID = "431269160141";
    public static final String FCM_SERVER_CONNECTION = "@gcm.googleapis.com";
    public static final String BACKEND_SERVER_IP = "10.0.2.2";
    public static final String BACKEND_URL_BASE = "http://" + BACKEND_SERVER_IP;
    public static final String BACKEND_ACTION_MESSAGE = "MESSAGE";
    public static final String BACKEND_ACTION_ECHO = "com.wedevol.ECHO";
    public static final Random RANDOM = new Random();


    private EditText editTextEcho;
    private TextView deviceText;
    private Button buttonUpstreamEcho;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        deviceText = (TextView) findViewById(R.id.deviceText);
        editTextEcho = (EditText) findViewById(R.id.editTextEcho);
        buttonUpstreamEcho = (Button) findViewById(R.id.buttonUpstreamEcho);

        Log.d(TAG, "Create logic");
        FirebaseMessaging.getInstance().subscribeToTopic("test");
        final String token = FirebaseInstanceId.getInstance().getToken();
        Log.d(TAG, "Token: " + token);
        deviceText.setText(token);

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

    public void registerTokenInDB(final String token) {
        // The call should have a back off strategy
        Log.d(TAG, "Register token in database logic");

        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(this);
        String url = BACKEND_URL_BASE + "/PHP/fcmtest/register.php";

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d(TAG, "Token registered successfully in the DB");
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(TAG, "Error trying to register the token in the DB");
            }
        }){
            @Override
            protected Map<String,String> getParams(){
                Map<String,String> params = new HashMap<String, String>();
                params.put("token",token);
                return params;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String,String> params = new HashMap<String, String>();
                params.put("Content-Type","application/x-www-form-urlencoded");
                return params;
            }
        };
        // Add the request to the RequestQueue.
        queue.add(stringRequest);

    }

}
