package com.edwardinubuntu.dailykind.activity;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.MenuItem;
import com.edwardinubuntu.dailykind.DailyKind;
import com.edwardinubuntu.dailykind.R;
import com.edwardinubuntu.dailykind.fragment.UserProfileBasicFragment;

/**
 * Created by edward_chiang on 2014/2/1.
 */
public class UserProfileActivity extends ActionBarActivity {

    private String userId;

    private UserProfileBasicFragment userProfileBasicFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_user_profile);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayUseLogoEnabled(true);

        userId = getIntent().getStringExtra("userId");

        Log.d(DailyKind.TAG, "user ID: " + userId);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // User Login fragment
        android.support.v4.app.FragmentManager fragmentManager = getSupportFragmentManager();
        userProfileBasicFragment = new UserProfileBasicFragment(userId);
        fragmentManager.beginTransaction().replace(R.id.user_profile_main_fragment, userProfileBasicFragment).commit();
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
