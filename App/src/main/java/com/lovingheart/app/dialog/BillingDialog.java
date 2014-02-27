package com.lovingheart.app.dialog;

import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.view.Window;
import com.android.vending.billing.IInAppBillingService;
import com.android.vending.util.IabHelper;
import com.android.vending.util.IabResult;
import com.android.vending.util.Inventory;
import com.beardedhen.androidbootstrap.BootstrapButton;
import com.lovingheart.app.DailyKind;
import com.lovingheart.app.R;

import java.util.ArrayList;

/**
 * Created by edward_chiang on 2014/2/27.
 */
public class BillingDialog extends Dialog {

    private IInAppBillingService billingService;

    private IabHelper iabHelper;

    private BootstrapButton upgradeMonthlyButton;

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

    public BillingDialog(Context context, boolean cancelable, OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.fragment_upgrade_to_premium);

        setTitle("Upgrade to Premium");

        upgradeMonthlyButton = (BootstrapButton)findViewById(R.id.upgrade_monthly_billing_button);

        // compute your public key and store it in base64EncodedPublicKey
        String base64EncodedPublicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAqavqOWQFio1oDfW8HUppNtuMxeB7uAZ8pRvn+aHGOXvg4OFfsrzlJkDHOqWAbnJKbU4vpPTH7TOgkeHl/PcztR1BA1P6Y2RZaPlMLvL838ZKgg0glro38QfMbrKFgzMjJ4jZAKEwGcc2feNnp3WGyRKnJnjds9sHK3Yg2G0jD6xanCng50z7+mC4Bt+zKtcSuCDoFq35H5WVhl32ecY+vPoorHYZFLB0LQ2miWdPmNb5WfY80PJy3ZuS1MIgDyBPr1TukCrJnpMpWQ9n57HsDwHbeNHw7Z4qfxuewLGInJuxQC0wY+JTgMQnrc0hxOE86YhlC5PTWvdKpjaQT47giwIDAQAB";
        iabHelper = new IabHelper(getContext(), base64EncodedPublicKey);

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
                                upgradeMonthlyButton.setText(
                                        inv.getSkuDetails("personal_happiness_report_monthly").getPrice()
                                                + getContext().getString(R.string.space)
                                                + getContext().getString(R.string.slash)
                                                + getContext().getString(R.string.space)
                                                + getContext().getString(R.string.upgrade_premium_monthly_button_unit)

                                );
                                upgradeMonthlyButton.requestLayout();
                            }
                        }
                    });
                }
            }
        });

        getContext().bindService(new Intent("com.android.vending.billing.InAppBillingService.BIND"), serviceConnection, Context.BIND_AUTO_CREATE);

        upgradeMonthlyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(DailyKind.TAG, "User start purchasing.");
            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (billingService != null) {
            getContext().unbindService(serviceConnection);
        }
        if (iabHelper!=null) iabHelper.dispose();
        iabHelper = null;
    }
}
