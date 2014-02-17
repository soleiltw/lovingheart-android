package com.edwardinubuntu.dailykind.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.*;
import android.widget.ImageView;
import android.widget.TextView;
import com.edwardinubuntu.dailykind.DailyKind;
import com.edwardinubuntu.dailykind.R;
import com.edwardinubuntu.dailykind.adapter.PersonalReportAdapter;
import com.edwardinubuntu.dailykind.object.UserImpact;
import com.edwardinubuntu.dailykind.util.CircleTransform;
import com.edwardinubuntu.dailykind.util.ReportManager;
import com.edwardinubuntu.dailykind.view.ExpandableListView;
import com.parse.*;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by edward_chiang on 2014/2/1.
 */
public class UserProfileBasicFragment extends UserProfileFragment {


    protected TextView storiesSharedCountTextView;

    protected TextView graphicEarnedCountTextView;

    protected TextView userNameTextView;

    protected TextView sinceTextView;

    protected TextView reviewStarsTextView;

    protected UserImpact userImpactInfo;

    private Menu menu;

    private boolean queryLoading;

    private String userId;

    protected ExpandableListView personalReportListView;

    private List<String> reportWordings;

    private PersonalReportAdapter personalReportAdapter;

    private View emptyView;

    private ReportManager reportManager = new ReportManager();

    public UserProfileBasicFragment() {
    }

    public UserProfileBasicFragment(String userId) {
        this.userId = userId;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);

        userImpactInfo = new UserImpact();

        reportWordings = new ArrayList<String>();

