package com.example.android.quakereport;

import android.content.Intent;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class maps_activity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    public static double latitude = -361;
    public static double longitude = -361;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps_activity);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment)getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                MarkerOptions markerOptions= new MarkerOptions().position(new LatLng(latLng.latitude,latLng.longitude)).title("Title");
                mMap.addMarker(markerOptions);
                latitude = latLng.latitude;
                longitude = latLng.longitude;
                Intent intent = new Intent(getApplicationContext(),EarthquakeActivity.class);
                startActivity(intent);
            }
        });
        mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
//        //GoogleMapOptions options = new GoogleMapOptions();
//        //options.mapType(GoogleMap.MAP_TYPE_NORMAL).compassEnabled(true).rotateGesturesEnabled(false).tiltGesturesEnabled(false);
//        // Add a marker in Sydney and move the camera

    }
}