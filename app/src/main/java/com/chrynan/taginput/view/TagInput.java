package com.chrynan.taginput.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v7.widget.AppCompatAutoCompleteTextView;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextPaint;
import android.text.TextWatcher;
import android.text.style.ImageSpan;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputConnectionWrapper;
import android.widget.AdapterView;
import android.widget.TextView;

import com.chrynan.taginput.R;
import com.chrynan.taginput.util.LinkTouchMovementMethod;
import com.chrynan.taginput.util.TouchableSpan;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by chrynan on 9/11/2015.
 * Some ideas used from the questions and answers at: http://stackoverflow.com/questions/10812316/contact-bubble-edittext
 */
public class TagInput extends AppCompatAutoCompleteTextView {
    private static final String TAG = "TagInput";
    private Context context;
    //for the actual text as the user types
    private final StringBuilder sb;
    //for the whole text and tags after the user enters new tag
    private final SpannableStringBuilder spannableStringBuilder;
    private List<String> tags;
    private boolean addTag = false;
    private boolean internalChange = false;
    private int lastStart = 0;
    private int lastDeleteIndex = -1;
    private int tagTextSize;
    private int tagBackground;
    private int tagCancelButtonResource;
    private Drawable tagCancelButton;
    private int tagTextColor;
    private List<OnTagListener> listeners;
    private List<AdapterView.OnItemSelectedListener> dropDownListeners;

    public TagInput(Context context) {
        super(context);
        Log.d(TAG, "TagInput constructor");
        this.context = context;
        this.sb = new StringBuilder();
        this.spannableStringBuilder = new SpannableStringBuilder();
        this.tags = new ArrayList<>();
        this.tagTextSize = 18;
        this.tagBackground = R.drawable.tag_input_background_oval;
        this.tagCancelButtonResource = R.drawable.tag_input_cancel_button;
        this.tagTextColor = R.color.white;
        this.listeners = new ArrayList<>();
        dropDownListeners = new ArrayList<>();
        this.setMovementMethod(new LinkTouchMovementMethod());
        initTextWatcher();
        initOnItemSelectedListener();

    }

    public TagInput(Context context, AttributeSet attrs) {
        super(context, attrs);
        Log.d(TAG, "TagInput constructor");
        this.context = context;
        this.sb = new StringBuilder();
        this.spannableStringBuilder = new SpannableStringBuilder();
        this.tags = new ArrayList<>();
        this.listeners = new ArrayList<>();
        dropDownListeners = new ArrayList<>();
        this.setMovementMethod(new LinkTouchMovementMethod());
        initAttributeSet(attrs);
        initTextWatcher();
        initOnItemSelectedListener();

    }

    public TagInput(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        Log.d(TAG, "TagInput constructor");
        this.context = context;
        this.sb = new StringBuilder();
        this.spannableStringBuilder = new SpannableStringBuilder();
        this.tags = new ArrayList<>();
        this.listeners = new ArrayList<>();
        dropDownListeners = new ArrayList<>();
        this.setMovementMethod(new LinkTouchMovementMethod());
        initAttributeSet(attrs);
        initTextWatcher();
        initOnItemSelectedListener();

    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public TagInput(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr);
        Log.d(TAG, "TagInput constructor");
        this.context = context;
        this.sb = new StringBuilder();
        this.spannableStringBuilder = new SpannableStringBuilder();
        this.tags = new ArrayList<>();
        this.listeners = new ArrayList<>();
        dropDownListeners = new ArrayList<>();
        this.setMovementMethod(new LinkTouchMovementMethod());
        initAttributeSet(attrs);
        initTextWatcher();
        initOnItemSelectedListener();

    }

