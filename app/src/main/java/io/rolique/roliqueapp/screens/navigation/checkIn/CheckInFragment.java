package io.rolique.roliqueapp.screens.navigation.checkIn;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v13.app.FragmentCompat;
import android.support.v4.content.ContextCompat;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.PopupWindow;
import android.widget.TextView;

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
import java.util.Date;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.OnClick;
import io.rolique.roliqueapp.BaseFragment;
import io.rolique.roliqueapp.R;
import io.rolique.roliqueapp.RoliqueApplication;
import io.rolique.roliqueapp.data.model.CheckIn;
import io.rolique.roliqueapp.screens.timesheetViewer.TimesheetViewerActivity;
import io.rolique.roliqueapp.util.DateUtil;
import timber.log.Timber;


public class CheckInFragment extends BaseFragment implements CheckInContract.View {

    private static final int RC_LOCATION_PERMISSION = 101;

    static final LatLng ROLIQUE_POSITION = new LatLng(49.841358007066034, 24.023118875920773);
    static final int RANGE_RADIUS = 50;

    public static CheckInFragment newInstance() {
        return new CheckInFragment();
    }

    @Inject CheckInPresenter mPresenter;

    GoogleMap mGoogleMap;
    GPSTracker mGPSTracker;
    LatLng mLatStart;
    Circle mCircle;
    boolean mIsVisibleToUser;
    boolean mIsPopUpShowing;
    boolean mIsInRange;
    PopupWindow mPopupWindow;
    View checkInTextView;

