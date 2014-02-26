package com.lovingheart.app.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import com.lovingheart.app.R;
import com.lovingheart.app.adapter.GalleryArrayAdapter;
import com.lovingheart.app.object.Graphic;
import com.lovingheart.app.util.parse.ParseObjectManager;
import com.parse.*;

import java.util.ArrayList;
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
            public void done(ParseUser parseUser, ParseException e) {
                if (parseUser != null) {
                    queryGraphicEarned(parseUser, new FindCallback<ParseObject>() {
                        @Override
                        public void done(List<ParseObject> parseObjects, ParseException e) {



                            if (parseObjects!=null && !parseObjects.isEmpty()) {

                                userGraphicsList.clear();
                                for (ParseObject eachGraphicObject : parseObjects) {
                                    Graphic graphic = new ParseObjectManager(eachGraphicObject).getGraphic();
                                    userGraphicsList.add(graphic);
                                }
                                galleryArrayAdapter.notifyDataSetChanged();

                                if (emptyTextView!=null){
                                    emptyTextView.setVisibility(View.GONE);
                                }

                            } else {
                                userGraphicsList.clear();
                                galleryArrayAdapter.notifyDataSetChanged();

                                if (emptyTextView!=null){
                                    emptyTextView.setVisibility(View.VISIBLE);
                                }
                            }
                        }
                    });
                }
            }
        });
    }
}
