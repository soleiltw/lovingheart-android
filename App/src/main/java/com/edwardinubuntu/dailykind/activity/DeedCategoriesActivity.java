package com.edwardinubuntu.dailykind.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import com.edwardinubuntu.dailykind.R;

/**
 * Created by edward_chiang on 2013/11/23.
 */
public class DeedCategoriesActivity extends ActionBarActivity {

    private ArrayAdapter<String> categoriesAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        setContentView(R.layout.activity_deed_categories);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        ListView categoriesListView = (ListView)findViewById(R.id.deed_categories_list_view);

        categoriesAdapter = new ArrayAdapter<String>(getApplicationContext(), R.layout.cell_category_text_view,
                new String[] {"Family & Friends",
                        "Environment",
                        "Animals",
                        "Charity",
                        "Stranger"
                });
        categoriesListView.setAdapter(categoriesAdapter);

        categoriesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getApplicationContext(), DeedContentActivity.class);

                startActivity(intent);
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
}
