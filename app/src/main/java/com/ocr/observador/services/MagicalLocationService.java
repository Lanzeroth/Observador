package com.ocr.observador.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Looper;
import android.preference.PreferenceManager;

import com.ocr.observador.FirstApplication;
import com.orhanobut.logger.Logger;

/**
 * Created by Vazh on 3/6/2015.
 */
public class MagicalLocationService extends Service {

    SharedPreferences prefs;

    LocationManager lm;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onStart(Intent intent, int startId) {

        super.onStart(intent, startId);
        addLocationListener();
        setIsLocationServiceRunningSharedPref(true);

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        setIsLocationServiceRunningSharedPref(false);
    }

    public void setIsLocationServiceRunningSharedPref(boolean isRunning) {
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(FirstApplication.PREF_IS_LOCATION_SERVICE_RUNNING, isRunning);
        editor.apply();
    }


    private void addLocationListener() {
        Thread triggerService = new Thread(new Runnable() {
            public void run() {
                try {
                    Looper.prepare();//Initialise the current thread as a looper.
                    lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

                    Criteria c = new Criteria();
                    c.setAccuracy(Criteria.ACCURACY_COARSE);

                    final String PROVIDER = lm.getBestProvider(c, true);

                    MyLocationListener myLocationListener = new MyLocationListener();
                    lm.requestLocationUpdates(PROVIDER, 1000 * 60 * 10, 0, myLocationListener);
                    Logger.d("LOC_SERVICE  Service RUNNING!");
                    Looper.loop();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }, "LocationThread");
        triggerService.start();
    }

    public static void updateLocation(Location location) {
        Context appCtx = FirstApplication.getContext();

        double latitude, longitude;

        latitude = location.getLatitude();
        longitude = location.getLongitude();

        Intent filterRes = new Intent();
        filterRes.setAction("com.ocr.observador.intent.action.LOCATION");
        filterRes.putExtra("latitude", latitude);
        filterRes.putExtra("longitude", longitude);
        appCtx.sendBroadcast(filterRes);
    }


    class MyLocationListener implements LocationListener {

        @Override
        public void onLocationChanged(Location location) {
            updateLocation(location);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    }

}
