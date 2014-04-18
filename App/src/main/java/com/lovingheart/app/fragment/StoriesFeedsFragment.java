package com.lovingheart.app.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.*;
import android.widget.AdapterView;
import android.widget.ListView;
import com.lovingheart.app.DailyKind;
import com.lovingheart.app.R;
import com.lovingheart.app.activity.StoryContentActivity;
import com.lovingheart.app.adapter.StoryArrayAdapter;
import com.lovingheart.app.listener.LoadMoreListener;
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

    protected StoryArrayAdapter storyArrayAdapter;

    private Menu menu;

    private View loadingView;

    protected View emptyTextView;

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
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (getActivity()!= null && ((ActionBarActivity) getActivity()).getSupportActionBar() != null) {
            android.support.v7.app.ActionBar actionBar = ((ActionBarActivity) getActivity()).getSupportActionBar();
            if (actionBar != null) {
                actionBar.setTitle(getString(R.string.title_stories));
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_stories, container, false);

        ListView userActivitiesListView = (ListView)rootView.findViewById(R.id.user_activities_list_view);
        userActivitiesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent storyContentIntent = new Intent(getActivity(), StoryContentActivity.class);
                ParseObject activity = userActivities.get(position);
                storyContentIntent.putExtra("objectId", activity.getObjectId());
                boolean isAnonymous = activity.getString("status") != null && activity.getString("status").contains("anonymous");
                if (isAnonymous) {
                    storyContentIntent.putStringArrayListExtra("status", DailyKind.getAnonymousStoriesStatusList(getActivity()));
                }
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

        emptyTextView = rootView.findViewById(R.id.stories_empty_text_view);

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

    @Override
    public void updateRefreshItem(boolean isLoading) {
        updateRefreshItem(isLoading, false);
    }

    public void updateRefreshItem(boolean isLoading, boolean more) {
        if (getActivity()!=null && loadingView != null) {
            if (isLoading && !more) {
                loadingView.setVisibility(View.VISIBLE);
            } else {
                loadingView.setVisibility(View.GONE);
            }
        }
        if (menu != null) {
            MenuItem refreshItem = menu.findItem(R.id.action_reload);
            if (refreshItem != null) {
                if (isLoading) {
                    MenuItemCompat.setActionView(refreshItem, R.layout.indeterminate_progress_action);
                } else {
                    MenuItemCompat.setActionView(refreshItem, null);
                }
            }
        }
    }

    protected void loadStories(boolean more) {
    }

    protected void queryToCallBack(ParseQuery<ParseObject> parseQuery, final boolean more) {

        updateRefreshItem(true, more);
        parseQuery.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> parseObjects, ParseException e) {

                if (parseObjects!=null) {

                    if (parseObjects.isEmpty()) {
                        emptyTextView.setVisibility(View.VISIBLE);
                    } else {
                        emptyTextView.setVisibility(View.GONE);
                    }

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

                updateRefreshItem(false, more);
            }
        });
    }
}
