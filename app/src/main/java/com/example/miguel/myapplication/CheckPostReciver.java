package com.example.miguel.myapplication;

import android.Manifest;
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

public class CheckPostReciver extends IntentService implements LocationListener {
    FusedLocationProviderClient mProviderClient;
    final String user_mail = FirebaseAuth.getInstance().getCurrentUser().getEmail();
    final String user_name = user_mail.substring(0,user_mail.length()-10);
    Context ctx = this;
    DatabaseReference mReference;
    int ini_tran, ok, status;
    LocationManager locationManager;

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public CheckPostReciver() {
        super(CheckPostReciver.class.getName());
    }

    @Override
    public void onCreate() {
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        /* se actualizará cada 100ms y 0 metros de cambio en la localización
            mientras más pequeños sean estos valores más frecuentes serán las actualizaciones */
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 100, 1, this);
        mProviderClient = LocationServices.getFusedLocationProviderClient(this);
        mReference = FirebaseDatabase.getInstance().getReference();
        mReference.child("blue").child("conductores").child(user_name).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {


                int solicitud = dataSnapshot.child("Solicitud").getValue(Integer.class);
                ok = solicitud;

                if(dataSnapshot.child("Status").getValue(Integer.class) == -1)
                {
                    if(ok == 1)
                    {
                        mReference.child("blue").child("conductores").child(user_name).child("Status").setValue(0);
                        mReference.child("blue").child("conductores").child(user_name).child("Solicitud").setValue(0);
                        ini_tran = 0;
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return super.onStartCommand(intent, flags, startId);
        }

        mProviderClient.getLastLocation()
                .addOnSuccessListener(new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // location es la ultima ubicacion conocida
                        subir_al_servidor(location);
                    }
                });
        return super.onStartCommand(intent, flags, startId);
    }
    public void subir_al_servidor(Location loc){
        if(loc==null && ok != -1){
            //mandar alerta de error de GPS

            mReference.child("blue").child("conductores").child(user_name).child("Status").setValue(2);
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

            if(ini_tran == 1)
            {
                mReference.child("blue").child("conductores").child(user_name).child("Hora").setValue(fecha, list);
                mReference.child("blue").child("conductores").child(user_name).child("Lat").setValue(loc.getLatitude(), list);
                mReference.child("blue").child("conductores").child(user_name).child("Lon").setValue(loc.getLongitude(), list);
            }

            mReference.child("blue").child("conductores").child(user_name).child("Transmision").setValue(ini_tran, list);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        final Intent it = intent;
        mReference.child("blue").child("conductores").child(user_name).child("Status").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                int st = dataSnapshot.getValue(Integer.class);
                if(st != -1){
                    onStartCommand(it, 0,0);
                }else {
                    onDestroy();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

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
        if(ini_tran != 0 && ok != -1)
        {
            mReference.child("blue").child("conductores").child(user_name).child("Status").setValue(1); }
    }

    @Override
    public void onProviderDisabled(String provider) {
        //Se Apago el GPS
        if(ini_tran != 0 && ok != -1)
        {
            mReference.child("blue").child("conductores").child(user_name).child("Status").setValue(3);
        }
    }
}
