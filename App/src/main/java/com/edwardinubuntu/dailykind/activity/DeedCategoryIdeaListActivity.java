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
import com.edwardinubuntu.dailykind.R;
import com.edwardinubuntu.dailykind.adapter.IdeaArrayAdapter;
import com.edwardinubuntu.dailykind.object.Category;
import com.edwardinubuntu.dailykind.object.Idea;
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
                intent.putExtra("idea", ideaList.get(position));
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
        ideasQuery.include("categoryPointer");

        ParseObject categoryParseObject = new ParseObject("Category");
        categoryParseObject.setObjectId(queryCategory.getObjectId());
        ideasQuery.whereEqualTo("categoryPointer", categoryParseObject);

        ideasQuery.orderByDescending("updatedAt");

        ideasQuery.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> parseObjects, ParseException e) {
                ideaList.clear();

                for (ParseObject parseObject: parseObjects) {
                    Idea idea = new Idea();

                    idea.setName(parseObject.getString("Name"));
                    idea.setIdeaDescription(parseObject.getString("Description"));

                    Category category = new Category();
                    ParseObject categoryObject = parseObject.getParseObject("categoryPointer");
                    category.setObjectId(categoryObject.getObjectId());
                    category.setName(categoryObject.getString("Name"));

                    idea.setCategory(category);

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
