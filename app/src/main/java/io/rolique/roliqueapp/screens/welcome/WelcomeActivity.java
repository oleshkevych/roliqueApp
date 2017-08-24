package io.rolique.roliqueapp.screens.welcome;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import io.rolique.roliqueapp.R;
import io.rolique.roliqueapp.screens.BaseActivity;
import io.rolique.roliqueapp.screens.welcome.fragments.welcome.WelcomeFragment;

public class WelcomeActivity extends BaseActivity {

    public static Intent startIntent(Context context) {
        Intent intent = new Intent(context, WelcomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        return intent;
    }

    public static String WELCOME_FRAGMENT_TAG = "WELCOME_FRAGMENT";

    private Fragment mWelcomeFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        mWelcomeFragment = WelcomeFragment.newInstance();
        getFragmentManager().beginTransaction()
                .add(R.id.fragments_container, mWelcomeFragment, WELCOME_FRAGMENT_TAG)
                .commit();
    }

    @Override
    protected void inject() {

    }

    @Override
    public void onBackPressed() {
        Fragment myFragment = getFragmentManager().findFragmentByTag(WELCOME_FRAGMENT_TAG);
        if (myFragment != null && myFragment.isVisible()) {
            super.onBackPressed();
        } else {
            getFragmentManager().beginTransaction()
                    .replace(R.id.fragments_container, mWelcomeFragment, WELCOME_FRAGMENT_TAG)
                    .commit();
        }
    }
}
