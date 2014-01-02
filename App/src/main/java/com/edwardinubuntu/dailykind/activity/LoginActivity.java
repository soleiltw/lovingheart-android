package com.edwardinubuntu.dailykind.activity;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.MenuItem;
import android.view.Window;
import com.edwardinubuntu.dailykind.R;
import com.edwardinubuntu.dailykind.fragment.UserLoginFragment;

/**
 * Created by edward_chiang on 2013/12/25.
 */
public class LoginActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().requestFeature(Window.FEATURE_ACTION_BAR_OVERLAY);

        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();

        setContentView(R.layout.activity_login_main);
    }



    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // User Login fragment
        android.support.v4.app.FragmentManager fragmentManager = getSupportFragmentManager();
        UserLoginFragment userLoginFragment = new UserLoginFragment();
        fragmentManager.beginTransaction().replace(R.id.login_main_fragment, userLoginFragment).commit();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: {
                finish();
                break;
            }
        }
        return super.onOptionsItemSelected(item);
    }
}
