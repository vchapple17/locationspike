package com.home_turf.location_spike;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.DialogInterface;
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
import android.support.v7.app.AlertDialog;
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
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;


/* BaseLocationActivity
 *
 * Extend this class for any activity that requires location permissions
 *
 * To correctly extend this class:
 * 1) use "extends BaseLocationActivity"
 * 2) call initLocationActivity(View view) with the activity's layout
 * 3) implement "void onLocationChanged(Location location)" for handling a location update
 * 4) implement "void onLocationServicesRemoved()" for handling when permissions are removed
 *
 * Note: Google Map functionality is not part of this activity.
 */

public abstract class BaseLocationActivity extends AppCompatActivity
        implements ActivityCompat.OnRequestPermissionsResultCallback {

    // String Constants
    private static final String TAG = BaseLocationActivity.class.getSimpleName();
    private static final String LOCATION_APP_PERMISSION_REQUIRED = "Let Home Turf use your current location.";
    private static final String LOCATION_PHONE_PERMISSION_REQUIRED = "Turn on device location.";
    private static final String NO_NETWORK_ACCESS = "No Network Access";
    private static final String NO_NETWORK_ACCESS_MSG = "Check Internet Connection.";
    private static final String NO_APP_PERMISSION_TITLE = "Home Turf Location Permission";
    private static final String NO_APP_PERMISSION_MSG = "Adjust App-level Location permissions in settings.";
    private static final String OKAY = "OKAY";
    private static final String FIX = "FIX";
    private static final String LOCATION_PERMISSION_DENIED = "Location permission denied.";

    // Location Status possible results
    private static final int LOCATION_SERVICES_OK = 0;
    private static final int LOCATION_SERVICES_NO_NETWORK = 1000;        // AIRPLANE MODE
    private static final int LOCATION_SERVICES_NO_PERMISSIONS = 2000;    // APP LOCATION OFF
    private static final int LOCATION_SERVICES_NO_PHONE_LOCATION = 3000; // PHONE LOCATION OFF

    // Location Request Settings
    private long UPDATE_INTERVAL = 5 * 1000;  /* 5 secs */
    private long FASTEST_INTERVAL = 2000; /* 2 sec */

    // Location Permission Activity Result Code
    private static final int LOCATION_PERMISSION_RESULT = 17;
    public static final int REQUEST_CHECK_SETTINGS = 1234;

    // FusedLocationProviderClient for Location (Google recommended)
    private FusedLocationProviderClient mFusedLocationClient;
    private LocationCallback mLocationCallback;
    private LocationRequest mLocationRequest;

    // Bind child activity's main view for displaying alerts to user, save Snackbars for removal
    private View mLayout;
    private HashSet<Snackbar> snackbars = new HashSet<>();


    /*
     * Activity Life- Cycle
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
        createLocationRequest();        // Take advantage of Google API checking phone Location settings
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopLocationUpdates();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        /* Handle any callbacks for API Exception Resolution of Permissions */

        // Result is already analyzed in the onResume() function

        // Can override in child class
    }


    /*
     * Child class must call initLocationActivity
     * AND override both onLocationChanged() and onLocationServicesRemoved()
     */
    protected void initLocationActivity(View layout) {
        if (mLayout == null) {
            this.mLayout = layout;
        }

        // Location Client
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Synchr. - Creates Simple callback to register with FusedLocation FusedLocationClient
        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) return;
                // Call onLocationChanged, which Child Class implements
                onLocationChanged(locationResult.getLastLocation());
            }
        };
    }
    abstract void onLocationChanged(Location location);     // called when location is updated
    abstract void onLocationServicesRemoved();              // called when location is no allowed


    /*
     * ALL LOCATION REQUIREMENTS (airplane mode, app-permissions, and phone location services)
     */
    private boolean handleLocationServices() {
        // Uses results of checkLocationServices to determine what course of
        // action to take to ensure the ability to get the user's location
        // If no action required, returns true synchronously
        // If action is required, returns false and asynchronously directs user to fix issues


        // If this function returns true, call startLocationUpdates()
        switch (checkLocationServices()) {
            case LOCATION_SERVICES_NO_NETWORK:
                // Network not connected
                Log.d(TAG, "LOCATION_SERVICES_NO_NETWORK Network not connected (Airplane Mode).");
                alertNoNetwork(); // Asynch: diverts user to Settings
                onLocationServicesRemoved();
                return false;
            case LOCATION_SERVICES_NO_PERMISSIONS:
                // App not permitted to access location
                Log.d(TAG, "LOCATION_SERVICES_NO_PERMISSIONS App needs location permissions.");
                requestLocationPermission(); // Asynch: Asks user to allow permission
                onLocationServicesRemoved();
                return false;
            case LOCATION_SERVICES_NO_PHONE_LOCATION:
                Log.d(TAG, "LOCATION_SERVICES_NO_PHONE_LOCATION Location off for phone.");
                requestPhoneLocationActivate(); // Asynch: Ask user to turn on Location for phone
                onLocationServicesRemoved();
                return false;
            // Fallthrough
            case LOCATION_SERVICES_OK:
            default:
                return true;
        }
    }
    private int checkLocationServices() {
        // Synchronously checks that all Location needs are met,
        // returning different integers based on what service failed

        // AIRPLANE MODE
        if (!isNetworkConnected()) {
            return LOCATION_SERVICES_NO_NETWORK;
        }

        // APP SPECIFIC PERMISSIONS
        if (!checkPermissions()) {
            return LOCATION_SERVICES_NO_PERMISSIONS;
        }

        // PHONE ALLOWS LOCATION IN GENERAL
        if (!checkProviders()) {
            return LOCATION_SERVICES_NO_PHONE_LOCATION;
        }
        return LOCATION_SERVICES_OK;
    }

    /*
     * AIRPLANE MODE
     */
    private boolean isNetworkConnected() {
        // Synchronously check that AIRPLANE MODE IS OFF
        ConnectivityManager cm = (ConnectivityManager) getSystemService(BaseLocationActivity.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null;
    }
    private void alertNoNetwork() {
        // Asynchronously Direct user to turn off airplane mode.
        AlertDialog.Builder builder;
        builder = new AlertDialog.Builder(this, android.R.style.Theme_Material_Dialog_Alert);
        builder.setTitle(NO_NETWORK_ACCESS)
                .setMessage(NO_NETWORK_ACCESS_MSG)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // Direct to settings and will come back to onResume()
                        startActivity(new Intent(Settings.ACTION_AIRPLANE_MODE_SETTINGS));
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // do nothing or return home.
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    /*
     * PHONE LOCATION SERVICES
     */
    private boolean checkProviders() {
        // Synchronously Check that phone allows location to be provided (GPS and Network enabled)
        LocationManager locationManager = (LocationManager) getApplicationContext()
                .getSystemService(LOCATION_SERVICE);
        try {
            List<String> providers = locationManager.getProviders(true);
            boolean isGpsEnabled = false;
            boolean isNetworkEnabled = false;
            for (String s : providers) {
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
    private void requestPhoneLocationActivate() {
        // The location services for the entire phone is turned off.
        // This function directs the user to the Android Settings to turn back on.
        Snackbar snack = Snackbar.make(mLayout, LOCATION_PHONE_PERMISSION_REQUIRED,
                Snackbar.LENGTH_INDEFINITE).setAction(OKAY,
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        // Request the permission
                        Log.d(TAG, "requestPhoneLocationActivate.");
                        startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                        // The above activity is the location settings in the phone
                        // When the user returns to this activity, the onResume() is called
                    }
                });
        snackbars.add(snack);
        snack.show();
    }


    /*
     * APP SPECIFIC LOCATION PERMISSIONS
     */
    protected boolean checkPermissions() {
        // Synchronously check that App has location permissions granted
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

    // Logic for App-level location permissions
    private void createLocationRequest() {
        // Asynchronous call to create and add a Location Request.
        // Upon success, location logic beings

        Log.d(TAG, "createLocationRequest");
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

        // Create call back for when location settings are good.
        task.addOnSuccessListener(this, new OnSuccessListener<LocationSettingsResponse>() {
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                // All location settings are satisfied. The client can initialize
                // location requests here.
                // ...
                if (handleLocationServices()) {
                    Log.d(TAG, "createLocationRequest handleLocationServices=TRUE");
                    startLocationUpdates();
                } else {
                    Log.d(TAG, "createLocationRequest handleLocationServices=FALSE");
                    // Denied

                }
            }
        });
        // If location settings are not good...
        task.addOnFailureListener(this, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG, "createLocationRequest OnFailure");
                if (e instanceof ResolvableApiException) {
                    // Location settings are not satisfied, but this can be fixed
                    // by showing the user a dialog.
                    try {
                        // Show the dialog by calling startResolutionForResult(),
                        // and check the result in onActivityResult().
                        ResolvableApiException resolvable = (ResolvableApiException) e;
                        resolvable.startResolutionForResult(BaseLocationActivity.this,
                                REQUEST_CHECK_SETTINGS);
                    } catch (IntentSender.SendIntentException sendEx) {
                        // Ignore the error.
                    }
                } else {
                    // Location permission given, but maybe not App permissions
                    if (handleLocationServices()) {
                        Log.d(TAG, "createLocationRequest OnFailure handleLocationServices=TRUE");
                        startLocationUpdates();
                    }
                }
            }
        });
    }
    private void requestLocationPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
            // Additional rationale for the use of the permission with button to request
            // Reference: Google's Examples
            Log.d(TAG, "requestLocationPermission Pre-Prompt Location Permission.");
            Snackbar snack = Snackbar.make(mLayout, LOCATION_APP_PERMISSION_REQUIRED,
                    Snackbar.LENGTH_INDEFINITE).setAction(OKAY, new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // Request the permission
                    Log.d(TAG, "requestLocationPermission OK to request Location Permission.");
                    startLocationPermissionRequest();
                }
            });
            snack.show();
            snackbars.add(snack);
        } else {
            Log.d(TAG, "requestLocationPermission Cannot Request Location Permission.");
            Snackbar snack = Snackbar.make(mLayout, LOCATION_PERMISSION_DENIED,
                    Snackbar.LENGTH_INDEFINITE).setAction(FIX, new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // Request the permission
                    Log.d(TAG, "requestLocationPermission Asking for Location Permission.");
                    startLocationSettingsAlert();
                }
            });
            snack.show();
            snackbars.add(snack);
        }
    }
    private void startLocationSettingsAlert() {
        // Users can deny and not show request for current location
        // This function allows us to sends user to fix App-level permissions in Settings
        AlertDialog.Builder builder;
        builder = new AlertDialog.Builder(this, android.R.style.Theme_Material_Dialog_Alert);
        builder.setTitle(NO_APP_PERMISSION_TITLE)
                .setMessage(NO_APP_PERMISSION_MSG)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // Direct to Location settings and will come back to onResume()
                        startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // do nothing or return home since user denied going to Settings
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }
    private void startLocationPermissionRequest() {
        // Prompt user to allow or deny location services
        // If user clicks deny and never ask again, then this
        // function is not called again from requestLocationPermission
        // Instead requestLocationPermission will call startLocationSettingsAlert();
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                LOCATION_PERMISSION_RESULT);    // IDENTIFIER FOR onRequestPermissionsResult
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        // Handle the callback from startLocationPermissionRequest()
        // Start Location Update Looper if permission granted
        Log.d(TAG, "onRequestPermissionsResult");
        if (requestCode == LOCATION_PERMISSION_RESULT) {
            // Permissions granted, make sure all permissions are still valid
            if (handleLocationServices()) {
                // Permission has been granted. & Providers are available
                Log.d(TAG, "onRequestPermissionsResult Permission granted");
                startLocationUpdates();
            }
        }
    }

    /*
     * START & STOP LOCATION UPDATES
     */
    @SuppressLint("MissingPermission")
    // NOTE: Ensure that startLocationUpdates() is only called when handleLocationServices() is true
    protected void startLocationUpdates() {
        // Starts loop to get location updates using the LocationRequest and LocationCallback

        // Must have already checked that app has permission!!!!
        Log.d(TAG, "startLocationUpdates");

        // Setup client to get last known location
        mFusedLocationClient.getLastLocation()
                .addOnSuccessListener(new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // FYI: GPS location can be null if GPS is switched off
                        Log.d(TAG, "startLocationUpdates: onSuccess, call onLocationChanged");
                        onLocationChanged(location);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "Error trying to get last GPS location");
                        e.printStackTrace();
                    }
                });
        // Start updates on a loop
        mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
    }

    private void stopLocationUpdates() {
        // Remove the location Updates from the looper
        mFusedLocationClient.removeLocationUpdates(mLocationCallback);
        // Remove snackbars
        Iterator<Snackbar> itr = snackbars.iterator();

        while(itr.hasNext()) {
            Snackbar n = itr.next();
            Log.d(TAG, n.toString());
            n.dismiss();
            itr.remove();
        }
    }



}
