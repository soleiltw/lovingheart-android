package com.edwardinubuntu.dailykind.activity;

import android.app.ActionBar;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import com.edwardinubuntu.dailykind.DailyKind;
import com.edwardinubuntu.dailykind.R;
import com.edwardinubuntu.dailykind.fragment.*;
import com.edwardinubuntu.dailykind.util.CheckUserLoginUtil;
import com.parse.ParseAnalytics;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MainActivity extends ActionBarActivity implements NavigationDrawerFragment.NavigationDrawerCallbacks {

    public final static int VIEW_PAGER_HOME_POSITION = 1;
    public final static int VIEW_PAGER_ME_POSITION = 0;
    public final static int VIEW_PAGER_STORIES_POSITION = 2;

    private NavigationDrawerFragment mNavigationDrawerFragment;

    private CharSequence contentTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.edwardinubuntu.dailykind.R.layout.activity_main);
        ParseAnalytics.trackAppOpened(getIntent());
        printPackageInfo();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(R.id.navigation_drawer,
                (DrawerLayout)findViewById(R.id.drawer_layout));

    }

    private void printPackageInfo() {
        try {
            PackageInfo info = getPackageManager().getPackageInfo(
                    this.getPackageName(),
                    PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.d(DailyKind.TAG, "KeyHash:" + Base64.encodeToString(md.digest(), Base64.DEFAULT));
            }
        } catch (PackageManager.NameNotFoundException e) {
            Log.d(DailyKind.TAG, e.getLocalizedMessage());

        } catch (NoSuchAlgorithmException e) {
            Log.d(DailyKind.TAG, e.getLocalizedMessage());
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        if (mNavigationDrawerFragment!= null && mNavigationDrawerFragment.isDrawerOpen()) {
            getMenuInflater().inflate(R.menu.global, menu);
        } else {
            getMenuInflater().inflate(com.edwardinubuntu.dailykind.R.menu.main, menu);
        }
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case com.edwardinubuntu.dailykind.R.id.action_post: {
                if (CheckUserLoginUtil.hasLogin()) {
                    Intent intent = new Intent(getApplicationContext(), PostStoryActivity.class);
                    startActivity(intent);
                } else {
                    CheckUserLoginUtil.askLoginDialog(MainActivity.this, MainActivity.this);
                }
                break;
            }
            case R.id.action_settings:
                Intent settingsIntent = new Intent(getApplicationContext(), SettingActivity.class);
                startActivity(settingsIntent);
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Called when an item in the navigation drawer is selected.
     *
     * @param position
     */
    @Override
    public void onNavigationDrawerItemSelected(int position) {
        Fragment fragment;
        switch (position) {
            case VIEW_PAGER_HOME_POSITION:
                fragment =  HomeFragment.newInstance(position + 1);
                contentTitle = getString(R.string.title_today);
                getActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
                getActionBar().setDisplayShowTitleEnabled(true);
                break;
            case VIEW_PAGER_ME_POSITION:
                fragment = MeFragment.newInstance(position + 1);
                contentTitle = getString(R.string.title_me);
                getActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
                getActionBar().setDisplayShowTitleEnabled(true);
                break;
            case VIEW_PAGER_STORIES_POSITION:
                fragment =  StoriesLatestFragment.newInstance(position + 1);
                contentTitle = getString(R.string.title_stories);
                getActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
                getActionBar().setDisplayShowTitleEnabled(false);
                ArrayAdapter<String> dropDownAdapter = new ArrayAdapter<String>(MainActivity.this, R.layout.navigation_spinner_item,
                        new String[]{getString(R.string.title_latest_activities),
                                getString(R.string.title_popular_activities)
                        });
                dropDownAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

                getActionBar().setListNavigationCallbacks(dropDownAdapter, new ActionBar.OnNavigationListener() {
                    /**
                     * This method is called whenever a navigation item in your action bar
                     * is selected.
                     *
                     * @param itemPosition Position of the item clicked.
                     * @param itemId       ID of the item clicked.
                     * @return True if the event was handled, false otherwise.
                     */
                    @Override
                    public boolean onNavigationItemSelected(int itemPosition, long itemId) {
                        switch (itemPosition) {
                            case 0: {
                                FragmentManager fragmentManager = getSupportFragmentManager();
                                fragmentManager.beginTransaction().replace(R.id.container, StoriesLatestFragment.newInstance(itemPosition + 1)).commit();
                                break;
                            }
                            case 1: {
                                FragmentManager fragmentManager = getSupportFragmentManager();
                                fragmentManager.beginTransaction().replace(R.id.container, StoriesPopularFragment.newInstance(itemPosition + 1)).commit();
                                break;
                            }
                        }
                        return true;
                    }
                });
                break;
            default:
                fragment =  PlaceholderFragment.newInstance(position + 1);
                getActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
                getActionBar().setDisplayShowTitleEnabled(true);
                break;
        }
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.container, fragment).commit();


    }

    @Override
    public void onNavigationDrawerOpened(View drawerView) {
        getActionBar().setTitle(R.string.app_name);

        boolean isNeedUpdate = PreferenceManager.getDefaultSharedPreferences(this).getBoolean(DailyKind.NEED_UPDATE_DRAWER, false);
        if (isNeedUpdate) {
            mNavigationDrawerFragment.getDrawerListAdapter().notifyDataSetChanged();
            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(this).edit();
            editor.putBoolean(DailyKind.NEED_UPDATE_DRAWER, false);
            editor.commit();
        }
    }

    @Override
    public void onNavigationDrawerClosed(View drawerView) {
        getActionBar().setTitle(contentTitle);
    }
}
