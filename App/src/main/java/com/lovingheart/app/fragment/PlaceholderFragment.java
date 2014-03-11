package com.lovingheart.app.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.lovingheart.app.R;

/**
 * Created by edward_chiang on 2013/11/23.
 */
public abstract class PlaceholderFragment extends Fragment {

    /**
     * The fragment argument representing the section number for this
     * fragment.
     */
    protected static final String ARG_SECTION_NUMBER = "section_number";

    public PlaceholderFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        TextView textView = (TextView) rootView.findViewById(R.id.section_label);
        if (getArguments() != null) {
            textView.setText(Integer.toString(getArguments().getInt(ARG_SECTION_NUMBER)));
        }
        return rootView;
    }


    public abstract void updateRefreshItem(boolean isLoading);
}
