package com.chrynan.taginput.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.os.Build;
import android.support.design.widget.TextInputLayout;
import android.support.v4.view.ViewCompat;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

/**
 * Created by chrynan on 9/14/2015.
 * Make sure to specify the textColorHint and the ellipsize="end"
 * Needed because of bug:
 * https://code.google.com/p/android/issues/detail?id=175228
 * Fix ideas found here:
 * https://gist.github.com/ljubisa987/e33cd5597da07172c55d
 * http://stackoverflow.com/a/30608974/1478764
 * https://gist.github.com/AfzalivE/eea5918ac0c61eb08343
 */

public class TextInputWrapper extends TextInputLayout {

    private boolean mIsHintSet = false;
    private CharSequence mHint;

    public TextInputWrapper(Context context) {
        super(context);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            setTransition();
        }
    }

    public TextInputWrapper(Context context, AttributeSet attrs) {
        super(context, attrs);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            setTransition();
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void setTransition(){
        setTransitionGroup(true);
    }

    @Override
    public void addView(View child, int index, ViewGroup.LayoutParams params) {
        if (child instanceof EditText) {
            // Since hint will be nullify on EditText once on parent addView, store hint value locally
            mHint = ((EditText)child).getHint();
        }
        super.addView(child, index, params);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (!mIsHintSet && ViewCompat.isLaidOut(this)) {
            // We have to reset the previous hint so that equals check pass
            setHint(null);

            // In case that hint is changed programatically
            EditText editText = getEditText();
            if(editText != null) {
                CharSequence currentEditTextHint = editText.getHint();
                if (!TextUtils.isEmpty(currentEditTextHint)) {
                    mHint = currentEditTextHint;
                    editText.setHint("");
                }
                setHint(mHint);
                mIsHintSet = true;
            }
        }
    }

}
