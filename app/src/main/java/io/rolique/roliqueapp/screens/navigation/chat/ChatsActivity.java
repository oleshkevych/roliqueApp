package io.rolique.roliqueapp.screens.navigation.chat;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.GravityCompat;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import javax.inject.Inject;

import io.rolique.roliqueapp.R;
import io.rolique.roliqueapp.RoliqueApplication;
import io.rolique.roliqueapp.RoliqueApplicationPreferences;
import io.rolique.roliqueapp.screens.login.LoginActivity;
import io.rolique.roliqueapp.screens.navigation.NavigationActivity;
import io.rolique.roliqueapp.util.ui.UiUtil;

public class ChatsActivity extends NavigationActivity implements ChatsContract.View {

    public static Intent startIntent(Context context) {
        Intent intent = new Intent(context, ChatsActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        return intent;
    }

    @Inject ChatsPresenter mPresenter;
    @Inject RoliqueApplicationPreferences mPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chats);
        mPresenter.isLogin();
    }

    @Override
    protected void inject() {
        DaggerChatsComponent.builder()
                .roliqueApplicationComponent(((RoliqueApplication) getApplication()).getRepositoryComponent())
                .chatsPresenterModule(new ChatsPresenterModule(ChatsActivity.this))
                .build()
                .inject(ChatsActivity.this);
    }

    @Override
    protected void setUpToolbar() {
        mNameTextView.setText(String.format("%s %s", mPreferences.getFirstName(), mPreferences.getLastName()));
        mToolbar.setTitle(R.string.activity_chats_title);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDrawerLayout.openDrawer(GravityCompat.START);
            }
        });
        Menu menu = mNavigationView.getMenu();
        for (int i = 0; i < menu.size(); i++) {
            MenuItem menuItem = menu.getItem(i);
            if (menuItem.getItemId() == R.id.menu_chats) {
                menuItem.setChecked(true);
                break;
            }
        }
    }

    @Override
    protected void onLogOutClicked() {
        mPresenter.logout();
    }

    @Override
    public void showLoginInView(boolean isLogin) {
        if (isLogin) {
            showSnackbar("Logged in successfully");
        } else {
            startActivity(LoginActivity.startIntent(ChatsActivity.this));
            finish();
        }
    }

    @Override
    public void setImage(String path) {
        UiUtil.setImage(mNavigationImageView, path);
    }
}
