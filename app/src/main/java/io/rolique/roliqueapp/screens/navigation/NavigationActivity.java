package io.rolique.roliqueapp.screens.navigation;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import io.rolique.cameralibrary.MediaLib;
import io.rolique.cameralibrary.data.model.MediaContent;
import io.rolique.roliqueapp.R;
import io.rolique.roliqueapp.RoliqueApplication;
import io.rolique.roliqueapp.RoliqueApplicationPreferences;
import io.rolique.roliqueapp.data.model.Media;
import io.rolique.roliqueapp.screens.BaseActivity;
import io.rolique.roliqueapp.screens.welcome.WelcomeActivity;
import io.rolique.roliqueapp.util.ui.UiUtil;

/**
 * Created by Volodymyr Oleshkevych on 8/22/2017.
 * Copyright (c) 2017, Rolique. All rights reserved.
 */
public class NavigationActivity extends BaseActivity implements NavigationContract.View {

    public static Intent startIntent(Context context) {
        Intent intent = new Intent(context, NavigationActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        return intent;
    }

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

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation);
        mNavigationImageSwitcher = mNavigationView.getHeaderView(0).findViewById(R.id.view_switcher);
        mNavigationViewSwitcher = mNavigationView.getHeaderView(0).findViewById(R.id.view_progress_switcher);
        mNavigationImageSwitcher.setOnClickListener(mOnImageClickListener);
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
                case R.id.menu_eat:
                    mToolbar.setTitle(R.string.fragment_eat_title);
                    mViewPager.setCurrentItem(FragmentViewPagerAdapter.Position.EAT, false);
                    break;
                case R.id.menu_check_in:
                    mToolbar.setTitle(R.string.fragment_check_in_title);
                    mViewPager.setCurrentItem(FragmentViewPagerAdapter.Position.CHECK_IN, false);
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
            if (menuItem.getItemId() == R.id.menu_chats) menuItem.setChecked(true);
        }
        mToolbar.setTitle(R.string.fragment_chats_title);
        mViewPager.setCurrentItem(FragmentViewPagerAdapter.Position.CHATS, false);
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
    public void onBackPressed() {
        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mPresenter.start();
    }

    @Override
    protected void onStop() {
        mPresenter.stop();
        super.onStop();
    }
}
