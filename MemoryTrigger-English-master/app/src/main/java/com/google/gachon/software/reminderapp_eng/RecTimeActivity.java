package com.google.gachon.software.reminderapp_eng;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.CountDownTimer;
import android.provider.CalendarContract;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.tsengvn.typekit.TypekitContextWrapper;

import java.util.Calendar;
import java.util.TimeZone;

public class RecTimeActivity extends AppCompatActivity {
    public static Activity RTActivity;
    String fileName, returnedValue, alarmTime, contentValue;
    DataBase db;
    TextView textView, textContent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rec_time);

        RTActivity = this;
        db = Main2Activity.getDBInstance();
        textView = (TextView) findViewById(R.id.text);
        textContent = (TextView) findViewById(R.id.textContent);

//        ImageButton button = (ImageButton) findViewById(R.id.button);
//        button.setOnClickListener(new View.OnClickListener() {
//            public void onClick(View v) {
//                //메인으로 돌아간다
//                finish();
//            }
//        });

        //RecordActivity로부터 전달받은 intent를 확인한다.
        Intent intent = getIntent();
        fileName = intent.getStringExtra("f_name");
        returnedValue = intent.getStringExtra("r_value");
        alarmTime = intent.getStringExtra("a_time");
        contentValue = intent.getStringExtra("c_value");
    }

    @Override
    protected void onStart() {
        super.onStart();

        String[] words = alarmTime.split(":");
        if (Integer.parseInt(words[3]) < 10) words[3] = '0' + words[3];
        if (Integer.parseInt(words[4]) < 10) words[4] = '0' + words[4];

        String daySuffix = new String();
        switch(Integer.parseInt(words[2]) % 10) {
            case 1:
                daySuffix = "st";
                break;
            case 2:
                daySuffix = "nd";
                break;
            case 3:
                daySuffix = "rd";
                break;
            default:
                daySuffix = "th";
        }
        if(11 <= Integer.parseInt(words[2]) && Integer.parseInt(words[2]) <= 13) {
            daySuffix = "th";
        }
        String timeRegistered = words[3] + ":" + words[4] + "(" + words[2] + daySuffix + ")" + "Alarm";

        String alarmText = new String();

        int year = Integer.parseInt("20" + words[0]);
        int month = Integer.parseInt(words[1]) - 1;
        int day = Integer.parseInt(words[2]);
        int hour = Integer.parseInt(words[3]);
        int minute = Integer.parseInt(words[4]);
        // calendar에 추가한다.
        ContentAnalysis contentAnalysis = new ContentAnalysis();
        System.out.println("test alarm : " + alarmTime);
        insertEvent(year, month, day, hour, minute, contentAnalysis.Analysis(contentValue));

        if (contentValue.equals("")) {
            textView.setText(timeRegistered);
            textContent.setText("No Contents");
            alarmText = timeRegistered;
        }
        else {
//            textView.setText(timeRegistered + "\n" + recordCutValue(contentValue, 2));
//            alarmText = timeRegistered + "\n" + recordCutValue(contentValue, 2);
            textView.setText(timeRegistered);
            textContent.setText(contentValue);
            alarmText = timeRegistered + "\n" + contentValue;
        }

        //녹음이 끝나고 알람화면이 뜰 경우, 알람 화면을 종료시키면 다시 onStart가 되기 때문에 db에 같은 값이
        //한번 더 저장된다. 이를 막기 위해서... fileName이 같은 파일이 2개이상 존재하면 안되므로 fileName으로 비교를 하면 되겠다.
        //db가 비어있는 경우 db.getLastFileName()을 호출하면 에러가 발생하므로 db.getAllPlayListNum()이 0보다 큰 경우에만 확인한다.
        if(db.getAllPlayListNum() == 0) { // db가 비어있을 때는 항상 저장해줘야 한다.
            db.insert(fileName, alarmTime, returnedValue);
            System.out.println("db에 일반메모 저장");
        }
        if(db.getAllPlayListNum() > 0 && !fileName.equals(db.getLastFileName())) { //마지막에 저장된 fileName과 다를 경우만 db에 insert
            db.insert(fileName, alarmTime, returnedValue);
            System.out.println("db에 시간메모 저장");
        }

        //Toast.makeText(getApplicationContext(), returnedValue, Toast.LENGTH_LONG).show();

        ///////////////////////////////알람 설정 //////////////////////////////////////////
        ///////////////////////알람 설정//////////////////////////
        System.out.println("알람 시간 형식 : " + alarmTime);
        //SharedPreferences 사용해서 누적된 알람의 개수 저장
        SharedPreferences alarmNumPref = getSharedPreferences("anPref", MODE_PRIVATE);
        int alarmNum = alarmNumPref.getInt("anum", 0); //anum에 해당하는 값이 없으면 0을 받아온다.
        SharedPreferences.Editor editor = alarmNumPref.edit();
        editor.putInt("anum", alarmNum + 1); // 값 수정.
        editor.commit();

        //SharedPreferences 사용해서 파일 이름에 해당하는 펜딩인텐트 request code 저장
        SharedPreferences pIntentPref = getSharedPreferences("piPref", MODE_PRIVATE);
        SharedPreferences.Editor piEditor = pIntentPref.edit();
        piEditor.putInt(fileName, alarmNum);
        piEditor.commit();
        System.out.println("piPref put " + alarmNum);

        int rCode = alarmNum;

        Calendar mCalendar = Calendar.getInstance();
        int yy, MM, dd, hh, mm;
        yy = 2000 + Integer.parseInt(words[0]);
        MM = Integer.parseInt(words[1]);
        dd = Integer.parseInt(words[2]);
        hh = Integer.parseInt(words[3]);
        mm = Integer.parseInt(words[4]);
        mCalendar.set(yy, MM - 1, dd, hh, mm, 0);

        Intent mAlarmIntent = new Intent("com.google.cloud.android.reminderapp.ALARM_START"); //->AlarmReceiver -> AlarmSoundService
        mAlarmIntent.putExtra("filename", fileName);
        mAlarmIntent.putExtra("alarmtext", alarmText);
        System.out.println("알람텍스트 in RecTimeActivity : " + alarmText);

        PendingIntent mPendingIntent =
                PendingIntent.getBroadcast(
                        getApplicationContext(),
                        rCode, /*request code*/
                        mAlarmIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );

        AlarmManager mAlarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        mAlarmManager.setExact(
                AlarmManager.RTC_WAKEUP,
                mCalendar.getTimeInMillis(),
                mPendingIntent
        );

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

    // 캘린더 연동, 캘린더에 알림 시간 기록하기.
    /*
    출처 : https://developer.android.com/guide/topics/providers/calendar-provider.html?hl=ko#calendar

     */
    // Projection array. Creating indices for this array instead of doing
// dynamic lookups improves performance.
    public static final String[] EVENT_PROJECTION = new String[] {
            CalendarContract.Calendars._ID,                           // 0
            CalendarContract.Calendars.ACCOUNT_NAME,                  // 1
            CalendarContract.Calendars.CALENDAR_DISPLAY_NAME,         // 2
            CalendarContract.Calendars.OWNER_ACCOUNT                  // 3
    };

    // The indices for the projection array above.
    private static final int PROJECTION_ID_INDEX = 0;
    private static final int PROJECTION_ACCOUNT_NAME_INDEX = 1;
    private static final int PROJECTION_DISPLAY_NAME_INDEX = 2;
    private static final int PROJECTION_OWNER_ACCOUNT_INDEX = 3;

    private void insertEvent(int year, int month, int day, int hour, int minute, String content) {

        SharedPreferences uaPref = getSharedPreferences("uaPref", MODE_PRIVATE);
        String userAccount = uaPref.getString("userAccount", "null");
        System.out.println("what the account " + userAccount);

/// Run query
        Cursor cur = null;
        ContentResolver cr = getContentResolver();
        Uri uri = CalendarContract.Calendars.CONTENT_URI;
        String selection = "((" + CalendarContract.Calendars.ACCOUNT_NAME + " = ?) AND ("
                + CalendarContract.Calendars.ACCOUNT_TYPE + " = ?) AND ("
                + CalendarContract.Calendars.OWNER_ACCOUNT + " = ?))";
        String[] selectionArgs = new String[] {userAccount, "com.google",
                userAccount};
// Submit the query and get a Cursor object back.
        try {
            cur = cr.query(uri, EVENT_PROJECTION, selection, selectionArgs, null);
        } catch(SecurityException e) {
            e.printStackTrace();
        }

        // Use the cursor to step through the returned records
        System.out.println("account cur" + cur);
        long tmp = 3;
        while (cur.moveToNext()) {
            long calID = 0;
            String displayName = null;
            String accountName = null;
            String ownerName = null;

            // Get the field values
            calID = cur.getLong(PROJECTION_ID_INDEX);
            displayName = cur.getString(PROJECTION_DISPLAY_NAME_INDEX);
            accountName = cur.getString(PROJECTION_ACCOUNT_NAME_INDEX);
            ownerName = cur.getString(PROJECTION_OWNER_ACCOUNT_INDEX);

            // Do something with the values...
            System.out.println("test account : " +  calID + " " + accountName);
            tmp = calID;
            break;
        }

        //long calID = 3; //3은 local인듯...
        long calID = tmp;
        long startMillis = 0;
        long endMillis = 0;
        Calendar beginTime = Calendar.getInstance();
        beginTime.set(year, month, day, hour, minute);
        startMillis = beginTime.getTimeInMillis();
        Calendar endTime = Calendar.getInstance();
        endTime.set(year, month, day, hour, minute);
        endMillis = endTime.getTimeInMillis();

      //  ContentResolver cr = getContentResolver();
        ContentValues values = new ContentValues();
        values.put(CalendarContract.Events.DTSTART, startMillis);
        values.put(CalendarContract.Events.DTEND, endMillis);
        values.put(CalendarContract.Events.TITLE, content);
        values.put(CalendarContract.Events.DESCRIPTION, content);
        values.put(CalendarContract.Events.CALENDAR_ID, calID);
        values.put(CalendarContract.Events.EVENT_TIMEZONE, TimeZone.getDefault().toString());
      //  Uri uri = null;
        try {
            uri = cr.insert(CalendarContract.Events.CONTENT_URI, values);
        } catch(SecurityException e) {
            e.printStackTrace();
        }

        // get the event ID that is the last element in the Uri
        long eventID = -1;
        if(uri != null)
            eventID = Long.parseLong(uri.getLastPathSegment());
//
// ... do something with event ID
        //SharedPreferences 사용해서 파일 이름에 해당하는 eventID 저장
        SharedPreferences calIntentPref = getSharedPreferences("calPref", MODE_PRIVATE);
        SharedPreferences.Editor piEditor = calIntentPref.edit();
        piEditor.putLong(fileName, eventID);
        piEditor.commit();

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
