package com.lovingheart.app.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import com.google.analytics.tracking.android.EasyTracker;
import com.lovingheart.app.DailyKind;
import com.lovingheart.app.R;
import com.lovingheart.app.adapter.IdeaArrayAdapter;
import com.lovingheart.app.object.Category;
import com.lovingheart.app.object.Idea;
import com.lovingheart.app.util.parse.ParseObjectManager;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by edward_chiang on 2013/12/21.
 */
public class DeedCategoryIdeaListActivity extends ActionBarActivity {

    private List<Idea> ideaList;

    private Category queryCategory;

    private IdeaArrayAdapter ideaArrayAdapter;

    private Menu menu;

    private boolean parseLoading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_good_ideas);

        ideaList = new ArrayList<Idea>();

        queryCategory = (Category) getIntent().getSerializableExtra("category");

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayUseLogoEnabled(true);
        actionBar.setTitle(queryCategory.getName());
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        ListView ideasListView = (ListView)findViewById(R.id.deed_idea_list_view);

        ideaArrayAdapter = new IdeaArrayAdapter(getApplicationContext(), android.R.layout.simple_list_item_1, ideaList);
        ideasListView.setAdapter(ideaArrayAdapter);

        ideasListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getApplicationContext(), DeedContentActivity.class);
                intent.putExtra("ideaObjectId", ideaList.get(position).getObjectId());
                startActivity(intent);
            }
        });

        loadIdeas();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: {
                finish();
                break;
            }
            case R.id.action_reload:
                loadIdeas();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void loadIdeas() {

        setParseLoading(true);
        updateRefreshItem();

        ParseQuery<ParseObject> ideasQuery = ParseQuery.getQuery("Idea");
        ArrayList<String> stringCollection = new ArrayList<String>();
        stringCollection.add("close");
        ideasQuery.whereNotContainedIn("status", stringCollection);
        ideasQuery.include("categoryPointer");

        ParseObject categoryParseObject = new ParseObject("Category");
        categoryParseObject.setObjectId(queryCategory.getObjectId());
        ideasQuery.whereEqualTo("categoryPointer", categoryParseObject);

        ideasQuery.orderByDescending("updatedAt");
        ideasQuery.setCachePolicy(ParseQuery.CachePolicy.CACHE_ELSE_NETWORK);
        ideasQuery.setMaxCacheAge(DailyKind.QUERY_MAX_CACHE_AGE);
        ideasQuery.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> parseObjects, ParseException e) {
                if (parseObjects != null) {
                    ideaList.clear();

                    for (ParseObject parseObject: parseObjects) {

                        ParseObjectManager parseObjectManager = new ParseObjectManager(parseObject);

                        Idea idea = parseObjectManager.getIdea();
                        idea.setCategory(parseObjectManager.getCategory());

                        ideaList.add(idea);
                    }
                    ideaArrayAdapter.notifyDataSetChanged();
                }

                setParseLoading(false);
                updateRefreshItem();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.categories, menu);
        this.menu = menu;
        return true;
    }

    public void updateRefreshItem() {
        if (isParseLoading()) {
            findViewById(R.id.good_ideas_progress_bar).setVisibility(View.VISIBLE);
        } else {
            findViewById(R.id.good_ideas_progress_bar).setVisibility(View.GONE);
        }
        if (menu != null) {
            MenuItem refreshItem = menu.findItem(R.id.action_reload);
            if (isParseLoading()) {
                refreshItem.setActionView(R.layout.indeterminate_progress_action);
            } else {
                refreshItem.setActionView(null);
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        EasyTracker.getInstance(this).activityStop(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        EasyTracker.getInstance(this).activityStart(this);
    }

    public boolean isParseLoading() {
        return parseLoading;
    }

    public void setParseLoading(boolean parseLoading) {
        this.parseLoading = parseLoading;
    }

    public void setQueryCategory(Category queryCategory) {
        this.queryCategory = queryCategory;
    }
}
