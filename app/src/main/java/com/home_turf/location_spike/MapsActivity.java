//package com.home_turf.location_spike;
//
//import android.Manifest;
//import android.annotation.SuppressLint;
//import android.content.DialogInterface;
//import android.content.Intent;
//import android.content.IntentSender;
//import android.content.pm.PackageManager;
//import android.location.Location;
//import android.location.LocationManager;
//import android.net.ConnectivityManager;
//import android.os.Bundle;
//import android.os.Looper;
//import android.provider.Settings;
//import android.support.annotation.NonNull;
//import android.support.design.widget.Snackbar;
//import android.support.v4.app.ActivityCompat;
//import android.support.v7.app.AlertDialog;
//import android.support.v7.app.AppCompatActivity;
//import android.util.Log;
//import android.view.View;
//import android.widget.Toast;
//
//import com.google.android.gms.common.api.ResolvableApiException;
//import com.google.android.gms.location.FusedLocationProviderClient;
//import com.google.android.gms.location.LocationCallback;
//import com.google.android.gms.location.LocationRequest;
//import com.google.android.gms.location.LocationResult;
//import com.google.android.gms.location.LocationServices;
//import com.google.android.gms.location.LocationSettingsRequest;
//import com.google.android.gms.location.LocationSettingsResponse;
//import com.google.android.gms.location.SettingsClient;
//import com.google.android.gms.maps.CameraUpdateFactory;
//import com.google.android.gms.maps.GoogleMap;
//import com.google.android.gms.maps.OnMapReadyCallback;
//import com.google.android.gms.maps.SupportMapFragment;
//import com.google.android.gms.maps.model.BitmapDescriptorFactory;
//import com.google.android.gms.maps.model.CameraPosition;
//import com.google.android.gms.maps.model.LatLng;
//import com.google.android.gms.maps.model.MarkerOptions;
//import com.google.android.gms.tasks.OnFailureListener;
//import com.google.android.gms.tasks.OnSuccessListener;
//import com.google.android.gms.tasks.Task;
//
//import java.util.ArrayList;
//import java.util.List;
//
//
//// TEST Map stays focused on current CameraPosition
//// TODO Override/overload onClick for Joining Room
//
//// Save State
//// https://developer.android.com/topic/libraries/architecture/saving-states.html
//
//// https://developer.android.com/training/location/change-location-settings.html
//
//// https://github.com/googlemaps/android-samples/blob/master/tutorials/CurrentPlaceDetailsOnMap/app/src/main/java/com/example/currentplacedetailsonmap/MapsActivityCurrentPlace.java
//public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback,
//        ActivityCompat.OnRequestPermissionsResultCallback,
//        GoogleMap.OnMyLocationButtonClickListener,
//        GoogleMap.OnMyLocationClickListener {
//
//    // Constants
//    private static final String TAG = MapsActivity.class.getSimpleName();
//    private static final Double DEFAULT_LAT = 45.0;
//    private static final Double DEFAULT_LON = -95.0;
//    private static final Float DEFAULT_ZOOM = 8.0f;
//    private static final Float CURRENT_ZOOM = 15.0f;
//
//    private static final int LOCATION_PERMISSION_RESULT = 17;
//    private static final int REQUEST_CHECK_SETTINGS = 1234;
//
//    // Check Location Services possible results
//    private static final int LOCATION_SERVICES_OK = 0;
//    private static final int LOCATION_SERVICES_NO_NETWORK = 1000;        // AIRPLANE MODE
//    private static final int LOCATION_SERVICES_NO_PERMISSIONS = 2000;    // APP LOCATION OFF
//    private static final int LOCATION_SERVICES_NO_PHONE_LOCATION = 3000; // PHONE LOCATION OFF
//
//    private static final List<String> SPORTS = new ArrayList<>();
//    private static final String BASEBALL_STRING = "baseball.png";
//    private static final String BASKETBALL_STRING = "basketball.png";
//    private static final String FASTPITCH_STRING = "fastpitch.png";
//    private static final String SLOWPITCH_STRING = "slowpitch.png";
//    private static final String PINGPONG_STRING = "pingpong.png";
//    private static final String TENNIS_STRING = "tennis.png";
//
//    // Outlets
//    private GoogleMap mMap;
//    private View mLayout;
//
//    //// Location
//    private FusedLocationProviderClient mFusedLocationClient;
//    private LocationCallback mLocationCallback;
//
//    // Keys for storing activity state in the Bundle.
//    protected final static String STATE_MAP_CAMERA_KEY = "camera-location-key";
//
//    // Location Services API
//    private LocationRequest mLocationRequest;
//    private long UPDATE_INTERVAL = 5 * 1000;  /* 5 secs */
//    private long FASTEST_INTERVAL = 2000; /* 2 sec */
//
//    // Google Map
//    private CameraPosition mCameraPosition;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        mCameraPosition = null;
//
//        // SPORT NAMES
//        SPORTS.add(BASEBALL_STRING);
//        SPORTS.add(BASKETBALL_STRING);
//        SPORTS.add(FASTPITCH_STRING);
//        SPORTS.add(SLOWPITCH_STRING);
//        SPORTS.add(PINGPONG_STRING);
//        SPORTS.add(TENNIS_STRING);
//
//        // Basic setup from bundle
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_maps);
//        updateValuesFromBundle(savedInstanceState);
//        mLayout = findViewById(R.id.map_layout);
//
//        // Location Client
//        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
//
//        // Load Map Fragment Asynchronously. Get notified via onMapReady function
//        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
//                .findFragmentById(R.id.map);
//        mapFragment.getMapAsync(this);
//
//        // Synchr. - Create Location Callback
//        createLocationCallback();
//
//    }
//
//    private void requestPhoneLocationActivate() {
//        Snackbar.make(mLayout, R.string.locationPermissionRequired,
//                Snackbar.LENGTH_INDEFINITE).setAction(R.string.okButtonText,
//                new View.OnClickListener() {
//                    @Override
//                    public void onClick(View view) {
//                        // Request the permission
//                        Log.d(TAG, "requestPhoneLocationActivate.");
//                        // Upon user hitting back button on this new activity,
//                        // the user returns to this Activity at function: onResume()
//                        startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
//                    }
//                }).show();
//    }
//
//    private void loadMap() {
//        if (mMap != null) {
//            // Restore any saved Instance of Camera Position (such as due to phone rotation)
//            if (mCameraPosition == null) {
//                // Default
//                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
//                        new LatLng(DEFAULT_LAT, DEFAULT_LON),
//                        DEFAULT_ZOOM));
//            } else {
//                // Saved Camera Location
//                mMap.moveCamera(CameraUpdateFactory.newCameraPosition(mCameraPosition));
//            }
//        }
//    }
//    private boolean handleLocationServices() {
//        // If this function returns true, call startLocationUpdates()
//        switch (checkLocationServices()) {
//            case LOCATION_SERVICES_NO_NETWORK:
//                // Network not connected
//                Log.d(TAG, "LOCATION_SERVICES_NO_NETWORK Network not connected (Airplane Mode).");
//                alertNoNetwork(); // Asynch: diverts user to Settings
//                tearDownMyLocationOnMap();
//                return false;
//            case LOCATION_SERVICES_NO_PERMISSIONS:
//                // App not permitted to access location
//                Log.d(TAG, "LOCATION_SERVICES_NO_PERMISSIONS App needs location permissions.");
//                requestLocationPermission(); // Asynch: Asks user to allow permission
//                tearDownMyLocationOnMap();
//                return false;
//            case LOCATION_SERVICES_NO_PHONE_LOCATION:
//                Log.d(TAG, "LOCATION_SERVICES_NO_PHONE_LOCATION Location off for phone.");
//                requestPhoneLocationActivate(); // Asynch: Ask user to turn on Location for phone
//                tearDownMyLocationOnMap();
//                return false;
//            // Fallthrough
//            case LOCATION_SERVICES_OK:
//            default:
//                return true;
//        }
//    }
//
//    @Override
//    public void onResume() {
//        super.onResume();
//        Log.d(TAG, "onResume");
//        createLocationRequest();        // Take advantage of Google API checking phone Location settings
//
//        // May not need...
////        if (mLocationRequest == null) {
////            // Async. - Create Location Request and Check Phone Settings
////            createLocationRequest();    // This will call startLocationUpdates() when successful
////        }
////
////        else if (handleLocationServices()) {    // Services are active, call startLocationUpdates
////            startLocationUpdates();
////        }
////        else {
////            // Do nothing
////            // handleLocationServices has already directed the user
////            // on how to fix issues with location services.
////            // then onResume will be called
////        }
//    }
//
//    @Override
//    protected void onPause() {
//        super.onPause();
//        stopLocationUpdates();
//    }
//
//    /* Handle any callbacks for API Exception Resolution of Permissions */
//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        // Handle that Settings Request Check was successful
//        if (requestCode == REQUEST_CHECK_SETTINGS) {    //
//            switch (resultCode) {
//                case RESULT_OK:
//                    Log.i(TAG, "User agreed to make required location settings changes.");
//                    // Nothing to do. startLocationUpdates() gets called in onResume, which occurs
//                    // after this function completes.
//                    break;
//                case RESULT_CANCELED:
//                    Log.i(TAG, "User chose not to make required location settings changes.");
//                    Snackbar.make(mLayout, "Location Services are Required", Snackbar.LENGTH_LONG);
//                    mMap.clear();
//                    // Send user back or to another activity
//                    break;
//            }
//        }
//    }
//
//
//    /*
//     * Handle UI saved state
//     */
//    @Override
//    protected void onSaveInstanceState(Bundle outState) {
//        Log.i(TAG, "onSaveInstanceState called");
//        if (mMap != null) {
//            outState.putParcelable(STATE_MAP_CAMERA_KEY, mMap.getCameraPosition());
//        }
//        super.onSaveInstanceState(outState);
//    }
//
//    private void updateValuesFromBundle(Bundle savedInstanceState) {
//        if (savedInstanceState == null) {
//            return;
//        }
//        // Update the value of mCameraPosition from the Bundle.
//        if (savedInstanceState.keySet().contains(STATE_MAP_CAMERA_KEY)) {
//            Log.i(TAG, "updateValuesFromBundle camera position");
//            mCameraPosition = savedInstanceState.getParcelable(STATE_MAP_CAMERA_KEY);
//        } else {
//            mCameraPosition = null;
//        }
//    }
//
//    /*
//     * Location Permission and Update Functions
//     * */
//    private int checkLocationServices() {
//        // AIRPLANE MODE
//        if (!isNetworkConnected()) {
//            return LOCATION_SERVICES_NO_NETWORK;
//        }
//
//        // APP SPECIFIC PERMISSIONS
//        if (!checkPermissions()) {
//            return LOCATION_SERVICES_NO_PERMISSIONS;
//        }
//
//        // PHONE ALLOWS LOCATION IN GENERAL
//        if (!checkProviders()) {
//            return LOCATION_SERVICES_NO_PHONE_LOCATION;
//        }
//        return LOCATION_SERVICES_OK;
//    }
//
//    // Check that AIRPLANE MODE IS OFF
//    private boolean isNetworkConnected() {
//        ConnectivityManager cm = (ConnectivityManager) getSystemService(MapsActivity.CONNECTIVITY_SERVICE);
//
//        return cm.getActiveNetworkInfo() != null;
//    }
//
//    private void alertNoNetwork() {
//        // Citation:
//        // https://stackoverflow.com/questions/2115758/how-do-i-display-an-alert-dialog-on-android
//        AlertDialog.Builder builder;
//        builder = new AlertDialog.Builder(this, android.R.style.Theme_Material_Dialog_Alert);
//        builder.setTitle(R.string.networkAccessRequired)
//                .setMessage(R.string.networkAccessRequiredMessage)
//                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
//                    public void onClick(DialogInterface dialog, int which) {
//                        // Direct to settings and will come back to onResume()
//                        startActivity(new Intent(Settings.ACTION_AIRPLANE_MODE_SETTINGS));
//                    }
//                })
//                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
//                    public void onClick(DialogInterface dialog, int which) {
//                        // do nothing or return home.
//                    }
//                })
//                .setIcon(android.R.drawable.ic_dialog_alert)
//                .show();
//    }
//
//    // Check that App has location permissions granted
//    private boolean checkPermissions() {
//        if (ActivityCompat.checkSelfPermission(this,
//                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
//                ActivityCompat.checkSelfPermission(this,
//                        Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//            return false;
//        }
//        else {
//            return true;
//        }
//    }
//
//    // Check that phone allows location to be provided
//    private boolean checkProviders() {
//        LocationManager locationManager = (LocationManager) getApplicationContext()
//                .getSystemService(LOCATION_SERVICE);
//        try {
//            List<String> providers = locationManager.getProviders(true);
//            boolean isGpsEnabled = false;
//            boolean isNetworkEnabled = false;
//            for (String s : providers) {
//                if (s.equals("gps")) {
//                    isGpsEnabled = true;
//                } else if (s.equals("network")) {
//                    isNetworkEnabled = true;
//                }
//            }
//
//            if (isGpsEnabled && isNetworkEnabled) {
//                return true;
//            } else {
//                return false;
//            }
//
//        } catch (NullPointerException e) {
//            return false;
//        }
//
//
//    }
//
//    // Asynchronous call to create and add a Location Request.
//    // Upon success, location logic beings
//    private void createLocationRequest() {
//        Log.d(TAG, "createLocationRequest");
//        // Location Request object for Updating location
//        // https://github.com/codepath/android_guides/wiki/Retrieving-Location-with-LocationServices-API
//        mLocationRequest = new LocationRequest();
//        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
//        mLocationRequest.setInterval(UPDATE_INTERVAL);
//        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
//
//        // LocationSettingsRequest object to check that location settings of phone are ready.
//        // https://developer.android.com/training/location/change-location-settings.html#java
//        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
//        builder.addLocationRequest(mLocationRequest);
//        LocationSettingsRequest locationSettingsRequest = builder.build();
//        SettingsClient settingsClient = LocationServices.getSettingsClient(this);
//        Task<LocationSettingsResponse> task = settingsClient.checkLocationSettings(locationSettingsRequest);
//        // If location settings are good...
//        task.addOnSuccessListener(this, new OnSuccessListener<LocationSettingsResponse>() {
//            @Override
//            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
//                // All location settings are satisfied. The client can initialize
//                // location requests here.
//                // ...
//                if (handleLocationServices()) {
//                    Log.d(TAG, "createLocationRequest handleLocationServices=TRUE");
//                    startLocationUpdates();
//                } else {
//                    Log.d(TAG, "createLocationRequest handleLocationServices=FALSE");
//                }
//            }
//        });
//        // If location settings are not good...
//        task.addOnFailureListener(this, new OnFailureListener() {
//            @Override
//            public void onFailure(@NonNull Exception e) {
//                Log.d(TAG, "createLocationRequest OnFailure");
//                if (e instanceof ResolvableApiException) {
//                    // Location settings are not satisfied, but this can be fixed
//                    // by showing the user a dialog.
//                    try {
//                        // Show the dialog by calling startResolutionForResult(),
//                        // and check the result in onActivityResult().
//                        ResolvableApiException resolvable = (ResolvableApiException) e;
//                        resolvable.startResolutionForResult(MapsActivity.this,
//                                REQUEST_CHECK_SETTINGS);
//                    } catch (IntentSender.SendIntentException sendEx) {
//                        // Ignore the error.
//                    }
//                } else {
//                    if (handleLocationServices()) {
//                        Log.d(TAG, "createLocationRequest OnFailure handleLocationServices=TRUE");
//                        startLocationUpdates();
//                    }
//                }
//            }
//        });
//    }
//
//    private void createLocationCallback() {
//        mLocationCallback = new LocationCallback() {
//            @Override
//            public void onLocationResult(LocationResult locationResult) {
//                if (locationResult == null) return;
//                // Update UI with location data
//                onLocationChanged(locationResult.getLastLocation());
//            }
//        };
//    }
//
//    private void requestLocationPermission() {
//        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
//            // Additional rationale for the use of the permission with button to request
//            // Reference: Google's Examples
//            Log.d(TAG, "requestLocationPermission Pre-Prompt Location Permission.");
//            Snackbar.make(mLayout, R.string.locationPermissionRequired,
//                    Snackbar.LENGTH_INDEFINITE).setAction(R.string.okButtonText, new View.OnClickListener() {
//                @Override
//                public void onClick(View view) {
//                    // Request the permission
//                    Log.d(TAG, "requestLocationPermission OK to request Location Permission.");
//                    startLocationPermissionRequest();
//                }
//            }).show();
//        } else {
//            Log.d(TAG, "requestLocationPermission Requesting Location Permission.");
//            startLocationPermissionRequest();
//        }
//    }
//
//    private void startLocationPermissionRequest() {
//        ActivityCompat.requestPermissions(MapsActivity.this,
//                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
//                LOCATION_PERMISSION_RESULT);    // IDENTIFIER FOR onRequestPermissionsResult
//    }
//
//    // Handle the callback from startLocationPermissionRequest()
//    @Override
//    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
//                                           @NonNull int[] grantResults) {
//        Log.d(TAG, "onRequestPermissionsResult");
//        if (requestCode == LOCATION_PERMISSION_RESULT) {
//            // Analyze for location permission.
//            // Permissions granted, make sure  Phone locations in general is ON.
//            if (handleLocationServices()) {
//                // Permission has been granted. & Providers are available
//                Log.d(TAG, "onRequestPermissionsResult Permission granted");
//                startLocationUpdates();
//            }
//        }
//    }
//
//    @SuppressLint("MissingPermission")
//    protected void startLocationUpdates() {
//        // Must have already checked that app has permission!!!!
//        Log.d(TAG, "startLocationUpdates");
//        // Set up client to get last known location
//        mFusedLocationClient.getLastLocation()
//            .addOnSuccessListener(new OnSuccessListener<Location>() {
//                @Override
//                public void onSuccess(Location location) {
//                    // FYI: GPS location can be null if GPS is switched off
//                    Log.d("MapDemoActivity", "startLocationUpdates: onSuccess, call onLocationChanged");
//                    onLocationChanged(location);
//                }
//            })
//            .addOnFailureListener(new OnFailureListener() {
//                @Override
//                public void onFailure(@NonNull Exception e) {
//                Log.d("MapDemoActivity", "Error trying to get last GPS location");
//                e.printStackTrace();
//                }
//            });
//        // Start updates
//        mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
//    }
//
//    private void stopLocationUpdates() {
//        mFusedLocationClient.removeLocationUpdates(mLocationCallback);
//    }
//
//    private void onLocationChanged(Location location) {
//        // Update UI with new location only when map is new.
//        // Note: New Location, may be null
//        if (location == null) {
//            Log.d(TAG, "onLocationChanged location==null");
//            return;
//        }
//
//        if ((mMap != null) && (!mMap.isMyLocationEnabled())) {
//            // Add My Location Capabilities to Map
//            setupMyLocationOnMap();
//
//            if (mCameraPosition == null) {
//                // Zoom to current location
//                float zoomLevel = CURRENT_ZOOM; //This goes up to 21
//                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
//                        new LatLng(location.getLatitude(),
//                                location.getLongitude()), zoomLevel));
//            }
//        }
//        if (mMap != null) {
//            // Save Camera View of map
//            mCameraPosition = mMap.getCameraPosition();
//        }
//    }
//
//
//    /* Current Location Google API Inferface
//     * https://developers.google.com/maps/documentation/android-api/location
//     * */
//    @Override
//    public boolean onMyLocationButtonClick() {
//        // Zooms to User location by default
//        return false;
//    }
//
//    @Override
//    public void onMyLocationClick(@NonNull Location location) {
//        // By default shows title and snippet.
//        Toast.makeText(this, "Current location:\n" + location, Toast.LENGTH_LONG).show();
//    }
//
//
//    /*
//     * Populate & Setup Map
//     */
//    @Override
//    public void onMapReady(GoogleMap googleMap) {
//        Log.d(TAG, "onMapReady");
//        mMap = googleMap;
//        mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
//        mMap.setMinZoomPreference(2.0f);
//        mMap.moveCamera(CameraUpdateFactory.zoomTo(14f));
//        mMap.setMaxZoomPreference(18.0f);
//
//        mMap.getUiSettings().setZoomControlsEnabled(true);
//        mMap.getUiSettings().setCompassEnabled(true);
//        mMap.getUiSettings().setAllGesturesEnabled(true);
//
//        loadMap();
//
//        // Add Mock Game Pins to Map
//        addGamePins(Game.newGames(15));  // Add game pins
//    }
//
//    @SuppressLint("MissingPermission")
//    private void setupMyLocationOnMap() {
//        // Access Current Location on map
//        if (mMap != null && checkPermissions() ) {
//            mMap.setMyLocationEnabled(true);
//            mMap.setOnMyLocationButtonClickListener(this);  // When upper right button clicked
//            mMap.setOnMyLocationClickListener(this);        // When blue dot clicked
//        }
//    }
//
//    @SuppressLint("MissingPermission")
//    private void tearDownMyLocationOnMap() {
//        // Access Current Location on map
//        if (mMap != null ) {
//            // Save camera state
//            mCameraPosition = mMap.getCameraPosition();
//            mMap.setMyLocationEnabled(false);
//        }
//    }
//
//
//    /*
//     *  Mock Data
//     */
//    private void addGamePins(List<Game> games) {
//        for (Game g :
//             games) {
//            addGamePin(g);
//        }
//    }
//
//    private void addGamePin(Game g) {
//        LatLng pin = new LatLng(g.getLatitude(), g.getLongitude());
//        MarkerOptions mark = new MarkerOptions()
//                .position(pin)
//                .title(g.getName())
//                .snippet(g.getSnippet())
//                .icon(BitmapDescriptorFactory.fromAsset(g.getFilename()));
////                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
//        mMap.addMarker(mark);
//    }
//
//    private static class Game {
//        static private List<String> sportStrings = new ArrayList<>();
//        static private List<Game> games = new ArrayList<>();
//
//        public Double getLongitude() {
//            return longitude;
//        }
//
//        public Double getLatitude() {
//            return latitude;
//        }
//
//        public String getName() {
//            return name;
//        }
//
//        public String getFilename() {
//            return filename;
//        }
//
//        public String getSnippet() {
//            return snippet;
//        }
//
//        private Double longitude;
//        private Double latitude;
//        private String name;
//        private String filename;
//        private String snippet;
//
//        Game(Double lat, Double lon, String n, String f, String s) {
//            this.longitude = lon;
//            this.latitude = lat;
//            this.name = n;
//            this.filename = f;
//            this.snippet = s;
//        }
//
//        static List<Game> getGames() {
//            return games;
//        }
//        static List<Game> newGames(int n) {
//            // Clear List of Games
//            if (games == null) { games = new ArrayList<>(); }
//            else { games.clear(); }
//
//            for(int i = 0; i < n; i++) {
//                games.add( newGame() );
//            }
//            return games;
//        }
//
//        static Game newGame() {
//            if (games == null) { games = new ArrayList<>(); }
////            Double lat = Math.random()*(180);
////            if (lat > 90) lat -= 90;
////            Double lon = Math.random()*(360);
////            if (lon > 180) lon -= 180;
//
//            Double lat = Math.random()*.01 + 39.12;
//            Double lon = -Math.random()*.01 - 94.53;
//
//            return new Game(lat, lon, "Game #" + String.valueOf(games.size()), randomSportString(), randomSnippet());
//
//        }
//
//        static String randomSportString() {
//            if (SPORTS.size() > 0 ) {
//                int r = (int) (Math.random() * SPORTS.size());
//                try {
//                    return SPORTS.get(r);
//                } catch (Exception e) {
//                    return SPORTS.get(0);
//                }
//            } else { return ""; }
//        }
//
//        static String randomSnippet() {
//            int r = (int) (Math.random() * 3);
//            switch (r) {
//                case 0:
//                    return "Beginner's looking for fun.";
//                case 1:
//                    return "Intermediate or Expert players.";
//                case 2:
//                    return "Senior league";
//            }
//            return "";
//        }
//    }
//}