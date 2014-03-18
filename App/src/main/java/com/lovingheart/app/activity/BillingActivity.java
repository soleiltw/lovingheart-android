package com.lovingheart.app.activity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import com.android.vending.billing.IInAppBillingService;
import com.android.vending.util.IabHelper;
import com.android.vending.util.IabResult;
import com.android.vending.util.Inventory;
import com.lovingheart.app.DailyKind;

import java.util.ArrayList;

/**
 * Created by edward_chiang on 2014/2/27.
 */
public class BillingActivity extends ActionBarActivity {

    private IInAppBillingService billingService;

    private IabHelper iabHelper;

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            billingService = IInAppBillingService.Stub.asInterface(service);
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

                                Log.d(DailyKind.TAG, "Inv: "+inv.getSkuDetails("personal_happiness_report_monthly"));
                            }
                        }
                    });
                }
            }
        });

        bindService(new Intent("com.android.vending.billing.InAppBillingService.BIND"), serviceConnection, Context.BIND_AUTO_CREATE);
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
