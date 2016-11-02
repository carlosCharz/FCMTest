package com.wedevol.fcmtest;

import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

/**
 * FirebaseInstanceIDService to register token in the database
 */
public class FirebaseInstanceIDService extends FirebaseInstanceIdService {

    private static final String TAG = "FCMInstanceIDService";

    @Override
    public void onTokenRefresh() {

        Log.d(TAG, "Token refresh logic");
        // Get updated fcm token
        // A better solution would be to save in preference that you already managed to register
        // a token. Start RegistrationIntentService only if you didn't already register.
        String token = FirebaseInstanceId.getInstance().getToken();
        Log.d(TAG, "Refreshed token: " + token);

        MainActivity.registerTokenInDB(token);
    }


}
