package com.edwardinubuntu.dailykind.util;

import android.graphics.*;
import com.squareup.picasso.Transformation;

/**
 * Created by edward_chiang on 2013/12/30.
 */
public class RadiusTransform implements Transformation {

    private int radiusSize;

    public RadiusTransform(int radiusSize) {
        this.radiusSize = radiusSize;
    }

    @Override
    public Bitmap transform(Bitmap bitmap) {
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap
                .getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        final RectF rectF = new RectF(rect);
        final float roundPx = this.radiusSize;

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawRoundRect(rectF, roundPx, roundPx, paint);

        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);

        return output;
    }

    @Override
    public String key() {
        return "radius";
    }
}
