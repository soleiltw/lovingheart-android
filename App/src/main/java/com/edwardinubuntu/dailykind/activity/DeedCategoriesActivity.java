package com.edwardinubuntu.dailykind.activity;

import android.annotation.TargetApi;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;
import com.edwardinubuntu.dailykind.DailyKind;
import com.edwardinubuntu.dailykind.ParseSettings;
import com.edwardinubuntu.dailykind.R;
import com.edwardinubuntu.dailykind.adapter.CategoryArrayAdapter;
import com.edwardinubuntu.dailykind.object.Category;
import com.parse.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by edward_chiang on 2013/11/23.
 */
public class DeedCategoriesActivity extends ActionBarActivity {

    private CategoryArrayAdapter categoriesAdapter;

    private List<Category> categoryList;

    private Menu menu;

    private boolean parseLoading;

    private SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        Parse.initialize(this, ParseSettings.PARSE_API_TOKEN, ParseSettings.PARSE_API_TOKEN_2);

        setContentView(R.layout.activity_deed_categories);

        categoryList = new ArrayList<Category>();

        preferences = PreferenceManager.getDefaultSharedPreferences(this);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        ListView categoriesListView = (ListView)findViewById(R.id.deed_categories_list_view);

        categoriesAdapter = new CategoryArrayAdapter(getApplicationContext(), R.layout.cell_category_text_view,
                categoryList );
        categoriesListView.setAdapter(categoriesAdapter);

        categoriesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getApplicationContext(), DeedCategoryIdeaListActivity.class);
                intent.putExtra("category", categoryList.get(position));
                startActivity(intent);
            }
        });

        loadCategories();
    }

    private void loadCategories() {

        setParseLoading(true);
        updateRefreshItem();

        ParseQuery<ParseObject> parseQuery = ParseQuery.getQuery("Category");
        parseQuery.orderByAscending("Name");
        ArrayList<String> stringCollection = new ArrayList<String>();
        stringCollection.add("close");

        ArrayList<String> languageCollection = new ArrayList<String>();
        boolean englishDefaultValue = Locale.getDefault().getLanguage().contains("en");
        boolean supportEnglish = preferences.getBoolean(DailyKind.PREFERENCE_SUPPORT_ENGLISH, englishDefaultValue);
        if (supportEnglish) {
            languageCollection.add("en");
        }
        boolean chineseDefaultValue = Locale.getDefault().getLanguage().contains("zh");
        boolean supportChinese = preferences.getBoolean(DailyKind.PREFERENCE_SUPPORT_CHINESE, chineseDefaultValue);
        if (supportChinese) {
            languageCollection.add("zh");
        }
        parseQuery.whereContainedIn("language", languageCollection);
        parseQuery.whereNotContainedIn("status", stringCollection);
        parseQuery.setCachePolicy(ParseQuery.CachePolicy.CACHE_ELSE_NETWORK);
        parseQuery.setMaxCacheAge(DailyKind.QUERY_MAX_CACHE_AGE);
        parseQuery.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> parseObjects, ParseException e) {

                categoryList.clear();
                for (ParseObject parseObject : parseObjects) {
                    Category category = new Category();
                    category.setObjectId(parseObject.getObjectId());
                    category.setName(parseObject.getString("Name"));
                    categoryList.add(category);
                }
                categoriesAdapter.notifyDataSetChanged();

                if (e != null && getApplicationContext() != null) {
                    Toast.makeText(getApplicationContext(), e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                }
                setParseLoading(false);
                updateRefreshItem();
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
            case R.id.action_reload:
                loadCategories();
                break;
        }
        return super.onOptionsItemSelected(item);
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
            findViewById(R.id.good_categories_progress_bar).setVisibility(View.VISIBLE);
        } else {
            findViewById(R.id.good_categories_progress_bar).setVisibility(View.GONE);
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
