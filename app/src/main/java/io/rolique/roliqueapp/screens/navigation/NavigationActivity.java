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
import android.widget.ImageView;
import android.widget.TextView;

import javax.inject.Inject;

import butterknife.BindView;
import io.rolique.roliqueapp.R;
import io.rolique.roliqueapp.RoliqueApplication;
import io.rolique.roliqueapp.RoliqueApplicationPreferences;
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
    private ImageView mNavigationImageView;
    private FragmentViewPagerAdapter mFragmentViewPagerAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation);
        mNavigationImageView =  mNavigationView.getHeaderView(0).findViewById(R.id.image_view);
        mNameTextView =  mNavigationView.getHeaderView(0).findViewById(R.id.text_view_name);
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
        mNameTextView.setText(String.format("%s %s", mPreferences.getFirstName(), mPreferences.getLastName()));
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

    @Override
    public void setImage(String path) {
        UiUtil.setImage(mNavigationImageView, path);
    }

    @Override
    public void onBackPressed() {
        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}
