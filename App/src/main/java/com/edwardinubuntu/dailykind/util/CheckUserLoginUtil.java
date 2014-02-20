package com.edwardinubuntu.dailykind.util;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import com.edwardinubuntu.dailykind.R;
import com.edwardinubuntu.dailykind.activity.LoginActivity;
import com.parse.ParseUser;

/**
 * Created by edward_chiang on 2014/1/23.
 */
public class CheckUserLoginUtil {

    public static int ASK_USER_LOGIN = 110;

    public static boolean hasLogin() {
        return ParseUser.getCurrentUser() != null && ParseUser.getCurrentUser().getUsername() != null;
    }

    public static String userId() {
        return ParseUser.getCurrentUser().getObjectId();
    }

    public static void askLoginDialog(final Context context, final Activity activity) {
        AlertDialog alertDialog = null;

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);

        alertDialogBuilder.setMessage(context.getString(R.string.ask_login_dialog_message))
                .setPositiveButton(context.getString(R.string.go), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent loginIntent = new Intent(context, LoginActivity.class);
                        if (activity != null) {
                            activity.startActivityForResult(loginIntent, ASK_USER_LOGIN);
                        } else if (context != null) {
                            context.startActivity(loginIntent);
                        }
                    }
                })
                .setNegativeButton(context.getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
        ;
        alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }
}
