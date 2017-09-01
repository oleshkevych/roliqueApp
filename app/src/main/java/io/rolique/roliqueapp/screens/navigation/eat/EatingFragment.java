package io.rolique.roliqueapp.screens.navigation.eat;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import io.rolique.roliqueapp.BaseFragment;
import io.rolique.roliqueapp.R;

public class EatingFragment extends BaseFragment {

    public static Fragment newInstance() {
        return new EatingFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_eating, container, false);
    }

    @Override
    protected void inject() {

    }
}
