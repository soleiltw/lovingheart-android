package com.edwardinubuntu.dailykind.activity;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import com.crashlytics.android.Crashlytics;
import com.edwardinubuntu.dailykind.DailyKind;
import com.edwardinubuntu.dailykind.ParseSettings;
import com.edwardinubuntu.dailykind.R;
import com.edwardinubuntu.dailykind.fragment.*;
import com.parse.Parse;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;

public class MainActivity extends ActionBarActivity implements ActionBar.TabListener {

    private final static int VIEW_PAGER_HOME_POSITION = 0;
    private final static int VIEW_PAGER_ME_POSITION = 1;
    private final static int VIEW_PAGER_LATEST_POSITION = 2;
    private final static int VIEW_PAGER_POPULAR_POSITION = 3;

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    ViewPager mViewPager;

    private MediaPlayer tabClick;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.edwardinubuntu.dailykind.R.layout.activity_main);

        Parse.initialize(this, ParseSettings.PARSE_API_TOKEN, ParseSettings.PARSE_API_TOKEN_2);

        Crashlytics.start(this);

        // Set up the action bar.
        final ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(com.edwardinubuntu.dailykind.R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        // When swiping between different sections, select the corresponding
        // tab. We can also use ActionBar.Tab#select() to do this if we have
        // a reference to the Tab.
        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                actionBar.setSelectedNavigationItem(position);
                playLockSound();
            }
        });

        // For each of the sections in the app, add a tab to the action bar.
        for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
            // Create a tab with text corresponding to the page title defined by
            // the adapter. Also specify this Activity object, which implements
            // the TabListener interface, as the callback (listener) for when
            // this tab is selected.
            actionBar.addTab(
                    actionBar.newTab()
                            .setText(mSectionsPagerAdapter.getPageTitle(i))
                            .setTabListener(this));
        }

        tabClick = MediaPlayer.create(this, R.raw.lock_padlock);
    }

    private void printPackageInfo() {
        try {
            PackageInfo info = getPackageManager().getPackageInfo(
                    "com.edwardinubuntu.dailykind",
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
        getMenuInflater().inflate(com.edwardinubuntu.dailykind.R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case com.edwardinubuntu.dailykind.R.id.action_post: {
                Intent intent = new Intent(getApplicationContext(), PostStoryActivity.class);
                startActivity(intent);
                break;
            }
//            case R.id.action_ask_for_help: {
//                Intent intent = new Intent(getApplicationContext(), AskForHelpActivity.class);
//                startActivity(intent);
//                break;
//            }
//            case R.id.action_nearby:
//                Intent intent = new Intent(getApplicationContext(), NearbyActivity.class);
//                startActivity(intent);
//                break;
            case R.id.action_settings:
                Intent settingsIntent = new Intent(getApplicationContext(), SettingActivity.class);
                startActivity(settingsIntent);
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        // When the given tab is selected, switch to the corresponding page in
        // the ViewPager.
        mViewPager.setCurrentItem(tab.getPosition());
        invalidateOptionsMenu();
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            switch (position) {
                case VIEW_PAGER_HOME_POSITION:
                    return HomeFragment.newInstance(position + 1);
                case VIEW_PAGER_ME_POSITION:
                    return MeFragment.newInstance(position + 1);
                case VIEW_PAGER_POPULAR_POSITION:
                    return StoriesPopularFragment.newInstance(position + 1);
                case VIEW_PAGER_LATEST_POSITION:
                    return StoriesLatestFragment.newInstance(position + 1);
                default:
                    return PlaceholderFragment.newInstance(position + 1);
            }
        }

        @Override
        public int getCount() {
            // Show 4 total pages.
            return 4;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            Locale l = Locale.getDefault();
            switch (position) {
                case VIEW_PAGER_HOME_POSITION:
                    return getString(com.edwardinubuntu.dailykind.R.string.title_today).toUpperCase(l);
                case VIEW_PAGER_ME_POSITION:
                    return getString(com.edwardinubuntu.dailykind.R.string.title_me).toUpperCase(l);
                case VIEW_PAGER_LATEST_POSITION:
                    return getString(com.edwardinubuntu.dailykind.R.string.title_latest_activities).toUpperCase(l);
                case VIEW_PAGER_POPULAR_POSITION:
                    return getString(com.edwardinubuntu.dailykind.R.string.title_popular_activities).toUpperCase(l);
            }
            return null;
        }
    }

    private void playLockSound() {
        //PLAY SOUND HERE
        AudioManager am = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
        if  (am.getRingerMode() == AudioManager.RINGER_MODE_NORMAL) {

            tabClick.setLooping(false);
            tabClick.start();
        }
    }
}
