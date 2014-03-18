package com.lovingheart.app.dialog;

import android.app.Activity;
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
import android.widget.AdapterView;
import com.android.vending.billing.IInAppBillingService;
import com.android.vending.util.IabHelper;
import com.android.vending.util.IabResult;
import com.android.vending.util.Inventory;
import com.android.vending.util.Purchase;
import com.beardedhen.androidbootstrap.BootstrapButton;
import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.MapBuilder;
import com.lovingheart.app.DailyKind;
import com.lovingheart.app.R;
import com.lovingheart.app.activity.WebViewActivity;
import com.lovingheart.app.adapter.PremiumFeatureAdapter;
import com.lovingheart.app.object.PremiumFeature;
import com.lovingheart.app.view.ExpandableListView;
import com.parse.*;

import java.util.ArrayList;

/**
 * Created by edward_chiang on 2014/2/27.
 */
public class BillingDialog extends Dialog {

    public static final String PERSONAL_HAPPINESS_REPORT_MONTHLY = "personal_happiness_report_monthly";
    public static final String PERSONAL_HAPPINESS_REPORT_YEARLY = "personal_happiness_report_yearly";
    public static final String ANDROID_TEST_PURCHASED = "android.test.purchased";

    private IInAppBillingService billingService;

    private IabHelper iabHelper;

    protected BootstrapButton upgradeMonthlyButton;

    private static int PAYMENT_REQUEST_CODE = 100;

    private ExpandableListView premiumFeatureListView;

    private PremiumFeatureAdapter premiumFeatureAdapter;

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
        if (context instanceof Activity) {
            setOwnerActivity((Activity)context);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.fragment_upgrade_to_premium);

        setTitle(getContext().getString(R.string.billing_upgrade_to_premium));

        premiumFeatureListView = (ExpandableListView)findViewById(R.id.upgrade_to_premium_list_view);
        premiumFeatureListView.setExpand(true);


        final ArrayList<PremiumFeature> premiumFeatureArrayList = new ArrayList<PremiumFeature>();
        PremiumFeature reportFeature = new PremiumFeature();
        reportFeature.setImageSrc(R.drawable.ic_action_grow);
        reportFeature.setTitle(getContext().getString(R.string.upgrade_premium_energy_report));
        reportFeature.setDescriptionUrl("https://lovingheart.uservoice.com/knowledgebase/articles/327414-正向能量報告會有什麼呢-");
        premiumFeatureArrayList.add(reportFeature);

        PremiumFeature privateFeature = new PremiumFeature();
        privateFeature.setDescriptionUrl("https://lovingheart.uservoice.com/knowledgebase/articles/327417-書寫不公開故事");
        privateFeature.setImageSrc(R.drawable.ic_action_lock_closed);
        privateFeature.setTitle(getContext().getString(R.string.upgrade_premium_write_private));
        premiumFeatureArrayList.add(privateFeature);

        PremiumFeature adFreeFeature = new PremiumFeature();
        adFreeFeature.setImageSrc(R.drawable.inner_icon);
        adFreeFeature.setTitle(getContext().getString(R.string.upgrade_premium_no_add_network));
        premiumFeatureArrayList.add(adFreeFeature);

        premiumFeatureAdapter = new PremiumFeatureAdapter(getContext(), R.layout.cell_premium_feature, premiumFeatureArrayList);

        premiumFeatureListView.setAdapter(premiumFeatureAdapter);
        premiumFeatureListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                PremiumFeature premiumFeature = premiumFeatureArrayList.get(position);

