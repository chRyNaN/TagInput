package com.chrynan.taginput.util;

import android.text.TextPaint;
import android.text.style.CharacterStyle;
import android.text.style.UpdateAppearance;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

/**
 * Created by chrynan on 9/13/2015.
 * Ideas from here:
 * http://stackoverflow.com/questions/7142378/how-can-i-use-ontouchlisteners-on-each-word-in-a-textview
 * http://stackoverflow.com/questions/20856105/change-the-text-color-of-a-single-clickablespan-when-pressed-without-affecting-o
 */
public abstract class TouchableSpan extends CharacterStyle implements UpdateAppearance{
    private String text;
    private TextView tagTextView;

    public TouchableSpan(String text){
        super();
        this.text = text;
    }

    public TouchableSpan(String text, TextView tagTextView){
        super();
        this.text = text;
        this.tagTextView = tagTextView;
    }

    @Override
    public abstract void updateDrawState(TextPaint tp);

    public abstract boolean onTouch(View widget, MotionEvent event, String text);

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public TextView getTagTextView() {
        return tagTextView;
    }

    public void setTagTextView(TextView tagTextView) {
        this.tagTextView = tagTextView;
    }
}
