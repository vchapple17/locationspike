package com.home_turf.location_spike;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.SettingsClient;


public class MainActivity extends AppCompatActivity
        implements ActivityCompat.OnRequestPermissionsResultCallback {
    //Constants
    private static final String TAG = "MAIN";
    private static final int LOCATION_PERMISSION_RESULT = 17;
    private static final double DEFAULT_LAT = 44.5;
    private static final double DEFAULT_LON = -123.2;

    // Location
    private FusedLocationProviderClient mFusedLocationClient;
    private Location mCurrentLocation;

    // Location Services API
    private LocationRequest mLocationRequest;
    private long UPDATE_INTERVAL = 10 * 1000;  /* 10 secs */
    private long FASTEST_INTERVAL = 2000; /* 2 sec */


    // Outlets
    private View mLayout;
    private TextView mLongitude;
    private TextView mLatitude;


    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mLayout = findViewById(R.id.main_layout);
        mLongitude = findViewById(R.id.longitude);
        mLatitude = findViewById(R.id.latitude);
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        mCurrentLocation = new Location("Default");
        mCurrentLocation.setLatitude(DEFAULT_LAT);
        mCurrentLocation.setLongitude(DEFAULT_LON);
        Log.d(TAG, String.valueOf(mCurrentLocation.getLatitude()));
        Log.d(TAG, String.valueOf(mCurrentLocation.getLongitude()));
        mLatitude.setText(String.valueOf(mCurrentLocation.getLatitude()));
        mLongitude.setText(String.valueOf(mCurrentLocation.getLongitude()));

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

    @Override
    public void onResume(){
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
        ActivityCompat.requestPermissions(MainActivity.this,
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

    public void onLocationChanged(Location location) {
        // New location has now been determined
        if (location != null) {
            mCurrentLocation = location;
            Log.d(TAG, String.valueOf(mCurrentLocation.getLatitude()));
            Log.d(TAG, String.valueOf(mCurrentLocation.getLongitude()));
            // Update Screen
            mLatitude.setText(String.valueOf(mCurrentLocation.getLatitude()));
            mLongitude.setText(String.valueOf(mCurrentLocation.getLongitude()));
        }
        else {
            // Update Screen
            mLatitude.setText(String.valueOf(DEFAULT_LAT));
            mLongitude.setText(String.valueOf(DEFAULT_LON));
        }




    }
}
