package io.rolique.roliqueapp.screens.navigation;

import android.Manifest;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v13.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import io.rolique.cameralibrary.MediaLib;
import io.rolique.cameralibrary.data.model.MediaContent;
import io.rolique.roliqueapp.BuildConfig;
import io.rolique.roliqueapp.R;
import io.rolique.roliqueapp.RoliqueApplication;
import io.rolique.roliqueapp.RoliqueApplicationPreferences;
import io.rolique.roliqueapp.data.model.CheckIn;
import io.rolique.roliqueapp.data.model.Media;
import io.rolique.roliqueapp.screens.BaseActivity;
import io.rolique.roliqueapp.screens.welcome.WelcomeActivity;
import io.rolique.roliqueapp.services.gps.GPSTrackerService;
import io.rolique.roliqueapp.services.notification.NotificationService;
import io.rolique.roliqueapp.util.AlarmBuilder;
import io.rolique.roliqueapp.util.DateUtil;
import io.rolique.roliqueapp.util.ui.UiUtil;
import io.rolique.roliqueapp.widget.CheckInDialog;
import io.rolique.roliqueapp.widget.ReasonDialogFragment;

/**
 * Created by Volodymyr Oleshkevych on 8/22/2017.
 * Copyright (c) 2017, Rolique. All rights reserved.
 */
public class NavigationActivity extends BaseActivity implements NavigationContract.View {

    private static final String EXTRA_IS_FOR_CHECK_IN = "IS_FOR_CHECK_IN";

