package com.edwardinubuntu.dailykind.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.edwardinubuntu.dailykind.R;
import com.parse.ParseUser;

/**
 * Created by edward_chiang on 2013/11/23.
 */
public class MeFragment extends PlaceholderFragment {

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
        userNameTextView.setText(ParseUser.getCurrentUser().getString("username"));

        TextView sinceTextView = (TextView)rootView.findViewById(R.id.me_since_text_view);
        if (ParseUser.getCurrentUser().getCreatedAt() != null) {
            sinceTextView.setText(ParseUser.getCurrentUser().getCreatedAt().toString());
        }

        return rootView;
    }
}
