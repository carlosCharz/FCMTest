package com.wedevol.fcmtest;

/**
 * Created by Asus3 on 11/4/2016.
 */

public interface IRequestListener {
    void onComplete();

    void onError(int code, String message);
}