    public static Intent startIntent(Context context) {
        Intent intent = new Intent(context, NavigationActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        return intent;
    }

    public static Intent startIntent(Context context, boolean onCheckIn) {
        Intent intent = new Intent(context, NavigationActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        intent.putExtra(EXTRA_IS_FOR_CHECK_IN, onCheckIn);
        return intent;
    }

    public static final int RC_LOCATION_PERMISSION = 101;

    @BindView(R.id.drawer_layout) DrawerLayout mDrawerLayout;
    @BindView(R.id.navigation_view) NavigationView mNavigationView;
    @BindView(R.id.toolbar) Toolbar mToolbar;
    @BindView(R.id.view_pager) ViewPager mViewPager;

    @Inject NavigationPresenter mPresenter;
    @Inject RoliqueApplicationPreferences mPreferences;

    private TextView mNameTextView;
    private ViewSwitcher mNavigationImageSwitcher;
    private ViewSwitcher mNavigationViewSwitcher;
    private FragmentViewPagerAdapter mFragmentViewPagerAdapter;
    MediaLib mMediaLib;

    boolean mIsAlreadyShown;
    private GPSTrackerService mGPSTrackerService;
    boolean mIsInRange;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation);
        mNavigationImageSwitcher = mNavigationView.getHeaderView(0).findViewById(R.id.view_switcher);
        mNavigationViewSwitcher = mNavigationView.getHeaderView(0).findViewById(R.id.view_progress_switcher);
        mNavigationImageSwitcher.setOnClickListener(mOnImageClickListener);
        mNavigationViewSwitcher.setOnClickListener(mOnImageClickListener);
        mNameTextView = mNavigationView.getHeaderView(0).findViewById(R.id.text_view_name);
        mNavigationView.getHeaderView(0).findViewById(R.id.drawable_text_view_logout)
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        onLogOutClicked();
                    }
                });
        mPresenter.isLogin();
    }

    @Override
    protected void inject() {
        DaggerNavigationComponent.builder()
                .roliqueApplicationComponent(((RoliqueApplication) getApplication()).getRepositoryComponent())
                .navigationPresenterModule(new NavigationPresenterModule(NavigationActivity.this))
                .build()
                .inject(NavigationActivity.this);
    }

    private void onLogOutClicked() {
        mPresenter.logout();
    }

    @Override
    public void showLoginInView(boolean isLogin) {
        if (isLogin) {
            setUpFragments();
            setUpToolbar();
            setChatsSelected();
            mPresenter.checkIfUserCheckedIn();
            if (getIntent().getBooleanExtra(EXTRA_IS_FOR_CHECK_IN, false)) {
                setCheckInSelected();
            }
        } else {
            startActivity(WelcomeActivity.startIntent(NavigationActivity.this));
            finish();
        }
    }

    private void setUpFragments() {
        mFragmentViewPagerAdapter = new FragmentViewPagerAdapter(getFragmentManager());
        mViewPager.setAdapter(mFragmentViewPagerAdapter);
        mViewPager.setOffscreenPageLimit(4);
    }

    private void setUpToolbar() {
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDrawerLayout.openDrawer(GravityCompat.START);
            }
        });
        mNavigationView.setNavigationItemSelectedListener(mNavigationListener);
    }

    final NavigationView.OnNavigationItemSelectedListener mNavigationListener
            = new NavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            Menu menu = mNavigationView.getMenu();
            for (int i = 0; i < menu.size(); i++) {
                MenuItem menuItem = menu.getItem(i);
                if (menuItem.equals(item)) menuItem.setChecked(true);
                else menuItem.setChecked(false);
            }
            switch (item.getItemId()) {
                case R.id.menu_chats:
                    mToolbar.setTitle(R.string.fragment_chats_title);
                    mViewPager.setCurrentItem(FragmentViewPagerAdapter.Position.CHATS, false);
                    break;
                case R.id.menu_contacts:
                    mToolbar.setTitle(R.string.fragment_contacts_title);
                    mViewPager.setCurrentItem(FragmentViewPagerAdapter.Position.CONTACTS, false);
                    break;
//                case R.id.menu_eat:
//                    mToolbar.setSingle(R.string.fragment_eat_title);
//                    mViewPager.setCurrentItem(FragmentViewPagerAdapter.Position.EAT, false);
//                    break;
                case R.id.menu_settings:
                    mToolbar.setTitle(R.string.fragment_settings_title);
                    mViewPager.setCurrentItem(FragmentViewPagerAdapter.Position.SETTINGS, false);
                    break;
                case R.id.menu_check_in:
                    mToolbar.setTitle(R.string.fragment_check_in_title);
                    mViewPager.setCurrentItem(FragmentViewPagerAdapter.Position.CHECK_IN, false);
                    break;
                case R.id.menu_suggestions:
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse(BuildConfig.SUGGESTIONS_URL));
                    startActivity(intent);
                    break;
            }
            mDrawerLayout.closeDrawer(GravityCompat.START);
            return true;
        }
    };

    private void setChatsSelected() {
        Menu menu = mNavigationView.getMenu();
        for (int i = 0; i < menu.size(); i++) {
            MenuItem menuItem = menu.getItem(i);
            menuItem.setChecked(false);
            if (menuItem.getItemId() == R.id.menu_chats) menuItem.setChecked(true);
        }
        mToolbar.setTitle(R.string.fragment_chats_title);
        mViewPager.setCurrentItem(FragmentViewPagerAdapter.Position.CHATS, false);
    }

    private void setCheckInSelected() {
        Menu menu = mNavigationView.getMenu();
        for (int i = 0; i < menu.size(); i++) {
            MenuItem menuItem = menu.getItem(i);
            menuItem.setChecked(false);
            if (menuItem.getItemId() == R.id.menu_check_in) menuItem.setChecked(true);
        }
        mToolbar.setTitle(R.string.fragment_check_in_title);
        mViewPager.setCurrentItem(FragmentViewPagerAdapter.Position.CHECK_IN, false);
    }

    View.OnClickListener mOnImageClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (mMediaLib == null) {
                mMediaLib = new MediaLib(NavigationActivity.this, new MediaLib.MediaLibListener() {
                    @Override
                    public void onSuccess(List<MediaContent> mediaContents) {
                        Media media = new Media.Builder()
                                .setMediaType(Media.CATEGORY_IMAGE)
                                .setHeight(mediaContents.get(0).getHeight())
                                .setWidth(mediaContents.get(0).getWidth())
                                .setImageUrl(mediaContents.get(0).getImage())
                                .create();
                        media.setImageUrl(UiUtil.resizeImage(NavigationActivity.this, media.getImageUrl(), media.getWidth(), media.getHeight()));

                        mPresenter.updateUserPicture(media);
                    }

                    @Override
                    public void onEmpty() {

                    }

                    @Override
                    public void onError(Exception e) {

                    }
                });
                mMediaLib.setStorage(MediaLib.LOCAL_APP_FOLDER);
                mMediaLib.setFrontCamera(true);
                mMediaLib.setSelectableFlash(true);
                mMediaLib.setSinglePhoto(true);
            }
            mMediaLib.startCamera();
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (mMediaLib == null) return;
        mMediaLib.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void setImage(String path, String userName) {
        UiUtil.setImageIfExists(mNavigationImageSwitcher, path, userName, 88);
    }

    @Override
    public void setUserName(String userName) {
        mNameTextView.setText(String.format("%s %s", mPreferences.getFirstName(), mPreferences.getLastName()));
    }

    @Override
    public void setImageProgress(boolean isActive) {
        mNavigationViewSwitcher.setDisplayedChild(isActive ? 1 : 0);
    }

    @Override
    public void showCheckInStatusInView(boolean isCheckedIn) {
        toggleLocationService(!isCheckedIn);
    }

    @Override
    public void showCheckedInInView(String checkInType) {
        String text = getString(R.string.activity_navigation_check_in_successful);
        Toast.makeText(NavigationActivity.this, String.format("%s %s", text, checkInType), Toast.LENGTH_LONG).show();
        toggleLocationService(false);
    }

    @Override
    public void showConnectionErrorInView() {
        Toast.makeText(NavigationActivity.this, R.string.activity_navigation_lack_connection, Toast.LENGTH_LONG).show();
    }

    @Override
    public void updateAlarm(boolean isCheckedIn, String checkInTime, boolean isNotificationAllowed) {
        AlarmBuilder.setAlarm(getApplicationContext(), checkInTime, isCheckedIn, isNotificationAllowed);
    }

    @Override
    public void onBackPressed() {
        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
        } else if (mViewPager.getCurrentItem() != FragmentViewPagerAdapter.Position.CHATS) {
            setChatsSelected();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mPresenter.start();
        NotificationManager notificationmanager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        assert notificationmanager != null;
        notificationmanager.cancel(NotificationService.NOTIFICATION_ID);
    }

    @Override
    protected void onStop() {
        mPresenter.stop();
        if (mGPSTrackerService != null) mIsAlreadyShown = false;
        toggleLocationService(false);
        super.onStop();
    }

    protected void toggleLocationService(boolean isStart) {
        if (isStart && !mIsAlreadyShown) {
            if (ContextCompat.checkSelfPermission(NavigationActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(NavigationActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION)
                            != PackageManager.PERMISSION_GRANTED) {
                requestPermission();
                return;
            }
            if (mGPSTrackerService != null) return;
            mGPSTrackerService = new GPSTrackerService(NavigationActivity.this, mPositionChanged);

            if (!mGPSTrackerService.canGetLocation) mGPSTrackerService.showSettingsAlert();
        } else {
            if (mGPSTrackerService != null) mGPSTrackerService.stopUsingGPS();
            mGPSTrackerService = null;
        }
    }

    private void requestPermission() {
        if (!(ActivityCompat.shouldShowRequestPermissionRationale(NavigationActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) &&
                ActivityCompat.shouldShowRequestPermissionRationale(NavigationActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION))) {
            ActivityCompat.requestPermissions(NavigationActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    RC_LOCATION_PERMISSION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == RC_LOCATION_PERMISSION) {
            if (grantResults.length != 2 || grantResults[0] != PackageManager.PERMISSION_GRANTED
                    || grantResults[1] != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(NavigationActivity.this, "Give me permissions!!!!!!!!", Toast.LENGTH_LONG).show();
            } else {
                toggleLocationService(true);
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    GPSTrackerService.PositionChanged mPositionChanged = new GPSTrackerService.PositionChanged() {
        @Override
        public void onPositionChanged(boolean isInRange) {
            mIsInRange = isInRange;
            if (mIsInRange) showCheckInMessage();
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    showCheckInMessage();
                }
            }, 3000);
        }
    };

    void showCheckInMessage() {
        if (mIsAlreadyShown) return;
        mIsAlreadyShown = true;
        CheckInDialog checkInDialog = new CheckInDialog(NavigationActivity.this, mIsInRange, mOnCheckInAction);
        checkInDialog.show();
        toggleLocationService(false);
    }

    CheckInDialog.OnCheckInAction mOnCheckInAction = new CheckInDialog.OnCheckInAction() {
        @Override
        public void onCheckInClick(String type) {
            final CheckIn checkIn = new CheckIn(DateUtil.getStringTime(), type);
            if (type.equals(CheckIn.CHECK_IN) && DateUtil.isLate()) {
                ReasonDialogFragment fragment = ReasonDialogFragment.getFragment();
                fragment.show(getFragmentManager(), ReasonDialogFragment.TAG);
                fragment.setOnClickListener(new ReasonDialogFragment.OnClickListener() {
                    @Override
                    public void onButtonClick(String reason) {
                        if (reason.isEmpty()) sendCheckIn(checkIn);
                        checkIn.setReason(reason);
                        sendCheckIn(checkIn);
                    }
                });
            } else {
                sendCheckIn(checkIn);
            }
        }
    };

    protected void sendCheckIn(CheckIn checkIn) {
        mPresenter.setNewCheckIn(checkIn, new Date());
    }
}
