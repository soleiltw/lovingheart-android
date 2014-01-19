package com.edwardinubuntu.dailykind.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.*;
import android.widget.ImageView;
import android.widget.TextView;
import com.edwardinubuntu.dailykind.R;
import com.edwardinubuntu.dailykind.activity.LoginActivity;
import com.edwardinubuntu.dailykind.adapter.GalleryArrayAdapter;
import com.edwardinubuntu.dailykind.object.Graphic;
import com.edwardinubuntu.dailykind.util.CircleTransform;
import com.edwardinubuntu.dailykind.util.parse.ParseObjectManager;
import com.edwardinubuntu.dailykind.view.ExpandableGridView;
import com.parse.*;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by edward_chiang on 2013/11/23.
 */
public class MeFragment extends PlaceholderFragment {

    private TextView storiesSharedCountTextView;

    private TextView graphicEarnedCountTextView;

    private Menu menu;

    private TextView userNameTextView;

    private TextView sinceTextView;

    private ExpandableGridView galleryGridView;

    private GalleryArrayAdapter galleryArrayAdapter;

    private List<Graphic> userGraphicsList;

    public static MeFragment newInstance(int sectionNumber) {
        MeFragment fragment = new MeFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);

        userGraphicsList = new ArrayList<Graphic>();
        galleryArrayAdapter = new GalleryArrayAdapter(getActivity(), android.R.layout.simple_list_item_1, userGraphicsList);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_me, container, false);

        userNameTextView = (TextView)rootView.findViewById(R.id.user_name_text_view);

        sinceTextView = (TextView)rootView.findViewById(R.id.me_since_text_view);

        storiesSharedCountTextView = (TextView)rootView.findViewById(R.id.user_impact_stories_share_text_view);
        graphicEarnedCountTextView = (TextView)rootView.findViewById(R.id.user_impact_graphic_earned_text_view);

        galleryGridView = (ExpandableGridView)rootView.findViewById(R.id.me_graphic_gallery_grid_view);
        galleryGridView.setExpand(true);
        galleryGridView.setNumColumns(3);
        galleryGridView.setAdapter(galleryArrayAdapter);

        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        refreshProfile();
    }

    private void refreshProfile() {
        if (ParseUser.getCurrentUser() != null) {

            userNameTextView.setText(ParseUser.getCurrentUser().getString("name"));

            if (ParseUser.getCurrentUser().getString("name") == null) {
                ParseUser.getCurrentUser().fetchInBackground(new GetCallback<ParseObject>() {
                    @Override
                    public void done(ParseObject parseObject, ParseException e) {
                        userNameTextView.setText(parseObject.getString("name"));
                    }
                });
            }

            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
            if  (sinceTextView!=null && ParseUser.getCurrentUser().getCreatedAt()!=null) {
                sinceTextView.setText(getString(R.string.me_since_pre_text) + " " + dateFormat.format(ParseUser.getCurrentUser().getCreatedAt()));
            }


            getActivity().findViewById(R.id.me_profile_layout).setVisibility(View.VISIBLE);
            getActivity().findViewById(R.id.me_ask_login_layout).setVisibility(View.GONE);

            if (ParseUser.getCurrentUser().has("avatar")) {
                ParseObject avatarObject = ParseUser.getCurrentUser().getParseObject("avatar");

                avatarObject.fetchIfNeededInBackground(new GetCallback<ParseObject>() {
                    @Override
                    public void done(ParseObject parseObject, ParseException e) {
                        ImageView avatarImageView = (ImageView)getActivity().findViewById(R.id.user_avatar_image_view);
                        if (parseObject!=null && parseObject.getString("imageType").equals("url")) {
                            Picasso.with(getActivity())
                                    .load(parseObject.getString("imageUrl"))
                                    .transform(new CircleTransform())
                                    .into(avatarImageView);
                        }
                    }
                });
            }
            loadStories();
            loadGraphicEarned();
        } else {
            getActivity().findViewById(R.id.me_profile_layout).setVisibility(View.GONE);
            getActivity().findViewById(R.id.me_ask_login_layout).setVisibility(View.VISIBLE);

            getActivity().findViewById(R.id.me_ask_login_button).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent loginIntent = new Intent(getActivity(), LoginActivity.class);
                    startActivity(loginIntent);
                }
            });
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.me, menu);
        this.menu = menu;
    }
      
    private void loadStories() {
        ParseQuery<ParseObject> storyQuery = new ParseQuery<ParseObject>("Story");
        storyQuery.whereEqualTo("StoryTeller", ParseUser.getCurrentUser());
        storyQuery.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> parseObjects, ParseException e) {
                if (parseObjects != null) {

                    storiesSharedCountTextView.setText(String.valueOf(parseObjects.size()));

                    int reviewImpactCount = 0;
                    for (ParseObject eachStory : parseObjects) {
                        if (eachStory.has("reviewImpact")) {
                            reviewImpactCount += eachStory.getInt("reviewImpact");
                        }
                    }
                    TextView reviewStarsTextView = (TextView)getActivity().findViewById(R.id.user_impact_review_stars_text_view);
                    reviewStarsTextView.setText(String.valueOf(reviewImpactCount));

                }
            }
        });
    }


    private void loadGraphicEarned() {
        ParseQuery<ParseObject> graphicsEarnedQuery = ParseQuery.getQuery("GraphicsEarned");
        graphicsEarnedQuery.whereEqualTo("userId", ParseUser.getCurrentUser());
        graphicsEarnedQuery.setCachePolicy(ParseQuery.CachePolicy.CACHE_THEN_NETWORK);
        graphicsEarnedQuery.getFirstInBackground(new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject parseObject, ParseException e) {
                if (parseObject!=null) {
                    ParseRelation graphicsRelation = parseObject.getRelation("graphicsEarned");
                    ParseQuery<ParseObject> graphicsEarnedQuery = graphicsRelation.getQuery();
                    graphicsEarnedQuery.findInBackground(new FindCallback<ParseObject>() {
                        @Override
                        public void done(List<ParseObject> parseObjects, ParseException e) {
                            if (parseObjects!=null) {

                                graphicEarnedCountTextView.setText(String.valueOf(parseObjects.size()));

                                if (getActivity()!=null && !parseObjects.isEmpty()) {
                                    getActivity().findViewById(R.id.me_graphic_gallery_layout).setVisibility(View.VISIBLE);
                                }

                                userGraphicsList.clear();
                                for (ParseObject eachGraphicObject : parseObjects) {
                                    Graphic graphic = new ParseObjectManager(eachGraphicObject).getGraphic();
                                    userGraphicsList.add(graphic);
                                }
                                galleryArrayAdapter.notifyDataSetChanged();
                            }
                        }
                    });
                }
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case R.id.action_reload: {
                refreshProfile();
                break;
            }
        }

        return super.onOptionsItemSelected(item);
    }
}
