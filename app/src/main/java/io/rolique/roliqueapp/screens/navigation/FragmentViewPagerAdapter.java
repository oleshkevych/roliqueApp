package io.rolique.roliqueapp.screens.navigation;

import android.app.Fragment;
import android.app.FragmentManager;
import android.support.v13.app.FragmentPagerAdapter;

import java.util.ArrayList;
import java.util.List;

import io.rolique.roliqueapp.screens.navigation.chats.ChatsFragment;
import io.rolique.roliqueapp.screens.navigation.checkIn.CheckInFragment;
import io.rolique.roliqueapp.screens.navigation.contacts.ContactsFragment;
import io.rolique.roliqueapp.screens.navigation.eat.EatingFragment;
import io.rolique.roliqueapp.screens.navigation.settings.SettingsFragment;

/**
 * Created by Victor Artemyev on 15/01/2017.
 * Copyright (c) 2017, Rolique. All rights reserved.
 */

class FragmentViewPagerAdapter extends FragmentPagerAdapter {

    final static class Position {
        static final int CHATS = 0;
        static final int EAT = 1;
        static final int CONTACTS = 2;
        static final int CHECK_IN = 3;
        static final int SETTINGS = 4;
    }

    public List<Fragment> getFragments() {
        return mFragments;
    }

    private final List<Fragment> mFragments = new ArrayList<>(5);

    {
        mFragments.add(ChatsFragment.newInstance());
        mFragments.add(EatingFragment.newInstance());
        mFragments.add(ContactsFragment.newInstance());
        mFragments.add(CheckInFragment.newInstance());
        mFragments.add(SettingsFragment.newInstance());
    }

    FragmentViewPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        return mFragments.get(position);
    }

    @Override
    public int getCount() {
        return mFragments.size();
    }
}
