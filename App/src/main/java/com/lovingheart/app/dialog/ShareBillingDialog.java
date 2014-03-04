package com.lovingheart.app.dialog;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import com.beardedhen.androidbootstrap.BootstrapButton;
import com.lovingheart.app.R;
import com.lovingheart.app.util.parse.ParseObjectManager;
import com.parse.ParseInstallation;
import com.parse.ParsePush;
import com.parse.ParseQuery;
import com.parse.ParseUser;

/**
 * Created by edward_chiang on 2014/3/4.
 */
public class ShareBillingDialog extends BillingDialog {

    private BootstrapButton shareUpgradeButton;

    private ParseUser parseUser;

    public ShareBillingDialog(Context context, boolean cancelable, OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
        if (context instanceof Activity) {
            setOwnerActivity((Activity)context);
        }
    }

    public ParseUser getParseUser() {
        return parseUser;
    }

    public void setParseUser(ParseUser parseUser) {
        this.parseUser = parseUser;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (getOwnerActivity() != null) {
            upgradeMonthlyButton = (BootstrapButton)findViewById(R.id.upgrade_monthly_billing_button);
            upgradeMonthlyButton.setVisibility(View.GONE);

            shareUpgradeButton = (BootstrapButton)findViewById(R.id.share_upgrade_monthly_billing_button);
            shareUpgradeButton.setVisibility(View.VISIBLE);
            shareUpgradeButton.setText(
                    getContext().getString(R.string.upgrade_to_premium_share_to) +
                            getContext().getString(R.string.space) +
                            parseUser.getString(ParseObjectManager.USER_NAME)
            );
            shareUpgradeButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (parseUser != null) {
                    ParseQuery pushQuery = ParseInstallation.getQuery();
                    pushQuery.whereEqualTo("user", parseUser);

                    ParsePush push = new ParsePush();
                    push.setQuery(pushQuery);

                    StringBuffer message = new StringBuffer();
                    message.append(ParseUser.getCurrentUser().getString(ParseObjectManager.USER_NAME)
                            + getContext().getResources().getString(R.string.space)
                            + getContext().getResources().getString(R.string.share_billing_push_message));
                    push.setMessage(message.toString());
                    push.sendInBackground();
                    }
                }
            });
        }
    }
}
