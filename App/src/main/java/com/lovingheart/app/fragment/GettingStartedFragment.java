package com.lovingheart.app.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import com.lovingheart.app.R;
import com.lovingheart.app.adapter.GettingStartedAdapter;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by edward_chiang on 2014/3/11.
 */
public class GettingStartedFragment extends PlaceholderFragment {

    private GettingStartedAdapter gettingStartedAdapter;

    private ListView listView;

    private java.util.List<ParseObject> gettingObjects;

    public static GettingStartedFragment newInstance(int sectionNumber) {
        GettingStartedFragment fragment = new GettingStartedFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    public GettingStartedFragment() {

        gettingObjects = new ArrayList<ParseObject>();


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_getting_started, container, false);

        listView = (ListView)rootView.findViewById(R.id.content_list_view);

        gettingStartedAdapter = new GettingStartedAdapter(getActivity(), android.R.layout.simple_list_item_2, gettingObjects);

        listView.setAdapter(gettingStartedAdapter);

        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ParseQuery<ParseObject> gettingStartedQuery = new ParseQuery<ParseObject>("GettingStarted");
        gettingStartedQuery.orderByAscending("sequence");
        gettingStartedQuery.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> parseObjects, ParseException e) {
                if (parseObjects.size() > 0) {
                    gettingObjects.clear();
                    gettingObjects.addAll(parseObjects);
                    gettingStartedAdapter.notifyDataSetChanged();
                }
            }
        });
    }
}
