package com.google.gachon.software.reminderapp_eng;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.tsengvn.typekit.TypekitContextWrapper;

import java.io.FileInputStream;
import java.io.FileNotFoundException;


public class RecordActivity extends AppCompatActivity {
    public static Activity RActivity; // AlarmSoundService에서 사용됨
    DataBase db;
    public static CountDownTimer timer; //AlarmSoundService에서 사용
    TextView mText, mText2;
//    ImageButton stopBtn, homeBtn;
    ImageButton stopBtn;
    ImageView recordImage;
    int value;
    String fileName;
    //boolean isRecStartButtonClicked = false;

    private SpeechService mSpeechService;

    TimeAnalysis timeAnalysis;
    ContentAnalysis contentAnalysis;

    Animation anim;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record);

        RActivity = this;
        db = Main2Activity.getDBInstance();
        stopBtn = (ImageButton) findViewById(R.id.stop_btn);
//        recStartBtn = (ImageButton) findViewById(R.id.rec_start_btn);
//        homeBtn = (ImageButton) findViewById(R.id.home);
        mText = (TextView) findViewById(R.id.text);
        mText2 = (TextView) findViewById(R.id.text2);
        recordImage = (ImageView) findViewById(R.id.recordImage);

        timeAnalysis = new TimeAnalysis();
        contentAnalysis = new ContentAnalysis();

       // isRecStartButtonClicked = false;

        //녹음 중일시
        anim = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.scale);

        //timer - 시간 제한 9초.
        value = 0;
        timer = new CountDownTimer(9000, 900) { // modified 7000 to 9000 (7sec -> 9sec)
            @Override
            public void onTick(long millisUntilFinished) {
                recordImage.startAnimation(anim);
//                mText.setText("녹음 중\n" + (9 - value) + "초 후 종료"); // 7 -> 9
                mText2.setText("End in "+(9 - value) + " seconds");
                value++;
            }

            @Override
            public void onFinish() {
                value = 0;
//                mText.setText("녹음 종료");
                if(Main2Activity.mVoiceRecorder.mIsRecording) {
                    stopVoiceRecorder();
                }
                recordImage.clearAnimation();
                timer.cancel();
                mText.setText("Processing...");
                mText2.setText("");
                stopBtn.setEnabled(false);
//                stopBtn.setVisibility(View.GONE);
//                homeBtn.setVisibility(View.VISIBLE);
            }
        };

//        recStartBtn.setOnClickListener(new View.OnClickListener() {
//            public void onClick(View v) {
//                isRecStartButtonClicked = true;
//                recStartBtn.setVisibility(View.GONE);
//                stopBtn.setVisibility(View.VISIBLE);
//                onStart();
//            }
//        });

        stopBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (Main2Activity.mVoiceRecorder.isRecording()) {
                    stopVoiceRecorder();
                    recordImage.clearAnimation();
                    timer.cancel();
                    mText.setText("Processing...");
                    mText2.setText("");
                    stopBtn.setEnabled(false);
//                    stopBtn.setVisibility(View.GONE);
//                    homeBtn.setVisibility(View.VISIBLE);
                }
            }
        });

