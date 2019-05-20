package com.google.gachon.software.reminderapp_eng;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.tsengvn.typekit.TypekitContextWrapper;

public class PlayInfoActivity extends AppCompatActivity {

    DataBase db;
    TextView mText1, mText2;
    ImageButton button;
    int playingPos = -1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_info);

        db = Main2Activity.getDBInstance();
        mText1 = (TextView) findViewById(R.id.text);
        mText2 = (TextView) findViewById(R.id.text2);
        button = (ImageButton) findViewById(R.id.backImage);

        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.putExtra("key", playingPos);
                setResult(-99, intent); //result code
                System.out.println("인포인포!!");
                //이전 화면으로 돌아간다
                finish();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        //재생 중지
//        int playingPos = Main2Activity.mVoicePlayer.stopPlaying();
        Intent intent = getIntent();
        playingPos = intent.getIntExtra("playingpos", -1);

        String[] alarmTimeArr = db.getAllAlarmTime();
        String[] fileNameArr = db.getAllFileName();

        if (alarmTimeArr[playingPos].equals("일반 메모")) {
            mText1.setText("Recording Time" + "\n"
                    + recordTime(fileNameArr[playingPos]));
        } else {
            String[] words = alarmTimeArr[playingPos].split(":");
            if (Integer.parseInt(words[3]) < 10) words[3] = '0' + words[3];
            if (Integer.parseInt(words[4]) < 10) words[4] = '0' + words[4];
            String timeRegistered = words[3] + ":" + words[4] + "(" + words[1] + "월" + words[2] + "일" + ")";

            mText1.setText("Recording Time" + "\n" + recordTime(fileNameArr[playingPos]) );
            mText2.setText("Alarm Time" + "\n" + timeRegistered);
        }
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        intent.putExtra("key", playingPos);
        setResult(-99, intent); //result code

        super.onBackPressed();
        finish(); //이전 화면으로 돌아간다
    }

    //녹음시간으로 나타내는 메소드
    public String recordTime(String fileName) {
        String retStr = fileName.substring(3, fileName.length() - 7);
        retStr = retStr.substring(6) + "(" + Integer.parseInt(retStr.substring(0, 2)) + "월" + Integer.parseInt(retStr.substring(3, 5)) + "일)";
        return retStr;
    }

    /**
     * 액티비티의 글꼴을 바꾸기 위해 불러지는 함수이다.
     * CustomStartApp과 연결되어 있다.
     */

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(TypekitContextWrapper.wrap(newBase));
    }
}
