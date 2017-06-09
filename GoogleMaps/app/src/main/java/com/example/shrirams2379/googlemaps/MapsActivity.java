package com.example.shrirams2379.googlemaps;

import android.*;
import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.annotation.TargetApi;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.ui.PlacePicker;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private LocationManager locationManager;
    private boolean isGPSEnabled = false;
    private boolean isNetworkEnabled = false;
    private boolean canGetLocation = false;
    private static final long MIN_TIME_BW_UPDATES = 1000 * 15 * 1;
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 5;
    private Location myLocation;
    private LatLng userLocation;
    private LatLng PointsofInterest;
    private static final int MY_LOC_ZOOM_FACTOR = 17;
    public boolean trackOn = false;
    private EditText editSearch;
    private Geocoder geocoder;
    private List<Address> myAddresses;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        editSearch = (EditText) findViewById(R.id.editText_search);
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

        // Add a marker in Sydney and move the camera
        LatLng birthPlace = new LatLng(34, -118);
        mMap.addMarker(new MarkerOptions().position(birthPlace).title("Born here"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(birthPlace));
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.d("GoogleMaps", "Failed Permission check 1");
            Log.d("GoogleMaps", Integer.toString(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)));
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 2);
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.d("GoogleMaps", "Failed Permission check 2");
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 2);
        }
        //mMap.setMyLocationEnabled(true);
    }

    public void changeView(View view) {
        if (mMap.getMapType() != GoogleMap.MAP_TYPE_NORMAL) {
            mMap.setMapType(mMap.MAP_TYPE_NORMAL);
        } else {
            mMap.setMapType(mMap.MAP_TYPE_SATELLITE);
        }
    }

    public void trackerEnabled(View view) {

        if (trackOn == false) {
            Log.d("MyMaps", "trackerEnabled: Tracker on");
            Toast.makeText(this, "trackerEnabled: Tracker On", Toast.LENGTH_SHORT).show();
            getLocation();
            trackOn = true;
        } else {

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

            locationManager.removeUpdates(locationListenerNetwork);
            locationManager.removeUpdates(locationListenerGps);
            Log.d("MyMaps", "trackerEnabled: Tracker off");
            Toast.makeText(this, "trackerEnabled: Tracker Off", Toast.LENGTH_SHORT).show();
            trackOn = false;

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
                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                            MIN_TIME_BW_UPDATES,
                            MIN_DISTANCE_CHANGE_FOR_UPDATES,
                            locationListenerNetwork);

                    Log.d("MyMaps", "getLocation: NetworkLoc update request successful.");
                    Toast.makeText(this, "Using Network", Toast.LENGTH_SHORT).show();
                }
                if (isGPSEnabled) {
                    Log.d("MyMaps", "getLocation: GPS enabled - requesting location updates");
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                            MIN_TIME_BW_UPDATES,
                            MIN_DISTANCE_CHANGE_FOR_UPDATES,
                            locationListenerGps);

                    Log.d("MyMaps", "getLocation: GPS update request successful.");
                    Toast.makeText(this, "Using GPS", Toast.LENGTH_SHORT).show();
                }
            }

        } catch (Exception e) {
            Log.d("MyMaps", "Caught exception in getLocation");
            e.printStackTrace();
        }



    }


    android.location.LocationListener locationListenerGps = new android.location.LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            //output is Log.d and Toast that GPS is enabled and working
            Log.d("MyMaps", "onLocationChanged: GPS enabled and working");
            Toast.makeText(MapsActivity.this, "GPS enabled and working", Toast.LENGTH_SHORT);

            //Drop a marker on map- create a method called dropMarker
            dropMarker(LocationManager.GPS_PROVIDER);

            //Remove the network location updates. See LocationManager for update removal method
            try {
                locationManager.removeUpdates(locationListenerNetwork);
            } catch (SecurityException e) {

            }


        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            //output is Log.d and Toast that GPS is enabled and working
            Log.d("MyMaps", "onStatusChanged: GPS enabled and working");
            Toast.makeText(MapsActivity.this, "GPS enabled and working", Toast.LENGTH_SHORT);
            //setup a switch statement to check the status input parameter
            switch (status) {
                //case LocationProvider.AVAILABLE --> output message to Log.d and Toast
                case LocationProvider.AVAILABLE:
                    Log.d("MyMaps", "onStatusChanged: Location Provider available");
                    Toast.makeText(MapsActivity.this, "Location Provider available", Toast.LENGTH_SHORT).show();
                    break;
                //case LocationProvider.OUT_OF_SERVICE --> output message and request updates from NETWORK_PROVIDER
                case LocationProvider.OUT_OF_SERVICE:
                    Log.d("MyMaps", "onStatusChanged: Location Provider out of service");
                    Toast.makeText(MapsActivity.this, "Location Provider out of service", Toast.LENGTH_SHORT).show();
                    if (ActivityCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.
                        return;
                    }
                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                            MIN_TIME_BW_UPDATES,
                            MIN_DISTANCE_CHANGE_FOR_UPDATES,
                            locationListenerNetwork);
                    break;
                //case LocationProvider.TEMPORARILY_UNAVAILABLE --> request updates from NETWORK_PROVIDER
                case LocationProvider.TEMPORARILY_UNAVAILABLE:
                    Log.d("MyMaps", "onStatusChanged: Location Provider temporarily unavailable");
                    Toast.makeText(MapsActivity.this, "Location Provider temporarily unavailable", Toast.LENGTH_SHORT).show();
                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                            MIN_TIME_BW_UPDATES,
                            MIN_DISTANCE_CHANGE_FOR_UPDATES,
                            locationListenerNetwork);
                    break;
                //case default --> request updates from NETWORK_PROVIDER
                default:
                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME_BW_UPDATES,
                            MIN_DISTANCE_CHANGE_FOR_UPDATES,
                            locationListenerNetwork);
            }

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    };

    android.location.LocationListener locationListenerNetwork = new android.location.LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            //output is Log.d and Toast that GPS is enabled and working
            Log.d("MyMaps", "onLocationChanged: GPS enabled and working");
            Toast.makeText(MapsActivity.this, "GPS enabled and working", Toast.LENGTH_SHORT).show();

            //Drop a marker on map- create a method called dropMarker
            dropMarker(LocationManager.NETWORK_PROVIDER);

            //Relaunch the network provider request (requestLocationUpdates(NETWORK_PROVIDER))
            if (ActivityCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                    MIN_TIME_BW_UPDATES,
                    MIN_DISTANCE_CHANGE_FOR_UPDATES,
                    locationListenerNetwork);

        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            //output is Log.d and Toast that GPS is enabled and working
            Log.d("MyMaps", "onLocationChanged: GPS enabled and working");
            Toast.makeText(MapsActivity.this, "GPS enabled and working", Toast.LENGTH_SHORT).show();

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {
        }
    };

    public void dropMarker(String provider) {

        if (locationManager != null) {

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
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                    MIN_TIME_BW_UPDATES,
                    MIN_DISTANCE_CHANGE_FOR_UPDATES,
                    locationListenerNetwork);
            myLocation = locationManager.getLastKnownLocation(provider);

        }
        if(myLocation == null){
            //display a message via Log.d and Toast
            Log.d("MyMaps", "dropMarker: myLocation is null");
            Toast.makeText(MapsActivity.this, "myLocation is null", Toast.LENGTH_SHORT).show();
        }else{
            userLocation = new LatLng(myLocation.getLatitude(), myLocation.getLongitude());
            //Display a message with the lat/lng
            CameraUpdate update = CameraUpdateFactory.newLatLngZoom(userLocation, MY_LOC_ZOOM_FACTOR);
            //Drop the actual marker on the map
            //if using circles, references Android Circle class
            if(provider.equals(locationManager.GPS_PROVIDER)) {
                mMap.addCircle(new CircleOptions()
                        .center(userLocation)
                        .radius(1)
                        .strokeColor(Color.GREEN)
                        .strokeWidth(2)
                        .fillColor(Color.GREEN));
            }
            else{
                mMap.addCircle(new CircleOptions()
                        .center(userLocation)
                        .radius(1)
                        .strokeColor(Color.RED)
                        .strokeWidth(2)
                        .fillColor(Color.RED));
            }
            mMap.animateCamera(update);

        }
    }

    public void clearMarkers(View view){

        mMap.clear();
    }

    public void searchMap(View view, String interest) throws IOException {
        mMap.clear();
        if (editSearch.getText().toString()== null) {
            return;
        }
        geocoder = new Geocoder(this, Locale.getDefault());
        if (geocoder.isPresent()) {
            Log.d("DEBUG", editSearch.getText().toString());
            try {
                Log.d("DEBUG", "Attempt");
                myAddresses  = geocoder.getFromLocationName(editSearch.getText().toString(), 300, myLocation.getLatitude() - 0.083333, myLocation.getLongitude() - 0.083333, myLocation.getLatitude() + 0.083333, myLocation.getLongitude() + 0.083333);
                Log.d("DEBUG", "Geocoder");
            } catch (SecurityException e) {
                Log.d("DEBUG", "IOException");

            }
            for (int i = 0; i < myAddresses.size(); i++) {
                Log.d("DEBUG", "Enters for loop");
                PointsofInterest = new LatLng(myAddresses.get(i).getLatitude(), myAddresses.get(i).getLongitude());
                mMap.addMarker(new MarkerOptions().position(PointsofInterest).title(editSearch.getText().toString()));

            }

        } else {

        }
    }

    public void clear(View v){
        mMap.clear();
    }
}