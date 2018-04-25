package com.home_turf.location_spike;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

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
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
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

    // Constants
    private static final String TAG = MapsActivity.class.getSimpleName();
    private static final int LOCATION_PERMISSION_RESULT = 17;
    private static final int REQUEST_CHECK_SETTINGS = 1234;
    private static final String BASEBALL_STRING = "BASEBALL";
    private static final String BASKETBALL_STRING = "BASKETBALL";
    private static final String FASTPITCH_STRING = "FASTPITCH";
    private static final String SLOWPITCH_STRING = "SLOWPITCH";
//    private static final String SOCCER_STRING = "SOCCER";
//    private static final String FOOTBALL_STRING = "FOOTBALL";
    private static final String TENNIS_STRING = "TENNIS";

    private static final String UNKNOWN_SPORT_STRING = "blah";

    // Outlets
    private GoogleMap mMap;
    private View mLayout;

    //// Location
    private FusedLocationProviderClient mFusedLocationClient;
    private LocationCallback mLocationCallback;
    private boolean mRequestingLocationUpdates;

    // Keys for storing activity state in the Bundle.
    protected final static String STATE_MAP_CAMERA_KEY = "camera-location-key";
    protected final static String STATE_MAP_RECENTER_KEY = "camera-recenter-key";

    // Location Services API
    private LocationRequest mLocationRequest;
    private long UPDATE_INTERVAL = 5 * 1000;  /* 5 secs */
    private long FASTEST_INTERVAL = 2000; /* 2 sec */

    // Google Map
    private boolean isMapReady;
    private boolean zoomToCurrentLocation;
    private CameraPosition mCameraPosition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Basic setup from bundle
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        zoomToCurrentLocation = true;
        updateValuesFromBundle(savedInstanceState);
        mLayout = findViewById(R.id.map_layout);

        // Location Client
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mRequestingLocationUpdates = false;        // Check permissions when false

        // Load Map Fragment Asynchronously. Get notified via onMapReady function
        isMapReady = false;

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // Synchr. - Create Location Callback
        createLocationCallback();

        // Async. - Create Location Request and Check Phone Settings
        createLocationRequest();

    }


    @Override
    public void onResume() {
        super.onResume();
        if (checkLocationServices()) {
            startLocationUpdates();
            if (isMapReady) {
                setupMyLocationOnMap();
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopLocationUpdates();
    }

    /* Handle any results Exception Resolution of Permissions */
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
                    Snackbar.make(mLayout, "Location Services are Required", Snackbar.LENGTH_LONG);
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
        Log.i(TAG, "onSaveInstanceState called");
        if (mMap != null) {
            outState.putBoolean(STATE_MAP_RECENTER_KEY, zoomToCurrentLocation);
            outState.putParcelable(STATE_MAP_CAMERA_KEY, mMap.getCameraPosition());
        }
        super.onSaveInstanceState(outState);
    }

    private void updateValuesFromBundle(Bundle savedInstanceState) {
        if (savedInstanceState == null) {
            return;
        }

        // Update the value of mCameraPosition from the Bundle.
        if (savedInstanceState.keySet().contains(STATE_MAP_RECENTER_KEY)) {
            zoomToCurrentLocation = savedInstanceState.getBoolean(
                    STATE_MAP_RECENTER_KEY);
            if (!zoomToCurrentLocation && (savedInstanceState.keySet().contains(STATE_MAP_CAMERA_KEY))) {
                Log.i(TAG, "updateValuesFromBundle camera position");
                mCameraPosition = savedInstanceState.getParcelable(STATE_MAP_CAMERA_KEY);
            }
        } else {
            zoomToCurrentLocation = true;
            mCameraPosition = null;
        }
    }


    /*
     * Location Permission and Update Functions
     * */
    private boolean checkLocationServices() {
        mRequestingLocationUpdates = false;
        // Check location permissions & providers
        boolean hasPermission = checkPermissions();
        boolean hasProviders = checkProviders();

        if (!isNetworkConnected()) {
            // Network not connected
            Log.d(TAG, "checkLocationServices Network not connected.");
            Snackbar.make(mLayout, R.string.locationPermissionRequired,
                    Snackbar.LENGTH_INDEFINITE).setAction(R.string.okButtonText, new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // Request the permission
                    startActivity(new Intent(Settings.ACTION_AIRPLANE_MODE_SETTINGS));
                }
            }).show();
            return false;
        }

        // Test permissions and providers
        if (hasPermission && hasProviders) {
            // Permissions granted and providers are enabled.
            Log.d(TAG, "checkLocationServices startLocationUpdates.");
            mRequestingLocationUpdates = true;
            return true;
        }
        if (!hasPermission) {
            // Permission denied.
            // Ask for permissions to access Location Information
            Log.d(TAG, "checkLocationServices Location Denied.");
            requestLocationPermission();
        }
        if (!hasProviders) {
            // Permissions granted but providers turned off
            // Send to main location settings to turn on GPS and Network
            Log.d(TAG, "checkLocationServices noProviders available.");
            Snackbar.make(mLayout, R.string.locationPermissionRequired,
                    Snackbar.LENGTH_INDEFINITE).setAction(R.string.okButtonText, new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // Request the permission
                    Log.d(TAG, "checkLocationServices OK to request Location Permission.");
                    startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                }
            }).show();
        }

        return false;

    }

