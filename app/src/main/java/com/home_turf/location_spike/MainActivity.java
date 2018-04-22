package com.home_turf.location_spike;

import android.location.Location;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.location.LocationRequest;

import com.home_turf.location_spike.R;


// TIME LOG
// 4/16 11am - 12:00p

public class MainActivity extends AppCompatActivity implements ActivityCompat.OnRequestPermissionsResultCallback {

    //Constants
    private static String TAG = MainActivity.class.getSimpleName();

    //Outlets
    private View mLayout;
    private TextView mLongitude;
    private TextView mLatitude;
//    private LocationHandler mLocationHandler;
    private Location mLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mLayout = findViewById(R.id.main_layout);
        mLongitude = findViewById(R.id.longitude);
        mLatitude = findViewById(R.id.latitude);
//
//        LocationHandler.initialize(getApplicationContext());
//        mLocationHandler = LocationHandler.getInstance();
//
//        // Location
//        // Check permissions
//        if (mLocationHandler.checkPermissions() == false) {
//            // Ask for permission
//            Log.d(TAG, "onCreate Location Denied.");
//            mLocationHandler.requestLocationPermission(this, mLayout);
//        } else {
//            // Get location
//            Log.d(TAG, "onCreate startLocationUpdates.");
////            startLocationUpdates();
//        }
//        mLocation = mLocationHandler.getCurrentLocation();
    }
}
