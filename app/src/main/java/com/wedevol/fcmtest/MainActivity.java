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

public class MainActivity extends AppCompatActivity implements Spinner.OnItemSelectedListener {

    private static final String TAG = "MainActivity";
    private static final int MAX_ATTEMPTS = 10;
    private static final int BACKOFF_MILLI_SECONDS = 500;
    private static final String FCM_PROJECT_SENDER_ID = "431269160141";
    private static final String FCM_PROJECT_SCOPE = "GCM";
    private static final String FCM_SERVER_CONNECTION = "@gcm.googleapis.com";
    private static final String BACKEND_SERVER_IP = "10.0.2.2";
    private static final String BACKEND_URL_BASE = "http://" + BACKEND_SERVER_IP;
    private static final String BACKEND_ACTION_MESSAGE = "com.wedevol.MESSAGE";
    private static final String BACKEND_ACTION_ECHO = "com.wedevol.ECHO";

    private EditText editTextSend;
    private EditText editTextEcho;
    private Spinner spinnerTargetDevice;
    private TextView deviceText;
    private Button buttonActivate;
    private Button buttonDelete;
    private Button buttonReset;
    private Button buttonUpstreamSend;
    private Button buttonUpstreamEcho;

    private Boolean sentTokenToServer;
    private Random random;
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

        random = new Random();
        sentTokenToServer = false;
        tokens = new ArrayList<String>();
        adapter = new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_spinner_dropdown_item, tokens);
        spinnerTargetDevice.setOnItemSelectedListener(this);
        buttonDelete.setEnabled(false);
        buttonUpstreamSend.setEnabled(false);
        buttonUpstreamEcho.setEnabled(false);

        getSpinnerData();

        buttonActivate.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                Log.d(TAG, "Activation logic");
                FirebaseMessaging.getInstance().subscribeToTopic("test");
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

                        registerTokenInAppServer(token);

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
                //recipient = getTokenFromSpinner(spinnerTargetDevice.getSelectedItemPosition());
                Log.d(TAG, "Message: " + message + ", recipient: " + recipient);
                FirebaseMessaging.getInstance().send(new RemoteMessage.Builder(FCM_PROJECT_SENDER_ID + FCM_SERVER_CONNECTION)
                        .setMessageId(Integer.toString(random.nextInt()))
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
                        .setMessageId(Integer.toString(random.nextInt()))
                        .addData("message", message)
                        .addData("action", BACKEND_ACTION_ECHO)
                        .build());
            }
        });

    }

    private void registerTokenPost(String token) throws IOException {

        OkHttpClient client = new OkHttpClient();
        String url = BACKEND_URL_BASE + "/fcmtest/register.php";
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
            throw new IOException("Post failed with error message: " + e.getMessage());
        }
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

    public void registerTokenInAppServer(String token) {

        long backoff = BACKOFF_MILLI_SECONDS + random.nextInt(1000);
        // Once GCM returns a registration id, we need to register on our server
        // As the server might be down, we will retry it a couple of times.
        for (int i = 1; i <= MAX_ATTEMPTS; i++) {
            Log.d(TAG, "Attempt #" + i + " to register");
            try {
                registerTokenPost(token);

                // You should store a boolean that indicates whether the generated token has been
                // sent to your server. If the boolean is false, send the token to your server,
                // otherwise your server should have already received the token.
                sentTokenToServer = true;
                return;
            } catch (IOException e) {

                sentTokenToServer = false;

                // Here we are simplifying and retrying on any error; in a real application,
                // it should retry only on unrecoverable errors (like HTTP error code 503).
                Log.e(TAG, "Failed to register on attempt " + i + ":" + e);
                if (i == MAX_ATTEMPTS) {
                    break;
                }
                try {
                    Log.d(TAG, "Sleeping for " + backoff + " ms before retry");
                    Thread.sleep(backoff);
                } catch (InterruptedException e1) {
                    // Activity finished before we complete - exit.
                    Log.d(TAG, "Thread interrupted: abort remaining retries!");
                    Thread.currentThread().interrupt();
                    return;
                }
                // increase backoff exponentially
                backoff *= 2;
            }
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
