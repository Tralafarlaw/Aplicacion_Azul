package com.example.miguel.myapplication;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

public class CheckPostReciver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        new Proceso().execute();
    }}
class Proceso extends AsyncTask<String, Void, String> {

    double lat,lon;
    FusedLocationProviderClient mClient;


    @Override
    protected String doInBackground(String... strings) {
        //Manda aca la ubicacion cada 5 seg


        return null;
    }


}
