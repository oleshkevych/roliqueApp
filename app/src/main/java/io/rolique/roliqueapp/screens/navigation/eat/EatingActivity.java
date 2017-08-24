package io.rolique.roliqueapp.screens.navigation.eat;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.GravityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import butterknife.BindView;
import io.rolique.roliqueapp.R;
import io.rolique.roliqueapp.screens.navigation.NavigationActivity;

public class EatingActivity extends NavigationActivity {

    public static Intent startIntent(Context context) {
        Intent intent = new Intent(context, EatingActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_eating);
    }

    @Override
    protected void setUpToolbar() {
        mToolbar.setTitle(R.string.activity_eat_title);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDrawerLayout.openDrawer(GravityCompat.START);
            }
        });
        Menu menu = mNavigationView.getMenu();
        for (int i = 0; i < menu.size(); i++) {
            MenuItem menuItem = menu.getItem(i);
            if (menuItem.getItemId() == R.id.menu_eat) {
                menuItem.setChecked(true);
                break;
            }
        }
    }

    @Override
    protected void onLogOutClicked() {

    }

    @Override
    protected void inject() {

    }
}
