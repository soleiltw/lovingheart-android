package com.edwardinubuntu.dailykind.activity;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import com.edwardinubuntu.dailykind.ParseSettings;
import com.edwardinubuntu.dailykind.R;
import com.edwardinubuntu.dailykind.adapter.IdeaArrayAdapter;
import com.edwardinubuntu.dailykind.object.Category;
import com.edwardinubuntu.dailykind.object.Idea;
import com.edwardinubuntu.dailykind.util.parse.ParseObjectManager;
import com.parse.*;

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

        Parse.initialize(this, ParseSettings.PARSE_API_TOKEN, ParseSettings.PARSE_API_TOKEN_2);


        ideaList = new ArrayList<Idea>();

        queryCategory = (Category) getIntent().getSerializableExtra("category");

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
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
        ideasQuery.setCachePolicy(ParseQuery.CachePolicy.NETWORK_ELSE_CACHE);
        ideasQuery.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> parseObjects, ParseException e) {
                ideaList.clear();

                for (ParseObject parseObject: parseObjects) {

                    ParseObjectManager parseObjectManager = new ParseObjectManager(parseObject);

                    Idea idea = parseObjectManager.getIdea();
                    idea.setCategory(parseObjectManager.getCategory());

                    ideaList.add(idea);
                }
                ideaArrayAdapter.notifyDataSetChanged();

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

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
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

    public boolean isParseLoading() {
        return parseLoading;
    }

    public void setParseLoading(boolean parseLoading) {
        this.parseLoading = parseLoading;
    }
}
