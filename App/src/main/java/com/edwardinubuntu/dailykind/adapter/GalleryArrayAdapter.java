package com.edwardinubuntu.dailykind.adapter;

import android.content.Context;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import com.edwardinubuntu.dailykind.R;
import com.edwardinubuntu.dailykind.object.Graphic;
import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Created by edward_chiang on 2014/1/10.
 */
public class GalleryArrayAdapter extends ArrayAdapter<Graphic> {
    /**
     * Constructor
     *
     * @param context  The current context.
     * @param resource The resource ID for a layout file containing a TextView to use when
     *                 instantiating views.
     * @param objects  The objects to represent in the ListView.
     */
    public GalleryArrayAdapter(Context context, int resource, List<Graphic> objects) {
        super(context, resource, objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View rootView = convertView;
        if (rootView == null) {
            rootView = inflater.inflate(R.layout.cell_graphic_image, null);
        }

        Graphic currentGraphic = getItem(position);

        String graphicImageUrl = currentGraphic.getFileType().equalsIgnoreCase("file") ?
                currentGraphic.getParseFileUrl() : currentGraphic.getImageUrl();

        ImageView imageView = (ImageView)rootView.findViewById(R.id.graphic_image_view);
        DisplayMetrics displayMetrics = getContext().getResources().getDisplayMetrics();
        LinearLayout.LayoutParams imageViewLayoutParams = (LinearLayout.LayoutParams)imageView.getLayoutParams();
        imageViewLayoutParams.width = displayMetrics.widthPixels / 3;
        imageViewLayoutParams.height = displayMetrics.widthPixels / 3;
        imageView.requestLayout();

        Picasso.with(getContext())
                .load(graphicImageUrl)
                .resize(imageViewLayoutParams.width, imageViewLayoutParams.height)
                .into(imageView);

        return rootView;
    }
}
