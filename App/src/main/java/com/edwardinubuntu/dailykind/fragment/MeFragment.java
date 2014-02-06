package com.edwardinubuntu.dailykind.fragment;

import android.app.ActionBar;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import com.edwardinubuntu.dailykind.DailyKind;
import com.edwardinubuntu.dailykind.R;
import com.edwardinubuntu.dailykind.activity.LoginActivity;
import com.edwardinubuntu.dailykind.object.UserImpact;
import com.edwardinubuntu.dailykind.util.CheckUserLoginUtil;
import com.edwardinubuntu.dailykind.util.CircleTransform;
import com.parse.*;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;

/**
 * Created by edward_chiang on 2013/11/23.
 */
public class MeFragment extends UserProfileFragment {

    public static MeFragment newInstance(int sectionNumber) {
        MeFragment fragment = new MeFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    public MeFragment() {
        super(null);
        setupUserId();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActionBar actionBar = getActivity().getActionBar();
        actionBar.setTitle(getString(R.string.title_me));
    }

    protected void setupUserId() {
        if (ParseUser.getCurrentUser() != null && ParseUser.getCurrentUser().getObjectId() != null) {
            setUserId(ParseUser.getCurrentUser().getObjectId());
        } else {
            setUserId(null);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CheckUserLoginUtil.ASK_USER_LOGIN) {
            setupUserId();
            queryProfile();
        }
    }

    protected void queryProfile() {

        if (getUserId() == null || getUserId().length() == 0) {
            getActivity().findViewById(com.edwardinubuntu.dailykind.R.id.me_profile_layout).setVisibility(View.GONE);
            getActivity().findViewById(com.edwardinubuntu.dailykind.R.id.user_ask_login_linear_layout).setVisibility(View.VISIBLE);

            getActivity().findViewById(com.edwardinubuntu.dailykind.R.id.me_ask_login_button).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent loginIntent = new Intent(getActivity(), LoginActivity.class);
                    startActivityForResult(loginIntent, CheckUserLoginUtil.ASK_USER_LOGIN);
                }
            });
            return;
        }

        ParseQuery<ParseUser> userQuery = ParseUser.getQuery();
        userQuery.include("avatar");
        userQuery.whereEqualTo("objectId", getUserId());
        userQuery.getFirstInBackground(new GetCallback<ParseUser>() {
            @Override
            public void done(ParseUser parseUser, ParseException e) {
                if (parseUser != null && parseUser.getObjectId() != null) {

                    userNameTextView.setText(parseUser.getString("name"));

                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
                    if (sinceTextView != null && parseUser.getCreatedAt() != null) {
                        sinceTextView.setText(getString(com.edwardinubuntu.dailykind.R.string.me_since_pre_text) + " " + dateFormat.format(parseUser.getCreatedAt()));
                    }


                    getActivity().findViewById(com.edwardinubuntu.dailykind.R.id.me_profile_layout).setVisibility(View.VISIBLE);
                    getActivity().findViewById(com.edwardinubuntu.dailykind.R.id.user_ask_login_linear_layout).setVisibility(View.GONE);

                    if (parseUser.has("avatar")) {
                        ParseObject avatarObject = parseUser.getParseObject("avatar");

                        ImageView avatarImageView = (ImageView) getActivity().findViewById(com.edwardinubuntu.dailykind.R.id.user_avatar_image_view);
                        if (avatarObject != null && avatarObject.getString("imageType").equals("url")) {
                            Picasso.with(getActivity())
                                    .load(avatarObject.getString("imageUrl"))
                                    .transform(new CircleTransform())
                                    .into(avatarImageView);
                        }
                    }

                    // Load user impact
                    loadUserImpact(parseUser);
                    queryStories(parseUser);
                    queryGraphicEarned(parseUser);

                } else {
                    Log.e(DailyKind.TAG, "queryProfile error: " + e.getLocalizedMessage());
                }
            }
        });


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

}
