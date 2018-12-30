package com.example.miguel.myapplication;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class TrackActivity extends AppCompatActivity {
    final long intervalo = 5000;
    final Context context = getApplicationContext();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_track);
    }

    public void iniciar_envio_datos (){
        //con este metodo se inicia un proceso en segundo plano que manda los datos cada 5 segundos
        AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent it = new Intent(context, CheckPostReciver.class);
        PendingIntent pit = PendingIntent.getBroadcast(context, 0, it,0);
        manager.setRepeating(AlarmManager.RTC, System.currentTimeMillis(), intervalo, pit);
    }
}
