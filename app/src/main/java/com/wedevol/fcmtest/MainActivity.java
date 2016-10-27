package com.wedevol.fcmtest;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

/**
 * Main Activity
 */
public class MainActivity extends AppCompatActivity implements Spinner.OnItemSelectedListener {

    private static final String TAG = "MainActivity";
    public static final String FCM_PROJECT_SENDER_ID = "431269160141";
    public static final String FCM_PROJECT_SCOPE = "GCM";
    public static final String FCM_SERVER_CONNECTION = "@gcm.googleapis.com";
    public static final String BACKEND_SERVER_IP = "wedevol.com";
    public static final String BACKEND_URL_BASE = "http://" + BACKEND_SERVER_IP;
    public static final String BACKEND_ACTION_MESSAGE = "com.momentous.wifiesta.notifier.MESSAGE";
    public static final String BACKEND_ACTION_ECHO = "com.momentous.wifiesta.notifier.ECHO";
    public static final Random RANDOM = new Random();


    private EditText editTextSend;
    private EditText editTextEcho;
    private Spinner spinnerTargetDevice;
    private TextView deviceText;
    private Button buttonActivate;
    private Button buttonDelete;
    private Button buttonReset;
    private Button buttonUpstreamSend;
    private Button buttonUpstreamEcho;

    private String message;
    private String recipient;
    private String token = "";
    private ArrayList<String> tokens;
    private ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        deviceText = (TextView) findViewById(R.id.deviceText);
        editTextSend = (EditText) findViewById(R.id.editTextSend);
        editTextEcho = (EditText) findViewById(R.id.editTextEcho);
        buttonActivate = (Button) findViewById(R.id.buttonActivate);
        buttonDelete = (Button) findViewById(R.id.buttonDelete);
        buttonReset = (Button) findViewById(R.id.buttonReset);
        buttonUpstreamSend = (Button) findViewById(R.id.buttonUpstreamSend);
        buttonUpstreamEcho = (Button) findViewById(R.id.buttonUpstreamEcho);
        spinnerTargetDevice = (Spinner) findViewById(R.id.spinnerTargetDevice);

        tokens = new ArrayList<String>();
        adapter = new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_spinner_dropdown_item, tokens);
        spinnerTargetDevice.setOnItemSelectedListener(this);

        Log.d(TAG, "On create logic");
        FirebaseMessaging.getInstance().subscribeToTopic("test");
        token = FirebaseInstanceId.getInstance().getToken();
        Log.d(TAG, "Token: " + token);
        deviceText.setText("Device: " + token);

        new AsyncTask() {

            @Override
            protected Object doInBackground(Object[] params) {

                FirebaseInstanceIDService.registerTokenInAppServer(token);
                getSpinnerData();

                return null;
            }
        }.execute(null, null, null);

        buttonActivate.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                Log.d(TAG, "Activation logic");
                token = FirebaseInstanceId.getInstance().getToken();
                Log.d(TAG, "Token: " + token);
                deviceText.setText("Device: " + token);
                if (!tokens.contains(token)) {
                    tokens.add(token);
                }
                adapter.notifyDataSetChanged();
                buttonDelete.setEnabled(true);
                buttonUpstreamSend.setEnabled(true);
                buttonUpstreamEcho.setEnabled(true);

                new AsyncTask() {

                    @Override
                    protected Object doInBackground(Object[] params) {

                        FirebaseInstanceIDService.registerTokenInAppServer(token);

                        return null;
                    }
                }.execute(null, null, null);
            }
        });


        buttonDelete.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                Log.d(TAG, "Deletion logic");
                deviceText.setText("");
                if (tokens.contains(token)) {
                    tokens.remove(token);
                }
                adapter.notifyDataSetChanged();
                buttonDelete.setEnabled(false);
                buttonUpstreamSend.setEnabled(false);
                buttonUpstreamEcho.setEnabled(false);

                new AsyncTask() {

                    @Override
                    protected Object doInBackground(Object[] params) {
                        try {

                            FirebaseInstanceId.getInstance().deleteToken(FCM_PROJECT_SENDER_ID, FCM_PROJECT_SCOPE);
                            deleteToken(token);

                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        return null;
                    }
                }.execute(null, null, null);

            }
        });

        buttonReset.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                Log.d(TAG, "Reset logic (delete all tokens)");
                deviceText.setText("");
                tokens.removeAll(tokens);
                adapter.notifyDataSetChanged();
                buttonDelete.setEnabled(false);
                buttonUpstreamSend.setEnabled(false);
                buttonUpstreamEcho.setEnabled(false);

                new AsyncTask() {

                    @Override
                    protected Object doInBackground(Object[] params) {
                        try {

                            //This should not be implemented (just for testing purposes)
                            FirebaseInstanceId.getInstance().deleteInstanceId();
                            deleteTokens();

                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        return null;
                    }
                }.execute(null, null, null);

            }
        });

        buttonUpstreamSend.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Log.d(TAG, "Upstream message logic");
                message = editTextSend.getText().toString();
                recipient = spinnerTargetDevice.getSelectedItem().toString();
                Log.d(TAG, "Message: " + message + ", recipient: " + recipient);
                FirebaseMessaging.getInstance().send(new RemoteMessage.Builder(FCM_PROJECT_SENDER_ID + FCM_SERVER_CONNECTION)
                        .setMessageId(Integer.toString(RANDOM.nextInt()))
                        .addData("message", message)
                        .addData("recipient", recipient)
                        .addData("action", BACKEND_ACTION_MESSAGE)
                        .build());
            }
        });

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
            }
        });

    }

    private void deleteToken(String token) {

        OkHttpClient client = new OkHttpClient();
        String url = BACKEND_URL_BASE + "/fcmtest/delete.php";
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
            e.printStackTrace();
        }
    }

    private void deleteTokens() {

        OkHttpClient client = new OkHttpClient();
        String url = BACKEND_URL_BASE + "/fcmtest/reset.php";
        RequestBody body = new FormBody.Builder()
                .build();

        Log.d(TAG, url);
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();

        try {
            client.newCall(request).execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void getSpinnerData() {

        String url = BACKEND_URL_BASE + "/fcmtest/tokens.php";
        Log.d(TAG, url);
        StringRequest stringRequest = new StringRequest(url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        JSONObject j = null;
                        try {
                            j = new JSONObject(response);
                            JSONArray result = j.getJSONArray("result");
                            getTokensId(result);

                            spinnerTargetDevice.setAdapter(adapter);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                    }
                });
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }

    private void getTokensId(JSONArray j) {
        for (int i = 0; i < j.length(); i++) {
            try {
                JSONObject json = j.getJSONObject(i);
                tokens.add(json.getString("token"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        //Setting the values to textviews for a selected item
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        //When no item is selected this method would execute

    }
}
