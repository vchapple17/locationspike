package com.home_turf.location_spike;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.home_turf.location_spike.mock_data.MockGameData;

import java.util.List;

public class MyLocationActivity extends BaseLocationActivity implements OnMapReadyCallback,
        GoogleMap.OnMyLocationButtonClickListener,
        GoogleMap.OnMyLocationClickListener {

    // String Constants
    private static final String TAG = MyLocationActivity.class.getSimpleName();
    protected final static String STATE_MAP_CAMERA_KEY = "camera-location-key";
    protected final static String STATE_LOCATION_KEY = "last-location-key";

    // Google Map Settings
    private static final Double DEFAULT_LAT = 45.0;
    private static final Double DEFAULT_LON = -95.0;
    private static final Float DEFAULT_ZOOM = 8.0f;
    private static final Float CURRENT_ZOOM = 15.0f;

    // Save Google Map Camera Position
    private CameraPosition mCameraPosition;

    // Outlets
    private GoogleMap mMap;
    private View mLayout;
    private Location mLastLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mCameraPosition = null;

        // Basic setup from bundle
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_location);
        updateValuesFromBundle(savedInstanceState);

        // Load Map Fragment Asynchronously. Get notified via onMapReady function
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // Initialize BaseLocationActivity (parent activity)
        mLayout = findViewById(R.id.map_layout);
        initLocationActivity(mLayout);
    }

    /*
     * Handle UI saved state
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        Log.i(TAG, "onSaveInstanceState called");
        if (mMap != null) {
            outState.putParcelable(STATE_MAP_CAMERA_KEY, mMap.getCameraPosition());
        }
        if (mLastLocation != null) {
            outState.putParcelable(STATE_LOCATION_KEY, mLastLocation);
        }
        super.onSaveInstanceState(outState);
    }

    private void updateValuesFromBundle(Bundle savedInstanceState) {
        if (savedInstanceState == null) {
            return;
        }
        // Update the value of mCameraPosition from the Bundle.
        if (savedInstanceState.keySet().contains(STATE_MAP_CAMERA_KEY)) {
            Log.i(TAG, "updateValuesFromBundle camera position");
            mCameraPosition = savedInstanceState.getParcelable(STATE_MAP_CAMERA_KEY);
        } else {
            mCameraPosition = null;
        }

        if (savedInstanceState.keySet().contains(STATE_LOCATION_KEY)) {
            mLastLocation = savedInstanceState.getParcelable(STATE_LOCATION_KEY);
        } else {
            mLastLocation = null;
        }
    }


    // OPtionally override onActivityResult from BaseLocationActivity
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        /* Handle any callbacks for API Exception Resolution of Permissions */
        super.onActivityResult(requestCode, resultCode, data);
        // Handle that Settings Request Check was successful
        if (requestCode == REQUEST_CHECK_SETTINGS) {    //
            switch (resultCode) {
                case RESULT_OK:
                    Log.i(TAG, "User agreed to make required location settings changes.");
                    // Nothing to do. startLocationUpdates() gets called in onResume, which occurs
                    // after this function completes.
                    // Set Up for Zoom to current location
                    mCameraPosition = null;
                    mLayout.clearAnimation();
                    break;
                case RESULT_CANCELED:
                    Log.i(TAG, "User chose not to make required location settings changes.");
                    Snackbar.make(mLayout, "Location Services are Required", Snackbar.LENGTH_LONG);
                    // Send user back or to another activity
                    break;
            }
        }
    }

    // Must Override onLocationChanged for BaseLocationActivity
    @Override
    void onLocationChanged(Location location) {
        // Update UI with new location only when map is new.
        // Note: New Location, may be null
        if ((location == null) && (mLastLocation == null)){
            // Default Location
            if (mMap != null) {
                if (mCameraPosition != null) {
                    mMap.moveCamera(CameraUpdateFactory.newCameraPosition(mCameraPosition));
                }
                else {
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                            new LatLng(DEFAULT_LAT,
                                    DEFAULT_LON), DEFAULT_ZOOM));
                    // Save Camera View of map
                    mCameraPosition = mMap.getCameraPosition();
                }
                return;
            }
        }
        else if (location != null){
            Log.d(TAG, "onLocationChanged location!=null");
            mLastLocation = location;
        }

        // Determine zoom
        if ((mMap != null) && (!mMap.isMyLocationEnabled())) {
            // Add My Location Capabilities to Map
            setupMyLocationOnMap();
            mCameraPosition = mMap.getCameraPosition();
        } else {
            assert mLastLocation != null;
            mCameraPosition = new CameraPosition( new LatLng(mLastLocation.getLatitude(),
                    mLastLocation.getLongitude()), CURRENT_ZOOM, 0, 0);
        }
    }

    @Override
    @SuppressLint("MissingPermission")
    void onLocationServicesRemoved() {
        // Access Current Location on map
        if (mMap != null ) {
            // Save camera state
            mCameraPosition = mMap.getCameraPosition();
            mMap.setMyLocationEnabled(false);
        }
    }
    // Google Map API
    private void loadMap() {
        Log.d(TAG, "loadMap");
        if (mMap != null) {
            // Restore any saved Instance of Camera Position (such as due to phone rotation)
            if (mCameraPosition == null) {
                // Default
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                        new LatLng(DEFAULT_LAT, DEFAULT_LON),
                        DEFAULT_ZOOM));
            } else {
                // Saved Camera Location
                mMap.moveCamera(CameraUpdateFactory.newCameraPosition(mCameraPosition));
            }
        }
    }

    /* Current Location Google API Inferface
     * https://developers.google.com/maps/documentation/android-api/location
     * */
    @Override
    public boolean onMyLocationButtonClick() {
        // Zooms to User location by default
        return false;
    }

    @Override
    public void onMyLocationClick(@NonNull Location location) {
        // By default shows title and snippet.
        Toast.makeText(this, "Current location:\n" + location, Toast.LENGTH_LONG).show();
    }


    /*
     * Populate & Setup Map
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d(TAG, "onMapReady");
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        mMap.setMinZoomPreference(2.0f);
        mMap.setMaxZoomPreference(18.0f);

        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setCompassEnabled(true);
        mMap.getUiSettings().setAllGesturesEnabled(true);

        loadMap();

        // Add Mock Game Pins to Map
        addGamePins(MockGameData.newGames(15));  // Add game pins
    }

    @SuppressLint("MissingPermission")
    private void setupMyLocationOnMap() {
        // Access Current Location on map
        if ((mMap != null) && checkPermissions()) {
            mMap.setMyLocationEnabled(true);
            mMap.setOnMyLocationButtonClickListener(this);  // When upper right button clicked
            mMap.setOnMyLocationClickListener(this);        // When blue dot clicked

            // Zooming
            if (mLastLocation == null) {
                // Zoom to default
                Log.d(TAG, "setupMyLocationOnMap default zooom");
                float zoomLevel = DEFAULT_ZOOM; //This goes up to 21
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                        new LatLng(DEFAULT_LAT, DEFAULT_LON), zoomLevel));
            } else {
                // Zoom to mLastLocation
                Log.d(TAG, "setupMyLocationOnMap zoom to last location");
                float zoomLevel = CURRENT_ZOOM; //This goes up to 21
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                        new LatLng(mLastLocation.getLatitude(),
                                mLastLocation.getLongitude()), zoomLevel));
            }
        }
    }


    /*
     *  Add Game Pins (Mock Data)
     */
    private void addGamePins(List<MockGameData> games) {
        for (MockGameData g :
                games) {
            addGamePin(g);
        }
    }

    private void addGamePin(MockGameData g) {
        LatLng pin = new LatLng(g.getLatitude(), g.getLongitude());
        MarkerOptions mark = new MarkerOptions()
                .position(pin)
                .title(g.getName())
                .snippet(g.getSnippet())
                .icon(BitmapDescriptorFactory.fromAsset(g.getFilename()));
//                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
        mMap.addMarker(mark);
    }
}