    private void initAttributeSet(AttributeSet attrs){
        Log.d(TAG, "iniAttributeSet");
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.TagInput, 0, 0);
        try{
            this.tagTextSize = a.getInt(R.styleable.TagInput_tagTextSize, 18);
            this.tagBackground = a.getInt(R.styleable.TagInput_tagBackground, R.drawable.tag_input_background_oval);
            this.tagCancelButtonResource = a.getInt(R.styleable.TagInput_tagCancelButton, R.drawable.tag_input_cancel_button);
            this.tagTextColor = a.getInt(R.styleable.TagInput_tagTextColor, R.color.white);
        }catch(Exception e){
            Log.e(TAG, "Error initializing attribute set.");
            e.printStackTrace();
        }finally{
            a.recycle();
        }
    }

    private void initTextWatcher(){
        this.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                try {
                    Log.d(TAG, "lastStart = " + lastStart + "; s = " + s + "; start = " + start + "; before = " + before + "; count = " + count);
                    if (!internalChange) {
                        String e = s.subSequence(lastStart, start + count).toString();
                        int i = e.indexOf(System.getProperty("line.separator"));
                        if (i == -1) { //enter button was NOT pressed
                            addTag = false;
                        } else { //enter button was pressed; so add tag
                            Log.d(TAG, "i = " + i + "; e = " + e);
                            Log.d(TAG, "e.replace = " + e.replace(System.getProperty("line.separator"), ""));
                            //String Builder is used for temporary text before it is added as a tag
                            //needed for afterTextChanged call
                            sb.append(e.replace(System.getProperty("line.separator"), ""));
                            Log.d(TAG, "sb = " + sb.toString());
                            addTag = true;
                        }
                        //to prevent duplicate of the same deletes
                        //index is set back to -1 if any other change was made other than a delete
                        lastDeleteIndex = -1;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (!internalChange && addTag) {
                    Log.d(TAG, "afterTextChanged: not an internal change and addTag is true: sb = " + sb.toString());
                    addTag(sb.toString());
                } else if (internalChange) {
                    internalChange = false; //internal change was finished so set back to false
                }
            }
        });
    }

    private void initOnItemSelectedListener(){
        super.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (dropDownListeners != null) {
                    for (AdapterView.OnItemSelectedListener l : dropDownListeners) {
                        l.onItemSelected(parent, view, position, id);
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                if (dropDownListeners != null) {
                    for (AdapterView.OnItemSelectedListener l : dropDownListeners) {
                        l.onNothingSelected(parent);
                    }
                }
            }
        });
    }

    @Override
    public void setSelection(int index){
        Log.d(TAG, "setSelection: index = " + index + " lastStart = " + lastStart);
        //attempt to prevent user from selecting tags and inserting text before and between tags
        index = (index < lastStart) ? lastStart : index;
        index = (index > getText().length()) ? getText().length() : index;
        super.setSelection(index);
    }

    @Override
    public void setSelection(int start, int stop){
        Log.d(TAG, "setSelection: start = " + start + " stop = " + stop + " lastStart = " + lastStart);
        //attempt to prevent user from selecting tags and inserting text before and between tags
        if(start < lastStart){
            if((lastStart + stop) > getText().length()){
                super.setSelection(lastStart, getText().length());
            }else{
                super.setSelection(lastStart, lastStart + stop);
            }
        }else{
            super.setSelection(start, stop);
        }
    }

    @Override
    public void extendSelection(int index){
        if(index < lastStart){
            super.extendSelection(lastStart);
        }else{
            super.extendSelection(index);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event){
        //if the user tries to place the cursor before the end of the last tag prevent it from doing so
        final int offset = this.getOffsetForPosition(event.getX(), event.getY());
        if(offset < lastStart){
            super.onTouchEvent(event);
            setSelection(lastStart);
            return true;
        }else{
            return super.onTouchEvent(event);
        }
    }

    @Override
    public void setOnItemSelectedListener(AdapterView.OnItemSelectedListener listener){
        //behaves the same way as addOnItemSelectedListener so call that method
        addOnItemSelectedListener(listener);
    }

    public void addOnItemSelectedListener(AdapterView.OnItemSelectedListener listener){
        //adds a listener for when an item is selected from the dropdown list
        //only providing specialized methods for the OnItemSelectedListeners, other listener methods will perform
        //their natural task
        dropDownListeners.add(listener);
    }

    public boolean removeOnItemSelectedListener(AdapterView.OnItemSelectedListener listener){
        return dropDownListeners.remove(listener);
    }

    private void setTagText(String text){
        //internal method to set text, overrides any existing text
        //to avoid any mistakes and duplicates of text clear the previous editable before calling set text
        //actual complete text is held in the spannableStringBuilder global variable which does not get altered
        //here. Alterations to that variable are assumed to of taking place before this method call.
        //avoid making changes to the variable spannableStringBuilder and then calling this method with a different input.
        this.getText().clearSpans();
        this.getText().clear();
        //so we don't capture this input with the text watcher
        this.internalChange = true;
        //make sure the now previous text has been deleted from the string builder
        this.sb.delete(0, sb.length());
        this.sb.setLength(0);
        this.setText(text);
        int length = text.length() - 1;
        length = (length < 0) ? 0 : length;
        this.lastStart = length; //to avoid adding duplicates restart the last start index
        this.setSelection(length); //places the cursor at the end
    }

    private void setTagText(SpannableStringBuilder spanText){
        //internal method to set text, overrides any existing text
        //to avoid any mistakes and duplicates of text clear the previous editable before calling set text.
        //actual complete text is held in the spannableStringBuilder global variable which does not get altered
        //here. Alterations to that variable are assumed to of taking place before this method call.
        //avoid making changes to the variable spannableStringBuilder and then calling this method with a different input.
        this.getText().clearSpans();
        this.getText().clear();
        //so we don't capture this input with the text watcher
        this.internalChange = true;
        //make sure the now previous text has been deleted from the string builder
        this.sb.delete(0, sb.length());
        this.sb.setLength(0);
        this.setText(spanText);
        int length = spanText.length() - 1;
        length = (length < 0) ? 0 : length;
        this.lastStart = length; //to avoid adding duplicates restart the last start index
        this.setSelection(length); //places the cursor at the end
    }

    private void setTagText(){
        //this method calls setTagText(SpannableStringBuilder) with the global spannableStringBuilder variable
        //any changes you want displayed in the EditText should be made to the spannableStringBuilder before
        //this method call.
        setTagText(spannableStringBuilder);
    }

    private void clearText(){
        this.sb.delete(0, sb.length());
        this.sb.setLength(0);
        this.getText().clearSpans();
        this.getText().clear();
        this.spannableStringBuilder.clearSpans();
        this.spannableStringBuilder.clear();
        this.internalChange = true;
        this.setText("");
        this.lastStart = 0;
        this.setSelection(0);
    }

    public Editable getTagText(){
        //returns all the tags in text form within an editable object
        Editable e = new Editable.Factory().newEditable("");
        if(tags == null || tags.size() < 1){
            return e;
        }else{
            String s;
            for(int i = 0; i < tags.size(); i++){
                s = tags.get(i);
                e.append(s);
                if(!((i + 1) >= tags.size())){
                    e.append(", ");
                }
            }
            return e;
        }
    }

    public String getCurrentText(){
        //gets the text that hasn't been added as a tag yet
        return sb.toString();
    }

    public boolean isBlank(){
        return isBlank(getText().toString());
    }

    public boolean isNotBlank(){
        return isNotBlank(getText().toString());
    }

    private boolean isBlank(String text){
        if(text == null){
            return true;
        }else if(text.length() <= 0 || text.equals("")){
            return true;
        }else if(text.trim().length() <= 0){
            return true;
        }else if(text.equals(System.getProperty("line.separator"))){
            return true;
        }else{
            return false;
        }
    }

    private boolean isNotBlank(String text){
        return !isBlank(text);
    }

    public List<String> getTags(){
        return tags;
    }

    public void addTags(List<String> tags){
        for(String s : tags){
            addTag(s);
        }
    }

    public void addTag(String text){
        Log.d(TAG, "addTag: text = " + text);
        try{
            if(isNotBlank(text)) {
                text = text.trim();
                if(tags.contains(text)){
                    return;
                }
                Log.d(TAG, "addTag: after trim called: text = " + text);
                Log.d(TAG, "addTag: text is not empty");
                TextView tv = createTagTextView(text);
                Bitmap b = converViewToBitmap(tv);
                BitmapDrawable bd = new BitmapDrawable(context.getResources(), b);
                bd.setBounds(0, 0, b.getWidth(), b.getHeight());
                Log.d(TAG, "addTag: spannableStringBuilder before append = " + spannableStringBuilder.toString());
                spannableStringBuilder.append(text + " ");
                final ImageSpan imageSpan = new ImageSpan(bd);
                spannableStringBuilder.setSpan(imageSpan, spannableStringBuilder.length() - (text.length() + 1),
                        spannableStringBuilder.length() - 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                //add touchable span to handle touch events
                spannableStringBuilder.setSpan(new TouchableSpan(text) {
                    @Override
                    public void updateDrawState(TextPaint tp) {
                        tp.setUnderlineText(false);
                        tp.setAntiAlias(true);
                    }
                    @Override
                    public boolean onTouch(View widget, MotionEvent event, String t) {
                        if(t != null){
                            Log.d(TAG, "onTouch: text = " + t);
                            if(cancelPressed(imageSpan, this.getTagTextView(), event)){
                                removeTag(t);
                            }else {
                                alertListenersTagSelected(t);
                            }
                        }
                        return true;
                    }
                }, spannableStringBuilder.length() - (text.length() + 1), spannableStringBuilder.length() - 1,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                spannableStringBuilder.append(" "); //seems to fix a bug with the deleteSurroundingText() method
                Log.d(TAG, "addTag: spannableStringBuilder after append = " + spannableStringBuilder.toString());
                tags.add(text);
                alertListenersTagAdded(text);
                setTagText();
            } else {
                Log.d(TAG, "addTag: input is blank: set back to spannableStringBuilder text");
                //remove the blank space or enter key added
                setTagText();
            }
        }catch(Exception e){
            Log.e(TAG, "Error adding tag:");
            e.printStackTrace();
        }
    }

    public boolean removeTag(String text){
        //Now that I think of it, it may be faster just to clear the entire spannable string builder, remove the tag from the tags list,
        //and rebuild all the tags. If not faster, it may be much easier and more readable. Perhaps, I'll change it to that later.
        try{
            /*
            Log.d(TAG, "removeTag: text = " + text);
            String atString = spannableStringBuilder.toString();
            int startIndex = atString.indexOf(text);
            if(startIndex != -1) {
                Log.d(TAG, "removeTag: edittext contains text");
                //should only return one span but use loop just incase
                ImageSpan[] spans = spannableStringBuilder.getSpans(startIndex, text.length(), ImageSpan.class);
                for(ImageSpan s : spans){
                    Log.d(TAG, "removeTag: in loop: span s = " + s.toString());
                    int start = spannableStringBuilder.getSpanStart(s);
                    int end = spannableStringBuilder.getSpanEnd(s);
                    char[] c = new char[end - start];
                    spannableStringBuilder.getChars(start, end, c, 0);
                    String source = new String(c);
                    Log.d(TAG, "removeTag: in loop: start = " + start + "; end = " + end + "; source = " + source);
                    if(source != null && source.equals(text)){
                        spannableStringBuilder.removeSpan(s);
                        spannableStringBuilder.delete(start, end);
                        setTagText();
                        tags.remove(text);
                        alertListenersTagRemoved(text);
                        return true;
                    }
                }
                Log.d(TAG, "removeTag: after set text = " + getText().toString());
            }
            */

            //remove the tag from the tags list
            Log.d(TAG, "removeTag: text = " + text + "; tags = " + tags.toString());
            List<String> oldTags = new ArrayList<>(tags);
            boolean removed = oldTags.remove(text);
            Log.d(TAG, "removeTag: removed = " + removed);
            if(removed){
                //if the tag was in the tags list and was successfully removed, clear the SpannableStringBuilder
                clearText();
                //clear the tags so there's no duplicates added
                tags.clear();
                //rebuild the tag list
                //if there is no more tags after the last tag got deleted clear the edit text
                for (String s : oldTags) {
                    addTag(s);
                }
                alertListenersTagRemoved(text);
                return true;
            }
        }catch(Exception e){
            Log.e(TAG, "Error removing tag:");
            e.printStackTrace();
        }
        return false;
    }

    public boolean containsTag(String text){
        return tags.contains(text);
    }

    private boolean cancelPressed(ImageSpan imageSpan, TextView textView, MotionEvent event){
        Log.d(TAG, "cancelPressed: imageSpan = " + imageSpan + "; textView = " + textView + "; event = " + event);
        //TODO get this method to work correctly
        if(textView != null && imageSpan != null && event != null){
            Rect bounds = imageSpan.getDrawable().getBounds();
            int left = bounds.left;
            int right = bounds.right;
            int width = bounds.width();
            int cancelWidth = textView.getCompoundDrawables()[2].getBounds().width();
            int cancelLeft = right - cancelWidth;
            if(event.getRawX() >= cancelLeft && event.getRawX() <= right){
                return true;
            }
        }
        return false;
    }

    private ImageSpan getLastSpan(){
        try{
            ImageSpan[] spans = getText().getSpans(0, getText().length(), ImageSpan.class);
            if(spans.length > 0) {
                return spans[spans.length - 1];
            }
        }catch(Exception e){
            Log.e(TAG, "Error getting last span:");
            e.printStackTrace();
        }
        return null;
    }

    private List<ImageSpan> getSpans(int start, int end){
        try{
            List<ImageSpan> result;
            ImageSpan[] spans = getText().getSpans(start, end, ImageSpan.class);
            result = new ArrayList<>(Arrays.asList(spans));
            return result;
        }catch(Exception e){
            Log.e(TAG, "Error getting spans:");
            e.printStackTrace();
        }
        return null;
    }

    private TextView createTagTextView(final String text){
        try {
            final TextView tv = new TextView(context);
            tv.setText(text);
            tv.setTextSize(tagTextSize);
            tv.setTextColor(context.getResources().getColor(tagTextColor));
            tv.setBackgroundResource(tagBackground);
            int drawableSize = Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, tagTextSize / 2,
                                                            context.getResources().getDisplayMetrics()));
            Drawable d = (tagCancelButton == null) ? context.getResources().getDrawable(tagCancelButtonResource) : tagCancelButton;
            Bitmap cb = ((BitmapDrawable) d).getBitmap();
            d = new BitmapDrawable(context.getResources(), Bitmap.createScaledBitmap(cb, drawableSize, drawableSize, true));
            tv.setCompoundDrawablesWithIntrinsicBounds(null, null, d, null);
            int p = (tagTextSize > 12) ? tagTextSize / 2 : 6;
            p = Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, p, context.getResources().getDisplayMetrics()));
            tv.setPadding(p, p, p, p);
            tv.setCompoundDrawablePadding(p);
            return tv;
        }catch(Exception e){
            Log.e(TAG, "Error creating custom TextView, possibly due to Context being null.");
            e.printStackTrace();
            return null;
        }
    }

    public static Bitmap converViewToBitmap(View view){
        try {
            int spec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
            view.measure(spec, spec);
            view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
            Bitmap b = Bitmap.createBitmap(view.getMeasuredWidth(), view.getMeasuredHeight(),
                    Bitmap.Config.ARGB_8888);
            Canvas c = new Canvas(b);
            c.translate(-view.getScrollX(), -view.getScrollY());
            view.draw(c);
            view.setDrawingCacheEnabled(true);
            Bitmap cacheBmp = view.getDrawingCache();
            Bitmap viewBmp = cacheBmp.copy(Bitmap.Config.ARGB_8888, true);
            view.destroyDrawingCache();
            return viewBmp;
        }catch(Exception e){
            Log.e(TAG, "Error creating Bitmap from View:");
            e.printStackTrace();
            return null;
        }
    }

    public static Drawable convertViewToDrawable(View view) {
        try {
            int spec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
            view.measure(spec, spec);
            view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
            Bitmap b = Bitmap.createBitmap(view.getMeasuredWidth(), view.getMeasuredHeight(),
                    Bitmap.Config.ARGB_8888);
            Canvas c = new Canvas(b);
            c.translate(-view.getScrollX(), -view.getScrollY());
            view.draw(c);
            view.setDrawingCacheEnabled(true);
            Bitmap cacheBmp = view.getDrawingCache();
            Bitmap viewBmp = cacheBmp.copy(Bitmap.Config.ARGB_8888, true);
            view.destroyDrawingCache();
            return new BitmapDrawable(viewBmp);
        }catch(Exception e){
            Log.d(TAG, "Error creating Drawable from View:");
            e.printStackTrace();
            return null;
        }
    }

    public int getTagTextSize(){
        return tagTextSize;
    }

    public void setTagTextSize(int size){
        this.tagTextSize = size;
    }

    public int getTagBackground(){
        return tagBackground;
    }

    public void setTagBackground(int backgroundResourceId){
        this.tagBackground = backgroundResourceId;
    }

    public int getTagCancelButtonResource(){
        return tagCancelButtonResource;
    }

    public void setTagCancelButtonResource(int resourceId){
        this.tagCancelButtonResource = resourceId;
    }

    public Drawable getTagCancelButton(){
        return tagCancelButton;
    }

    public void setTagCancelButton(Drawable cancelDrawable){
        this.tagCancelButton = cancelDrawable;
    }


    public static interface OnTagListener{
        void onTagSelected(String tagText);
        void onTagRemoved(String tagText);
        void onTagAdded(String tagText);
    }


    public void addTagListener(OnTagListener listener){
        listeners.add(listener);
    }

    public void removeTagListener(OnTagListener listener){
        listeners.remove(listener);
    }

    private void alertListenersTagSelected(String tagText){
        for(OnTagListener l : listeners){
            l.onTagSelected(tagText);
        }
    }

    private void alertListenersTagRemoved(String tagText){
        for(OnTagListener l : listeners){
            l.onTagRemoved(tagText);
        }
    }

    private void alertListenersTagAdded(String tagText){
        for(OnTagListener l : listeners){
            l.onTagAdded(tagText);
        }
    }


    /* Android's way of handling soft keyboard input sucks major d**k. Which is odd considering almost every device now
     * uses one. Unfortunately, to handle the input is pretty complex and non-standard. I've used a TextWatcher for most
     * of the typing handling, including the enter key press, but for backspace I need to perform a little more elaborate of a
     * hack. I need to override the onCreateInputConnection() method and wrap the InputConnection in a custom InputConnectionWrapper
     * class. Then, in that custom class, override either the sendKeyEvent() or deleteSurroundingText() methods (dependant on device).
     * Unfortunately, this method may not work for all devices but it's the closest I can get to solving the issue.
     * Figured this solution out with some help from the questions and answers here:
     * http://stackoverflow.com/questions/4886858/android-edittext-deletebackspace-key-event */

    @Override
    public InputConnection onCreateInputConnection(EditorInfo outAttrs){
        return new TagInputConnection(super.onCreateInputConnection(outAttrs), true);
    }


    private class TagInputConnection extends InputConnectionWrapper{

        public TagInputConnection(InputConnection connection, boolean mutable){
            super(connection, mutable);

        }

        @Override
        public boolean sendKeyEvent(KeyEvent event) {
            Log.d(TAG, "sendKeyEvent");
            if (event.getAction() == KeyEvent.ACTION_DOWN && event.getKeyCode() == KeyEvent.KEYCODE_DEL) {
                Log.d(TAG, "sendKeyEvent: backspace pressed");
                // Un-comment if you wish to cancel the backspace:
                // return false;
                int start = TagInput.this.getSelectionStart();
                int end = TagInput.this.getSelectionEnd();
                int length = spannableStringBuilder.length();
                Log.d(TAG, "backspace: start = " + start + "; end = " + end + "; length = " + length);
                if(lastDeleteIndex == start){
                    //prevent duplicate calls to sendKeyEvent
                    //lastDeleteIndex will equal -1 if any change happened after a delete
                    //therefore this condition wouldn't be met
                    //but if no change was made since the last delete and the delete index is the same as last time,
                    //then it must be a duplicate call to sendKeyEvent, so prevent it
                    return false;
                }
                if(start == length || start == length - 1){
                    Log.d(TAG, "backspace");
                    //delete the previous tag
                    if(tags.size() > 0) { //make sure there's tags to delete
                        removeTag(tags.get(tags.size() - 1));
                    }
                    return true;
                }else if(start < length && end < length){
                    //delete tag(s)
                    List<ImageSpan> spans = getSpans(start, end);
                    if(spans != null){
                        for(ImageSpan span : spans){
                            String source = span.getSource();
                            if(source != null) {
                                removeTag(span.getSource());
                                return true;
                            }
                        }
                    }
                }
            }
            Log.d(TAG, "super.sendKeyEvent");
            return super.sendKeyEvent(event);
        }

        @Override
        public boolean deleteSurroundingText(int beforeLength, int afterLength) {
            Log.d(TAG, "deleteSurroundingText: beforeLength = " + beforeLength + "; afterLength = " + afterLength);
            // magic: in latest Android, deleteSurroundingText(1, 0) will be called for backspace
            if (beforeLength == 1 && afterLength == 0) {
                // backspace
                return sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DEL))
                        && sendKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_DEL));
            }

            return true;
        }

    }

}
