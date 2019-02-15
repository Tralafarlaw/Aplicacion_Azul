package com.example.miguel.myapplication;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.IntentService;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.format.Time;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Timer;
import java.util.TimerTask;

public class CheckPostReciver extends IntentService implements LocationListener {
    FusedLocationProviderClient mProviderClient;
    static final long UPDATE_INTERVAL = 5000;
    final String user_mail = FirebaseAuth.getInstance().getCurrentUser().getEmail();
    final String user_name = user_mail.substring(0,user_mail.length()-10);
    Context ctx = this;
    DatabaseReference mReference;
    int ini_tran=1, ok, status;
    LocationManager locationManager;
    Timer timer = new Timer();

    public CheckPostReciver() {
        super(CheckPostReciver.class.getName());
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onCreate() {
        super.onCreate();
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        /* se actualizará cada 100ms y 0 metros de cambio en la localización
            mientras más pequeños sean estos valores más frecuentes serán las actualizaciones */
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 100, 1, this);
        mProviderClient = LocationServices.getFusedLocationProviderClient(this);
        mReference = FirebaseDatabase.getInstance().getReference().child("blue").child("conductores").child(user_name);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        repetidor();
        return START_STICKY;
    }
    private void repetidor () {
        timer.scheduleAtFixedRate(new TimerTask() {

            @SuppressLint("MissingPermission")
            @Override
            public void run() {
                // TODO Auto-generated method stub
                mProviderClient.getLastLocation()
                        .addOnSuccessListener(new OnSuccessListener<Location>() {
                            @Override
                            public void onSuccess(Location location) {
                                // location es la ultima ubicacion conocida
                                subir_al_servidor(location);
                            }
                        });
            }

        }, 0, UPDATE_INTERVAL);
    }
    public void subir_al_servidor(Location loc){
        if(loc==null && ok != -1){
            //mandar alerta de error de GPS

            mReference.child("Status").setValue(2);
        }else{
            Time hoy = new Time(Time.getCurrentTimezone());
            hoy.setToNow();
            String fecha = Integer.toString(hoy.hour) + ":" + Integer.toString(hoy.minute) + ":" + Integer.toString(hoy.second);

            DatabaseReference.CompletionListener list = new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                    // Toast.makeText(getApplicationContext(), "Exito", Toast.LENGTH_SHORT).show();
                }
            };
            //subir de forma regular al servidor
            mReference.child("Status").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    int st = dataSnapshot.getValue(Integer.class);
                    status = st;
                    if(st == -1){
                        ini_tran=0;
                    }else {
                        ini_tran=1;
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

            if(ini_tran == 1)
            {
                mReference.child("Hora").setValue(fecha, list);
                mReference.child("Lat").setValue(loc.getLatitude(), list);
                mReference.child("Lon").setValue(loc.getLongitude(), list);
            }

            mReference.child("Transmision").setValue(ini_tran, list);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        timer.cancel();
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) { }


    @Override
    public void onLocationChanged(Location location) {
        //La Ubicacion Cambio (No Usar este Metodo) en el metodo anterior manda la ultima ubiccion conocida asi aunque no se mueva mandara la ultima ubicacion conocida al servidor
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        //Cambio el estado o Proveedor del GPS la verdad no entiendo bien que hace

        if(ini_tran != 0 && ok != -1)
        {
            //TranButton.setText("Trabajando!");
            if(status == LocationProvider.AVAILABLE){
                mReference.child("blue").child("conductores").child(user_name).child("Status").setValue(1);
            }else if (status == LocationProvider.TEMPORARILY_UNAVAILABLE){
                mReference.child("blue").child("conductores").child(user_name).child("Status").setValue(2);
            }else if (status == LocationProvider.OUT_OF_SERVICE){
                mReference.child("blue").child("conductores").child(user_name).child("Status").setValue(2);
            }
        }
    }

    @Override
    public void onProviderEnabled(String provider) {
        //Se Encendio El GPS
        if(ini_tran != 0 && ok != -1 && status != -1)
        {
            mReference.child("blue").child("conductores").child(user_name).child("Status").setValue(1); }
    }

    @Override
    public void onProviderDisabled(String provider) {
        //Se Apago el GPS
        if(ini_tran != 0 && ok != -1 && status != -1)
        {
            mReference.child("blue").child("conductores").child(user_name).child("Status").setValue(3);
        }
    }
}
