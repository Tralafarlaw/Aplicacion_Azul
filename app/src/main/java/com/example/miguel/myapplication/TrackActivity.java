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
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BaseTransientBottomBar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.format.Time;
import android.view.View;
import android.view.ViewDebug;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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

import org.osmdroid.api.IMapController;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.w3c.dom.Text;

import java.util.Timer;
import java.util.TimerTask;

public class TrackActivity extends AppCompatActivity implements LocationListener {
    final long intervalo = 5000;
    final String user_mail = FirebaseAuth.getInstance().getCurrentUser().getEmail();
    final String user_name = user_mail.substring(0,user_mail.length()-10);
    boolean is_runing = false;
    Context context = this;
    LocationManager locationManager;
    FusedLocationProviderClient mProviderClient;
    ImageView estado;
    DatabaseReference mReference;
    Button StartButton;
    AlarmManager manager;
    Button TranButton, Descon;
    Intent newIntent;
    PendingIntent pendingIntent;
    int ini_tran, ok, status;
    TextView Nombre, Matricula, txt2, txt3, txt4, txt5, txt6;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_track);
        newIntent = new Intent(getBaseContext(), CheckPostReciver.class);
        TranButton = (Button) findViewById(R.id.btn2);
        Descon = (Button) findViewById(R.id.btn3);
        Descon.setVisibility(View.INVISIBLE);
        init();
    }

    @Override
    protected void onStart() {

        super.onStart();
        mReference.child("Status").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                int st = dataSnapshot.getValue(Integer.class);
                if(st == -1){
                    detener_servicio();
                }else {

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        mReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String name = dataSnapshot.child("Nombre").getValue(String.class);
                txt2.setText(name);

                String placa = dataSnapshot.child("Placa").getValue(String.class);
                txt3.setText(placa);


                txt5.setText(user_mail);

                String telefono = dataSnapshot.child("Telefono").getValue(String.class);
                txt6.setText(telefono);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        mReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {


                int solicitud = dataSnapshot.child("Solicitud").getValue(Integer.class);
                ok = solicitud;

                if(dataSnapshot.child("Status").getValue(Integer.class) == -1)
                {
                    if(ok == 1)
                    {
                        Descon.setVisibility(View.INVISIBLE);
                        txt4.setText("DESCANSANDO");
                        mReference.child("blue").child("conductores").child(user_name).child("Status").setValue(0);
                        mReference.child("blue").child("conductores").child(user_name).child("Solicitud").setValue(0);
                        estado.setImageResource(R.drawable.plomo);
                        TranButton.setText("INICIAR TRANSMISION");
                        ini_tran = 0;
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void init() {
        estado = (ImageView) findViewById(R.id.estado);
        mProviderClient = LocationServices.getFusedLocationProviderClient(this);
        txt2 = (TextView) findViewById(R.id.textView2);
        txt3 = (TextView) findViewById(R.id.textView3);
        txt4 = (TextView) findViewById(R.id.textView4);
        txt5 = (TextView) findViewById(R.id.textView5);
        txt6 = (TextView) findViewById(R.id.textView6);
        onStatusChanged("",1, new Bundle());
        TranButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(ini_tran == 0)
                {
                    Descon.setVisibility(View.VISIBLE);
                    txt4.setText("Conectando...");
                    mReference.child("Status").setValue(1);
                    estado.setImageResource(R.drawable.verde_on);
                    TranButton.setText("TRABAJANDO !");
                    ini_tran = 1;
                    iniciar_servicio();
                }
                else
                {

                }


            }
        });
        Descon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mReference.child("Solicitud").setValue(1);
                txt4.setText("Esperando...");

            }
        });
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        /* se actualizará cada 100ms y 0 metros de cambio en la localización
            mientras más pequeños sean estos valores más frecuentes serán las actualizaciones */
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 100, 1, this);
        final Button btn = (Button) findViewById(R.id.button_start);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent it = new Intent(getApplicationContext(), mapaosm.class);
                startActivity(it);
            }
        });

        mReference = FirebaseDatabase.getInstance().getReference().child("blue").child("conductores").child(user_name);
        mReference.child("Status").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.getValue(Integer.class)==-1){
                    detener_servicio();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
    public void iniciar_servicio (){
        startService(newIntent);
    }
    public void detener_servicio (){
        stopService(newIntent);
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
                txt4.setText("Encendido");
               // mReference.child("blue").child("conductores").child(user_name).child("Status").setValue(1);
                estado.setImageResource(R.drawable.verde_on);
            }else if (status == LocationProvider.TEMPORARILY_UNAVAILABLE){
                txt4.setText("No Disponible \n Temporalmente");
               // mReference.child("blue").child("conductores").child(user_name).child("Status").setValue(2);
                estado.setImageResource(R.drawable.naranja_alert);
            }else if (status == LocationProvider.OUT_OF_SERVICE){
               // mReference.child("blue").child("conductores").child(user_name).child("Status").setValue(2);
                txt4.setText("Fuera de Servicio");
                estado.setImageResource(R.drawable.rojo_off);
            }
        }
        else
        {
            txt4.setText("FUERA DE SERVICIO");
            estado.setImageResource(R.drawable.plomo);
        }
    }

    @Override
    public void onProviderEnabled(String provider) {
        //Se Encendio El GPS
        if(ini_tran != 0 && ok != -1)
        {
           // TranButton.setText("Trabajando!");
            txt4.setText("Encendido");
            //mReference.child("Status").setValue(1);
            estado.setImageResource(R.drawable.verdeon);
        }

        else
        {
            txt4.setText("FUERA DE SERVICIO");
            estado.setImageResource(R.drawable.plomo);
        }
    }

    @Override
    public void onProviderDisabled(String provider) {
        //Se Apago el GPS
        if(ini_tran != 0 && ok != -1)
        {
            //TranButton.setText("Trabajando!");
            txt4.setText("Apagado");
            //mReference.child("Status").setValue(3);
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
        else
        {
            txt4.setText("FUERA DE SERVICIO");
            estado.setImageResource(R.drawable.plomo);
        }

    }

    public void onBackPressed() {
        //super.onBackPressed();
    }

}
