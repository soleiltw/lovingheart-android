package com.lovingheart.app.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.lovingheart.app.R;
import com.lovingheart.app.object.PremiumFeature;

import java.util.List;

/**
 * Created by edward_chiang on 2014/3/2.
 */
public class PremiumFeatureAdapter extends ArrayAdapter<PremiumFeature> {

    /**
     * Constructor
     *
     * @param context  The current context.
     * @param resource The resource ID for a layout file containing a TextView to use when
     *                 instantiating views.
     * @param objects  The objects to represent in the ListView.
     */
    public PremiumFeatureAdapter(Context context, int resource, List<PremiumFeature> objects) {
        super(context, resource, objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View rootView = convertView;
        if (rootView == null) {
            rootView = inflater.inflate(R.layout.cell_premium_feature, null);
        }

        PremiumFeature premiumFeature = getItem(position);

        ImageView imageView = (ImageView)rootView.findViewById(R.id.premium_image_view);
        imageView.setImageResource(premiumFeature.getImageSrc());

        TextView textView = (TextView)rootView.findViewById(R.id.premium_text_view);
        textView.setText(premiumFeature.getTitle());

        return rootView;
    }
}
