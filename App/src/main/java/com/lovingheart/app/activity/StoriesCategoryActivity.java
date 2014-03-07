package com.lovingheart.app.activity;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.MenuItem;
import com.google.analytics.tracking.android.EasyTracker;
import com.lovingheart.app.R;
import com.lovingheart.app.fragment.StoriesFromCategoryFragment;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

/**
 * Created by edward_chiang on 2014/3/1.
 */
public class StoriesCategoryActivity extends ActionBarActivity {

    private String ideaId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_stories_category_main);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayUseLogoEnabled(true);

        ideaId = getIntent().getStringExtra("ideaObjectId");
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        ParseQuery<ParseObject> ideaQuery = ParseQuery.getQuery("Idea");
        ideaQuery.whereEqualTo("objectId", ideaId);
        ideaQuery.getFirstInBackground(new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject parseObject, ParseException e) {
                // User Login fragment
                if (parseObject != null) {
                setTitle(parseObject.getString("Name"));
                android.support.v4.app.FragmentManager fragmentManager = getSupportFragmentManager();
                StoriesFromCategoryFragment deedCategoriesFragment = StoriesFromCategoryFragment.newInstance(0);
                deedCategoriesFragment.setIdeaObject(parseObject);
                fragmentManager.beginTransaction().replace(R.id.stories_category_main_fragment, deedCategoriesFragment).commit();
                }
            }
        });


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

    @Override
    protected void onStart() {
        super.onStart();
        EasyTracker.getInstance(this).activityStart(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        EasyTracker.getInstance(this).activityStop(this);
    }
}
