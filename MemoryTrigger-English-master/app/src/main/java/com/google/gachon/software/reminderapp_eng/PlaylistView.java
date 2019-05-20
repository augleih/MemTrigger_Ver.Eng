package com.google.gachon.software.reminderapp_eng;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Created by 이상원 on 2017-07-27.
 */

public class PlaylistView extends LinearLayout {
    TextView textView;
    TextView textView2;
    TextView textView3;
    ImageView imageView;
    CheckBox checkbox;
    boolean checkState = false;

//    Typeface type = Typeface.createFromAsset(getContext().getAssets(), "font.ttf");

    public PlaylistView(Context context) {
        super(context);
        init(context);
    }

    public PlaylistView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public void init(Context context) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.play_list, this, true);

        textView = (TextView) findViewById(R.id.textView);
        textView2 = (TextView) findViewById(R.id.textView2);
        textView3 = (TextView) findViewById(R.id.textViewNum);
        imageView = (ImageView) findViewById(R.id.imageView);
        checkbox = (CheckBox) findViewById(R.id.checkbox);

        //글씨체 설정
//        textView.setTypeface(type);
    }

    public void setVisible() {
        checkbox.setVisibility(View.VISIBLE);
    }

    public void setGone() {
        checkbox.setVisibility(View.GONE);
    }

    public void setChecked()
    {
        checkState = true;
    }

    public void setUnChecked()
    {
        checkState = false;
    }

    public boolean getIsChecked() {
        return checkbox.isChecked();
    }

    public boolean getChecked()
    {
        return checkState;
    }

    public String getTitle()
    {
        return textView.getText().toString();
    }

    public void setContent(String content) {
        textView.setText(content);
    }

    public void setAlarmTime(String alarmTime) {
        textView2.setText(alarmTime);
    }

    public void setNumber(String numStr) {
        textView3.setText(numStr);
    }

    public void setImage(int resId) {
        imageView.setImageResource(resId);
    }

    public void setAlarmTimeColor(String strColor) {
        //참조 : http://shstarkr.tistory.com/147
        textView2.setTextColor(Color.parseColor(strColor));
    }
}
