package com.lovingheart.app.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import com.google.analytics.tracking.android.Fields;
import com.lovingheart.app.DailyKind;
import com.lovingheart.app.R;
import com.lovingheart.app.adapter.GalleryArrayAdapter;
import com.lovingheart.app.object.Graphic;
import com.lovingheart.app.util.AnalyticsManager;
import com.lovingheart.app.util.parse.ParseObjectManager;
import com.parse.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by edward_chiang on 2014/2/14.
 */
public class UserProfileGraphicsFragment extends UserProfileFragment {


    private GridView galleryGridView;

    private GalleryArrayAdapter galleryArrayAdapter;

    private List<Graphic> userGraphicsList;

    private View emptyTextView;

    public UserProfileGraphicsFragment() {
        super();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        userGraphicsList = new ArrayList<Graphic>();
        galleryArrayAdapter = new GalleryArrayAdapter(getActivity(), android.R.layout.simple_list_item_1, userGraphicsList);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_user_profile_graphics, container, false);

        galleryGridView = (GridView)rootView.findViewById(R.id.me_graphic_gallery_grid_view);
        galleryGridView.setNumColumns(3);
        galleryGridView.setAdapter(galleryArrayAdapter);

        emptyTextView = rootView.findViewById(com.lovingheart.app.R.id.user_profile_graphics_empty_text_view);

        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        queryProfile(new GetCallback<ParseUser>() {
            @Override
            public void done(final ParseUser parseUser, ParseException e) {
                if (parseUser != null && isAdded()) {
                    Log.d(DailyKind.TAG, "Query User done.");
                    queryGraphicEarned(parseUser, new GetCallback<ParseObject>() {
                        @Override
                        public void done(ParseObject parseObject, ParseException e) {

                            emptyTextView.setVisibility(View.VISIBLE);

                            if (parseObject!=null) {
                                ParseRelation graphicsRelation = parseObject.getRelation("graphicsEarned");
                                ParseQuery<ParseObject> graphicsEarnedQuery = graphicsRelation.getQuery();
                                graphicsEarnedQuery.findInBackground(new FindCallback<ParseObject>() {
                                    @Override
                                    public void done(List<ParseObject> parseObjects, ParseException e) {
                                        Log.d(DailyKind.TAG, "Query Graphic Earned done.");

                                        if (parseObjects != null && !parseObjects.isEmpty()) {
                                            userGraphicsList.clear();
                                            for (ParseObject eachGraphicObject : parseObjects) {
                                                Graphic graphic = new ParseObjectManager(eachGraphicObject).getGraphic();
                                                userGraphicsList.add(graphic);
                                            }
                                            galleryArrayAdapter.notifyDataSetChanged();
                                            emptyTextView.setVisibility(View.GONE);
                                        }
                                    }
                                });
                            }

                        }
                    });
                }
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();

        HashMap<String, String> gaParams = new HashMap<String, String>();
        gaParams.put(Fields.SCREEN_NAME, "User Profile Graphics");
        gaParams.put(Fields.EVENT_ACTION, "View");
        gaParams.put(Fields.EVENT_CATEGORY, "User Profile Graphics");
        gaParams.put(Fields.EVENT_LABEL, "user/" + getUserId());
        AnalyticsManager.getInstance().getGaTracker().send(gaParams);
    }

    @Override
    public void updateRefreshItem(boolean isLoading) {

    }
}
