package com.edwardinubuntu.dailykind.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import com.edwardinubuntu.dailykind.R;
import com.edwardinubuntu.dailykind.object.Idea;

/**
 * Created by edward_chiang on 2013/11/23.
 */
public class DeedContentActivity extends ActionBarActivity {

    private Idea idea;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        setContentView(R.layout.activity_good_deed_content);

        idea = (Idea)getIntent().getSerializableExtra("idea");
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        TextView contentTextView = (TextView)findViewById(R.id.deed_content_text_view);
        contentTextView.setText(idea.getName());

        TextView numberOfPeopleTextView = (TextView)findViewById(R.id.number_of_people_involved_text_view);
        numberOfPeopleTextView.setText(getString(R.string.deed_of_number_of_people));

        findViewById(R.id.good_deed_content_now_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final AlertDialog confirmDialog = new AlertDialog.Builder(DeedContentActivity.this)
                        .setTitle("Deed")
                        .setMessage("I have done this.")
                        .setPositiveButton("Done", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int which) {
                                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(intent);
                            }
                        })
                        .setNegativeButton("Not now", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .setCancelable(false).create();
                confirmDialog.show();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: {
                finish();
                break;
            }
        }
        return super.onOptionsItemSelected(item);
    }
}
