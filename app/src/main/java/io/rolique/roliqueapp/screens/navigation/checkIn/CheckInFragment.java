package io.rolique.roliqueapp.screens.navigation.checkIn;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import io.rolique.roliqueapp.BaseFragment;
import io.rolique.roliqueapp.R;


public class CheckInFragment extends BaseFragment {

    public static CheckInFragment newInstance() {
       return new CheckInFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_check_in, container, false);
    }

    @Override
    protected void inject() {

    }
}
