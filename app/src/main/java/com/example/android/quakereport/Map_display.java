package com.example.android.quakereport;

import android.content.Intent;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;

import static java.security.AccessController.getContext;

public class Map_display extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    public static double latitude = -361;
    public static double longitude = -361;
    private EarthquakeActivity earthquakeActivity;
    ArrayList<Earthquake> Quakes;
    CameraPosition cameraPosition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps_activity);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment)getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        Quakes = EarthquakeActivity.FinalQuakes;
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
        try {
            for (int i = 0; i < Quakes.size(); i++) {
                String primaryLocation;
                String locationOffset;
                String LOCATION_SEPARATOR = "of";
                String originalLocation = Quakes.get(i).getLocation();
                if (originalLocation.contains(LOCATION_SEPARATOR)) {
                    // Split the string into different parts (as an array of Strings)
                    // based on the " of " text. We expect an array of 2 Strings, where
                    // the first String will be "5km N" and the second String will be "Cairo, Egypt".
                    String[] parts = originalLocation.split(LOCATION_SEPARATOR);
                    // Location offset should be "5km N " + " of " --> "5km N of"
                    locationOffset = parts[0] + LOCATION_SEPARATOR;
                    // Primary location should be "Cairo, Egypt"
                    primaryLocation = parts[1];
                } else {
                    // Otherwise, there is no " of " text in the originalLocation string.
                    // Hence, set the default location offset to say "Near the".
                    locationOffset = "Near The";
                    // The primary location will be the full location string "Pacific-Antarctic Ridge".
                    primaryLocation = originalLocation;
                }
                MarkerOptions markerOptions = new MarkerOptions().position(new LatLng(Quakes.get(i).getLatitude(), Quakes.get(i).getLongitude())).title(primaryLocation+", "+Quakes.get(i).getMagnitude());
                mMap.addMarker(markerOptions);

            }
        }catch (NullPointerException e){
            Toast.makeText(getApplicationContext(),"Loading Not Finished",Toast.LENGTH_SHORT).show();
        }
        //cameraPosition = new CameraPosition().builder().target(new LatLng((float)Quakes.get(0).getLatitude(),(float)Quakes.get(0).getLongitude())).zoom(12).build();
        try {
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(new LatLng(Quakes.get(0).getLatitude(), Quakes.get(0).getLongitude()), 2);
            mMap.animateCamera(cameraUpdate);
        }
        catch (NullPointerException e){
            Toast.makeText(getApplicationContext(),"Loading Not Done",Toast.LENGTH_SHORT).show();
        }
        /*mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                MarkerOptions markerOptions= new MarkerOptions().position(new LatLng(latLng.latitude,latLng.longitude)).title("Title");
                mMap.addMarker(markerOptions);
                latitude = latLng.latitude;
                longitude = latLng.longitude;
                Intent intent = new Intent(getApplicationContext(),EarthquakeActivity.class);
                startActivity(intent);
            }
        });*/
        mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
//        //GoogleMapOptions options = new GoogleMapOptions();
//        //options.mapType(GoogleMap.MAP_TYPE_NORMAL).compassEnabled(true).rotateGesturesEnabled(false).tiltGesturesEnabled(false);
//        // Add a marker in Sydney and move the camera

    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(getApplicationContext(),EarthquakeActivity.class);
        startActivity(intent);
        super.onBackPressed();
    }
}