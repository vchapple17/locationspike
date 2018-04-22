//package com.home_turf.location_spike.location;
//
//import android.Manifest;
//import android.app.Activity;
//import android.content.Context;
//import android.content.pm.PackageManager;
//import android.location.Location;
//import android.os.Looper;
//import android.support.annotation.NonNull;
//import android.support.design.widget.Snackbar;
//import android.support.v4.app.ActivityCompat;
//import android.util.Log;
//import android.view.View;
//
//
//import com.google.android.gms.location.FusedLocationProviderClient;
//import com.google.android.gms.location.LocationCallback;
//import com.google.android.gms.location.LocationRequest;
//import com.google.android.gms.location.LocationResult;
//import com.google.android.gms.location.LocationServices;
//import com.google.android.gms.location.LocationSettingsRequest;
//import com.google.android.gms.location.SettingsClient;
//
//import com.google.android.gms.tasks.OnSuccessListener;
//import com.home_turf.location_spike.R;
//
//public class LocationHandler {
//    private static String TAG = LocationHandler.class.getSimpleName();
//    private static final int LOCATION_PERMISSION_RESULT = 17;
//    private static final double DEFAULT_LAT = 44.5;
//    private static final double DEFAULT_LON = -123.2;
//    private long UPDATE_INTERVAL = 10 * 1000;  /* 10 secs */
//    private long FASTEST_INTERVAL = 2000; /* 2 sec */
//
//
//    // STATIC Location instances
//    private static LocationHandler instance = null;
//
//    private FusedLocationProviderClient fusedLocationClient;
//    private Context context;
//    private LocationRequest locationRequest;
//    private Location currentLocation;
//
//
//    public Location getCurrentLocation() {
//        return currentLocation;
//    }
//
//
//
//    public static LocationHandler getInstance() { return instance; }
//
//    public static void initialize(Context context) {
//        if (instance == null) {
//            instance = new LocationHandler(context);
//        }
//    }
//
//    private LocationHandler(Context context) {
//        this.context = context;
//        this.fusedLocationClient = LocationServices.getFusedLocationProviderClient(context);
//        this.currentLocation = new Location("default");
//        this.currentLocation.setLatitude(DEFAULT_LAT);
//        this.currentLocation.setLongitude(DEFAULT_LON);
//
////        // Check location permissions
////        if (checkPermissions() == false) {
////            // Ask for permission
////            Log.d(TAG, "onCreate Location Denied.");
////            requestLocationPermission();
////        } else {
////            // Get location
////            Log.d(TAG, "onCreate startLocationUpdates.");
//////            startLocationUpdates();
////        }
//    }
//
//    public Context getContext() {
//        return this.context;
//    }
//
//    public boolean checkPermissions() {
//        if (ActivityCompat.checkSelfPermission(this.context,
//                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
//            return true;
//        } else {
//            return false;
//        }
//    }
//
//    // Trigger new location updates at interval
////    @SuppressLint("MissingPermission")
//    protected void startLocationUpdates() {
////        // Modified from Reference:
////        // https://github.com/codepath/android_guides/wiki/Retrieving-Location-with-LocationServices-API
////        // Create the location request to start receiving updates
////        locationRequest = new LocationRequest();
////        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
////        locationRequest.setInterval(UPDATE_INTERVAL);
////        locationRequest.setFastestInterval(FASTEST_INTERVAL);
////
////        // Create LocationSettingsRequest object using location request
////        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
////        builder.addLocationRequest(locationRequest);
////        LocationSettingsRequest locationSettingsRequest = builder.build();
////
////        // Check whether location settings are satisfied
////        // https://developers.google.com/android/reference/com/google/android/gms/location/SettingsClient
////        SettingsClient settingsClient = LocationServices.getSettingsClient(this);
////        settingsClient.checkLocationSettings(locationSettingsRequest);
////
////        // new Google API SDK v11 uses getFusedLocationProviderClient(this)
////        fusedLocationClient.requestLocationUpdates(locationRequest, new LocationCallback() {
////            @Override
////            public void onLocationResult(LocationResult locationResult) {
////                onLocationChanged(locationResult.getLastLocation());
////            }
////        }, Looper.myLooper());
//    }
//
//
//    // Request update
////    Intent pendingIntent = new Intent();
////        pendingIntent.putExtra("location", this.currentLocation.toString());
////        fusedLocationClient.requestLocationUpdates( LocationRequest.create(), pendingIntent)
////            .addOnCompleteListener(new OnCompleteListener() {
////        @Override
////        public void onComplete(@NonNull Task task) {
////            Log.d("MainActivity", "Result: " + task.getResult());
////        }
////    });
//
//
//
////
////    @Override
////    public void onResume(){
////        super.onResume();
////        // Check location permissions
////        if (checkPermissions() == false) {
////            // Ask for permission
////            Log.d(TAG, "onResume Location Denied.");
////            requestLocationPermission();
////        } else {
////            // Get location
////            Log.d(TAG, "onResume startLocationUpdates.");
////            startLocationUpdates();
////        }
////
////    }
//
//
//
////    // Handle Click and Location Permissions
////    private void submitText() {
////        // Get Text
////        // Connect Edit Text
////        textView = findViewById(R.id.editText);
////
////        String txt = textView.getText().toString();
////
////        // Check txt is not empty
////        if (txt.length() > 0) {
////            Log.d(TAG, "submitText Not Empty.");
////
////            // Check location permissions
////            if (checkPermissions() == false) {
////                // Ask for permission
////                Log.d(TAG, "submitText Location Denied.");
////                mCurrentLocation = new Location("default");
////                mCurrentLocation.setLatitude(DEFAULT_LAT);
////                mCurrentLocation.setLongitude(DEFAULT_LON);
////                requestLocationPermission();
////                saveTextLabel();
////
////            } else {
////                // Get location
////                Log.d(TAG, "submitText Logging with location.");
////                saveLastLocation();
////            }
////        }
////    }
//
//    public void requestLocationPermission(Activity activ, View layout) {
//        if (ActivityCompat.shouldShowRequestPermissionRationale( activ, Manifest.permission.ACCESS_FINE_LOCATION)) {
//            // Additional rationale for the use of the permission with button to request
//            // Reference: Google's Examples
//            Log.d(TAG, "requestLocationPermission Pre-Prompt Location Permission.");
//            Snackbar.make(activ,"HELLO", Snackbar.LENGTH_INDEFINITE);
////            Snackbar.make(activ, R.string.locationPermissionRequired, Snackbar.LENGTH_INDEFINITE);
//////            Snackbar.make(activity, R.string.locationPermissionRequired, Snackbar.LENGTH_INDEFINITE)
////                    .setAction(R.string.okButtonText, new View.OnClickListener() {
//                @Override
//                public void onClick(View view) {
//                    // Request the permission
//                    Log.d(TAG, "requestLocationPermission OK to request Location Permission.");
//                    startLocationPermissionRequest();
//                }
//            }).show();
//
//        } else {
//            Log.d(TAG, "requestLocationPermission Requesting Location Permission.");
//            startLocationPermissionRequest();
//        }
//    }
//
//    private void startLocationPermissionRequest() {
////        ActivityCompat.requestPermissions()
////        ActivityCompat.requestPermissions(this.context,
////                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
////                LOCATION_PERMISSION_RESULT);    // IDENTIFIER FOR REQUEST CALLBACK
//    }
//
////    @Override
////    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
////                                           @NonNull int[] grantResults) {
////        Log.d(TAG, "onRequestPermissionsResult");
////        if (requestCode == LOCATION_PERMISSION_RESULT) {
////            // Analyze for location permission.
////            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
////                // Permission has been granted. Get Location
////                Log.d(TAG, "onRequestPermissionsResult Permission granted");
////                startLocationUpdates();
////            }
////        }
////    }
//
//    @SuppressWarnings("MissingPermission")
//    private void saveLastLocation() {
////        Log.d(TAG, "updateLocation with current location.");
////        mFusedLocationClient.getLastLocation()
////                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
////                    @Override
////                    public void onSuccess(Location location) {
////                        if (location != null) {
////                            Log.d(TAG, "Location returned.");
////                            onLocationChanged(location);
////                        } else {
////                            Log.d(TAG, "Null location returned.");
////                        }
////                        saveTextLabel();
////                    }
////                });
//    }
//
////    private void saveTextLabel() {
////        Log.d(TAG, String.valueOf(mCurrentLocation.getLatitude()));
////        Log.d(TAG, String.valueOf(mCurrentLocation.getLongitude()));
////
////        if (mDB != null && textView != null) {
////            ContentValues vals = new ContentValues();
////            vals.put(SQLiteDB.LocationTable.COLUMN_NAME_TEXT_STRING,
////                    textView.getText().toString());
////            vals.put(SQLiteDB.LocationTable.COLUMN_NAME_LATITUDE,
////                    String.valueOf(mCurrentLocation.getLatitude()));
////            vals.put(SQLiteDB.LocationTable.COLUMN_NAME_LONGITUDE,
////                    String.valueOf(mCurrentLocation.getLongitude()));
////            mDB.insert(SQLiteDB.LocationTable.TABLE_NAME, null, vals);
////            populateTable();
////        } else {
////            Log.d(TAG, "saveTextLabel Unable to access database for writing.");
////        }
////    }
//
//
//
//
//
//
//    public void onLocationChanged(Location location) {
////        // New location has now been determined
////        mCurrentLocation = location;
////        Log.d(TAG, String.valueOf(mCurrentLocation.getLatitude()));
////        Log.d(TAG, String.valueOf(mCurrentLocation.getLongitude()));
//    }
//}
