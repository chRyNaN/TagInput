package com.chrynan.taginput.util;

import android.text.Layout;
import android.text.Spannable;
import android.text.method.LinkMovementMethod;
import android.view.MotionEvent;
import android.widget.TextView;

/**
 * Created by chrynan on 9/13/2015.
 */
public class LinkTouchMovementMethod extends LinkMovementMethod {
    private TouchableSpan lastTouchedSpan;

    @Override
    public boolean onTouchEvent(TextView textView, Spannable spannable, MotionEvent event){
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            lastTouchedSpan = getPressedSpan(textView, spannable, event);
            if(lastTouchedSpan != null){
                lastTouchedSpan.onTouch(textView, event, lastTouchedSpan.getText());
                return true;
            }
        }
        return super.onTouchEvent(textView, spannable, event);
    }

    private TouchableSpan getPressedSpan(TextView textView, Spannable spannable, MotionEvent event) {
        int x = (int) event.getX();
        int y = (int) event.getY();

        x -= textView.getTotalPaddingLeft();
        y -= textView.getTotalPaddingTop();

        x += textView.getScrollX();
        y += textView.getScrollY();

        Layout layout = textView.getLayout();
        int line = layout.getLineForVertical(y);
        int off = layout.getOffsetForHorizontal(line, x);

        TouchableSpan[] link = spannable.getSpans(off, off, TouchableSpan.class);
        TouchableSpan touchedSpan = null;
        if (link.length > 0) {
            touchedSpan = link[0];
        }
        return touchedSpan;
    }

    public TouchableSpan getLastTouchedSpan() {
        return lastTouchedSpan;
    }

    public void setLastTouchedSpan(TouchableSpan lastTouchedSpan) {
        this.lastTouchedSpan = lastTouchedSpan;
    }

}
