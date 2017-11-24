package io.rolique.roliqueapp.services.gps;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;

import com.google.android.gms.maps.model.LatLng;

import java.text.DecimalFormat;
import java.util.Arrays;

import timber.log.Timber;

/**
 * Created by Volodymyr Oleshkevych on 9/8/2017.
 * Copyright (c) 2017, Rolique. All rights reserved.
 */

public class GPSTrackerService extends Service implements LocationListener {

    // The minimum distance to change Updates in meters
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 10; // 10 meters

    // The minimum time between updates in milliseconds
    private static final long MIN_TIME_BW_UPDATES = 1000 * 60 * 1; // 1 minute

    public static final LatLng ROLIQUE_POSITION = new LatLng(49.841358007066034, 24.023118875920773);
    public static final int RANGE_RADIUS = 50;

    public interface PositionChanged {
        void onPositionChanged(boolean isInRange, double distance);
    }

    private final PositionChanged mPositionChanged;

    private final Context mContext;

    // flag for GPS status
    boolean mIsGPSEnabled = false;

    // flag for network status
    boolean mIsNetworkEnabled = false;

    // flag for GPS status
    public boolean mCanGetLocation = false;

    Location mLocation; // mLocation
    double mLatitude; // mLatitude
    double mLongitude; // mLongitude
    double mDistance; // mLongitude

    // Declaring a Location Manager
    LocationManager mLocationManager;
    AlertDialog mSettingsDialog;

    public GPSTrackerService(Context context, PositionChanged positionChanged) {
        mContext = context;
        mPositionChanged = positionChanged;
        onPositionChanged(getLocation());
    }

    @SuppressLint("MissingPermission")
    public Location getLocation() {
        try {
            if (mLocationManager == null)
                mLocationManager = (LocationManager) mContext
                        .getSystemService(LOCATION_SERVICE);

            // getting GPS status
            assert mLocationManager != null;
            mIsGPSEnabled = mLocationManager
                    .isProviderEnabled(LocationManager.GPS_PROVIDER);

            // getting network status
            mIsNetworkEnabled = mLocationManager
                    .isProviderEnabled(LocationManager.NETWORK_PROVIDER);
            if (mIsNetworkEnabled) {
                mCanGetLocation = true;
                mLocationManager.requestLocationUpdates(
                        LocationManager.NETWORK_PROVIDER,
                        MIN_TIME_BW_UPDATES,
                        MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                mLocation = mLocationManager
                        .getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                if (mLocation != null) {
                    mLatitude = mLocation.getLatitude();
                    mLongitude = mLocation.getLongitude();
                }
            }
            mLocationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    MIN_TIME_BW_UPDATES,
                    MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
            if (mIsGPSEnabled) {
                mCanGetLocation = true;
                mLocation = mLocationManager
                        .getLastKnownLocation(LocationManager.GPS_PROVIDER);
                if (mLocation != null) {
                    mLatitude = mLocation.getLatitude();
                    mLongitude = mLocation.getLongitude();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        onPositionChanged(mLocation);
        return mLocation;
    }

    /**
     * Stop using GPS listener
     * Calling this function will stop using GPS in your app
     */
    public void stopUsingGPS() {
        if (mLocationManager != null) {
            mLocationManager.removeUpdates(GPSTrackerService.this);
            onDestroy();
        }
    }

    /**
     * Function to get mLatitude
     */
    public double getLatitude() {
        if (mLocation != null) mLatitude = mLocation.getLatitude();
        return mLatitude;
    }

    /**
     * Function to get mLongitude
     */
    public double getLongitude() {
        if (mLocation != null) mLongitude = mLocation.getLongitude();
        return mLongitude;
    }

    /**
     * Function to get Distance
     */
    public double getDistance() {
        return mDistance;
    }

    /**
     * Function to check GPS/wifi enabled
     *
     * @return boolean
     */
    public boolean isCanGetLocation() {
        return mCanGetLocation;
    }

    /**
     * Function to show settings alert dialog
     * On pressing Settings button will lauch Settings Options
     */
    public void showSettingsAlert() {
        if (mSettingsDialog != null && mSettingsDialog.isShowing())
            mSettingsDialog.dismiss();
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(mContext);

        // Setting Dialog Title
        alertDialog.setTitle("GPS is settings");

        // Setting Dialog Message_Preview
        alertDialog.setMessage("GPS is not enabled. Do you want to go to settings menu?");

        // On pressing Settings button
        alertDialog.setPositiveButton("Settings", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                mContext.startActivity(intent);
            }
        });

        // on pressing cancel button
        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        // Showing Alert Message_Preview
        mSettingsDialog = alertDialog.create();
        mSettingsDialog.show();
    }

    public void onPositionChanged(Location location) {
        if (location == null) return;
        LatLng mLatStart = new LatLng(location.getLatitude(), location.getLongitude());
        Timber.e("From listener " + mLatStart.toString());
        float[] distance = new float[2];

        Location.distanceBetween(mLatStart.latitude,
                mLatStart.longitude,
                ROLIQUE_POSITION.latitude,
                ROLIQUE_POSITION.longitude,
                distance);

        mDistance = distance[0];
        mPositionChanged.onPositionChanged(distance[0] <= RANGE_RADIUS, distance[0]);
        Timber.e(Arrays.toString(distance));
        Timber.d("is in range: " + (distance[0] <= RANGE_RADIUS));
    }

    private void calculationByDistance(LatLng StartP, LatLng EndP) {
        final int RADIUS = 6371000;// radius of earth in m
        double lat1 = StartP.latitude;
        double lat2 = EndP.latitude;
        double lon1 = StartP.longitude;
        double lon2 = EndP.longitude;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1))
                * Math.cos(Math.toRadians(lat2)) * Math.sin(dLon / 2)
                * Math.sin(dLon / 2);
        double c = 2 * Math.asin(Math.sqrt(a));
        double valueResult = RADIUS * c;
        double m = valueResult / 1;
        DecimalFormat newFormat = new DecimalFormat("####");
        int mInDec = Integer.valueOf(newFormat.format(m));
        double km = valueResult % 1000;
        int kmInDec = Integer.valueOf(newFormat.format(km));
        Timber.e("Radius Value " + valueResult + "   M  " + mInDec
                + " Meter   " + kmInDec);

//        return RADIUS * c <= RANGE_RADIUS;
    }

    @Override
    public void onLocationChanged(Location location) {
        onPositionChanged(location);
    }

    @Override
    public void onProviderDisabled(String provider) {
        Timber.e(provider);
    }

    @Override
    public void onProviderEnabled(String provider) {
        Timber.e(provider);
        if (mSettingsDialog != null && mSettingsDialog.isShowing())
            mSettingsDialog.dismiss();
        getLocation();
        mCanGetLocation = true;
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        Timber.e("onStatusChanged " + provider);
        Timber.e((status == GpsStatus.GPS_EVENT_STARTED) + " started");
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }
}
