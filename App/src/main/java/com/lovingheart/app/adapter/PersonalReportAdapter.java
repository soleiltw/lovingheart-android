package com.lovingheart.app.adapter;

import android.content.Context;
import android.text.Html;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.lovingheart.app.R;
import com.lovingheart.app.object.Info;
import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Created by edward_chiang on 2014/2/17.
 */
public class PersonalReportAdapter extends ArrayAdapter<Info> {

    /**
     * Constructor
     *
     * @param context  The current context.
     * @param resource The resource ID for a layout file containing a TextView to use when
     *                 instantiating views.
     * @param objects  The objects to represent in the ListView.
     */
    public PersonalReportAdapter(Context context, int resource, List<Info> objects) {
        super(context, resource, objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View rootView;

        Info currentInfo = getItem(position);

        LayoutInflater inflater = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        if (currentInfo.getGraphicDirection()!=null) {
            // Because we want to update the first view
            switch (currentInfo.getGraphicDirection()) {
                case RIGHT:
                    rootView = inflater.inflate(R.layout.cell_report_item_image_right, null);
                    break;
                case LEFT:
                default:
                    rootView = inflater.inflate(R.layout.cell_report_item_image_left, null);
                    break;
            }
        } else {
            rootView = inflater.inflate(R.layout.cell_report_item_image_left, null);
        }

        TextView reportTitleTextView = (TextView)rootView.findViewById(R.id.report_title_text_view);
        if (currentInfo.getTitle() != null && currentInfo.getTitle().length() > 0) {
            reportTitleTextView.setText(Html.fromHtml(currentInfo.getTitle()));
            reportTitleTextView.setVisibility(View.VISIBLE);
        } else {
            reportTitleTextView.setVisibility(View.GONE);
        }

        TextView descriptionTextView = (TextView)rootView.findViewById(R.id.report_description_text_view);
        descriptionTextView.setText(Html.fromHtml(currentInfo.getDescription()));

        ImageView graphicImageView = (ImageView)rootView.findViewById(R.id.graphic_image_view);

        if (currentInfo.getGraphicResource() > 0) {
            RelativeLayout.LayoutParams graphicImageViewLayout = (RelativeLayout.LayoutParams)graphicImageView.getLayoutParams();
            DisplayMetrics displayMetrics = getContext().getResources().getDisplayMetrics();
            graphicImageViewLayout.width = displayMetrics.widthPixels / 4;
            graphicImageViewLayout.height = displayMetrics.widthPixels / 4;
            graphicImageView.requestLayout();

            Picasso.with(getContext())
                    .load(currentInfo.getGraphicResource())
                    .resize(graphicImageViewLayout.width, graphicImageViewLayout.height)
                    .into(graphicImageView);

            graphicImageView.setVisibility(View.VISIBLE);
        } else {
            graphicImageView.setVisibility(View.GONE);
        }

        return rootView;
    }
}
