package com.edwardinubuntu.dailykind.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import com.edwardinubuntu.dailykind.*;
import com.edwardinubuntu.dailykind.fragment.*;
import com.parse.*;

import java.util.Locale;

public class MainActivity extends ActionBarActivity implements ActionBar.TabListener {

    private final static int VIEW_PAGER_HOME_POSITION = 0;
    private final static int VIEW_PAGER_ME_POSITION = 1;
    private final static int VIEW_PAGER_ACTIVITIES_POSITION = 2;
    private final static int VIEW_PAGER_NOW_POSITION = 3;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.edwardinubuntu.dailykind.R.layout.activity_main);

        Parse.initialize(this, ParseSettings.PARSE_API_TOKEN, ParseSettings.PARSE_API_TOKEN_2);

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
        // TODO Create user
        if (ParseUser.getCurrentUser() == null) {
            ParseUser user = new ParseUser();
            user.setUsername("Edward");
            user.setPassword("edward");
            user.setEmail("ed_jiang@yahoo.com.tw");
            user.signUpInBackground(new SignUpCallback() {
                @Override
                public void done(ParseException e) {

                }
            });
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
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        // When the given tab is selected, switch to the corresponding page in
        // the ViewPager.
        mViewPager.setCurrentItem(tab.getPosition());
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
                case VIEW_PAGER_NOW_POSITION:
                    return FeedsActivitiesFragment.newInstance(position + 1);
                case VIEW_PAGER_ACTIVITIES_POSITION:
                    return UserActivitiesFragment.newInstance(position + 1);
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
                case VIEW_PAGER_ACTIVITIES_POSITION:
                    return getString(com.edwardinubuntu.dailykind.R.string.title_activities).toUpperCase(l);
                case VIEW_PAGER_NOW_POSITION:
                    return getString(com.edwardinubuntu.dailykind.R.string.title_now).toUpperCase(l);
            }
            return null;
        }
    }

}
