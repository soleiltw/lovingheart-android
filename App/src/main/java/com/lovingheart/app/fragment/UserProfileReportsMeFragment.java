package com.lovingheart.app.fragment;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.google.analytics.tracking.android.Fields;
import com.lovingheart.app.DailyKind;
import com.lovingheart.app.R;
import com.lovingheart.app.dialog.BillingDialog;
import com.lovingheart.app.util.AnalyticsManager;
import com.lovingheart.app.util.parse.ParseObjectManager;
import com.parse.*;

import java.util.Calendar;
import java.util.HashMap;

/**
 * Created by edward_chiang on 2014/3/4.
 */
public class UserProfileReportsMeFragment extends UserProfileReportsFragment {

    protected BillingDialog billingDialog;

    public static UserProfileReportsMeFragment newInstance(int sectionNumber) {
        UserProfileReportsMeFragment fragment = new UserProfileReportsMeFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


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

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_user_reports_me, container, false);

        createViews(rootView);

        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Check if the user has premium
        setQueryLoading(true);
        updateRefreshItem();
        queryProfile(new ProfileCallBack());
    }

    @Override
    protected void createViews(View rootView) {
        super.createViews(rootView);
        billingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                billingDialog.show();
            }
        });
    }

    protected class ProfileCallBack extends GetCallback<ParseUser> {

        @Override
        public void done(final ParseUser parseUser, ParseException e) {
            setQueryLoading(false);
            updateRefreshItem();
            if (parseUser != null && parseUser.getObjectId() != null) {

                ParseQuery<ParseObject> premiumQuery = ParseQuery.getQuery("Premium");
                premiumQuery.whereEqualTo("UserId", parseUser);
                premiumQuery.orderByDescending("createdAt");
                premiumQuery.setCachePolicy(ParseQuery.CachePolicy.CACHE_ELSE_NETWORK);
                premiumQuery.setMaxCacheAge(DailyKind.QUERY_AT_LEAST_CACHE_AGE);
                premiumQuery.getFirstInBackground(new GetCallback<ParseObject>() {
                    @Override
                    public void done(ParseObject parseObject, ParseException e) {

                        boolean showNeedUpgradeWord = false;

                        if (parseObject != null) {
                            Calendar purchaseTimeCal = Calendar.getInstance();
                            long purchaseTimeInMillis = parseObject.getLong("PurchaseTime");
                            Log.d(DailyKind.TAG, "purchaseTimeInMillis: " + purchaseTimeInMillis);
                            purchaseTimeCal.setTimeInMillis(purchaseTimeInMillis);
                            purchaseTimeCal.add(Calendar.DAY_OF_MONTH, NUMBER_OF_DAYS_TO_VIEW_REPORT);
                            if (purchaseTimeCal.compareTo(Calendar.getInstance()) > 0) {
                                Log.d(DailyKind.TAG, "Purchased Date passed. " + purchaseTimeCal.toString());
                                showNeedUpgradeWord = false;
                            } else {
                                // Show the purchase button
                                Log.d(DailyKind.TAG, "It's out of date, " + purchaseTimeCal.toString());
                                showNeedUpgradeWord = true;
                            }

                        } else {
                            // Show the purchase button
                            Log.d(DailyKind.TAG, "Can't find any purchase record.");
                            showNeedUpgradeWord = true;
                        }

                        // Check if premium
                        if (ParseUser.getCurrentUser().has("premium")) {
                            String noCheck = ParseUser.getCurrentUser().getString("premium");
                            if (noCheck != null && DailyKind.PARSE_PREMIUM_NOCHECK.equalsIgnoreCase(noCheck)) {
                                showNeedUpgradeWord = false;
                            }
                        }

                        // TODO Unlock for all user
                        showNeedUpgradeWord = false;
                        if (showNeedUpgradeWord) {
                            premiumLockTextView.setText(getResources().getString(R.string.premium_lock_view_by_self_text));
                            premiumLockTextView.setVisibility(View.VISIBLE);
                            billingButton.setVisibility(View.VISIBLE);
                        } else {
                            ParseObjectManager.userLogDone("PLMhFQQ0BH");
                            ParseObjectManager.userLogDone("DM1l2ZZz1J");
                            validPassShowReport(parseUser);
                            premiumLockTextView.setVisibility(View.GONE);
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (billingDialog != null && billingDialog.getIabHelper() != null) {
            billingDialog.getIabHelper().handleActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        HashMap<String, String> gaParams = new HashMap<String, String>();
        gaParams.put(Fields.SCREEN_NAME, "User Profile Reports Me");
        gaParams.put(Fields.EVENT_ACTION, "View");
        gaParams.put(Fields.EVENT_CATEGORY, "User Profile Reports Me");
        gaParams.put(Fields.EVENT_LABEL, "user/" + getUserId());
        AnalyticsManager.getInstance().getGaTracker().send(gaParams);
    }

    @Override
    public void updateRefreshItem(boolean isLoading) {

    }
}
