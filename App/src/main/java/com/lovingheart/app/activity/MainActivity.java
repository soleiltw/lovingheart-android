package com.lovingheart.app.activity;

import android.app.ActionBar;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import com.lovingheart.app.DailyKind;
import com.lovingheart.app.R;
import com.lovingheart.app.fragment.*;
import com.lovingheart.app.object.Category;
import com.lovingheart.app.util.CheckUserLoginUtil;
import com.parse.*;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends FragmentActivity implements NavigationDrawerFragment.NavigationDrawerCallbacks {

    public final static int VIEW_PAGER_HOME_POSITION = 1;
    public final static int VIEW_PAGER_ME_POSITION = 0;
    public final static int VIEW_PAGER_STORIES_POSITION = 3;
    public final static int VIEW_PAGER_GOOD_DEEDS_POSITION = 2;

    private NavigationDrawerFragment mNavigationDrawerFragment;

    private CharSequence contentTitle;

    private int navigationMode;

    private ArrayAdapter<String> storiesDropDownAdapter;

    private ArrayAdapter<String> ideaCategoryDropDownAdapter;

    private List<String> ideaCategoryList;

    private List<Category> categoryList;

    private Fragment fragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.lovingheart.app.R.layout.activity_main);

        if (getIntent()!=null) {
            ParseAnalytics.trackAppOpened(getIntent());
        }
        printPackageInfo();

        storiesDropDownAdapter = new ArrayAdapter<String>(MainActivity.this, R.layout.navigation_spinner_item,
                new String[]{getString(R.string.title_latest_activities),
                        getString(R.string.title_popular_activities)
                });
        storiesDropDownAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        ideaCategoryList = new ArrayList<String>();
        categoryList = new ArrayList<Category>();

        ideaCategoryDropDownAdapter = new ArrayAdapter<String>(MainActivity.this, R.layout.navigation_spinner_item,
                ideaCategoryList);
        ideaCategoryDropDownAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(R.id.navigation_drawer,
                (DrawerLayout)findViewById(R.id.drawer_layout));

        loadCategories();
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
            getMenuInflater().inflate(com.lovingheart.app.R.menu.main, menu);
        }
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(fragment != null){
            fragment.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case com.lovingheart.app.R.id.action_post: {
                if (CheckUserLoginUtil.hasLogin()) {
                    Intent intent = new Intent(getApplicationContext(), PostStoryActivity.class);
                    startActivity(intent);
                } else {
                    CheckUserLoginUtil.askLoginDialog(MainActivity.this, MainActivity.this);
                }
                break;
            }
            case R.id.action_reload:
                loadCategories();
                break;
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

        ActionBar actionBar = getActionBar();


        switch (position) {
            case VIEW_PAGER_HOME_POSITION:
                fragment =  HomeFragment.newInstance(position + 1);
                contentTitle = getString(R.string.title_today);
                navigationMode = ActionBar.NAVIGATION_MODE_STANDARD;
                actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
                actionBar.setDisplayShowTitleEnabled(true);
                break;
            case VIEW_PAGER_ME_POSITION:
                fragment = UserProfileMainFragment.newInstance(position + 1);
                contentTitle = getString(R.string.title_me);
                navigationMode = ActionBar.NAVIGATION_MODE_STANDARD;
                actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
                actionBar.setDisplayShowTitleEnabled(true);
                break;
            case VIEW_PAGER_GOOD_DEEDS_POSITION:
                fragment = DeedIdeaListFragment.newInstance(99);
                contentTitle = getString(R.string.activity_deed_category);
                navigationMode = ActionBar.NAVIGATION_MODE_LIST;
                actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
                actionBar.setDisplayShowTitleEnabled(false);

                loadCategories();

                actionBar.setListNavigationCallbacks(ideaCategoryDropDownAdapter, new ActionBar.OnNavigationListener(){
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
                            case  0: {
                                // All
                                DeedIdeaListFragment fragment = DeedIdeaListFragment.newInstance(99);
                                FragmentManager fragmentManager = getSupportFragmentManager();
                                fragmentManager.beginTransaction().replace(R.id.container,
                                        fragment).commit();
                                break;
                            }
                            default:
                                int index = itemPosition - 1;
                                Category selectCategory = categoryList.get(index);
                                FragmentManager fragmentManager = getSupportFragmentManager();
                                DeedIdeaListFragment deedIdeaListFragment = DeedIdeaListFragment.newInstance(itemPosition + 1);
                                deedIdeaListFragment.setQueryCategory(selectCategory);
                                fragmentManager.beginTransaction().replace(R.id.container,
                                        deedIdeaListFragment ).commit();
                                break;
                        }
                        return true;
                    }
                });

                break;
            case VIEW_PAGER_STORIES_POSITION:
                fragment =  StoriesLatestFragment.newInstance(position + 1);
                contentTitle = getString(R.string.title_stories);
                navigationMode = ActionBar.NAVIGATION_MODE_LIST;
                actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
                actionBar.setDisplayShowTitleEnabled(false);

                actionBar.setListNavigationCallbacks(storiesDropDownAdapter, new ActionBar.OnNavigationListener() {
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
                navigationMode = ActionBar.NAVIGATION_MODE_STANDARD;
                getActionBar().setDisplayShowTitleEnabled(true);
                break;
        }
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.container, fragment).commit();


    }

    @Override
    public void onNavigationDrawerOpened(View drawerView) {
        getActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        getActionBar().setTitle(R.string.app_name);
        getActionBar().setDisplayShowTitleEnabled(true);
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
        getActionBar().setNavigationMode(navigationMode);
        getActionBar().setDisplayShowTitleEnabled(navigationMode == ActionBar.NAVIGATION_MODE_STANDARD);
    }

    private void loadCategories() {

        ParseQuery<ParseObject> parseQuery = ParseQuery.getQuery("Category");
        parseQuery.orderByAscending("Name");
        ArrayList<String> stringCollection = new ArrayList<String>();
        stringCollection.add("close");

        parseQuery.whereContainedIn("language", DailyKind.getLanguageCollection(MainActivity.this));
        parseQuery.whereNotContainedIn("status", stringCollection);
        parseQuery.setCachePolicy(ParseQuery.CachePolicy.CACHE_ELSE_NETWORK);
        parseQuery.setMaxCacheAge(DailyKind.QUERY_MAX_CACHE_AGE);
        parseQuery.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> parseObjects, ParseException e) {

                if (parseObjects != null) {
                    ideaCategoryList.clear();
                    categoryList.clear();

                    // Add all
                    ideaCategoryList.add(getString(R.string.idea_category_select_item_all));

                    for (ParseObject parseObject : parseObjects) {
                        Category category = new Category();
                        category.setObjectId(parseObject.getObjectId());
                        category.setName(parseObject.getString("Name"));
                        categoryList.add(category);

                        ideaCategoryList.add(parseObject.getString("Name"));
                    }
                    ideaCategoryDropDownAdapter.notifyDataSetChanged();
                }
            }
        });
    }
}