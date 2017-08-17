package io.rolique.roliqueapp.screens.main;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.google.firebase.auth.FirebaseAuth;

import javax.inject.Inject;

import io.rolique.roliqueapp.R;
import io.rolique.roliqueapp.RoliqueApplication;
import io.rolique.roliqueapp.RoliqueApplicationPreferences;
import io.rolique.roliqueapp.screens.BaseActivity;
import io.rolique.roliqueapp.screens.login.LoginActivity;

public class MainActivity extends BaseActivity implements MainContract.View {

    public static Intent startIntent(Context context) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        return intent;
    }

    FirebaseAuth mAuth;

    @Inject MainPresenter mPresenter;
    @Inject RoliqueApplicationPreferences mPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mAuth = FirebaseAuth.getInstance();
        if(!isLogin()) {
            startActivity(LoginActivity.startIntent(MainActivity.this));
            finish();
        }
        showSnackbar("Logged in successfully");
        Button button = (Button) findViewById(R.id.button_logout);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mAuth.signOut();
                mPreferences.logOut();
                startActivity(LoginActivity.startIntent(MainActivity.this));
                finish();
            }
        });

    }

    private boolean isLogin() {
        return mAuth.getCurrentUser() != null;
    }

    @Override
    protected void inject() {
        DaggerMainComponent.builder()
                .roliqueApplicationComponent(((RoliqueApplication) getApplication()).getRepositoryComponent())
                .mainPresenterModule(new MainPresenterModule(MainActivity.this))
                .build()
                .inject(MainActivity.this);
    }
}
