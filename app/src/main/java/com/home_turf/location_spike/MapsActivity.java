package com.home_turf.location_spike;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;


// https://github.com/googlemaps/android-samples/blob/master/tutorials/CurrentPlaceDetailsOnMap/app/src/main/java/com/example/currentplacedetailsonmap/MapsActivityCurrentPlace.java
public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback,
        ActivityCompat.OnRequestPermissionsResultCallback,
        GoogleMap.OnMyLocationButtonClickListener,
        GoogleMap.OnMyLocationClickListener {
    private static final String TAG = MapsActivity.class.getSimpleName();
    private static final int LOCATION_PERMISSION_RESULT = 17;
    private static final double DEFAULT_LAT = 44.5;
    private static final double DEFAULT_LON = -123.2;

    // Outlets
    private GoogleMap mMap;
    private View mLayout;
    private boolean isMapReady;


    private String mLongitude;
    private String mLatitude;


    //// Location
    private FusedLocationProviderClient mFusedLocationClient;
    private Location mCurrentLocation;


    // Location Services API
    private LocationRequest mLocationRequest;
    private long UPDATE_INTERVAL = 10 * 1000;  /* 10 secs */
    private long FASTEST_INTERVAL = 2000; /* 2 sec */


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        mLongitude = getIntent().getStringExtra("Lat");
        mLatitude = getIntent().getStringExtra("Lon");
        mCurrentLocation = new Location("Default");
        mCurrentLocation.setLatitude(Double.valueOf(mLatitude));
        mCurrentLocation.setLongitude(Double.valueOf(mLongitude));
        isMapReady = false;

        // Check location permissions
        if (checkPermissions() == false) {
            // Ask for permission
            Log.d(TAG, "onCreate Location Denied.");
            requestLocationPermission();
        } else {
            // Get location
            Log.d(TAG, "onCreate startLocationUpdates.");
            startLocationUpdates();
        }
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
    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMyLocationEnabled(true);
        mMap.setOnMyLocationButtonClickListener(this);
        mMap.setOnMyLocationClickListener(this);

        addGamePins();
    }

    @Override
    public void onResume() {
        super.onResume();
        // Check location permissions
        if (checkPermissions() == false) {
            // Ask for permission
            Log.d(TAG, "onResume Location Denied.");
            requestLocationPermission();
        } else {
            // Get location
            Log.d(TAG, "onResume startLocationUpdates.");
            startLocationUpdates();
        }
    }

    private void requestLocationPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
            // Additional rationale for the use of the permission with button to request
            // Reference: Google's Examples
            Log.d(TAG, "requestLocationPermission Pre-Prompt Location Permission.");
            Snackbar.make(mLayout, R.string.locationPermissionRequired,
                    Snackbar.LENGTH_INDEFINITE).setAction(R.string.okButtonText, new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // Request the permission
                    Log.d(TAG, "requestLocationPermission OK to request Location Permission.");
                    startLocationPermissionRequest();
                }
            }).show();
        } else {
            Log.d(TAG, "requestLocationPermission Requesting Location Permission.");
            startLocationPermissionRequest();
        }
    }

    private void startLocationPermissionRequest() {
        ActivityCompat.requestPermissions(MapsActivity.this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                LOCATION_PERMISSION_RESULT);    // IDENTIFIER FOR REQUEST CALLBACK
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        Log.d(TAG, "onRequestPermissionsResult");
        if (requestCode == LOCATION_PERMISSION_RESULT) {
            // Analyze for location permission.
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission has been granted. Get Location
                Log.d(TAG, "onRequestPermissionsResult Permission granted");
                startLocationUpdates();
            }
        }
    }

    // Trigger new location updates at interval
    @SuppressLint("MissingPermission")
    protected void startLocationUpdates() {
        // Modified from Reference:
        // https://github.com/codepath/android_guides/wiki/Retrieving-Location-with-LocationServices-API
        // Create the location request to start receiving updates
        mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);

        // Create LocationSettingsRequest object using location request
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);
        LocationSettingsRequest locationSettingsRequest = builder.build();

        // Check whether location settings are satisfied
        // https://developers.google.com/android/reference/com/google/android/gms/location/SettingsClient
        SettingsClient settingsClient = LocationServices.getSettingsClient(this);
        settingsClient.checkLocationSettings(locationSettingsRequest);

        // new Google API SDK v11 uses getFusedLocationProviderClient(this)
        mFusedLocationClient.requestLocationUpdates(mLocationRequest, new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                onLocationChanged(locationResult.getLastLocation());
            }
        }, Looper.myLooper());
    }

    private boolean checkPermissions() {
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            return false;
        }
    }

    @SuppressLint("MissingPermission")
    private void onLocationChanged(Location location) {
        if (isMapReady) {
            // New location has now been determined
            if (location != null) {
                mCurrentLocation = location;
                mLatitude = String.valueOf(mCurrentLocation.getLatitude());
                mLongitude = String.valueOf(mCurrentLocation.getLongitude());
                Log.d(TAG, mLatitude + " " + mLongitude);
                // Update Screen

            } else {
                // Update Screen

            }
        }
    }
    
    private void addGamePins() {
        // Mock game
        ArrayList<Double> latitudes = new ArrayList<>();
        latitudes.add(-30.0);
        latitudes.add(-40.0);
        latitudes.add(-50.0);
        latitudes.add(-60.0);

        ArrayList<Double> longitudes = new ArrayList<>();
        longitudes.add(90.0);
        longitudes.add(80.0);
        longitudes.add(70.0);
        longitudes.add(60.0);

        ArrayList<String> names = new ArrayList<>();
        names.add("Game 1");
        names.add("Game 2");
        names.add("Game 3");
        names.add("Game 4");

        final int size = latitudes.size();
        for (int i = 0; i < size; i++) {
            addGamePin(latitudes.get(i), longitudes.get(i), names.get(i));
        }

    }

    private void addGamePin(Double lat, Double lon, String name) {
        LatLng pin = new LatLng(lat, lon);
        mMap.addMarker(new MarkerOptions().position(pin).title(name));
//        mMap.moveCamera(CameraUpdateFactory.newLatLng(current));
    }

    @Override
    public boolean onMyLocationButtonClick() {
//        Toast.makeText(this, "MyLocation button clicked", Toast.LENGTH_SHORT).show();
        // Zooms to User location
        return false;
    }

    @Override
    public void onMyLocationClick(@NonNull Location location) {
//        Toast.makeText(this, "Current location:\n" + location, Toast.LENGTH_LONG).show();
    }
}