package io.rolique.roliqueapp.screens

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.IntentSender
import android.location.Location
import android.os.Bundle
import android.os.Handler
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.common.api.ResultCallback
import com.google.android.gms.location.*
import com.google.android.gms.maps.model.LatLng
import timber.log.Timber

/**
 * Created by Volodymyr Oleshkevych on 12/9/2017.
 * Copyright (c) 2017, Rolique. All rights reserved.
 */
abstract class BaseLocationActivity : BaseActivity() {

    companion object {
        val ROLIQUE_POSITION = LatLng(49.841358007066034, 24.023118875920773)
        val RANGE_RADIUS = 50
    }

    private val RC_LOCATION_SETTINGS: Int = 5416
    private val UPDATE_INTERVAL_IN_MILLISECONDS: Long = 2000
    private val FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS = UPDATE_INTERVAL_IN_MILLISECONDS / 2
    var isLocationUpdatesCanceled = false

    private lateinit var googleApiClient: GoogleApiClient
    lateinit var locationRequest1: LocationRequest
    lateinit var locationSettingsRequest: LocationSettingsRequest

    var location: Location? = null
    var wasLocationStarted = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setUpLocationListeners()
    }

    override fun onResume() {
        super.onResume()
        resumeLocationUpdates()
    }

    private fun resumeLocationUpdates() {
        if (!wasLocationStarted) return
        setUpLocationListenersSettings()
        if (!googleApiClient.isConnected)
            googleApiClient.connect()
    }

    fun startLocationSearching() {
        if (googleApiClient.isConnected && wasLocationStarted) return
        wasLocationStarted = true
        resumeLocationUpdates()
    }

    fun stopLocationSearching() {
        if (!googleApiClient.isConnected && !wasLocationStarted) return
        wasLocationStarted = false
        stopLocationUpdates()
    }

    private fun setUpLocationListeners() {
        googleApiClient = GoogleApiClient.Builder(this@BaseLocationActivity)
                .addConnectionCallbacks(connectionCallbacks)
                .addOnConnectionFailedListener(connectionFailedListener)
                .addApi(LocationServices.API)
                .build()
        locationRequest1 = createLocationRequest()
        buildLocationSettingsRequest(locationRequest1)
    }

    private fun setUpLocationListenersSettings() {
        isLocationUpdatesCanceled = false
        val result = LocationServices.SettingsApi
                .checkLocationSettings(
                        googleApiClient,
                        locationSettingsRequest
                )
        result.setResultCallback(resultCallback)
    }

    private val resultCallback = ResultCallback<LocationSettingsResult> { p0 ->
        val status = p0.status
        when (status.statusCode) {
            LocationSettingsStatusCodes.SUCCESS -> {
                Timber.d("All location settings are satisfied.")
                startLocationUpdates()
            }
            LocationSettingsStatusCodes.RESOLUTION_REQUIRED -> {
                Timber.d("Location settings are not satisfied. Show the user a dialog to upgrade location settings ")
                if (isLocationUpdatesCanceled) return@ResultCallback
                try {
                    // Show the dialog by calling startResolutionForResult(), and check the result
                    // in onActivityResult().
                    status.startResolutionForResult(this@BaseLocationActivity, RC_LOCATION_SETTINGS)
                } catch (ex: IntentSender.SendIntentException) {
                    Timber.e(ex, "PendingIntent unable to execute request.")
                }

            }
            LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE ->
                Timber.d("Location settings are inadequate, and cannot be fixed here. Dialog not created.")
        }
    }

    @SuppressLint("MissingPermission")
    private fun startLocationUpdates() {
        try {
            LocationServices.FusedLocationApi.requestLocationUpdates(
                    googleApiClient,
                    locationRequest1,
                    locationListener
            )
        } catch (e: Exception) {
            Timber.e(e)
            Handler().postDelayed({ startLocationUpdates() }, 1000)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        if (requestCode == RC_LOCATION_SETTINGS)
            when (resultCode) {
                Activity.RESULT_OK -> {
                    Timber.d("User agreed to make required location settings changes.")
                    startLocationUpdates()
                    showGPSEnabledInView()
                }
                Activity.RESULT_CANCELED -> {
                    Timber.d("User chose not to make required location settings changes.")
                    showGPSDisabledInView()
                }
            }
    }

    abstract fun showGPSEnabledInView()

    abstract fun showGPSDisabledInView()

    private fun createLocationRequest(): LocationRequest {
        val locationRequest = LocationRequest()
        locationRequest.interval = UPDATE_INTERVAL_IN_MILLISECONDS
        locationRequest.fastestInterval = FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        return locationRequest
    }

    private fun buildLocationSettingsRequest(locationRequest: LocationRequest) {
        val builder = LocationSettingsRequest.Builder()
        builder.addLocationRequest(locationRequest)
        builder.setAlwaysShow(true)
        locationSettingsRequest = builder.build()
    }

    private val connectionCallbacks = object : GoogleApiClient.ConnectionCallbacks {
        override fun onConnected(var1: Bundle?) {
            Timber.d("onConnected")
        }

        override fun onConnectionSuspended(var1: Int) {
            Timber.d("onConnectionSuspended")
        }
    }

    private val connectionFailedListener = GoogleApiClient.OnConnectionFailedListener { p0 ->
        Timber.e(p0.errorMessage)
        GoogleApiAvailability.getInstance().getErrorDialog(this@BaseLocationActivity, p0.errorCode, RC_LOCATION_SETTINGS).show()
    }

    private val locationListener: LocationListener = LocationListener { location ->
        this.location = location
        val distance = FloatArray(2)

        Location.distanceBetween(location.latitude,
                location.longitude,
                ROLIQUE_POSITION.latitude,
                ROLIQUE_POSITION.longitude,
                distance)

        distance[0].toDouble()
        this@BaseLocationActivity.onLocationChanged(distance[0].toDouble() <= RANGE_RADIUS,distance[0].toDouble())
    }

    abstract fun onLocationChanged(isInRange: Boolean, distance: Double)

    override fun onPause() {
        stopLocationUpdates()
        super.onPause()
    }

    private fun stopLocationUpdates() {
        if (!googleApiClient.isConnected) return
        LocationServices.FusedLocationApi
                .removeLocationUpdates(googleApiClient, locationListener)
                .setResultCallback({ isLocationUpdatesCanceled = true })
        googleApiClient.disconnect()
    }

    override fun onStop() {
        super.onStop()
        if (googleApiClient.isConnected)
            googleApiClient.disconnect()
    }
}