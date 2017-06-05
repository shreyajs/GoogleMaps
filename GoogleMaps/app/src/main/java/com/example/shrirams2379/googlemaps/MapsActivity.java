package com.example.shrirams2379.googlemaps;

import android.*;
import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.location.LocationListener;
import android.location.LocationProvider;
import android.support.annotation.MainThread;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

//import com.google.android.gms.location.LocationListener;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private LocationManager locationManager;
    private boolean isGPSEnabled = false;
    private boolean isNetworkEnabled = false;
    private boolean canGetLocation = false;
    private static final long MIN_TIME_BW_UPDATES = 1000 * 15 * 1;
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 5;
    private Location myLocation;
    private static final int MY_LOC_ZOOM_FACTOR = 17;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
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

        // Add a marker in birthplace and move the camera
        LatLng birthPlace = new LatLng(34, -118);
        mMap.addMarker(new MarkerOptions().position(birthPlace).title("Born here"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(birthPlace));
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.d("Google Maps", "Failed permission check 1");
            Log.d("GoogleMaps", Integer.toString(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)));
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 2);
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.d("GoogleMaps", "Failed permission check 2");
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 2);
        }
        mMap.setMyLocationEnabled(true);
    }

    public void changeView(View view) {
        if (mMap.getMapType() != GoogleMap.MAP_TYPE_SATELLITE) {
            mMap.setMapType(mMap.MAP_TYPE_SATELLITE);
        } else {
            mMap.setMapType(mMap.MAP_TYPE_NORMAL);
        }
    }

    public void getLocation() {
        try {
            locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
            isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            if (isGPSEnabled) Log.d("MyMaps", "getLocation: GPS is enabled");

            isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
            if (isNetworkEnabled) Log.d("MyMaps", "getLocation: NETWORK is enabled");

            if (!isGPSEnabled && !isNetworkEnabled) {
                Log.d("MyMaps", "getLocation: No provider is enabled!");
            } else {
                this.canGetLocation = true;

                if (isNetworkEnabled) {
                    Log.d("MyMaps", "getLocation: Network enabled - requesting location updates");
                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                            MIN_TIME_BW_UPDATES,
                            MIN_DISTANCE_CHANGE_FOR_UPDATES,
                            locationListenerNetwork);

                    Log.d("MyMaps", "getLocation: NetworkLoc update request successful.");
                    Toast.makeText(this, "Using Network", Toast.LENGTH_SHORT);
                }

                if (isGPSEnabled) {
                    Log.d("MyMaps", "getLocation: GPS enabled - requesting location updates");
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
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                            MIN_TIME_BW_UPDATES,
                            MIN_DISTANCE_CHANGE_FOR_UPDATES,
                            locationListenerGPS);

                    Log.d("MyMaps", "getLocation: GPS update request successful.");
                    Toast.makeText(this, "using GPS", Toast.LENGTH_SHORT);
                }
            }
        } catch (Exception e) {
            Log.d("MyMaps", "Exception in getLocation");
            e.printStackTrace();
        }
        //case default --> request updates from NETWORK_PROVIDER
        default:
        requestUpdatesNetworkProvider();
    }



    public void onLocationChanged(Location location) {
        //output Log.d and Toast message that GPS is enabled and working
        Log.d("MyMaps" , "onLocationChanged: GPS enabled and working");
        Toast.makeText(MapsActivity.this , "GPS enabled and working" , Toast.LENGTH_SHORT);

        //drop a marker on map
        dropMarker (LocationManager.GPS_PROVIDER);
        Log.d("MyMaps" , "onLocationChanged: Dropping markers-network");

        //remove network location updates
       try{
        locationManager.removeUpdates(locationListenerNetwork);
    } catch (SecurityException e) {
       }
       }


    public void onStatusChanged(String provider, int status, Bundle extras) {
        //output is Log.d and Toast that network is switched to GPS
        Log.d("MyMaps" , "onStatusChanged: Network switched to GPS");
        Toast.makeText(MapsActivity.this, "Network switched to GPS" , Toast.LENGTH_SHORT);
        //setup a switch statement to check the status imput parameter
        switch(status){
            case LocationProvider.AVAILABLE:
                Log.d("Status", "Location Provider is Available");
                break;
            case LocationProvider.OUT_OF_SERVICE:
                Log.d("Status", "Location Provider is Out of Service");
                try {
                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, locationListenerNetwork);
                }catch(SecurityException e){

                }
                break;
                }
        }

    public void dropMarker(String provider) {

        LatLng userLocation = null;

        if (locationManager != null)
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
        myLocation = locationManager.getLastKnownLocation(provider);
    }

    if(myLocation == null){
        Log.d("MyMaps" , "dropMarker: myLocation is null");
        Toast.makeText(MapsActivity.this, "myLocation is null", Toast.LENGTH_SHORT);
    }

    else{
        userLocation = new LatLng(myLocation.getLatitude(), myLocation.getLongitude());

        CameraUpdate update = CameraUpdateFactory.newLatLngZoom(userLocation, MY_LOC_ZOOM_FACTOR);

        if (provider.equals(locationManager.NETWORK_PROVIDER)) {
        Circle circle = mMap.addCircle(new CircleOptions()
                .center (userLocation)
                .radius(1)
                .strokeColor(Color.GREEN)
                .strokeWidth(2)
                .fillColor(Color.GREEN)
        mMap.animateCamera(update);
        }

        else if (provider.equals(locationManager.GPS_PROVIDER)) {
        Circle circle = mMap.addCircle(new CircleOptions()
            .center (userLocation)
            .radius(1)
            .strokeColor(Color.ORANGE)
            .strokeWidth(2)
            .fillColor(Color.ORANGE)
        mMap.animateCamera(update);
            }
        }
    }



