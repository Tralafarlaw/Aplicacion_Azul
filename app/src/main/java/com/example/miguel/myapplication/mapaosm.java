package com.example.miguel.myapplication;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import org.osmdroid.api.IMapController;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

public class mapaosm extends AppCompatActivity implements LocationListener {


    MapView map;
    Marker mk;

    LocationManager locationManager;
    FusedLocationProviderClient mProviderClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mapaosm);

        mProviderClient = LocationServices.getFusedLocationProviderClient(this);

        map = (MapView) findViewById(R.id.map);
        map.setTileSource(TileSourceFactory.MAPNIK);
        final IMapController driver = map.getController();
        map.setBuiltInZoomControls(true);
        map.setMultiTouchControls(true);

        mk = new Marker(map);
        mk.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        driver.setZoom(19.5);
        map.getOverlays().add(mk);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
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

    }

    @Override
    public void onLocationChanged(Location location) {

        Marker i = (Marker) map.getOverlays().get(0);
        i.setPosition(new GeoPoint(location));
        String vel = String.valueOf((int)location.getSpeed()*3.6);
        i.setTitle(vel+" Km/h");
        map.getController().setCenter(i.getPosition());
        map.invalidate();

    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }
}
