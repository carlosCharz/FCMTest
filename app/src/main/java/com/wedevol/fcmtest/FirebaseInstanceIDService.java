package com.wedevol.fcmtest;

import com.google.firebase.iid.FirebaseInstanceIdService;

/**
 * Created by charz on 8/9/16.
 */
public class FirebaseInstanceIDService extends FirebaseInstanceIdService {
    @Override
    public void onTokenRefresh() {

        // Get updated instanceID token
        // A better solution would be to save in preference that you already managed to register
        // a token. start RegistrationIntentService only if you didn't already register.
        //String token = FirebaseInstanceId.getInstance().getToken();
        //Log.d("FCM Token", "Token: " + token);
        //registerToken(token);
    }
}
