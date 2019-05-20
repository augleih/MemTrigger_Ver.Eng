
package com.google.gachon.software.reminderapp_eng;

import android.app.Service;
import android.content.Context;
import android.content.Intent;

import android.os.AsyncTask;
import android.os.IBinder;

import com.tsengvn.typekit.TypekitContextWrapper;

public class AlarmSoundService extends Service {
    Context context;
    public static VoicePlayer mVoicePlayerAlarm;

    public AlarmSoundService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String alarmText = intent.getStringExtra("alarmtext");
        String fileName = intent.getStringExtra("filename");
        System.out.println("알람텍스트 in AlarmSoundService : " + alarmText);
        Intent alarmScreenIntent = new Intent(getApplicationContext(), AlarmActivity.class);
        alarmScreenIntent.putExtra("alarmtext", alarmText);
        alarmScreenIntent.putExtra("filename", fileName);
        alarmScreenIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        context.startActivity(alarmScreenIntent);

        //녹음이 종료된 직후 녹음 결과 화면이 나올 때 알람이 울릴 경우에 UI처리 필요.
        //하지만 결과 화면이 알람이 실행된 직후에 출력되는 타이밍이라면... 좀 더 생각해 봐야할 듯

        // 시간표현 결과 화면(액티비티)이라면 알람이 끝난 후에 다시 RecTImeActivity onStart를 호출하고, 이미 지난 시간을 알람으로 설정해서
        // 알람이 끝나고 또 알람이 계속해서 울리는 버그가 있다(이전 시간으로 설정되면 알람이 바로 울림)
        // 알람이 시작될 때, 시간표현 결과 액티비티는 종료시킨다.
        if(RecTimeActivity.RTActivity != null) {
            RecTimeActivity.RTActivity.finish();
        }

        //녹음 중이라면 녹음 종료
        if(Main2Activity.mVoiceRecorder != null && Main2Activity.mVoiceRecorder.mIsRecording) {
            String fileName2 = Main2Activity.mVoiceRecorder.stopRecording();

            //DB에는 fileName이 저장되지 않았지만, 음성 파일 자체는 내부 저장소에 저장되어 있으므로 삭제한다.
            context.deleteFile(fileName2);

            RecordActivity.timer.cancel(); //** 잊기 쉬움. 주의!
            RecordActivity.RActivity.finish();
            System.out.println("녹음 중에 알람이 울린다");
            //db에서 해당 음성 파일을 삭제하는 작업이 필요하지만, 현재 구현상 STT전에 멈추면,
            //db에 파일이름이 저장되지 않아서 큰 상관은 없어보인다. 단, 내장 메모리에는 음성 파일이 저장돼 있다.
            //그래서 녹음 중 알람이 울릴 때가 되면 녹음을 중지시키고 알람을 울리게 하는 기능이 겉으로 볼 땐 큰 문제 없을 것 같다.
            //fileName을 이용해서 나중에 삭제도 구현하자.
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        String temp = intent.getStringExtra("filename");

       // Toast.makeText(this, "알람이 울립니다. " + temp, Toast.LENGTH_LONG).show();

        class BackgroundTask extends AsyncTask<String, String, String> {
            protected void onPreExecute() {
                if(Main2Activity.mVoicePlayer != null)
                    Main2Activity.mVoicePlayer.stopPlaying();
                //기존 재생을 중지 후에 알람을 울릴 것인데, stopPlaying으로 중지하지만
                //완전히 중지가 되지 않아 바로 알람을 재생할 경우 문제가 생긴다. Illegal state exception
                //그래서 thread로 1초정도 여유를 두고 중지가 완료되도록 한다.
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            protected String doInBackground(String ... values) {
//                if(Main2Activity.mVoicePlayer != null) {
//                    Main2Activity.mVoicePlayer.playWaveFileAlarm(16000, 1024, values[0]);
//                }
//                else { //프로그램이 종료됐을 때도 알람이 울리도록 하기 위함
//                    //내부저장소에 접근할 때, 꼭 Main의 context를 사용하지 않아도 접근이 가능하구나
//                    mVoicePlayerAlarm = new VoicePlayer(getApplicationContext());
//                    mVoicePlayerAlarm.playWaveFileAlarm(16000, 1024, values[0]);
//                }
//                publishProgress(values[0]);
                return values[0];
            }

            protected void onProgressUpdate(String values) {
//                Toast.makeText(getApplicationContext(), "알람이 울립니다. ", Toast.LENGTH_LONG).show();
//                MainActivity.device.callOnClick();
            }

            protected void onPostExecute(String result) {
            }

            protected void onCancelled() {

            }
        }

        BackgroundTask task = new BackgroundTask();
        task.execute(temp);

        return START_NOT_STICKY;
        //return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
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
