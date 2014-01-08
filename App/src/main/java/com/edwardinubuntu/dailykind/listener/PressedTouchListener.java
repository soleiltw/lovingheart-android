package com.edwardinubuntu.dailykind.listener;

import android.view.MotionEvent;
import android.view.View;
import com.edwardinubuntu.dailykind.R;

/**
 * Created by edward_chiang on 2013/12/30.
 */
public class PressedTouchListener implements View.OnTouchListener {

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        switch (motionEvent.getAction()) {
            case MotionEvent.ACTION_DOWN:
                view.setBackgroundResource(R.color.pressed_lovingheart);
                break;
            case MotionEvent.ACTION_POINTER_UP:
            case MotionEvent.ACTION_UP:
                view.performClick();
            case MotionEvent.ACTION_CANCEL:
            default:
                view.setBackgroundResource(R.color.pressed_clear);
                break;
        }
        return true;
    }
}
