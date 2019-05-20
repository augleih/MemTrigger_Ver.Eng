package com.google.gachon.software.reminderapp_eng;

import android.content.Context;
import android.content.Intent;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.tsengvn.typekit.TypekitContextWrapper;

public class RecNoTimeActivity extends AppCompatActivity {
    String fileName, returnedValue;
    DataBase db;
    TextView textView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rec_no_time);

        db = Main2Activity.getDBInstance();
        textView = (TextView) findViewById(R.id.text);

//        ImageButton button = (ImageButton) findViewById(R.id.button);
//        button.setOnClickListener(new View.OnClickListener() {
//            public void onClick(View v) {
//                //메인으로 돌아간다
//                finish();
//            }
//        });

        //RecordActivity로부터 전달 받은 인텐트를 확인한다
        Intent intent = getIntent();
        fileName = intent.getStringExtra("f_name");
        returnedValue = intent.getStringExtra("r_value");
    }

    @Override
    protected void onStart() {
        super.onStart();

        //녹음이 끝나고 알람화면이 뜰 경우, 알람 화면을 종료시키면 다시 onStart가 되기 때문에 db에 같은 값이
        //한번 더 저장된다. 이를 막기 위해서... fileName이 같은 파일이 2개이상 존재하면 안되므로 fileName으로 비교를 하면 되겠다.
        //db가 비어있는 경우 db.getLastFileName()을 호출하면 에러가 발생하므로 db.getAllPlayListNum()이 0보다 큰 경우에만 확인한다.
        if(db.getAllPlayListNum() == 0) { // db가 비어있을 때는 항상 저장해줘야 한다.
            db.insert(fileName, "일반 메모", returnedValue);
            System.out.println("db에 일반메모 저장");
        }
        if(db.getAllPlayListNum() > 0 && !fileName.equals(db.getLastFileName())) { //마지막에 저장된 fileName과 다를 경우만 db에 insert
            db.insert(fileName, "일반 메모", returnedValue);
            System.out.println("db에 일반메모 저장");
        }

//        textView.setText(recordCutValue(returnedValue.replaceAll(" ", ""), 1));
        textView.setText(returnedValue);

        //화면을 2초간 보여주고 메인화면으로 이동
        CountDownTimer timer = new CountDownTimer(2000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                //
            }

            public void onFinish() {
                //2초뒤 home(메인) 화면으로 이동
                finish();
            }
        };
        timer.start();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    //녹음 후 화면에 표시해주는 것을 설정해준다.
    public String recordCutValue(String contentValue, int i) { //기기에 따라 화면의 한 줄에 들어갈 수 있는 글자 수가 다를 수 있고, 화면도 크기 때문에 사용 안 할 예정.
        String cutvalue = "";

        if (i == 1) {

            if (contentValue.length() > 27) {
                cutvalue = contentValue.substring(0, 9) + "\n" + contentValue.substring(8, 18) + "\n" + contentValue.substring(18, 27) + "..";
            }
            else if (contentValue.length() > 18) {
                cutvalue = contentValue.substring(0, 9) + "\n" + contentValue.substring(9, 18) + "\n" + contentValue.substring(18, contentValue.length());
            }
            else if (contentValue.length() > 9) {
                cutvalue = contentValue.substring(0, 9) + "\n" + contentValue.substring(9, contentValue.length());
            }
            else {
                cutvalue = contentValue.substring(0, contentValue.length());
            }

        }
        else if (i == 2) {

            //두줄 처리
            if (contentValue.length() > 18) {
                cutvalue = contentValue.substring(0, 9) + "\n" + contentValue.substring(9, 18) + "..";
            }
            else if (contentValue.length() > 9) {
                cutvalue = contentValue.substring(0, 9) + "\n" + contentValue.substring(9, contentValue.length());
            }
            else {
                cutvalue = contentValue.substring(0, contentValue.length());
            }

            //한줄 처리일 경우

            if (contentValue.length() > 9) {
                cutvalue = contentValue.substring(0, 7) + "..";
            }
            else {
                cutvalue = contentValue.substring(0, contentValue.length());
            }

        }
        return cutvalue;
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
