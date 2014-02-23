package com.edwardinubuntu.dailykind.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.*;
import android.widget.AdapterView;
import android.widget.ListView;
import com.edwardinubuntu.dailykind.DailyKind;
import com.edwardinubuntu.dailykind.R;
import com.edwardinubuntu.dailykind.activity.DeedCategoryIdeaListActivity;
import com.edwardinubuntu.dailykind.adapter.CategoryArrayAdapter;
import com.edwardinubuntu.dailykind.object.Category;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by edward_chiang on 2014/2/11.
 */
public class DeedCategoriesFragment extends PlaceholderFragment {

    private CategoryArrayAdapter categoriesAdapter;

    private List<Category> categoryList;

    private Menu menu;

    private boolean parseLoading;

    private View progressLoadingView;

    public static DeedCategoriesFragment newInstance(int sectionNumber) {
        DeedCategoriesFragment fragment = new DeedCategoriesFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        categoryList = new ArrayList<Category>();

        categoriesAdapter = new CategoryArrayAdapter(getActivity(), R.layout.cell_category_text_view,
                categoryList );

        setHasOptionsMenu(true);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        loadCategories();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_deed_categories, container, false);

        ListView categoriesListView = (ListView)rootView.findViewById(R.id.deed_categories_list_view);
        categoriesListView.setAdapter(categoriesAdapter);
        categoriesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getActivity(), DeedCategoryIdeaListActivity.class);
                intent.putExtra("category", categoryList.get(position));
                startActivity(intent);
            }
        });

        progressLoadingView = rootView.findViewById(R.id.good_categories_progress_bar);


        return rootView;
    }

    private void loadCategories() {

        setParseLoading(true);
        updateRefreshItem();

        ParseQuery<ParseObject> parseQuery = ParseQuery.getQuery("Category");
        parseQuery.orderByAscending("Name");
        ArrayList<String> stringCollection = new ArrayList<String>();
        stringCollection.add("close");

        parseQuery.whereContainedIn("language", DailyKind.getLanguageCollection(getActivity()));
        parseQuery.whereNotContainedIn("status", stringCollection);
        parseQuery.setCachePolicy(ParseQuery.CachePolicy.CACHE_ELSE_NETWORK);
        parseQuery.setMaxCacheAge(DailyKind.QUERY_MAX_CACHE_AGE);
        parseQuery.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> parseObjects, ParseException e) {

                if (parseObjects != null) {
                    categoryList.clear();
                    for (ParseObject parseObject : parseObjects) {
                        Category category = new Category();
                        category.setObjectId(parseObject.getObjectId());
                        category.setName(parseObject.getString("Name"));
                        categoryList.add(category);
                    }
                    categoriesAdapter.notifyDataSetChanged();
                }
                setParseLoading(false);
                updateRefreshItem();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_reload:
                loadCategories();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        this.menu = menu;
    }

    public void updateRefreshItem() {

        if (isParseLoading()) {
            progressLoadingView.setVisibility(View.VISIBLE);
        } else {
            progressLoadingView.setVisibility(View.GONE);
        }

        if (menu != null) {
            MenuItem refreshItem = menu.findItem(R.id.action_reload);
            if (refreshItem!= null) {
                if (isParseLoading()) {
                    refreshItem.setActionView(R.layout.indeterminate_progress_action);
                } else {
                    refreshItem.setActionView(null);
                }
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
