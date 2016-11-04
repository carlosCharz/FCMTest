package com.wedevol.fcmtest;

import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

/**
 * FirebaseInstanceIDService to register token in the database
 */
public class FirebaseInstanceIDService extends FirebaseInstanceIdService implements IRequestListener {

    private static final String TAG = "FCMInstanceIDService";
    private TokenService tokenService;

    @Override
    public void onTokenRefresh() {

        Log.d(TAG, "Token refresh logic");
        String token = FirebaseInstanceId.getInstance().getToken();
        Log.d(TAG, "Refreshed token: " + token);

        //Call the token service to save the token in the database
        tokenService = new TokenService(this, this);
        tokenService.registerTokenInDB(token);
    }

    @Override
    public void onComplete() {
        Log.d(TAG, "Token registered successfully in the DB");

    }

    @Override
    public void onError(String message) {
        Log.d(TAG, "Error trying to register the token in the DB: " + message);
    }

}
