package com.wedevol.fcmtest;

import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;
import com.google.firebase.messaging.FirebaseMessaging;

import java.io.IOException;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

/**
 * FirebaseInstanceIDService with backoff algorithm to register in backend
 */
public class FirebaseInstanceIDService extends FirebaseInstanceIdService {

    private static final String TAG = "FCMInstanceIDService";

    public static final int MAX_ATTEMPTS = 10;
    public static final int BACKOFF_MILLI_SECONDS = 500;
    private static Boolean sentTokenToServer = false;

    @Override
    public void onTokenRefresh() {

        Log.d(TAG, "On token refresh logic");
        // Get updated instanceID token
        // A better solution would be to save in preference that you already managed to register
        // a token. start RegistrationIntentService only if you didn't already register.
        FirebaseMessaging.getInstance().subscribeToTopic("test");
        String token = FirebaseInstanceId.getInstance().getToken();
        Log.d(TAG, "Token: " + token);
        registerTokenInAppServer(token);
    }

    private static void registerTokenPost(String token) throws IOException {

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
            throw new IOException("Post failed with error message: " + e.getMessage());
        }
    }

    public static void registerTokenInAppServer(String token) {
        if (token == null){
            return;
        }

        long backoff = BACKOFF_MILLI_SECONDS + MainActivity.RANDOM.nextInt(1000);
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
}
