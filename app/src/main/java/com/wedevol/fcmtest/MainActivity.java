package com.wedevol.fcmtest;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.RemoteMessage;

import java.io.IOException;
import java.util.Random;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

public class MainActivity extends AppCompatActivity {

    Random random = new Random();
    EditText editTextMessage;
    String message;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button buttonActivate = (Button) findViewById(R.id.buttonActivate);
        buttonActivate.setOnClickListener( new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                new AsyncTask() {

                    @Override
                    protected Object doInBackground(Object[] params) {
                            Log.d("Main", "Activate button");
                            FirebaseMessaging.getInstance().subscribeToTopic("test");
                            String token = FirebaseInstanceId.getInstance().getToken();

                            registerToken(token);
                        return null;
                    }
                }.execute(null, null, null);
            }
        });

        Button buttonReset = (Button) findViewById(R.id.buttonReset);
        buttonReset.setOnClickListener( new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                new AsyncTask() {

                    @Override
                    protected Object doInBackground(Object[] params) {
                        try {
                            Log.d("Main", "Reset button");
                            //Reset Instance ID and revokes all tokens but it just should remove one token
                            FirebaseInstanceId.getInstance().deleteInstanceId();

                            //Delete tokens from database
                            deleteTokens();

                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        return null;
                    }
                }.execute(null, null, null);

            }
        });

        editTextMessage = (EditText) findViewById(R.id.editTextMessage);
        Button buttonUpstream = (Button) findViewById(R.id.buttonUpstream);
        buttonUpstream.setOnClickListener( new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Log.d("Main", "Upstream message button");
                message = editTextMessage.getText().toString();
                FirebaseMessaging fm = FirebaseMessaging.getInstance();
                fm.send(new RemoteMessage.Builder("431269160141" + "@gcm.googleapis.com")
                        .setMessageId(Integer.toString(random.nextInt()))
                        .addData("message", message)
                        .addData("action","com.wedevol.MESSAGE")
                        .build());
            }
        });

    }

    public static void registerToken(String token) {

        OkHttpClient client = new OkHttpClient();
        RequestBody body = new FormBody.Builder()
                .add("token",token)
                .build();

        Request request = new Request.Builder()
                .url("http://10.0.2.2/fcmtest/register.php")
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
}
