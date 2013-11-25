package com.edwardinubuntu.dailykind.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.edwardinubuntu.dailykind.R;
import com.parse.*;

import java.text.SimpleDateFormat;

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

        TextView userNameTextView = (TextView)rootView.findViewById(R.id.me_username_text_view);
        if (ParseUser.getCurrentUser() != null) {
            userNameTextView.setText(ParseUser.getCurrentUser().getString("username"));
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

        ParseQuery<ParseObject> userQuery = ParseQuery.getQuery("UserImpact");
        userQuery.whereEqualTo("User", ParseUser.getCurrentUser());
        userQuery.getFirstInBackground(new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject parseObject, ParseException e) {
                storiesSharedCountTextView.setText(parseObject.getNumber("sharedStoriesCount") + " " + getString(R.string.me_number_of_stories_shared));
            }
        });
    }
}
