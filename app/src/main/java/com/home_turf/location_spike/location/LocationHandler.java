package com.home_turf.location_spike.location;

import android.location.Location;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;

public class LocationHandler {
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
}
