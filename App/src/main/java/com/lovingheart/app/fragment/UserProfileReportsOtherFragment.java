package com.lovingheart.app.fragment;

import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.google.analytics.tracking.android.Fields;
import com.google.analytics.tracking.android.MapBuilder;
import com.lovingheart.app.DailyKind;
import com.lovingheart.app.R;
import com.lovingheart.app.dialog.ShareBillingDialog;
import com.lovingheart.app.util.AnalyticsManager;
import com.parse.*;

import java.util.Calendar;
import java.util.List;

/**
 * Created by edward_chiang on 2014/3/4.
 */
public class UserProfileReportsOtherFragment extends UserProfileReportsFragment {

    private ShareBillingDialog shareBillingDialog;

    public static UserProfileReportsOtherFragment newInstance(int sectionNumber) {
        UserProfileReportsOtherFragment fragment = new UserProfileReportsOtherFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        shareBillingDialog = new ShareBillingDialog(getActivity(), true, new DialogInterface.OnCancelListener() {
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

        View rootView = inflater.inflate(R.layout.fragment_user_reports_other, container, false);

        createViews(rootView);

        return rootView;
    }

    @Override
    protected void createViews(View rootView) {
        super.createViews(rootView);
        billingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shareBillingDialog.show();
            }
        });
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Check if the user has premium
        setQueryLoading(true);
        updateRefreshItem();
        queryProfile(new ProfileCallBack());
    }

    protected class ProfileCallBack extends GetCallback<ParseUser> {

        @Override
        public void done(final ParseUser parseUser, ParseException e) {
            setQueryLoading(false);
            updateRefreshItem();
            if (parseUser != null && parseUser.getObjectId() != null) {

                shareBillingDialog.setParseUser(parseUser);

                ParseQuery<ParseObject> premiumQuery = ParseQuery.getQuery("Premium");
                premiumQuery.whereEqualTo("UserId", parseUser);
                premiumQuery.orderByDescending("createdAt");
                premiumQuery.setCachePolicy(ParseQuery.CachePolicy.CACHE_ELSE_NETWORK);
                premiumQuery.setMaxCacheAge(DailyKind.QUERY_MAX_CACHE_AGE);
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
                                // Show the purchase button
                                Log.d(DailyKind.TAG, "It's out of date, " + purchaseTimeCal.toString());
                                showNeedUpgradeWord = true;
                            }

                        } else {
                            // Show the purchase button
                            Log.d(DailyKind.TAG, "Can't find any purchase record.");
                            showNeedUpgradeWord = true;
                        }

                        if (showNeedUpgradeWord) {
                            premiumLockTextView.setText(getResources().getString(R.string.premium_lock_ask_part_1) +
                                        getResources().getString(R.string.space) +
                                        parseUser.getString("name") +
                                        getString(R.string.space) +
                                        getResources().getString(R.string.premium_lock_ask_part_2));

                            billingButton.setText(getString(R.string.upgrade_to_premium_share_to) +
                                    getString(R.string.space) +
                                    parseUser.getString("name"));

                            premiumLockTextView.setVisibility(View.VISIBLE);
                        } else {
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
    public void onStart() {
        super.onStart();
        AnalyticsManager.getInstance().getGaTracker().send(
                MapBuilder.createAppView().set(
                        Fields.SCREEN_NAME, UserProfileReportsOtherFragment.class.getName())
                        .build());
    }
}