//        homeBtn.setOnClickListener(new View.OnClickListener() {
//            public void onClick(View v) {
//                onBackPressed();
//            }
//        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        //녹음 시작 버튼을 누르지 않았으면 시작하지 않는다.
        //녹음 시작 버튼을 눌러야 녹음을 시작한다.
        //if(!isRecStartButtonClicked) return;

        // Prepare Cloud Speech API
        bindService(new Intent(this, SpeechService.class), mServiceConnection, BIND_AUTO_CREATE);

        if(!Main2Activity.mVoiceRecorder.isRecording()) {
            startVoiceRecorder();

            //TimeLimit 구현
            timer.cancel();
            timer.start(); //타이머 시작
        }
    }

    @Override
    public void onBackPressed() { //back button을 누르면 알람이 종료되도록 한다.
        super.onBackPressed();
        recordImage.clearAnimation();
        finish();
    }

    /**
     * RecordActivity가 멈출 때, SpeechService를 종료한다.
     */
    @Override
    protected void onStop() {
        super.onStop();

        // Stop Cloud Speech API
        if(mSpeechService != null) {
            mSpeechService.removeListener(mSpeechServiceListener);
            unbindService(mServiceConnection);
            mSpeechService = null;
        }

        if(Main2Activity.mVoiceRecorder.mIsRecording) {
            Main2Activity.mVoiceRecorder.stopRecording();
        }
        finish();
    }

    @Override
    protected void onPause() {
        super.onPause();

//        if(Main2Activity.mVoiceRecorder.mIsRecording) {
//            Main2Activity.mVoiceRecorder.stopRecording();
//        }
//        finish();
    }


    private void startVoiceRecorder() {
//        if (mVoiceRecorder != null) {
//            mVoiceRecorder.stopRecording();
//        }
        Main2Activity.mVoiceRecorder.startRecording();
    }

    private void stopVoiceRecorder() {
        if (Main2Activity.mVoiceRecorder != null) {
            fileName = Main2Activity.mVoiceRecorder.stopRecording();
            FileInputStream fis = null;
            try {
//                String fileName = db.getLastFileName();
                fis = openFileInput(fileName);
            }
            catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            System.out.println("파일 이름을 보내남 : " + fileName);
            if(mSpeechService != null)
                mSpeechService.recognizeInputStream(fis);
        }
    }


    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder binder) {
            mSpeechService = SpeechService.from(binder);
            mSpeechService.addListener(mSpeechServiceListener);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mSpeechService = null;
        }

    };

    /**
     * 구글 STT 서버로 부터 분석된 텍스트를 받아 onCreate에 있는 handler로 전송시킨다.
     *
     * @param text 파일의 음성을 텍스트로 변경한 값
     * @param isFinal 구글 STT 서버로부터 텍스트를 받았는지 아닌지 확인하는 값
     */
    private final SpeechService.Listener mSpeechServiceListener =
            new SpeechService.Listener() {
                @Override
                public void onSpeechRecognized(final String text, final boolean isFinal) {
                    if(isFinal) {
                        handleText(text);
                    }
                    else {
                        handleText("");
                    }
                }
            };

    public void handleText(String text) {
        System.out.println("핸들텍스트");
        String returnedValue = text;
        //TODO 나중에 살펴보고 사용되지 않으면 삭제하기
        String extractValue = new String();

        //아무말 없이 취소했을 경우
        if (returnedValue.equals("")) { //음성인식 실패
            //여기서 음성인식 실패 activity를 호출한다
            Intent intent = new Intent(getApplicationContext(), RecFailActivity.class);
            startActivity(intent);
            //TODO if else각각에 쓰지말고, 이것을 맨 아래쪽에 하나를 쓰는 것으로 나중에 고치기
            finish();
        }
        else {
            //여기서는 시간표현이 있는지 없는지 판단 후, 시간표현 존재 여부에 따라 엑티비티를 호출한다
            String alarmTime = timeAnalysis.Analysis(returnedValue);
            String contentValue = contentAnalysis.Analysis(returnedValue);

            if (alarmTime.equals("note")) { //시간표현이 없는 경우 -> RecNoTimeActivity
                //시간표현이 없는 녹음 결과 activity를 호출한다
                Intent intent = new Intent(getApplicationContext(), RecNoTimeActivity.class);
                intent.putExtra("f_name", fileName);
                intent.putExtra("r_value", returnedValue);
                startActivity(intent);

                finish();

            }
            else { //시간표현이 있는 경우 -> 여기서 알람 시간 설정을 해줄까 -> 알람 시간 설정은 시간표현 엑티비티 가서 해주자. -> RecTimeActivity
                Intent intent = new Intent(getApplicationContext(), RecTimeActivity.class);
                intent.putExtra("f_name", fileName);
                intent.putExtra("r_value", returnedValue);
                intent.putExtra("a_time", alarmTime);
                intent.putExtra("c_value", returnedValue); //여기 수정했음(returnedValue로) <- contentValue였음
                startActivity(intent);

                finish();
            }
        }
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
