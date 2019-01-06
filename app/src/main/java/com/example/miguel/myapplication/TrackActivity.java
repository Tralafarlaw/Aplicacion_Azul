package com.example.miguel.myapplication;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.Timer;
import java.util.TimerTask;

public class TrackActivity extends AppCompatActivity implements LocationListener {
    final long intervalo = 5000;
    boolean is_runing = false;
    Context context = this;
    LocationManager locationManager;
    FusedLocationProviderClient mProviderClient;
    ImageView estado;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_track);
        init();
        mProviderClient = LocationServices.getFusedLocationProviderClient(this);
        iniciar_thread();


    }

    public void init() {
        estado = (ImageView) findViewById(R.id.estado);


        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

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
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 100, 0, this);
    }


    public void iniciar_envio_datos() {
        //con este metodo se inicia un proceso en segundo plano que manda los datos cada 5 segundos
        AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent it = new Intent(context, CheckPostReciver.class);
        PendingIntent pit = PendingIntent.getBroadcast(context, 0, it, 0);
        manager.setRepeating(AlarmManager.RTC, System.currentTimeMillis(), intervalo, pit);
    }

    public void iniciar_thread() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    while (is_runing) {
                        enviar_ultima_ubicacion();
                        Thread.sleep(intervalo);
                    }
                } catch (Exception e) {

                }
            }
        }).start();

    }

    public void enviar_ultima_ubicacion() {
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
        mProviderClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // location es la ultima ubicacion conocida
                        subir_al_servidor(location);
                    }
                });

    }
    public void subir_al_servidor(Location loc){
        if(loc==null){
            //mandar alerta de error de GPS
        }else{
            //subir de forma regular al servidor
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        //La Ubicacion Cambio (No Usar este Metodo) en el metodo anterior manda la ultima ubiccion conocida asi aunque no se mueva mandara la ultima ubicacion conocida al servidor
        estado.setImageResource(R.drawable.verdeon);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        //Cambio el estado o Proveedor del GPS la verdad no entiendo bien que hace
        estado.setImageResource(R.drawable.naranjaalert);

    }

    @Override
    public void onProviderEnabled(String provider) {
        //Se Encendio El GPS
        estado.setImageResource(R.drawable.verdeon);
    }

    @Override
    public void onProviderDisabled(String provider) {
        //Se Apago el GPS
        estado.setImageResource(R.drawable.rojooff);

        AlertDialog.Builder builder = new AlertDialog.Builder(TrackActivity.this);

        builder.setIcon(R.mipmap.ic_launcher).
                setTitle("ERROR GPS").
                setMessage("No se puede acceder a su ubicacion actual, es posible que este en una zona sin conexion").
                setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(TrackActivity.this, "Tocado Aceptar", Toast.LENGTH_SHORT).show();
                    }
                });


            AlertDialog titulo = builder.create();
            titulo.show();
    }

    public void onBackPressed() {
        //super.onBackPressed();
        Intent intent = new Intent(getApplicationContext(), TrackActivity.class);
        startActivity(intent);




    }

}
