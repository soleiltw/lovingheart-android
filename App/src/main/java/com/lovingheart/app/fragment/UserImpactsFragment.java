package com.lovingheart.app.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import com.lovingheart.app.DailyKind;
import com.lovingheart.app.R;
import com.lovingheart.app.activity.UserProfileActivity;
import com.lovingheart.app.adapter.UserImpactsAdapter;
import com.lovingheart.app.listener.LoadMoreListener;
import com.parse.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by edward_chiang on 2014/5/6.
 */
public class UserImpactsFragment extends PlaceholderFragment {

    private List<ParseObject> userImpactList;

    private UserImpactsAdapter userImpactsAdapter;

    private View loadingView;

    public static UserImpactsFragment newInstance(int sectionNumber) {
        UserImpactsFragment fragment = new UserImpactsFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        userImpactList = new ArrayList<ParseObject>();
        userImpactsAdapter = new UserImpactsAdapter(getActivity(), android.R.layout.simple_list_item_1, userImpactList);

        loadUserImpacts(false);
    }

    @Override
    public void updateRefreshItem(boolean isLoading) {
        if (getActivity()!=null && loadingView != null) {
            if (isLoading) {
                loadingView.setVisibility(View.VISIBLE);
            } else {
                loadingView.setVisibility(View.GONE);
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
                loadUserImpacts(false);
                break;
            }
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_user_impacts, container, false);

        loadingView = rootView.findViewById(R.id.loading_progress_bar);

        ListView userImpactsListView = (ListView)rootView.findViewById(R.id.user_impacts_list_view);
        userImpactsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ParseObject userImpact = userImpactList.get(position);

                Intent userIntent = new Intent(getActivity(), UserProfileActivity.class);
                userIntent.putExtra("userId", userImpact.getParseUser("User").getObjectId());
                startActivity(userIntent);
            }
        });
        userImpactsListView.setAdapter(userImpactsAdapter);

        userImpactsAdapter.setLoadMoreListener(new LoadMoreListener() {
            @Override
            public void notifyLoadMore() {
                loadUserImpacts(true);
            }
        });

        return rootView;
    }

    protected void loadUserImpacts(final boolean more) {
        final ParseQuery<ParseObject> parseQuery = ParseQuery.getQuery("UserImpact");
        parseQuery.include("User");
        parseQuery.whereGreaterThanOrEqualTo("sharedStoriesCount", 1);
        parseQuery.orderByDescending("sharedStoriesCount");

        parseQuery.setLimit(DailyKind.PARSE_QUERY_LIMIT);
        parseQuery.setCachePolicy(ParseQuery.CachePolicy.CACHE_THEN_NETWORK);
        parseQuery.setMaxCacheAge(DailyKind.QUERY_AT_LEAST_CACHE_AGE);

        if (more && userImpactList.size() > 0) {
            parseQuery.setSkip(userImpactList.size());
        }
        if (!more) {
            userImpactList.clear();
            updateRefreshItem(true);

            queryInBackground(parseQuery);
        } else {
            ParseQuery parseCountQuery = ParseQuery.getQuery("UserImpact");
            parseCountQuery.setCachePolicy(ParseQuery.CachePolicy.CACHE_ELSE_NETWORK);
            parseCountQuery.setMaxCacheAge(DailyKind.QUERY_MAX_CACHE_AGE);
            parseQuery.whereGreaterThanOrEqualTo("sharedStoriesCount", 1);
            parseCountQuery.countInBackground(new CountCallback() {
                @Override
                public void done(int totalCount, ParseException e) {
                    if (totalCount > userImpactList.size()) {
                        parseQuery.setSkip(userImpactList.size());
                        queryInBackground(parseQuery);
                    } else {
                        Log.d(DailyKind.TAG, "End of query.");
                    }
                }
            });
        }

    }

    private void queryInBackground(ParseQuery<ParseObject> parseQuery) {
        parseQuery.findInBackground( new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> parseObjects, ParseException e) {
                updateRefreshItem(false);

                if  (parseObjects!=null) {

                    boolean dataHasChange = false;
                    for (ParseObject eachParseObject : parseObjects) {
                        boolean hasAdd = false;
                        // If use cache then network, then done will be call 2 times.
                        for (ParseObject addedParseObject : userImpactList) {
                            if (eachParseObject.getObjectId().equals(addedParseObject.getObjectId())) {
                                hasAdd = true;
                                break;
                            }
                        }
                        if (!hasAdd) {
                            userImpactList.add(eachParseObject);
                            dataHasChange = true;
                        } else {
                            Log.d(DailyKind.TAG, "CachePolicy Skip object: " + eachParseObject.getObjectId());
                        }
                    }
                    if (dataHasChange) {
                        userImpactsAdapter.notifyDataSetChanged();
                    }
                }
            }
        });
    }
}
