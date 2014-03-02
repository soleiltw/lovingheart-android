package com.lovingheart.app.fragment;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.*;
import com.android.vending.billing.IInAppBillingService;
import com.beardedhen.androidbootstrap.BootstrapButton;
import com.lovingheart.app.DailyKind;
import com.lovingheart.app.R;
import com.lovingheart.app.adapter.PersonalReportAdapter;
import com.lovingheart.app.dialog.BillingDialog;
import com.lovingheart.app.object.Info;
import com.lovingheart.app.util.ReportManager;
import com.lovingheart.app.view.ExpandableListView;
import com.parse.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by edward_chiang on 2014/3/2.
 */
public class UserProfileReportsFragment extends UserProfileFragment {

    private ReportManager reportManager = new ReportManager();

    private View loadingProgressBar;

    private Menu menu;

    private IInAppBillingService billingService;

    private BillingDialog billingDialog;

    protected ExpandableListView personalReportListView;

    private List<Info> reportWordings;

    private PersonalReportAdapter personalReportAdapter;

    private boolean queryLoading;

    private BootstrapButton billingButton;

    protected View billingView;

    public static UserProfileReportsFragment newInstance(int sectionNumber) {
        UserProfileReportsFragment fragment = new UserProfileReportsFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        reportWordings = new ArrayList<Info>();

        personalReportAdapter = new PersonalReportAdapter(getActivity(), android.R.layout.simple_list_item_1, reportWordings);


        billingDialog = new BillingDialog(getActivity(), true, new DialogInterface.OnCancelListener() {
            /**
             * This method will be invoked when the dialog is canceled.
             *
             * @param dialog The dialog that was canceled will be passed into the
             *               method.
             */
            @Override
            public void onCancel(DialogInterface dialog) {
                dialog.dismiss();
            }
        });

        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_user_reports, container, false);

        personalReportListView = (ExpandableListView)rootView.findViewById(R.id.personal_report_list_view);
        personalReportListView.setExpand(true);
        personalReportListView.setAdapter(personalReportAdapter);

        loadingProgressBar = rootView.findViewById(R.id.loading_progress_bar);

        billingButton = (BootstrapButton)rootView.findViewById(R.id.user_profile_billing_button);

        billingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                billingDialog.show();
            }
        });

        billingView = rootView.findViewById(R.id.user_profile_billing_layout);

        return rootView;
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

        queryProfile(new ProfileCallBack());
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

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.me, menu);
        this.menu = menu;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (billingDialog != null && billingDialog.getIabHelper() != null) {
            billingDialog.getIabHelper().handleActivityResult(requestCode, resultCode, data);
        }
    }

    public boolean isQueryLoading() {
        return queryLoading;
    }

    public void setQueryLoading(boolean queryLoading) {
        this.queryLoading = queryLoading;
    }

    protected class ProfileCallBack extends GetCallback<ParseUser> {

        @Override
        public void done(ParseUser parseUser, ParseException e) {
            if (parseUser != null && parseUser.getObjectId() != null) {

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

            } else {
                Log.i(DailyKind.TAG, "Couldn't find the user profile : " + getUserId());
            }
            if (e != null) {
                Log.e(DailyKind.TAG, e.getLocalizedMessage());
            }
        }
    }
}
