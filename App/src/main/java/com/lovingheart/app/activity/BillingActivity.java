package com.lovingheart.app.activity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import com.android.vending.billing.IInAppBillingService;
import com.android.vending.util.IabHelper;
import com.android.vending.util.IabResult;
import com.android.vending.util.Inventory;
import com.lovingheart.app.DailyKind;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by edward_chiang on 2014/2/27.
 */
public class BillingActivity extends ActionBarActivity {

    private IInAppBillingService billingService;

    private IabHelper iabHelper;

    private AsyncTask<Void, Void, Bundle> task;

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            billingService = IInAppBillingService.Stub.asInterface(service);

            task.execute();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            billingService = null;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // compute your public key and store it in base64EncodedPublicKey
        String base64EncodedPublicKey = new String();
        iabHelper = new IabHelper(this, base64EncodedPublicKey);

        final ArrayList<String> skuList = new ArrayList<String>();
        skuList.add("personal_happiness_report_monthly");

        iabHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
            /**
             * Called to notify that setup is complete.
             *
             * @param result The result of the setup process.
             */
            @Override
            public void onIabSetupFinished(IabResult result) {
                if (!result.isSuccess()) {
                    Log.d(DailyKind.TAG, "Problem setting up In-app Billing" + result);
                } else {
                    Log.d(DailyKind.TAG, "Hooray, IAB is fully set up!");

                    iabHelper.queryInventoryAsync(true, skuList, new IabHelper.QueryInventoryFinishedListener() {
                        @Override
                        public void onQueryInventoryFinished(IabResult result, Inventory inv) {
                            if (!result.isFailure()) {
                                Log.d(DailyKind.TAG, "onQueryInventoryFinished Result success.");

                                String price = inv.getSkuDetails("personal_happiness_report_monthly").getPrice();
                                Log.d(DailyKind.TAG, "Price: "+price);
                            }
                        }
                    });
                }
            }
        });

        bindService(new Intent("com.android.vending.billing.InAppBillingService.BIND"), serviceConnection, Context.BIND_AUTO_CREATE);

        task = new AsyncTask<Void, Void, Bundle>() {
            /**
             * Override this method to perform a computation on a background thread. The
             * specified parameters are the parameters passed to {@link #execute}
             * by the caller of this task.
             * <p/>
             * This method can call {@link #publishProgress} to publish updates
             * on the UI thread.
             *
             * @param params The parameters of the task.
             * @return A result, defined by the subclass of this task.
             * @see #onPreExecute()
             * @see #onPostExecute
             * @see #publishProgress
             */
            @Override
            protected Bundle doInBackground(Void... params) {

                Bundle querySkus = new Bundle();
                querySkus.putStringArrayList("ITEM_ID_LIST", skuList);

                Bundle skuDetails = null;
                try {
                    skuDetails = billingService.getSkuDetails(3, getPackageName(), "subscription", querySkus);



                } catch (RemoteException e) {
                    Log.d(DailyKind.TAG, "Remote Exception: " + e.getLocalizedMessage());
                }
                return skuDetails;
            }

            @Override
            protected void onPostExecute(Bundle bundle) {
                super.onPostExecute(bundle);
                int response = bundle.getInt("RESPONSE_CODE");
                if (response == 0) {
                    ArrayList<String> responseList
                            = bundle.getStringArrayList("DETAILS_LIST");

                    for (String thisResponse : responseList) {
                        try {
                            JSONObject object = new JSONObject(thisResponse);
                            Log.d(DailyKind.TAG, "JSONObject: " + object);
                        } catch (JSONException e) {
                            Log.d(DailyKind.TAG, "JSONException: " + e.getLocalizedMessage());
                        }
                    }
                }
            }
        };
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);


    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        if (billingService != null) {
            unbindService(serviceConnection);
        }
        if (iabHelper!=null) iabHelper.dispose();
        iabHelper = null;
    }
}
