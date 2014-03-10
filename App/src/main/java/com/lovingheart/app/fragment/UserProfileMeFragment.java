package com.lovingheart.app.fragment;

import android.app.ActionBar;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.google.analytics.tracking.android.Fields;
import com.google.analytics.tracking.android.MapBuilder;
import com.lovingheart.app.DailyKind;
import com.lovingheart.app.R;
import com.lovingheart.app.object.UserImpact;
import com.lovingheart.app.util.AnalyticsManager;
import com.lovingheart.app.util.CheckUserLoginUtil;
import com.parse.*;

/**
 * Created by edward_chiang on 2013/11/23.
 */
public class UserProfileMeFragment extends UserProfileBasicFragment {

    public static UserProfileMeFragment newInstance(int sectionNumber) {
        UserProfileMeFragment fragment = new UserProfileMeFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActionBar actionBar = getActivity().getActionBar();
        actionBar.setTitle(getString(R.string.title_me));
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View contentView = super.onCreateView(inflater, container, savedInstanceState);
        return contentView;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CheckUserLoginUtil.ASK_USER_LOGIN && resultCode == getActivity().RESULT_OK) {
            setupUserId();
            queryProfile(new ProfileCallBack());
        }
    }

    protected void setupUserId() {
        setUserId(CheckUserLoginUtil.userId());
    }

    protected void updateUserImpact(final UserImpact userImpactInfo) {
        ParseQuery<ParseObject> userImpactQuery = new ParseQuery<ParseObject>("UserImpact");
        userImpactQuery.whereEqualTo("User", ParseUser.getCurrentUser());
        userImpactQuery.getFirstInBackground(new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject parseObject, ParseException e) {

                if (e != null) {
                    Log.e(DailyKind.TAG, e.getLocalizedMessage());
                }
                if (parseObject!=null) {
                    ParseObject userImpactObject = parseObject;

                    if (userImpactInfo.getStoriesSharedCount() > 0){
                        userImpactObject.put("sharedStoriesCount", userImpactInfo.getStoriesSharedCount());
                    }
                    if (userImpactInfo.getGraphicEarnedCount() > 0){
                        userImpactObject.put("graphicsEarnedCount", userImpactInfo.getGraphicEarnedCount());
                    }
                    if (userImpactInfo.getStarsReviewCount() > 0){
                        userImpactObject.put("reviewStarsImpact", userImpactInfo.getStarsReviewCount());
                    }
                    userImpactObject.saveInBackground(new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            if  (e==null && getActivity()!=null) {
                                Log.d(DailyKind.TAG, "User Impact report updated.");
                            }
                        }
                    });
                }



            }
        });
    }

    protected void saveUserImpact(final UserImpact userImpactInfo) {
        ParseQuery<ParseObject> userImpactQuery = new ParseQuery<ParseObject>("UserImpact");
        userImpactQuery.whereEqualTo("User", ParseUser.getCurrentUser());
        userImpactQuery.getFirstInBackground(new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject parseObject, ParseException e) {

                if (e != null) {
                    Log.e(DailyKind.TAG, e.getLocalizedMessage());
                }

                ParseObject userImpactObject = new ParseObject("UserImpact");
                userImpactObject.put("User", ParseUser.getCurrentUser());
                if (parseObject!=null) {
                    userImpactObject = parseObject;
                }

                if (userImpactInfo.getStoriesSharedCount() > 0){
                    userImpactObject.put("sharedStoriesCount", userImpactInfo.getStoriesSharedCount());
                }
                if (userImpactInfo.getGraphicEarnedCount() > 0){
                    userImpactObject.put("graphicsEarnedCount", userImpactInfo.getGraphicEarnedCount());
                }
                if (userImpactInfo.getStarsReviewCount() > 0){
                    userImpactObject.put("reviewStarsImpact", userImpactInfo.getStarsReviewCount());
                }
                userImpactObject.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        if  (e==null && getActivity()!=null) {
                            Log.d(DailyKind.TAG, "User Impact report updated.");
                        }
                    }
                });

            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        AnalyticsManager.getInstance().getGaTracker().send(
                MapBuilder.createAppView().set(Fields.SCREEN_NAME, UserProfileMeFragment.class.getName()).build());
    }
}
