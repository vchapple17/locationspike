package com.home_turf.location_spike;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.util.ArrayList;
import java.util.List;

// Save State
// https://developer.android.com/topic/libraries/architecture/saving-states.html

// https://developer.android.com/training/location/change-location-settings.html

// https://github.com/googlemaps/android-samples/blob/master/tutorials/CurrentPlaceDetailsOnMap/app/src/main/java/com/example/currentplacedetailsonmap/MapsActivityCurrentPlace.java
public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback,
        ActivityCompat.OnRequestPermissionsResultCallback,
        GoogleMap.OnMyLocationButtonClickListener,
        GoogleMap.OnMyLocationClickListener {

    private static final String TAG = MapsActivity.class.getSimpleName();
    private static final int LOCATION_PERMISSION_RESULT = 17;
    private static final int REQUEST_CHECK_SETTINGS = 1234;
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
    private LocationCallback mLocationCallback;
    private boolean mRequestingLocationUpdates;

    // Keys for storing activity state in the Bundle.
    protected final static String REQUESTING_LOCATION_UPDATES_KEY = "requesting-location-updates-key";
    protected final static String CAMERA_LOCATION_KEY = "camera-location-key";
//    protected final static String CAMERA_LOCATION_KEY = "camera-location-key";


    // Location Services API
    private LocationRequest mLocationRequest;
    private long UPDATE_INTERVAL = 5 * 1000;  /* 5 secs */
    private long FASTEST_INTERVAL = 2000; /* 2 sec */


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Basic setup from bundle
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        updateValuesFromBundle(savedInstanceState);
        mLayout = findViewById(R.id.map_layout);

        // Location Client
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Load Map Fragment Asynchronously. Get notified via onMapReady function
        isMapReady = false;
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // Create Location Request and Check Phone Settings
        createLocationRequest();

        // Location Callback
        createLocationCallback();

    }


    @Override
    public void onResume() {
        super.onResume();
        checkLocationServices();
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopLocationUpdates();
    }

    /* Handle any results from Activities or Exception Resolutions */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Handle that Settings Request Check was successful
        if (requestCode == REQUEST_CHECK_SETTINGS) {    //
            switch (resultCode) {
                case RESULT_OK:
                    Log.i(TAG, "User agreed to make required location settings changes.");
                    // Nothing to do. startLocationUpdates() gets called in onResume again.
                    break;
                case RESULT_CANCELED:
                    Log.i(TAG, "User chose not to make required location settings changes.");
                    mRequestingLocationUpdates = false;
                    Snackbar.make(mLayout, "Location Services are Required",Snackbar.LENGTH_LONG);
                    mMap.clear();
                    break;
            }
        }
    }


    /*
     * Handle UI saved state
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putBoolean(REQUESTING_LOCATION_UPDATES_KEY,
                mRequestingLocationUpdates);

        // Save Current Location and Camera Angle

        super.onSaveInstanceState(outState);
    }

    private void updateValuesFromBundle(Bundle savedInstanceState) {
        if (savedInstanceState == null) {
            return;
        }

        // Update the value of mRequestingLocationUpdates from the Bundle.
        if (savedInstanceState.keySet().contains(REQUESTING_LOCATION_UPDATES_KEY)) {
            mRequestingLocationUpdates = savedInstanceState.getBoolean(
                    REQUESTING_LOCATION_UPDATES_KEY);
        }

        // ...

        // Update UI to match restored state
//        updateUI();
    }


    /*
    * Location Permission and Update Functions
    * */


    private void checkLocationServices() {
        // Check location permissions & providers
        if (!checkPermissions()) {
            // Ask for permissions to access Location Information
            Log.d(TAG, "onResume Location Denied.");
            requestLocationPermission();
        } else {
            if (checkProviders()) {
                // Permissions granted and providers are enabled.
                Log.d(TAG, "onResume startLocationUpdates.");
                startLocationUpdates();
            }
            else {
                // Permissions granted but providers turned off
                // Send to main location settings to turn on GPS and Network
                Log.d(TAG, "onResume noProviders available.");
                startLocationUpdates();
                Snackbar.make(mLayout, R.string.locationPermissionRequired,
                        Snackbar.LENGTH_INDEFINITE).setAction(R.string.okButtonText, new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        // Request the permission
                        Log.d(TAG, "requestLocationPermission OK to request Location Permission.");
                        startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                }).show();
            }
        }
    }


    private boolean checkPermissions() {
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            return false;
        }
    }

    private boolean checkProviders() {
        LocationManager locationManager = (LocationManager) getApplicationContext()
                .getSystemService(LOCATION_SERVICE);
        try {
            List<String> providers = locationManager.getProviders(true);
            boolean isGpsEnabled = false;
            boolean isNetworkEnabled = false;
            for (String s: providers) {
                if (s.equals("gps")) { isGpsEnabled = true; }
                else if (s.equals("network")) { isNetworkEnabled = true; }
            }

            if (isGpsEnabled && isNetworkEnabled) { return true; }
            else { return false; }
            
        } catch (NullPointerException e) {
            return false;
        }


    }

    private void createLocationRequest() {
        // Location Request object for Updating location
        // https://github.com/codepath/android_guides/wiki/Retrieving-Location-with-LocationServices-API
        mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);

        // LocationSettingsRequest object to check that location settings of phone are ready.
        // https://developer.android.com/training/location/change-location-settings.html#java
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);
        LocationSettingsRequest locationSettingsRequest = builder.build();
        SettingsClient settingsClient = LocationServices.getSettingsClient(this);
        Task<LocationSettingsResponse> task = settingsClient.checkLocationSettings(locationSettingsRequest);
        // If location settings are good...
        task.addOnSuccessListener(this, new OnSuccessListener<LocationSettingsResponse>() {
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                // All location settings are satisfied. The client can initialize
                // location requests here.
                // ...
            }
        });
        // If location settings are not good...
        task.addOnFailureListener(this, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                if (e instanceof ResolvableApiException) {
                    // Location settings are not satisfied, but this can be fixed
                    // by showing the user a dialog.
                    try {
                        // Show the dialog by calling startResolutionForResult(),
                        // and check the result in onActivityResult().
                        ResolvableApiException resolvable = (ResolvableApiException) e;
                        resolvable.startResolutionForResult(MapsActivity.this,
                                REQUEST_CHECK_SETTINGS);
                    } catch (IntentSender.SendIntentException sendEx) {
                        // Ignore the error.
                    }
                }
            }
        });
    }

    private void createLocationCallback() {
        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) return;
                for (Location location : locationResult.getLocations()) {
                    // Update UI with location data
                    onLocationChanged(locationResult.getLastLocation());
                }
            };
        };
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


    @SuppressLint("MissingPermission")
    protected void startLocationUpdates() {
        // new Google API SDK v11 uses getFus   edLocationProviderClient(this)
        mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
    }

    private void stopLocationUpdates() {
        mFusedLocationClient.removeLocationUpdates(mLocationCallback);
        mRequestingLocationUpdates = false;
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


    /* Current Location Functions
    * https://developers.google.com/maps/documentation/android-api/location
    * */
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



    /*
     * Populate Map
     */
    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        // Access Current Location
        mMap.setMyLocationEnabled(true);
        mMap.setOnMyLocationButtonClickListener(this);  // When upper right button clicked
        mMap.setOnMyLocationClickListener(this);        // When blue dot clicked

        addGamePins();  // Add game pins
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



}