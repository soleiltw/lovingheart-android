package com.edwardinubuntu.dailykind.activity;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import com.edwardinubuntu.dailykind.R;
import com.edwardinubuntu.dailykind.fragment.DeedCategoriesFragment;

/**
 * Created by edward_chiang on 2013/11/23.
 */
public class DeedCategoriesActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_deed_categories_main);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayUseLogoEnabled(true);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // User Login fragment
        android.support.v4.app.FragmentManager fragmentManager = getSupportFragmentManager();
        DeedCategoriesFragment deedCategoriesFragment = new DeedCategoriesFragment();
        fragmentManager.beginTransaction().replace(R.id.deed_categories_main_fragment, deedCategoriesFragment).commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.categories, menu);
        return true;
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
