package com.lovingheart.app.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import com.lovingheart.app.DailyKind;
import com.lovingheart.app.R;
import com.lovingheart.app.util.CircleTransform;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Created by edward_chiang on 2014/3/11.
 */
public class OrgListArrayAdapter extends ArrayAdapter<ParseObject> {


    /**
     * Constructor
     *
     * @param context  The current context.
     * @param resource The resource ID for a layout file containing a TextView to use when
     *                 instantiating views.
     * @param objects  The objects to represent in the ListView.
     */
    public OrgListArrayAdapter(Context context, int resource, List<ParseObject> objects) {
        super(context, resource, objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ParseObject orgObject = getItem(position);

        View rootView = convertView;
        if (rootView == null) {
            LayoutInflater inflater = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            rootView = inflater.inflate(R.layout.layout_org_image_view, null);
        }

        final ImageView orgImageView =  (ImageView)rootView.findViewById(R.id.org_image_view);
        if (orgObject.has("graphicPointer")) {
            Log.d(DailyKind.TAG, "Org has graphicPointer. "  + orgObject.getObjectId());
            orgObject.getParseObject("graphicPointer").fetchIfNeededInBackground(new GetCallback<ParseObject>() {
                @Override
                public void done(ParseObject parseObject, ParseException e) {
                    if (parseObject != null && parseObject.has("imageFile")) {
                        Log.d(DailyKind.TAG, "Graphic image url: " + parseObject.getParseFile("imageFile").getUrl());

                        Picasso.with(getContext())
                                .load(parseObject.getParseFile("imageFile").getUrl())
                                .placeholder(R.drawable.ic_action_user)
                                .transform(new CircleTransform())
                                .into(orgImageView);
                    }
                }
            });
        }

        return rootView;
    }
}
