package com.ocr.observador.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.orhanobut.logger.Logger;

/**
 * Created by Vazh on 3/6/2015.
 */
public class LocationReceiver extends BroadcastReceiver {

    double latitude, longitude;

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("LOC_RECEIVER", "Location RECEIVED!");

        latitude = intent.getDoubleExtra("latitude", -1);
        longitude = intent.getDoubleExtra("longitude", -1);

        updateRemote(latitude, longitude);
    }

    private void updateRemote(final double latitude, final double longitude) {
        Logger.i("pew pew pew loc: " + latitude + " " + longitude);
        //HERE YOU CAN PUT YOUR ASYNCTASK TO UPDATE THE LOCATION ON YOUR SERVER
    }
}
