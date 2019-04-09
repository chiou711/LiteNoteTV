package com.cw.litenotetv.page;

import android.content.Context;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;
import android.view.MotionEvent;

// used for removing Accessibility warning: has setOnTouchListener called on it but does not override performClick
public class ImageViewCustom extends AppCompatImageView
{
    public ImageViewCustom(Context context){
        super(context);
    }

    public ImageViewCustom(Context context,AttributeSet attrs){
        super(context,attrs);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        super.onTouchEvent(event);

        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                return true;

            case MotionEvent.ACTION_UP:
                performClick();
                return true;
        }
        return false;
    }

    @Override
    public boolean performClick() {
        super.performClick();
        return true;
    }
}
