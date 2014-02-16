package com.edwardinubuntu.dailykind.fragment;

import android.app.ActionBar;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.*;
import android.widget.AdapterView;
import android.widget.ListView;
import com.edwardinubuntu.dailykind.DailyKind;
import com.edwardinubuntu.dailykind.R;
import com.edwardinubuntu.dailykind.activity.StoryContentActivity;
import com.edwardinubuntu.dailykind.adapter.StoryArrayAdapter;
import com.edwardinubuntu.dailykind.listener.LoadMoreListener;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by edward_chiang on 2013/11/23.
 */
public class StoriesFeedsFragment extends PlaceholderFragment {

    protected List<ParseObject> userActivities;

    private StoryArrayAdapter storyArrayAdapter;

    private Menu menu;

    private boolean queryLoading;

    private View loadingView;

    public static StoriesFeedsFragment newInstance(int sectionNumber) {
        StoriesFeedsFragment fragment = new StoriesFeedsFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        userActivities = new ArrayList<ParseObject>();

        storyArrayAdapter = new StoryArrayAdapter(getActivity(), android.R.layout.simple_list_item_1, userActivities);

        setHasOptionsMenu(true);

        ActionBar actionBar = getActivity().getActionBar();
        actionBar.setTitle(getString(R.string.title_stories));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_user_activities, container, false);

        ListView userActivitiesListView = (ListView)rootView.findViewById(R.id.user_activities_list_view);
        userActivitiesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent storyContentIntent = new Intent(getActivity(), StoryContentActivity.class);
                ParseObject activity = userActivities.get(position);
                storyContentIntent.putExtra("objectId", activity.getObjectId());
                startActivity(storyContentIntent);
            }
        });
        userActivitiesListView.setAdapter(storyArrayAdapter);

        storyArrayAdapter.setLoadMoreListener(new LoadMoreListener() {
            @Override
            public void notifyLoadMore() {
                loadStories(true);
            }
        });

        loadingView = rootView.findViewById(R.id.loading_progress_bar);

        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.activities, menu);
        this.menu = menu;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case R.id.action_reload: {
                loadStories(false);
                break;
            }
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        loadStories(false);
    }

    public void updateRefreshItem() {
        if (getActivity()!=null && loadingView != null) {
            if (isQueryLoading()) {
                loadingView.setVisibility(View.VISIBLE);
            } else {
                loadingView.setVisibility(View.GONE);
            }
        }
        if (menu != null) {
            MenuItem refreshItem = menu.findItem(R.id.action_reload);
            if (refreshItem != null) {
                if (isQueryLoading()) {
                    refreshItem.setActionView(R.layout.indeterminate_progress_action);
                } else {
                    refreshItem.setActionView(null);
                }
            }
        }
    }

    protected void loadStories(boolean more) {
    }

    protected void queryToCallBack(ParseQuery<ParseObject> parseQuery) {

        setQueryLoading(true);
        updateRefreshItem();
        parseQuery.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> parseObjects, ParseException e) {

                if (parseObjects!=null) {

                    boolean dataHasChange = false;
                    for (ParseObject eachParseObject : parseObjects) {
                        boolean hasAdd = false;
                        // If use cache then network, then done will be call 2 times.
                        for (ParseObject addedParseObject : userActivities) {
                            if (eachParseObject.getObjectId().equals(addedParseObject.getObjectId())) {
                                hasAdd = true;
                                break;
                            }
                        }
                        if (!hasAdd) {
                            dataHasChange = true;
                            userActivities.add(eachParseObject);
                        } else {
                            Log.d(DailyKind.TAG, "CachePolicy Skip object: " + eachParseObject.getObjectId());
                        }
                    }
                    if (dataHasChange) {
                        storyArrayAdapter.notifyDataSetChanged();
                    }
                }

                setQueryLoading(false);
                updateRefreshItem();
            }
        });
    }

    public boolean isQueryLoading() {
        return queryLoading;
    }

    public void setQueryLoading(boolean queryLoading) {
        this.queryLoading = queryLoading;
    }
}
