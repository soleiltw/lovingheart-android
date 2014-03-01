package com.lovingheart.app.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.*;
import android.widget.AdapterView;
import android.widget.ListView;
import com.lovingheart.app.DailyKind;
import com.lovingheart.app.R;
import com.lovingheart.app.activity.DeedContentActivity;
import com.lovingheart.app.adapter.IdeaArrayAdapter;
import com.lovingheart.app.listener.LoadMoreListener;
import com.lovingheart.app.object.Category;
import com.lovingheart.app.object.Idea;
import com.lovingheart.app.util.parse.ParseObjectManager;
import com.parse.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by edward_chiang on 2014/2/23.
 */
public class DeedIdeaListFragment extends PlaceholderFragment {

    private List<Idea> ideaList;

    private Category queryCategory;

    private IdeaArrayAdapter ideaArrayAdapter;

    private Menu menu;

    private boolean parseLoading;

    private View progressLoadingView;

    public static DeedIdeaListFragment newInstance(int sectionNumber) {
        DeedIdeaListFragment fragment = new DeedIdeaListFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ideaList = new ArrayList<Idea>();

        setHasOptionsMenu(true);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        loadIdeas(false);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View contentView = inflater.inflate(R.layout.activity_good_ideas, container, false);

        ListView ideasListView = (ListView)contentView.findViewById(R.id.deed_idea_list_view);

        ideaArrayAdapter = new IdeaArrayAdapter(getActivity(), android.R.layout.simple_list_item_1, ideaList);
        ideaArrayAdapter.setLoadMoreListener(new LoadMoreListener() {
            @Override
            public void notifyLoadMore() {
                loadIdeas(true);
            }
        });
        ideasListView.setAdapter(ideaArrayAdapter);

        ideasListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getActivity(), DeedContentActivity.class);
                intent.putExtra("ideaObjectId", ideaList.get(position).getObjectId());
                startActivity(intent);
            }
        });

        progressLoadingView = contentView.findViewById(R.id.good_ideas_progress_bar);

        return contentView;
    }

    private void loadIdeas(boolean more) {

        final ParseQuery<ParseObject> ideasQuery = ParseQuery.getQuery("Idea");

        ArrayList<String> stringCollection = new ArrayList<String>();
        stringCollection.add("close");
        ideasQuery.whereNotContainedIn("status", stringCollection);
        ideasQuery.include("categoryPointer");

        if (queryCategory != null) {
            ParseObject categoryParseObject = new ParseObject("Category");
            categoryParseObject.setObjectId(queryCategory.getObjectId());
            ideasQuery.whereEqualTo("categoryPointer", categoryParseObject);
        }

        ideasQuery.whereContainedIn("language", DailyKind.getLanguageCollection(getActivity()));
        ideasQuery.orderByDescending("updatedAt");
        ideasQuery.setCachePolicy(ParseQuery.CachePolicy.CACHE_ELSE_NETWORK);
        ideasQuery.setMaxCacheAge(DailyKind.QUERY_MAX_CACHE_AGE);

        ideasQuery.setLimit(10);

        if (more) {
            ParseQuery<ParseObject> ideasCountQuery = ParseQuery.getQuery("Idea");
            ideasCountQuery.whereNotContainedIn("status", stringCollection);

            if (queryCategory != null) {
                ParseObject categoryParseObject = new ParseObject("Category");
                categoryParseObject.setObjectId(queryCategory.getObjectId());
                ideasCountQuery.whereEqualTo("categoryPointer", categoryParseObject);
            }

            ideasCountQuery.whereContainedIn("language", DailyKind.getLanguageCollection(getActivity()));

            ideasCountQuery.setCachePolicy(ParseQuery.CachePolicy.CACHE_ELSE_NETWORK);
            ideasCountQuery.setMaxCacheAge(DailyKind.QUERY_MAX_CACHE_AGE);

            ideasCountQuery.countInBackground(new CountCallback() {
                @Override
                public void done(int totalCount, ParseException e) {
                    Log.d(DailyKind.TAG, "Idea totalCount: " + totalCount);
                    if (totalCount > ideaList.size()) {
                        ideasQuery.setSkip(ideaList.size());
                        ideasQuery.findInBackground(getIdeasCallback(true));
                    } else {
                        Log.d(DailyKind.TAG, "End of query.");
                        ideaArrayAdapter.setLoadMoreEnd(true);
                    }
                }
            });


        } else {
            setParseLoading(true);
            updateRefreshItem();
            ideasQuery.findInBackground(getIdeasCallback(false));
        }
    }

    private FindCallback<ParseObject> getIdeasCallback(final boolean more) {

        return new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> parseObjects, ParseException e) {
                if (parseObjects != null) {

                    if (!more) {
                        ideaList.clear();
                    }

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
        };
    }

    public void updateRefreshItem() {
        if (isParseLoading()) {
            progressLoadingView.setVisibility(View.VISIBLE);
        } else {
            progressLoadingView.setVisibility(View.GONE);
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
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case R.id.action_reload: {
                loadIdeas(false);
                break;
            }
        }

        return super.onOptionsItemSelected(item);
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
