package com.edwardinubuntu.dailykind.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.edwardinubuntu.dailykind.DailyKind;
import com.edwardinubuntu.dailykind.R;
import com.edwardinubuntu.dailykind.object.Story;
import com.edwardinubuntu.dailykind.util.CircleTransform;
import com.edwardinubuntu.dailykind.util.parse.ParseObjectManager;
import com.parse.*;
import com.squareup.picasso.Picasso;
import org.ocpsoft.prettytime.PrettyTime;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Created by edward_chiang on 2013/11/23.
 */
public class MeFragment extends PlaceholderFragment {

    private TextView storiesSharedCountTextView;

    public static MeFragment newInstance(int sectionNumber) {
        MeFragment fragment = new MeFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_me, container, false);

        TextView userNameTextView = (TextView)rootView.findViewById(R.id.user_name_text_view);
        if (ParseUser.getCurrentUser() != null) {
            userNameTextView.setText(ParseUser.getCurrentUser().getString("name"));
        }

        TextView sinceTextView = (TextView)rootView.findViewById(R.id.me_since_text_view);
        if (ParseUser.getCurrentUser() != null) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
            sinceTextView.setText(getString(R.string.me_since_pre_text) + " " + dateFormat.format(ParseUser.getCurrentUser().getCreatedAt()));
        }

        storiesSharedCountTextView = (TextView)rootView.findViewById(R.id.me_stories_share_text_view);

        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ParseObject avatarObject = ParseUser.getCurrentUser().getParseObject("avatar");
        avatarObject.fetchIfNeededInBackground(new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject parseObject, ParseException e) {
                ImageView avatarImageView = (ImageView)getActivity().findViewById(R.id.user_avatar_image_view);
                if (parseObject.getString("imageType").equals("url")) {
                    Picasso.with(getActivity())
                            .load(parseObject.getString("imageUrl"))
                            .transform(new CircleTransform())
                            .into(avatarImageView);
                }
            }
        });

        // Check if user have report
        ParseQuery<ParseObject> userQuery = ParseQuery.getQuery("UserImpact");
        userQuery.whereEqualTo("User", ParseUser.getCurrentUser());
        userQuery.getFirstInBackground(new GetCallback<ParseObject>() {
            @Override
            public void done(final ParseObject parseObject, ParseException e) {
                if (parseObject!=null) {
                    updateUserImpact(parseObject);
                } else {
                    generateUserImpact();
                }
            }
        });

        ParseQuery<ParseObject> parseObjectParseQuery = new ParseQuery<ParseObject>("Story");
        parseObjectParseQuery.whereEqualTo("StoryTeller", ParseUser.getCurrentUser());
        parseObjectParseQuery.orderByDescending("createdAt");
        parseObjectParseQuery.include("ideaPointer");
        parseObjectParseQuery.include("StoryTeller");
        parseObjectParseQuery.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> parseObjects, ParseException e) {
                // Check Story Number

                // Get first one to update story date
                if (parseObjects != null && !parseObjects.isEmpty()) {
                    ParseObject storyParseObject = parseObjects.get(0);

                    ParseObjectManager parseObjectManager = new ParseObjectManager(storyParseObject);
                    Story story = parseObjectManager.getStory();

                    TextView lastSharedContentTextView = (TextView)getActivity().findViewById(R.id.me_stories_last_share_content_text_view);
                    lastSharedContentTextView.setText(story.getContent());

                    if (storyParseObject.getParseObject("ideaPointer") != null) {

                        story.setIdea(new ParseObjectManager(storyParseObject.getParseObject("ideaPointer")).getIdea());

                        TextView lastInspiredTextView = (TextView)getActivity().findViewById(R.id.me_stories_last_share_inspired_from_text_view);
                        lastInspiredTextView.setText(
                                getActivity().getString(R.string.stories_last_share_inspired_by_text_prefix)+
                                        getActivity().getString(R.string.space) +
                                story.getIdea().getName());
                    }

                    TextView lastSharedDateTextView = (TextView)getActivity().findViewById(R.id.me_stories_last_share_date_Text_view);
                    PrettyTime prettyTime = new PrettyTime(new Date());
                    lastSharedDateTextView.setText(
                            prettyTime.format(story.getCreatedAt()));
                }
            }
        });
    }

    private void generateUserImpact() {
        ParseQuery<ParseObject> storyQuery = new ParseQuery<ParseObject>("Story");
        storyQuery.whereEqualTo("StoryTeller", ParseUser.getCurrentUser());
        storyQuery.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> parseObjects, ParseException e) {

                if (parseObjects != null && parseObjects.size() > 0) {
                    int storiesCount = parseObjects.size();

                    storiesSharedCountTextView.setText(storiesCount + " " + getString(R.string.me_number_of_stories_shared));

                    ParseObject userImpactObject = new ParseObject("UserImpact");
                    userImpactObject.put("User", ParseUser.getCurrentUser());
                    userImpactObject.put("sharedStoriesCount", storiesCount);
                    userImpactObject.saveInBackground(new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            if (e == null) {
                                Toast.makeText(getActivity(), "Report generated.", Toast.LENGTH_SHORT).show();

                            } else {
                                Log.e(DailyKind.TAG, "UserImpact save: " + e.getLocalizedMessage());
                            }
                        }
                    });
                }

            }
        });
    }

    private void updateUserImpact(final ParseObject parseObject) {
        ParseQuery<ParseObject> storyQuery = new ParseQuery<ParseObject>("Story");
        storyQuery.whereEqualTo("StoryTeller", ParseUser.getCurrentUser());
        storyQuery.countInBackground(new CountCallback() {
            @Override
            public void done(int count, ParseException e) {
                storiesSharedCountTextView.setText(count + getString(R.string.space) + getString(R.string.me_number_of_stories_shared));

                parseObject.put("sharedStoriesCount", count);
                parseObject.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        if (e == null) {
                            Toast.makeText(getActivity(), "Report updated.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
    }
}
