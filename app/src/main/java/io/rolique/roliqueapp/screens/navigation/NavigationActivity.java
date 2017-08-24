package io.rolique.roliqueapp.screens.navigation;

import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import butterknife.BindView;
import io.rolique.roliqueapp.R;
import io.rolique.roliqueapp.screens.BaseActivity;
import io.rolique.roliqueapp.screens.navigation.chat.ChatsActivity;
import io.rolique.roliqueapp.screens.navigation.contacts.ContactsActivity;
import io.rolique.roliqueapp.screens.navigation.eat.EatingActivity;

/**
 * Created by Volodymyr Oleshkevych on 8/22/2017.
 * Copyright (c) 2017, Rolique. All rights reserved.
 */
public abstract class NavigationActivity extends BaseActivity {

    @BindView(R.id.drawer_layout) protected DrawerLayout mDrawerLayout;
    @BindView(R.id.navigation_view) protected NavigationView mNavigationView;
    @BindView(R.id.container) FrameLayout mContainer;
    protected TextView mNameTextView;
    protected Toolbar mToolbar;
    protected ImageView mNavigationImageView;

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
    }

    @Override
    public void setContentView(@LayoutRes int layoutResID) {
        if (layoutResID == R.layout.activity_navigation) {
            super.setContentView(layoutResID);
            setUpNavigationView();
        } else {
            LayoutInflater.from(NavigationActivity.this).inflate(layoutResID, mContainer);
            mToolbar = getViewById(R.id.toolbar);
            setUpToolbar();
        }
    }

    private void setUpNavigationView() {
        mNavigationView.setNavigationItemSelectedListener(mNavigationListener);
    }

    final NavigationView.OnNavigationItemSelectedListener mNavigationListener
            = new NavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.menu_chats:
                    startActivity(ChatsActivity.startIntent(NavigationActivity.this));
                    break;
                case R.id.menu_contacts:
                    startActivity(ContactsActivity.startIntent(NavigationActivity.this));
                    break;
                case R.id.menu_eat:
                    startActivity(EatingActivity.startIntent(NavigationActivity.this));
                    break;
                case R.id.menu_check_in:
                    showCheckInFragment();
                    break;
            }
            mDrawerLayout.closeDrawer(GravityCompat.START);
            return true;
        }
    };

    private void showCheckInFragment() {
        //TODO: make check in fragment
    }

    protected abstract void setUpToolbar();

    protected abstract void onLogOutClicked();

    @Override
    public void onBackPressed() {
        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}
