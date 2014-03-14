package com.lovingheart.app.fragment;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import com.beardedhen.androidbootstrap.BootstrapButton;
import com.lovingheart.app.R;
import com.lovingheart.app.adapter.PersonalReportAdapter;
import com.lovingheart.app.object.Info;
import com.lovingheart.app.util.ReportManager;
import com.lovingheart.app.view.ExpandableListView;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by edward_chiang on 2014/3/2.
 */
public abstract class UserProfileReportsFragment extends UserProfileFragment {

    public static final int NUMBER_OF_DAYS_TO_VIEW_REPORT = 31;

    protected ReportManager reportManager = new ReportManager();

    protected View loadingProgressBar;

    private Menu menu;

    protected ExpandableListView personalReportListView;

    protected List<Info> reportWordings;

    protected PersonalReportAdapter personalReportAdapter;

    private boolean queryLoading;

    protected BootstrapButton billingButton;

    protected View billingView;

    protected TextView premiumLockTextView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        reportWordings = new ArrayList<Info>();

        personalReportAdapter = new PersonalReportAdapter(getActivity(), android.R.layout.simple_list_item_1, reportWordings);

        setHasOptionsMenu(true);
    }

    protected void createViews(View rootView) {
        personalReportListView = (ExpandableListView)rootView.findViewById(R.id.personal_report_list_view);
        personalReportListView.setExpand(true);
        personalReportListView.setAdapter(personalReportAdapter);
        personalReportListView.setClickable(false);

        loadingProgressBar = rootView.findViewById(R.id.loading_progress_bar);

        billingButton = (BootstrapButton)rootView.findViewById(R.id.user_profile_billing_button);

        billingView = rootView.findViewById(R.id.user_profile_billing_layout);

        premiumLockTextView = (TextView)rootView.findViewById(R.id.premium_lock_text_view);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        reportManager.setAnalyseListener(new ReportManager.AnalyseListener() {
            @Override
            public void done() {
                reportWordings.clear();
                reportWordings.addAll(reportManager.getReportWordings());
                personalReportAdapter.notifyDataSetChanged();
            }
        });
    }

    public void updateRefreshItem() {
        if (menu != null) {
            MenuItem refreshItem = menu.findItem(R.id.action_reload);
            if (refreshItem != null) {
                if (isQueryLoading()) {
                    refreshItem.setActionView(R.layout.indeterminate_progress_action);
                    loadingProgressBar.setVisibility(View.VISIBLE);
                } else {
                    refreshItem.setActionView(null);

                    loadingProgressBar.setVisibility(View.GONE);
                }
            }
        }
    }

    protected void validPassShowReport(ParseUser parseUser) {
        reportManager.setUser(parseUser);

        queryStories(parseUser, new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> parseObjects, ParseException e) {
                if (parseObjects != null && parseObjects.size() > 0) {

                    reportManager.setStoriesObjects(parseObjects);
                    reportManager.analyse();
                }
            }
        });

        billingButton.setVisibility(View.GONE);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.me, menu);
        this.menu = menu;
    }

    public boolean isQueryLoading() {
        return queryLoading;
    }

    public void setQueryLoading(boolean queryLoading) {
        this.queryLoading = queryLoading;
    }


}
