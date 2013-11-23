package com.edwardinubuntu.dailykind.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import com.edwardinubuntu.dailykind.R;
import com.edwardinubuntu.dailykind.activity.DeedCategoriesActivity;

/**
 * Created by edward_chiang on 2013/11/23.
 */
public class HomeFragment extends PlaceholderFragment {

    private Button goodDeedOfDayButton;

    public static HomeFragment newInstance(int sectionNumber) {
        HomeFragment fragment = new HomeFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_home, container, false);

        goodDeedOfDayButton = (Button)rootView.findViewById(R.id.home_good_deed_of_button);
        goodDeedOfDayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), DeedCategoriesActivity.class);
                startActivity(intent);
            }
        });

        return rootView;
    }
}
