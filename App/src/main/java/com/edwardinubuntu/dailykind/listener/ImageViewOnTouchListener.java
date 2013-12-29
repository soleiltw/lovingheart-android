package com.edwardinubuntu.dailykind.listener;

import android.graphics.Color;
import android.graphics.PorterDuff;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

/**
 * Created by edward_chiang on 2013/12/29.
 */
public class ImageViewOnTouchListener implements View.OnTouchListener {

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {

        switch (motionEvent.getAction()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_POINTER_DOWN:
                ((ImageView)view).setColorFilter(Color.LTGRAY, PorterDuff.Mode.MULTIPLY);
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP:
                ((ImageView)view).clearColorFilter();
                view.performClick();
                break;
            default:
                ((ImageView)view).clearColorFilter();
        }
        return true;
    }
}
