package com.example.miguel.myapplication;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;

public class TrackActivity extends AppCompatActivity {
    final long intervalo = 5000;
    Context context = this;
    LocationManager locationManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_track);

        ImageView estado = (ImageView) findViewById(R.id.estado);
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        if(!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
        {
            estado.setImageResource(R.drawable.rojooff);

        }
        else
        {
            estado.setImageResource(R.drawable.verdeon);
        }
        cambioLogo(estado, locationManager);
    }



    public void iniciar_envio_datos (){
        //con este metodo se inicia un proceso en segundo plano que manda los datos cada 5 segundos
        AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent it = new Intent(context, CheckPostReciver.class);
        PendingIntent pit = PendingIntent.getBroadcast(context, 0, it,0);
        manager.setRepeating(AlarmManager.RTC, System.currentTimeMillis(), intervalo, pit);
    }

    public void cambioLogo(ImageView log, LocationManager locationManager)
    {

        if(!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
        {
            log.setImageResource(R.drawable.verdeon);
        }
        else
        {
            log.setImageResource(R.drawable.rojooff);
        }





    }
}
