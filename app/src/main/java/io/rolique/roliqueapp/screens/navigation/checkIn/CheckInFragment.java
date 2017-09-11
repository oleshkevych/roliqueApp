package io.rolique.roliqueapp.screens.navigation.checkIn;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v13.app.FragmentCompat;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.text.DecimalFormat;
import java.util.Arrays;

import io.rolique.roliqueapp.BaseFragment;
import io.rolique.roliqueapp.R;
import timber.log.Timber;


public class CheckInFragment extends BaseFragment {

    private static final int RC_LOCATION_PERMISSION = 101;

    static final LatLng ROLIQUE_POSITION = new LatLng(49.841358007066034, 24.023118875920773);
    static final int RANGE_RADIUS = 50;

    public static CheckInFragment newInstance() {
        return new CheckInFragment();
    }

    MapView mMapView;
    GoogleMap mGoogleMap;
    GPSTracker mGPSTracker;
    LatLng mLatStart;
    Circle mCircle;
    boolean mIsVisibleToUser;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_check_in, container, false);

        mMapView = rootView.findViewById(R.id.mapView);
        mMapView.onCreate(savedInstanceState);

        return rootView;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        mIsVisibleToUser = isVisibleToUser;
        toggleMapServices(isVisibleToUser);
    }

    private void toggleMapServices(boolean isVisibleToUser) {
        if (mMapView == null) return;
        if (isVisibleToUser) {
            if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestPermission();
                return;
            }
            startMapServices();
        } else {
            mMapView.onPause();
            if (mGPSTracker != null) mGPSTracker.stopUsingGPS();
        }
    }

    private void startMapServices() {
        setUtMap();
        mGPSTracker = new GPSTracker(getActivity(), mPositionChanged);
        if (mGPSTracker.canGetLocation) {
            mLatStart = new LatLng(mGPSTracker.getLatitude(), mGPSTracker.getLongitude());
            Timber.e("from resume " + mLatStart.toString());
        } else {
            mGPSTracker.showSettingsAlert();
        }
    }

    private void requestPermission() {
        if (FragmentCompat.shouldShowRequestPermissionRationale(CheckInFragment.this, Manifest.permission.ACCESS_FINE_LOCATION) &&
                FragmentCompat.shouldShowRequestPermissionRationale(CheckInFragment.this, Manifest.permission.ACCESS_COARSE_LOCATION)) {
            //TODO: Show OK/Cancel confirmation dialog about camera permission.
        } else {
            FragmentCompat.requestPermissions(CheckInFragment.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    RC_LOCATION_PERMISSION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == RC_LOCATION_PERMISSION) {
            if (grantResults.length != 2 || grantResults[0] != PackageManager.PERMISSION_GRANTED
                    || grantResults[1] != PackageManager.PERMISSION_GRANTED) {
                showSnackbar(getView(), "Give me permissions!!!!!!!!");
            } else {
                startMapServices();
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    GPSTracker.PositionChanged mPositionChanged = new GPSTracker.PositionChanged() {
        @Override
        public void onPositionChanged(Location location) {
            mLatStart = new LatLng(location.getLatitude(), location.getLongitude());
            Timber.e("From listener " + mLatStart.toString());
            float[] distance = new float[2];

            Location.distanceBetween(mLatStart.latitude,
                    mLatStart.longitude,
                    mCircle.getCenter().latitude,
                    mCircle.getCenter().longitude,
                    distance);

            calculationByDistance(mLatStart, mCircle.getCenter());

            if ( distance[0] <= mCircle.getRadius()) {
                showSnackbar(getView(), "In range " + Arrays.toString(distance));
            } else {
                showSnackbar(getView(), "Outside range " + Arrays.toString(distance));
            }
            Timber.e(Arrays.toString(distance));
        }
    };

    public boolean calculationByDistance(LatLng StartP, LatLng EndP) {
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

        return RADIUS * c <= RANGE_RADIUS;
    }

    private void setUtMap() {
        try {
            MapsInitializer.initialize(getActivity().getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }
        mMapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap mMap) {
                mGoogleMap = mMap;
                mGoogleMap.setMyLocationEnabled(true);

                mCircle = mGoogleMap.addCircle(new CircleOptions()
                        .center(ROLIQUE_POSITION)
                        .radius(RANGE_RADIUS)
                        .strokeColor(ContextCompat.getColor(mMapView.getContext(), R.color.green_700_alpha_50))
                        .fillColor(ContextCompat.getColor(mMapView.getContext(), R.color.green_700_alpha_80)));

                // For dropping a marker at a point on the Map
                mGoogleMap.addMarker(new MarkerOptions().position(ROLIQUE_POSITION).title("Rolique").snippet("Your lovely job"));

                // For zooming automatically to the location of the marker
                CameraPosition cameraPosition = new CameraPosition.Builder().target(ROLIQUE_POSITION).zoom(18).build();
                mGoogleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

            }
        });
        mMapView.onResume();
    }

    @Override
    protected void inject() {

    }

    @Override
    public void onResume() {
        super.onResume();
        toggleMapServices(mIsVisibleToUser);
    }

    @Override
    public void onPause() {
        toggleMapServices(!mIsVisibleToUser);
        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
    }
}