        personalReportAdapter = new PersonalReportAdapter(getActivity(), android.R.layout.simple_list_item_1, reportWordings);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_user_profile_basic, container, false);

        userNameTextView = (TextView)rootView.findViewById(R.id.user_name_text_view);

        sinceTextView = (TextView)rootView.findViewById(R.id.me_since_text_view);

        storiesSharedCountTextView = (TextView)rootView.findViewById(R.id.user_impact_stories_share_text_view);
        graphicEarnedCountTextView = (TextView)rootView.findViewById(R.id.user_impact_graphic_earned_text_view);

        reviewStarsTextView = (TextView)rootView.findViewById(R.id.user_impact_review_stars_text_view);

        personalReportListView = (ExpandableListView)rootView.findViewById(R.id.personal_report_list_view);
        personalReportListView.setExpand(true);
        personalReportListView.setAdapter(personalReportAdapter);

        emptyView = rootView.findViewById(R.id.user_profile_stories_empty_text_view);

        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.me, menu);
        this.menu = menu;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case R.id.action_reload: {
                setupUserId();
                queryProfile(new ProfileCallBack());
                break;
            }
        }

        return super.onOptionsItemSelected(item);
    }

    protected void setupUserId() {
    }

    public void updateRefreshItem() {
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

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        reportManager.setAnalyseListener(new ReportManager.AnalyseListener() {
            @Override
            public void done() {
                reportWordings.clear();
                reportWordings.addAll(reportManager.getReportWordings());
                personalReportAdapter.notifyDataSetChanged();
            }
        });

        queryProfile(new ProfileCallBack());
    }

    protected class ProfileCallBack extends GetCallback<ParseUser> {

        @Override
        public void done(ParseUser parseUser, ParseException e) {
            if (parseUser != null && parseUser.getObjectId() != null) {

                userNameTextView.setText(parseUser.getString("name"));

                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
                if (sinceTextView != null && parseUser.getCreatedAt() != null) {
                    sinceTextView.setText(getString(R.string.me_since_pre_text) + " " + dateFormat.format(parseUser.getCreatedAt()));
                }

                reportManager.setUser(parseUser);

                if (parseUser.has("avatar")) {
                    ParseObject avatarObject = parseUser.getParseObject("avatar");

                    ImageView avatarImageView = (ImageView) getActivity().findViewById(R.id.user_avatar_image_view);
                    if (avatarObject != null && avatarObject.getString("imageType").equals("url")) {
                        Picasso.with(getActivity())
                                .load(avatarObject.getString("imageUrl"))
                                .transform(new CircleTransform())
                                .into(avatarImageView);
                    }
                }

                // Load user impact
                loadUserImpact(parseUser);
                queryStories(parseUser, new FindCallback<ParseObject>() {
                    @Override
                    public void done(List<ParseObject> parseObjects, ParseException e) {
                        if (parseObjects != null && parseObjects.size() > 0) {

                            reportManager.setStoriesObjects(parseObjects);
                            reportManager.analyse();

                            if (emptyView != null) {
                                emptyView.setVisibility(View.GONE);
                            }

                            userImpactInfo.setStoriesSharedCount(parseObjects.size());
                            storiesSharedCountTextView.setText(String.valueOf(parseObjects.size()));

                            int reviewImpactCount = 0;
                            for (ParseObject eachStory : parseObjects) {
                                if (eachStory.has("reviewImpact")) {
                                    reviewImpactCount += eachStory.getInt("reviewImpact");
                                }
                            }

                            reviewStarsTextView.setText(String.valueOf(reviewImpactCount));
                            userImpactInfo.setStarsReviewCount(reviewImpactCount);

                            saveUserImpact(userImpactInfo);

                        } else {
                            getActivity().findViewById(R.id.user_profile_stories_empty_text_view).setVisibility(View.VISIBLE);
                        }
                    }
                });
                queryGraphicEarned(parseUser, new FindCallback<ParseObject>() {
                    @Override
                    public void done(List<ParseObject> parseObjects, ParseException e) {
                        if (parseObjects!=null && !parseObjects.isEmpty()) {

                            graphicEarnedCountTextView.setText(String.valueOf(parseObjects.size()));
                            userImpactInfo.setGraphicEarnedCount(parseObjects.size());

                            updateUserImpact(userImpactInfo);
                        } else {
                            graphicEarnedCountTextView.setText(String.valueOf(0));
                        }
                    }
                });

            } else {
                Log.i(DailyKind.TAG, "Couldn't find the user profile : " + getUserId());
            }
            if (e != null) {
                Log.e(DailyKind.TAG, e.getLocalizedMessage());
            }
        }
    }

    protected void loadUserImpact(final ParseUser parseUser) {
        setQueryLoading(true);
        updateRefreshItem();

        ParseQuery<ParseObject> userImpactQuery = new ParseQuery<ParseObject>("UserImpact");
        userImpactQuery.whereEqualTo("User", parseUser);
        userImpactQuery.setCachePolicy(ParseQuery.CachePolicy.CACHE_ELSE_NETWORK);
        userImpactQuery.setMaxCacheAge(DailyKind.QUERY_AT_LEAST_CACHE_AGE);
        userImpactQuery.getFirstInBackground(new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject parseObject, ParseException e) {
                if (parseObject!=null) {
                    if (parseObject.has("sharedStoriesCount")) {
                        storiesSharedCountTextView.setText(String.valueOf(parseObject.getInt("sharedStoriesCount")));
                    }
                    if (parseObject.has("graphicsEarnedCount")) {
                        int graphicsEarnedCount = parseObject.getInt("graphicsEarnedCount");
                        graphicEarnedCountTextView.setText(String.valueOf(graphicsEarnedCount));
                    }
                    if (parseObject.has("reviewStarsImpact")) {
                        reviewStarsTextView.setText(String.valueOf(parseObject.getInt("reviewStarsImpact")));
                    }
                }
                setQueryLoading(false);
                updateRefreshItem();

            }
        });
    }

    protected void updateUserImpact(final UserImpact userImpactInfo) {
        // We don't update user impact
    }

    protected void saveUserImpact(final UserImpact userImpactInfo) {
        // We don't update user impact
    }

    public boolean isQueryLoading() {
        return queryLoading;
    }

    public void setQueryLoading(boolean queryLoading) {
        this.queryLoading = queryLoading;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

}
