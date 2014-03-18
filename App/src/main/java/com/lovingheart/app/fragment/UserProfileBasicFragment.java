package com.lovingheart.app.fragment;

import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;
import com.google.analytics.tracking.android.Fields;
import com.lovingheart.app.DailyKind;
import com.lovingheart.app.R;
import com.lovingheart.app.adapter.PersonalReportAdapter;
import com.lovingheart.app.object.Info;
import com.lovingheart.app.object.UserImpact;
import com.lovingheart.app.util.AnalyticsManager;
import com.lovingheart.app.util.CircleTransform;
import com.lovingheart.app.util.ReportManager;
import com.lovingheart.app.util.parse.ParseObjectManager;
import com.lovingheart.app.view.ExpandableListView;
import com.parse.*;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
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

    private String userId;

    private ImageView avatarImageView;
    protected View aboutView;

    protected PersonalReportAdapter personalReportAdapter;

    private List<Info> infoList;

    public UserProfileBasicFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);

        userImpactInfo = new UserImpact();

        infoList = new ArrayList<Info>();
        personalReportAdapter = new PersonalReportAdapter(getActivity(), android.R.layout.simple_list_item_1, infoList);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_user_profile_basic, container, false);

        userNameTextView = (TextView)rootView.findViewById(R.id.user_name_text_view);

        sinceTextView = (TextView)rootView.findViewById(R.id.me_since_text_view);

        storiesSharedCountTextView = (TextView)rootView.findViewById(R.id.user_impact_stories_share_text_view);
        graphicEarnedCountTextView = (TextView)rootView.findViewById(R.id.user_impact_graphic_earned_text_view);

        reviewStarsTextView = (TextView)rootView.findViewById(R.id.user_impact_review_stars_text_view);

        avatarImageView = (ImageView)rootView.findViewById(R.id.user_avatar_image_view);

        aboutView = rootView.findViewById(R.id.user_profile_about_layout);
        aboutView.setVisibility(View.GONE);

        ExpandableListView reportListView = (ExpandableListView)rootView.findViewById(R.id.personal_about_list_view);
        reportListView.setAdapter(personalReportAdapter);
        reportListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (getActivity()!=null) {
                    ViewPager viewPager = (ViewPager)getActivity().findViewById(R.id.pager);
                    viewPager.setCurrentItem(UserProfileMainFragment.VIEW_PAGER_REPORT, true);
                }
            }
        });

        return rootView;
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

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        queryProfile(new ProfileCallBack());

    }

    protected class ProfileCallBack extends GetCallback<ParseUser> {

        @Override
        public void done(final ParseUser parseUser, ParseException e) {
            if (parseUser != null && parseUser.getObjectId() != null && isAdded()) {

                userNameTextView.setText(parseUser.getString("name"));

                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
                if (sinceTextView != null && parseUser.getCreatedAt() != null) {
                    sinceTextView.setText(getString(R.string.me_since_pre_text) + " " + dateFormat.format(parseUser.getCreatedAt()));
                }

                if (parseUser.has("avatar") && getActivity() != null) {
                    ParseObject avatarObject = parseUser.getParseObject("avatar");

                    if (avatarObject != null) {
                        if (avatarObject.getString("imageType").equals("url")) {
                        Picasso.with(getActivity())
                                .load(avatarObject.getString("imageUrl"))
                                .transform(new CircleTransform())
                                .into(avatarImageView);
                        } else if (avatarObject.getString("imageType").equals("file")) {
                            Picasso.with(getActivity())
                                    .load(avatarObject.getParseFile("imageFile").getUrl())
                                    .transform(new CircleTransform())
                                    .into(avatarImageView);
                        }
                    }
                }

                // Update user log
                ParseObjectManager.userLogDone("XO0bGuSWk9");
                ParseObjectManager.userLogDone("9ZwlQvpHsA");

                // Load user impact
                loadUserImpact(parseUser);
                queryStories(parseUser, new FindCallback<ParseObject>() {
                    @Override
                    public void done(List<ParseObject> parseObjects, ParseException e) {
                        if (parseObjects != null && parseObjects.size() > 0) {

                            userImpactInfo.setStoriesSharedCount(parseObjects.size());
                            storiesSharedCountTextView.setText(String.valueOf(parseObjects.size()));

                            StringBuffer tagsBuffer = new StringBuffer();
                            int reviewImpactCount = 0;
                            for (ParseObject eachStory : parseObjects) {
                                if (eachStory.has("reviewImpact")) {
                                    reviewImpactCount += eachStory.getInt("reviewImpact");
                                }

                                if (eachStory.has("ideaPointer")) {
                                    ParseObject ideaObject = eachStory.getParseObject("ideaPointer");
                                    if (ideaObject.has("Tags")) {
                                        String tags = ideaObject.getString("Tags");
                                        tagsBuffer.append(tags);
                                        tagsBuffer.append(ReportManager.TAGS_SEPARATOR);
                                    }
                                }
                            }

                            reviewStarsTextView.setText(String.valueOf(reviewImpactCount));
                            userImpactInfo.setStarsReviewCount(reviewImpactCount);

                            saveUserImpact(userImpactInfo);

                            // Show up for every user
                            ReportManager reportManager = new ReportManager();
                            reportManager.setUser(parseUser);
                            reportManager.setStoriesObjects(parseObjects);
                            int count = reportManager.extractIdeaTags(tagsBuffer ,5);
                            if (count <= 0) {
                                aboutView.setVisibility(View.GONE);
                            } else {
                                infoList.clear();
                                infoList.addAll(reportManager.getReportWordings());
                                personalReportAdapter.notifyDataSetChanged();

                                aboutView.setVisibility(View.VISIBLE);
                            }


                        } else {
                            aboutView.setVisibility(View.GONE);
                        }
                    }
                });
                queryGraphicEarned(parseUser, new GetCallback<ParseObject>() {
                    @Override
                    public void done(ParseObject parseObject, ParseException e) {
                        if (parseObject != null) {
                            ParseRelation graphicsRelation = parseObject.getRelation("graphicsEarned");
                            ParseQuery<ParseObject> graphicsEarnedQuery = graphicsRelation.getQuery();
                            graphicsEarnedQuery.findInBackground(new FindCallback<ParseObject>() {
                                @Override
                                public void done(List<ParseObject> parseObjects, ParseException e) {
                                    if (parseObjects != null && !parseObjects.isEmpty()) {

                                        graphicEarnedCountTextView.setText(String.valueOf(parseObjects.size()));
                                        userImpactInfo.setGraphicEarnedCount(parseObjects.size());

                                        updateUserImpact(userImpactInfo);
                                    } else {
                                        graphicEarnedCountTextView.setText(String.valueOf(0));
                                    }
                                }
                            });
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

        updateRefreshItem(true);

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
                updateRefreshItem(false);

            }
        });
    }

    public void updateRefreshItem() {
    }

    protected void updateUserImpact(final UserImpact userImpactInfo) {
        // We don't update user impact
    }

    protected void saveUserImpact(final UserImpact userImpactInfo) {
        // We don't update user impact
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }


    @Override
    public void onStart() {
        super.onStart();

        HashMap<String, String> gaParams = new HashMap<String, String>();
        gaParams.put(Fields.SCREEN_NAME, "User Profile Basic");
        gaParams.put(Fields.EVENT_ACTION, "View");
        gaParams.put(Fields.EVENT_CATEGORY, "User Profile Basic");
        gaParams.put(Fields.EVENT_LABEL, "user/" +userId);
        AnalyticsManager.getInstance().getGaTracker().send(gaParams);
    }

    @Override
    public void updateRefreshItem(boolean isLoading) {

    }
}
