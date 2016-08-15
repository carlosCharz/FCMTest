package com.wedevol.fcmtest;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;

import java.io.IOException;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button buttonActivate = (Button) findViewById(R.id.buttonActivate);
        buttonActivate.setOnClickListener( new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Log.d("Main", "Activate button");
                FirebaseMessaging.getInstance().subscribeToTopic("test");
                FirebaseInstanceId.getInstance().getToken();
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