    @BindView(R.id.button_check_in) FloatingActionButton mCheckInButton;
    @BindView(R.id.map_view) MapView mMapView;
    @BindView(R.id.container) CoordinatorLayout mCoordinatorLayout;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_check_in, container, false);
        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mMapView.onCreate(savedInstanceState);
        updateCheckInButton();
        setUpPopUpView(mCheckInButton);
    }

    @Override
    protected void inject() {
        DaggerCheckInComponent.builder()
                .roliqueApplicationComponent(((RoliqueApplication) getActivity().getApplication()).getRepositoryComponent())
                .checkInPresenterModule(new CheckInPresenterModule(CheckInFragment.this))
                .build()
                .inject(CheckInFragment.this);
    }

    private void setUpPopUpView(View v) {
        final View popupView = LayoutInflater.from(v.getContext()).inflate(R.layout.content_check_in_popup, null);
        mPopupWindow = new PopupWindow(popupView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        mPopupWindow.setBackgroundDrawable(ContextCompat.getDrawable(v.getContext(), R.drawable.shape_text_view_message_alien_single));
        mPopupWindow.setOutsideTouchable(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            mPopupWindow.setAttachedInDecor(true);
        }
        mPopupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        mPopupWindow.setAnimationStyle(R.style.popupCheckInAnimation);
        mPopupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mIsPopUpShowing = false;
                        mCheckInButton.setEnabled(true);
                    }
                }, 500);
            }
        });
        checkInTextView = popupView.findViewById(R.id.text_view_check_in);
        View businessTextView = popupView.findViewById(R.id.text_view_business);
        businessTextView.setOnTouchListener(mOnTouchListener);
        businessTextView.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                createAndSendCheckIn(CheckIn.BUSINESS_TRIP);
            }
        });
        View remotelyTextView = popupView.findViewById(R.id.text_view_remotely);
        remotelyTextView.setOnTouchListener(mOnTouchListener);
        remotelyTextView.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                createAndSendCheckIn(CheckIn.REMOTELY);
            }
        });
        View dayOffInTextView = popupView.findViewById(R.id.text_view_day_off);
        dayOffInTextView.setOnTouchListener(mOnTouchListener);
        dayOffInTextView.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                createAndSendCheckIn(CheckIn.DAY_OFF);
            }
        });
    }

    View.OnTouchListener mOnTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(final View v, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    v.setBackground(ContextCompat.getDrawable(v.getContext(), R.drawable.shape_text_view_check_in_toched));
                    ((TextView) v).setTextColor(ContextCompat.getColor(v.getContext(),R.color.black_alpha_50));
                    break;
                case MotionEvent.ACTION_UP:
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            v.setBackground(ContextCompat.getDrawable(v.getContext(), R.drawable.shape_text_view_message_user_single));
                            ((TextView) v).setTextColor(ContextCompat.getColor(v.getContext(),R.color.white));
                        }
                    }, 100);
                    break;
            }
            return false;
        }
    };

    void createAndSendCheckIn(@CheckIn.Type String type) {
        CheckIn checkIn = new CheckIn(DateUtil.getStringTime(), type);
        mPresenter.setNewCheckIn(checkIn, new Date());
        mPopupWindow.dismiss();
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        mIsVisibleToUser = isVisibleToUser;
        toggleMapServices(isVisibleToUser);
        if (mIsVisibleToUser && mPresenter != null)
        mPresenter.isUserAlreadyCheckedIn(new Date());
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
        if (!(FragmentCompat.shouldShowRequestPermissionRationale(CheckInFragment.this, Manifest.permission.ACCESS_FINE_LOCATION) &&
                FragmentCompat.shouldShowRequestPermissionRationale(CheckInFragment.this, Manifest.permission.ACCESS_COARSE_LOCATION))) {
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
                showSnackbar(mCoordinatorLayout, "Give me permissions!!!!!!!!");
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

            if (distance[0] <= mCircle.getRadius()) {
                showSnackbar(mCoordinatorLayout, "In range ");
                mIsInRange = true;
            } else {
                showSnackbar(mCoordinatorLayout, "Outside range ");
                mIsInRange = false;
            }
            Timber.e(Arrays.toString(distance));
            updateCheckInButton();
        }
    };

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
                        .fillColor(ContextCompat.getColor(mMapView.getContext(), R.color.green_700_alpha_90)));

                // For dropping a marker at a point on the Map
                mGoogleMap.addMarker(new MarkerOptions().position(ROLIQUE_POSITION).title("Rolique").snippet("Your lovely job"));

                // For zooming automatically to the location of the marker
                CameraPosition cameraPosition = new CameraPosition.Builder().target(ROLIQUE_POSITION).zoom(18).build();
                mGoogleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

            }
        });
        mMapView.onResume();
    }

    @OnClick(R.id.button_timesheet)
    void onTimeSheetClick() {
        startActivity(TimesheetViewerActivity.startIntent(getActivity()));
    }

    @OnClick(R.id.button_check_in)
    void onCheckInClick(View view) {
        if (mIsPopUpShowing) {
            mIsPopUpShowing = false;
            mPopupWindow.dismiss();
        } else {
            updateCheckInView();
            mCheckInButton.setEnabled(false);
            Point mDisplaySize = new Point();
            getActivity().getWindowManager().getDefaultDisplay().getSize(mDisplaySize);
            mIsPopUpShowing = true;
            int yOffset = (-1) * (mDisplaySize.y - (int) view.getY()) +
                    (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 80, getResources().getDisplayMetrics());
            int xOffset = (-1) * (mDisplaySize.x - (int) view.getX()) - view.getWidth() -
                    (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16, getResources().getDisplayMetrics());
            mPopupWindow.showAsDropDown(view, xOffset, yOffset);
        }
    }

    private void updateCheckInView() {
        if(mIsInRange) {
            checkInTextView.setOnTouchListener(mOnTouchListener);
            checkInTextView.setOnClickListener(new Button.OnClickListener() {
                @Override
                public void onClick(View v) {
                    createAndSendCheckIn(CheckIn.CHECK_IN);
                }
            });
        }
        checkInTextView.setAlpha(mIsInRange ? 1.0f : 0.5f);
        checkInTextView.setEnabled(mIsInRange);
    }

    boolean mIsAlreadyCheckedIn;

    @Override
    public void updateCheckInInView(boolean isCheckedIn) {
        mIsAlreadyCheckedIn = isCheckedIn;
        updateCheckInButton();
    }

    private void updateCheckInButton() {
        if (mIsAlreadyCheckedIn) {
            mCheckInButton.setEnabled(false);
            mCheckInButton.setAlpha(0.6f);
            return;
        }
        mCheckInButton.setEnabled(mIsInRange);
        mCheckInButton.setAlpha(mIsInRange ? 1.0f : 0.6f);
    }

    @Override
    public void onResume() {
        super.onResume();
        toggleMapServices(mIsVisibleToUser);
    }

    @Override
    public void onPause() {
        toggleMapServices(false);
        super.onPause();
    }

    @Override
    public void onDestroy() {
        if (mMapView != null)
        mMapView.onDestroy();
        super.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
    }
}