//    private void checkLocationServices() {
//        mRequestingLocationUpdates = false;
//        // Check location permissions & providers
//        if (!checkPermissions()) {
//            // Ask for permissions to access Location Information
//            Log.d(TAG, "onResume Location Denied.");
//            requestLocationPermission();
//        } else {
//            if (checkProviders()) {
//                // Permissions granted and providers are enabled.
//                Log.d(TAG, "onResume startLocationUpdates.");
//                startLocationUpdates();
//            } else {
//                // Permissions granted but providers turned off
//                // Send to main location settings to turn on GPS and Network
//                Log.d(TAG, "onResume noProviders available.");
////                startLocationUpdates();
//                Snackbar.make(mLayout, R.string.locationPermissionRequired,
//                        Snackbar.LENGTH_INDEFINITE).setAction(R.string.okButtonText, new View.OnClickListener() {
//                    @Override
//                    public void onClick(View view) {
//                        // Request the permission
//                        Log.d(TAG, "requestLocationPermission OK to request Location Permission.");
//                        startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
//                    }
//                }).show();
//            }
//        }
//    }
    private boolean checkPermissions() {
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return false;
        }
        else {
            return true;
        }
    }

    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(MapsActivity.CONNECTIVITY_SERVICE);

        return cm.getActiveNetworkInfo() != null;
    }

    private boolean checkProviders() {
        LocationManager locationManager = (LocationManager) getApplicationContext()
                .getSystemService(LOCATION_SERVICE);
        try {
            List<String> providers = locationManager.getProviders(true);
            boolean isGpsEnabled = false;
            boolean isNetworkEnabled = false;
            for (String s : providers) {
                Log.d(TAG, s);
                if (s.equals("gps")) {
                    isGpsEnabled = true;
                } else if (s.equals("network")) {
                    isNetworkEnabled = true;
                }
            }

            if (isGpsEnabled && isNetworkEnabled) {
                return true;
            } else {
                return false;
            }

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
                checkLocationServices();
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
            }
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

    // Handle the callback from startLocationPermissionRequest()
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        Log.d(TAG, "onRequestPermissionsResult");
        if (requestCode == LOCATION_PERMISSION_RESULT) {
            // Analyze for location permission.
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission has been granted. Get Location
                Log.d(TAG, "onRequestPermissionsResult Permission granted");
                mRequestingLocationUpdates = true;
                if (checkLocationServices()) {
                    startLocationUpdates();
                    if (isMapReady) {
                        setupMyLocationOnMap();
                    }
                }
            }
            else {
                mRequestingLocationUpdates = false;
            }
        }
    }

    @SuppressLint("MissingPermission")
    protected void startLocationUpdates() {
        // Get last known location
        mFusedLocationClient.getLastLocation()
                .addOnSuccessListener(new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // GPS location can be null if GPS is switched off
                        if (location != null) {
                            onLocationChanged(location);
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        mRequestingLocationUpdates = false;
                        Log.d("MapDemoActivity", "Error trying to get last GPS location");
                        e.printStackTrace();
                    }
                });
        mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
    }

    private void stopLocationUpdates() {
        mFusedLocationClient.removeLocationUpdates(mLocationCallback);
        mRequestingLocationUpdates = false;
    }

    @SuppressLint("MissingPermission")
    private void onLocationChanged(Location location) {
        if (isMapReady && zoomToCurrentLocation) {
            // New location has now been determined
            if (location != null) {
                // Update Screen To current location on start
                float zoomLevel = 13.0f; //This goes up to 21
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                        new LatLng(location.getLatitude(),
                                location.getLongitude()), zoomLevel));
                zoomToCurrentLocation = false;
            }
        }
    }


    /* Current Location Google API Inferface
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
        Toast.makeText(this, "Current location:\n" + location, Toast.LENGTH_LONG).show();
    }


    /*
     * Populate Map
     */
    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(GoogleMap googleMap) {
        isMapReady = true;

        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        mMap.setMinZoomPreference(6.0f);
        mMap.moveCamera(CameraUpdateFactory.zoomTo(21f));
        mMap.setMaxZoomPreference(18.0f);

        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setCompassEnabled(true);
        mMap.getUiSettings().setAllGesturesEnabled(true);

        // Restore Camera Position
        if (mCameraPosition != null) {
            Log.d(TAG, "camera position");
            Log.d(TAG, mCameraPosition.toString());
            mMap.moveCamera(CameraUpdateFactory.newCameraPosition(mCameraPosition));
        }

        setupMyLocationOnMap();

        addGamePins();  // Add game pins
    }


    @SuppressLint("MissingPermission")
    private void setupMyLocationOnMap() {
        // Access Current Location
        if (mRequestingLocationUpdates && isMapReady) {
            if (checkLocationServices()) {
                mMap.setMyLocationEnabled(true);
                mMap.setOnMyLocationButtonClickListener(this);  // When upper right button clicked
                mMap.setOnMyLocationClickListener(this);        // When blue dot clicked
            }
        }
    }
    private void addGamePins() {
        // Mock game
        ArrayList<Double> latitudes = new ArrayList<>();
        latitudes.add(39.125);
        latitudes.add(39.120);
        latitudes.add(39.123);
        latitudes.add(39.122);
        latitudes.add(39.130);
        latitudes.add(39.120);
        latitudes.add(39.123);


        ArrayList<Double> longitudes = new ArrayList<>();
        longitudes.add(-94.532);
        longitudes.add(-94.535);
        longitudes.add(-94.531);
        longitudes.add(-94.536);
        longitudes.add(-94.535);
        longitudes.add(-94.531);
        longitudes.add(-94.532);

        ArrayList<String> names = new ArrayList<>();
        names.add("Game 1");
        names.add("Game 2");
        names.add("Game 3");
        names.add("Game 4");
        names.add("Game 5");
        names.add("Game 6");
        names.add("Game 7");

        final int size = latitudes.size();
        int num = 5 + 1;
        for (int i = 0; i < size; i++) {
            if (i % num == 0) {
                addGamePin(latitudes.get(i), longitudes.get(i), names.get(i), BASEBALL_STRING);
            } else if (i % num == 1) {
                addGamePin(latitudes.get(i), longitudes.get(i), names.get(i), BASKETBALL_STRING);
            } else if (i % num == 2) {
                addGamePin(latitudes.get(i), longitudes.get(i), names.get(i), FASTPITCH_STRING);
            } else if (i % num == 3) {
                addGamePin(latitudes.get(i), longitudes.get(i), names.get(i), SLOWPITCH_STRING);
            } else if (i % num == 4) {
                addGamePin(latitudes.get(i), longitudes.get(i), names.get(i), TENNIS_STRING);
            } else if (i % num == 5) {
                addGamePin(latitudes.get(i), longitudes.get(i), names.get(i), UNKNOWN_SPORT_STRING);
            }
        }
    }

    private void addGamePin(Double lat, Double lon, String name, String sport) {
        LatLng pin = new LatLng(lat, lon);
        MarkerOptions mark = new MarkerOptions()
                .position(pin)
                .title(name)
                .snippet("More information here");
//                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
        switch(sport) {
            case BASEBALL_STRING:
                mark.icon(BitmapDescriptorFactory.fromAsset("baseball.png"));
                break;
            case BASKETBALL_STRING:
                mark.icon(BitmapDescriptorFactory.fromAsset("basketball.png"));
                break;
            case TENNIS_STRING:
                mark.icon(BitmapDescriptorFactory.fromAsset("tennis.png"));
                break;
            case FASTPITCH_STRING:
                mark.icon(BitmapDescriptorFactory.fromAsset("fastpitch.png"));
                break;
            case SLOWPITCH_STRING:
                mark.icon(BitmapDescriptorFactory.fromAsset("slowpitch.png"));
                break;
        }

        mMap.addMarker(mark);
    }




}