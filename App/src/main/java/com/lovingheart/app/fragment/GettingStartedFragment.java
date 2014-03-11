package com.lovingheart.app.fragment;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
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

    private View progressLoadingView;

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

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ParseObject gettingObject = gettingObjects.get(position);

                if (gettingObject.has("webUrl")) {

                    AlertDialog.Builder alertBuilder = new AlertDialog.Builder(getActivity());
                    alertBuilder.setTitle(gettingObject.getString("title"));

                    WebView webView = new WebView(getActivity());
                    webView.loadUrl(gettingObject.getString("webUrl"));
                    webView.setWebViewClient(new WebViewClient(){
                        /**
                         * Give the host application a chance to take over the control when a new
                         * url is about to be loaded in the current WebView. If WebViewClient is not
                         * provided, by default WebView will ask Activity Manager to choose the
                         * proper handler for the url. If WebViewClient is provided, return true
                         * means the host application handles the url, while return false means the
                         * current WebView handles the url.
                         * This method is not called for requests using the POST "method".
                         *
                         * @param view The WebView that is initiating the callback.
                         * @param url  The url to be loaded.
                         * @return True if the host application wants to leave the current WebView
                         * and handle the url itself, otherwise return false.
                         */
                        @Override
                        public boolean shouldOverrideUrlLoading(WebView view, String url) {
                            view.loadUrl(url);
                            return true;
                        }
                    });

                    alertBuilder.setView(webView);
                    alertBuilder.setNegativeButton(getString(R.string.close), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    alertBuilder.show();
                }

            }
        });

        progressLoadingView = rootView.findViewById(R.id.loading_progress_bar);

        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ParseQuery<ParseObject> gettingStartedQuery = new ParseQuery<ParseObject>("GettingStarted");
        gettingStartedQuery.orderByAscending("sequence");
        updateRefreshItem(true);
        gettingStartedQuery.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> parseObjects, ParseException e) {
                updateRefreshItem(false);
                if (parseObjects.size() > 0) {
                    gettingObjects.clear();
                    gettingObjects.addAll(parseObjects);
                    gettingStartedAdapter.notifyDataSetChanged();
                }
            }
        });
    }

    @Override
    public void updateRefreshItem(boolean isLoading) {
        if (isLoading) {
            progressLoadingView.setVisibility(View.VISIBLE);
        } else {
            progressLoadingView.setVisibility(View.GONE);
        }
    }
}
