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
import android.widget.Toast;

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
    private Boolean sentTokenToServer = false;
    private Random random = new Random();
    private EditText editTextMessage;
    private EditText editTextMessage2;
    private String message;
    private String recipient;
    private ArrayList<String> tokens;
    private ArrayAdapter<String> adapter;
    private JSONArray result;
    private Spinner spinner;
    private TextView deviceText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        deviceText = (TextView) findViewById(R.id.deviceText);
        deviceText.setText("Device: ");
        tokens = new ArrayList<String>();
        sentTokenToServer = false;

        Button buttonActivate = (Button) findViewById(R.id.buttonActivate);
        buttonActivate.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                final String token = FirebaseInstanceId.getInstance().getToken();
                deviceText.setText("Device: "+ token);
                if (!tokens.contains(token)) {
                    tokens.add(token);
                }
                adapter.notifyDataSetChanged();

                new AsyncTask() {

                    @Override
                    protected Object doInBackground(Object[] params) {
                        Log.d("Main", "Activate button");
                        FirebaseMessaging.getInstance().subscribeToTopic("test");
                        String token = FirebaseInstanceId.getInstance().getToken();

                        registerTokenInAppServer(token);
                        return null;
                    }
                }.execute(null, null, null);
            }
        });

        Button buttonDelete = (Button) findViewById(R.id.buttonDelete);
        buttonDelete.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                final String token = deviceText.getText().toString();
                deviceText.setText("");
                tokens.remove(token);
                adapter.notifyDataSetChanged();
                new AsyncTask() {

                    @Override
                    protected Object doInBackground(Object[] params) {
                        try {
                            Log.d("Main", "Delete button");

                            String token = FirebaseInstanceId.getInstance().getToken();
                            FirebaseInstanceId.getInstance().deleteToken("431269160141", "GCM");

                            deleteToken(token);

                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        return null;
                    }
                }.execute(null, null, null);

            }
        });

        Button buttonReset = (Button) findViewById(R.id.buttonReset);
        buttonReset.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                //tokens.removeAll();
                adapter.notifyDataSetChanged();
                new AsyncTask() {

                    @Override
                    protected Object doInBackground(Object[] params) {
                        try {
                            Log.d("Main", "Reset button");

                            FirebaseInstanceId.getInstance().deleteInstanceId();

                            //Delete tokens from database
                            resetTokens();


                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        return null;
                    }
                }.execute(null, null, null);

            }
        });

        spinner = (Spinner) findViewById(R.id.targetDevice);
        spinner.setOnItemSelectedListener(this);

        getSpinnerData();

        editTextMessage = (EditText) findViewById(R.id.editTextMessage);
        Button buttonUpstream = (Button) findViewById(R.id.buttonUpstream);
        buttonUpstream.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Log.d("Main", "Upstream message button");
                message = editTextMessage.getText().toString();
                recipient = spinner.getSelectedItem().toString();
                //recipient = getTokenFromSpinner(spinner.getSelectedItemPosition());
                Log.d("Main", "Message: " + message + ", recipient: " + recipient);
                FirebaseMessaging fm = FirebaseMessaging.getInstance();
                fm.send(new RemoteMessage.Builder("431269160141" + "@gcm.googleapis.com")
                        .setMessageId(Integer.toString(random.nextInt()))
                        .addData("message", message)
                        .addData("recipient", recipient)
                        .addData("action", "com.wedevol.MESSAGE")
                        .build());
            }
        });

        editTextMessage2 = (EditText) findViewById(R.id.editTextMessage2);
        Button buttonUpstream2 = (Button) findViewById(R.id.buttonUpstream2);
        buttonUpstream2.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Log.d("Main", "Echo Upstream message button");
                message = editTextMessage2.getText().toString();
                Log.d("Main", "Message: " + message);
                FirebaseMessaging fm = FirebaseMessaging.getInstance();
                fm.send(new RemoteMessage.Builder("431269160141" + "@gcm.googleapis.com")
                        .setMessageId(Integer.toString(random.nextInt()))
                        .addData("message", message)
                        .addData("action", "com.wedevol.ECHO")
                        .build());
            }
        });

    }

    private void registerTokenPost(String token) throws IOException {

        OkHttpClient client = new OkHttpClient();
        RequestBody body = new FormBody.Builder()
                .add("token", token)
                .build();

        Request request = new Request.Builder()
                .url("http://10.0.2.2/fcmtest/register.php")
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
        RequestBody body = new FormBody.Builder()
                .add("token", token)
                .build();

        Request request = new Request.Builder()
                .url("http://10.0.2.2/fcmtest/delete.php")
                .post(body)
                .build();

        try {
            client.newCall(request).execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void resetTokens() {

        OkHttpClient client = new OkHttpClient();
        RequestBody body = new FormBody.Builder()
                .build();

        Request request = new Request.Builder()
                .url("http://10.0.2.2/fcmtest/reset.php")
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
                Toast.makeText(getApplicationContext(), "Failed to register ...", Toast.LENGTH_SHORT).show();

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
    private void getSpinnerData(){
        StringRequest stringRequest = new StringRequest("http://10.0.2.2/fcmtest/tokens.php",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        JSONObject j = null;
                        try {
                            j = new JSONObject(response);
                            result = j.getJSONArray("result");
                            getTokensId(result);
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

    private void getTokensId(JSONArray j){
        for(int i=0;i<j.length();i++){
            try {
                JSONObject json = j.getJSONObject(i);
                tokens.add(json.getString("token"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        adapter = new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_spinner_dropdown_item, tokens);
        spinner.setAdapter(adapter);
    }

    private String getTokenFromSpinner(int position){
        String token="";
        try {
            //Getting object of given index
            JSONObject json = result.getJSONObject(position);

            //Fetching token from that object
            token = json.getString("token");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        //Returning the token
        return token;
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        //Setting the values to textviews for a selected item
    }

    //When no item is selected this method would execute
    @Override
    public void onNothingSelected(AdapterView<?> parent) {
    }
}