                Intent webIntent = new Intent(getContext(), WebViewActivity.class);
                webIntent.putExtra("webUrl", premiumFeature.getDescriptionUrl());
                getContext().startActivity(webIntent);
            }
        });

        // compute your public key and store it in base64EncodedPublicKey
        String base64EncodedPublicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAqavqOWQFio1oDfW8HUppNtuMxeB7uAZ8pRvn+aHGOXvg4OFfsrzlJkDHOqWAbnJKbU4vpPTH7TOgkeHl/PcztR1BA1P6Y2RZaPlMLvL838ZKgg0glro38QfMbrKFgzMjJ4jZAKEwGcc2feNnp3WGyRKnJnjds9sHK3Yg2G0jD6xanCng50z7+mC4Bt+zKtcSuCDoFq35H5WVhl32ecY+vPoorHYZFLB0LQ2miWdPmNb5WfY80PJy3ZuS1MIgDyBPr1TukCrJnpMpWQ9n57HsDwHbeNHw7Z4qfxuewLGInJuxQC0wY+JTgMQnrc0hxOE86YhlC5PTWvdKpjaQT47giwIDAQAB";
        iabHelper = new IabHelper(getContext(), base64EncodedPublicKey);

        final ArrayList<String> skuList = new ArrayList<String>();
        skuList.add(PERSONAL_HAPPINESS_REPORT_MONTHLY);
        skuList.add(PERSONAL_HAPPINESS_REPORT_YEARLY);
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

                                Log.d(DailyKind.TAG, "Inv: "+inv.getSkuDetails(PERSONAL_HAPPINESS_REPORT_MONTHLY));

                                Purchase reportMonthlyPurchase =  inv.getPurchase(PERSONAL_HAPPINESS_REPORT_MONTHLY);

                                if (verifyDeveloperPayload(reportMonthlyPurchase)) {
                                    iabHelper.consumeAsync(reportMonthlyPurchase, consumeFinishedListener);
                                }

                                if (!inv.hasPurchase(PERSONAL_HAPPINESS_REPORT_MONTHLY) && inv.hasDetails(PERSONAL_HAPPINESS_REPORT_MONTHLY)) {
                                    upgradeMonthlyButton = (BootstrapButton)findViewById(R.id.upgrade_monthly_billing_button);
                                    upgradeMonthlyButton.setText(
                                            inv.getSkuDetails(PERSONAL_HAPPINESS_REPORT_MONTHLY).getPrice()
                                                    + getContext().getString(R.string.space)
                                                    + getContext().getString(R.string.slash)
                                                    + getContext().getString(R.string.space)
                                                    + getContext().getString(R.string.upgrade_premium_year_button_unit)

                                    );
                                    upgradeMonthlyButton.requestLayout();
                                } else {
                                    String successText = getContext().getString(R.string.billing_premium_has_becom_member_thanks);
                                    upgradeMonthlyButton.setText(successText);
                                    upgradeMonthlyButton.setEnabled(false);
                                }

                            } else {
                                Log.d(DailyKind.TAG, "onQueryInventoryFinished Result. " +  result);
                            }
                        }
                    });
                }
            }
        });

    }


    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (getOwnerActivity() != null) {

            getOwnerActivity().bindService(new Intent("com.android.vending.billing.InAppBillingService.BIND"), serviceConnection, Context.BIND_AUTO_CREATE);

            upgradeMonthlyButton = (BootstrapButton)findViewById(R.id.upgrade_monthly_billing_button);
            upgradeMonthlyButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d(DailyKind.TAG, "User start purchasing.");

                    // TODO start loading
                    if  (iabHelper!=null) {
                        iabHelper.flagEndAsync();

                        iabHelper.launchPurchaseFlow(getOwnerActivity(), PERSONAL_HAPPINESS_REPORT_MONTHLY, PAYMENT_REQUEST_CODE, new IabHelper.OnIabPurchaseFinishedListener() {
                            @Override
                            public void onIabPurchaseFinished(IabResult result, Purchase purchase) {

                                Log.d(DailyKind.TAG, "onIabPurchaseFinished.");

                                if (purchase!= null && purchase.getSku()!=null) {
                                    if (purchase.getSku().equals("android.test.purchased")) {
                                        Log.d(DailyKind.TAG, "Purchase success. Result :" + result);
                                    } else if (purchase.getSku().equals(PERSONAL_HAPPINESS_REPORT_MONTHLY)) {
                                        // consume the PERSONAL_HAPPINESS_REPORT_MONTHLY and update the UI
                                        Log.d(DailyKind.TAG, "Purchase success. Result :" + result);
                                        iabHelper.consumeAsync(purchase, consumeFinishedListener);

                                        EasyTracker easyTracker = EasyTracker.getInstance(getOwnerActivity());

                                        easyTracker.send(MapBuilder
                                        .createTransaction(purchase.getOrderId(),
                                                purchase.getItemType(),
                                                90.d,
                                                0.d,
                                                0.d,
                                                "NTD"
                                                ).build());

                                        easyTracker.send(MapBuilder
                                        .createItem(purchase.getOrderId(),
                                                purchase.getSku(),
                                                purchase.getSku(),
                                                purchase.getItemType(),
                                                90.d,
                                                1L,
                                                "NTD").build());
                                    }
                                }

                                if (result.isFailure()) {
                                    Log.d(DailyKind.TAG, "Error purchasing: " + result);
                                    return;
                                }

                            }
                        }, ParseUser.getCurrentUser().getObjectId());
                    }
                }
            });
        }
    }

    private IabHelper.OnConsumeFinishedListener consumeFinishedListener = new IabHelper.OnConsumeFinishedListener() {
        @Override
        public void onConsumeFinished(final Purchase purchase, IabResult result) {
            Log.d(DailyKind.TAG, "Purchase: " + purchase + ", IabResult: " + result);
            if (iabHelper == null) return;

            if (result.isSuccess() || (purchase.getDeveloperPayload() != null && purchase.getToken() != null)) {

                // Update to parse
                ParseQuery<ParseObject> premiumCheck = ParseQuery.getQuery("Premium");
                premiumCheck.whereEqualTo("UserId", ParseUser.getCurrentUser());
                premiumCheck.whereEqualTo("Sku", purchase.getSku());
                premiumCheck.whereEqualTo("OrderId", purchase.getOrderId());
                premiumCheck.getFirstInBackground(new GetCallback<ParseObject>() {
                    @Override
                    public void done(ParseObject parseObject, ParseException e) {
                        ParseObject premiumObject = null;
                        if (parseObject == null) {
                            premiumObject = new ParseObject("Premium");
                        } else {
                            premiumObject = parseObject;
                        }
                        premiumObject.put("UserId", ParseUser.getCurrentUser());
                        premiumObject.put("DeveloperPayload", purchase.getDeveloperPayload());
                        premiumObject.put("OrderId", purchase.getOrderId());
                        premiumObject.put("ItemType", purchase.getItemType());
                        premiumObject.put("PurchaseTime", purchase.getPurchaseTime());
                        premiumObject.put("Sku", purchase.getSku());
                        premiumObject.put("Signature", purchase.getSignature());
                        premiumObject.put("PackageName", purchase.getPackageName());
                        premiumObject.put("PurchaseState", purchase.getPurchaseState());

                        premiumObject.saveInBackground(new SaveCallback() {
                            @Override
                            public void done(ParseException e) {
                                if (e==null) {
                                    Log.d(DailyKind.TAG, "Premium purchase record updated.");
                                }
                            }
                        });
                    }
                });
            }

            if (result.isSuccess()) {
                // TODO
            } else {
                // We can check Items of type 'subs' can't be consumed. (response: -1010:Invalid consumption attempt)
                if (result != null) {
                    Log.e(DailyKind.TAG, result.getMessage());
                }
            }
        }
    };

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (billingService != null) {
            getContext().unbindService(serviceConnection);
        }
    }

    public IabHelper getIabHelper() {
        return iabHelper;
    }

    /** Verifies the developer payload of a purchase. */
    public boolean verifyDeveloperPayload(Purchase p) {
        if (p == null || p.getDeveloperPayload() == null) return false;
        String payload = p.getDeveloperPayload();

        /*
         * TODO: verify that the developer payload of the purchase is correct. It will be
         * the same one that you sent when initiating the purchase.
         *
         * WARNING: Locally generating a random string when starting a purchase and
         * verifying it here might seem like a good approach, but this will fail in the
         * case where the user purchases an item on one device and then uses your app on
         * a different device, because on the other device you will not have access to the
         * random string you originally generated.
         *
         * So a good developer payload has these characteristics:
         *
         * 1. If two different users purchase an item, the payload is different between them,
         *    so that one user's purchase can't be replayed to another user.
         *
         * 2. The payload must be such that you can verify it even when the app wasn't the
         *    one who initiated the purchase flow (so that items purchased by the user on
         *    one device work on other devices owned by the user).
         *
         * Using your own server to store and verify developer payloads across app
         * installations is recommended.
         */
        ParseQuery<ParseObject> premiumQuery = new ParseQuery<ParseObject>("Premium");
        premiumQuery.whereEqualTo("UserId", ParseUser.getCurrentUser());
        premiumQuery.orderByDescending("PurchaseTime");
        try {
            ParseObject premiumRecord = premiumQuery.getFirst();
            if (premiumRecord.has("DeveloperPayload")) {
                return premiumRecord.getString("DeveloperPayload").equalsIgnoreCase(payload);
            }
        } catch (ParseException e) {
            e.printStackTrace();
            Log.e(DailyKind.TAG, "ParseQuery get first: " + e.getLocalizedMessage());
        }
        Log.d(DailyKind.TAG, "Payload: "+ payload);

        return false;
    }
}
