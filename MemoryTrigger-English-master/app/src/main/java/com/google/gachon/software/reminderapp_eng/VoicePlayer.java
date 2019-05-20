package com.google.gachon.software.reminderapp_eng;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Message;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * 이 클래스는 DB로부터 녹음된 파일명을 받아와 해당 파일을 재생하는 역할을 수행한다.
 */

public class VoicePlayer {

    private static final int CHANNEL = AudioFormat.CHANNEL_OUT_MONO;
    private static final int ENCODING = AudioFormat.ENCODING_PCM_16BIT;

    Context context;
    boolean mIsPlaying  = false;
    //많은 액티비티에서 mIsPlaying을 사용하기 때문에 가끔 동시에 접근하는 경우 에러 발생하는 것 같음
    //알람 재생은 mIsPlaying2를 사용한다.
    boolean mIsPlaying2 = false;
    DataBase db;

    AudioTrack audioTrack;
    private Thread mPlayingThread = null;
    int playCount, i = 0;

    VoicePlayer(Context c)
    {
        context = c;
        db = Main2Activity.getDBInstance();
    }

    //TODO : 피어리뷰에 적어야 할 것들 minBufferSize - 사용되지 않는 변수 제거, 불필요한 AudioTrack인스턴스 생성 없도록 개선 필요
    /**
     * 이 메소드는 새로운 thread를 생성하여 playWaveFile 메소드를 실행한다.
     * 음성 파일을 재생하기 위해 호출된다.
     *
     * @param SampleRate     녹음 시 사용된 sample rate(Hertz)
     * @param mBufferSize    재생 시 음성 파일에서 한 번에 읽어오는 음성 데이터의 최대 크기
     */
    public void startPlaying(final int SampleRate, final int mBufferSize, int position) {
        // int minBufferSize = AudioTrack.getMinBufferSize(SampleRate, CHANNEL, ENCODING);
        playCount = position;
        mIsPlaying = true;
        mPlayingThread = new Thread(new Runnable() {

            @Override
            public void run() {
                playWaveFile(SampleRate, mBufferSize);
            }
        }, "AudioRecorder Thread");
        mPlayingThread.start();
    }

    /**
     * 이 메소드는 새로운 thread를 생성하여 playWaveFileAlarm 메소드를 실행한다.
     * 알람에 해당하는 음성 파일을 재생하기 위해 호출된다.
     *
     * @param SampleRate     녹음 시 사용된 sample rate(Hertz)
     * @param mBufferSize    재생 시 음성 파일에서 한 번에 읽어오는 음성 데이터의 최대 크기
     * @param fn      재생하려는 음성파일명
     */
    public void startPlaying2(final int SampleRate, final int mBufferSize, final String fn) {
        // int minBufferSize = AudioTrack.getMinBufferSize(SampleRate, CHANNEL, ENCODING);
        mIsPlaying = true;
        mPlayingThread = new Thread(new Runnable() {

            @Override
            public void run() {
                playWaveFileAlarm(SampleRate, mBufferSize, fn);
            }
        }, "AudioRecorder Thread");
        mPlayingThread.start();
    }

    //TODO 변수 playing을 mIsplaying으로 바꾸기
    /**
     * 이 메소드는 변수 playing을 false로 설정하여 재생을 중지한다.
     *
     * @return int 현재 재생 중인 파일의 index
     */
    public int stopPlaying()
    {
        mIsPlaying = false;
        return i == -1 ? 0 : i;
    }

    /**
     * 이 메소드는 녹음 시 사용된 sample rate에 따라 audioTrack instance를 생성하고 음성 파일로부터 buffer size만큼 정보를 읽어와 재생한다.
     * 변수 playing의 값이 true일 때 재생한다.
     *
     * @param SampleRate     녹음 시 사용된 sample rate(Hertz)
     * @param mBufferSize    재생 시 음성 파일에서 한 번에 읽어오는 음성 데이터의 최대 크기
     *
     *
     * @exception FileNotFoundException
     * @exeption IOException
     */
    public void playWaveFile(int SampleRate,int mBufferSize) {
        String fileName[] = db.getAllFileName();
        String alarmTime[] = db.getAllAlarmTime();
        String returnedValue[] = db.getAllContent(); //contentValue -> returnedValue로 수정. (시간표현, 내용이 모두 포함된 원본이므로)
        int cnt = fileName.length; //목록에서 선택 시 playCount값이 변하기 때문에... 이렇게 따로 cnt에 저장해놓자.

        for(i = playCount-1; i >= 0; i--){
            int count = 0;
            byte[] data = new byte[mBufferSize];

            if(!mIsPlaying) {
                break; //추가했음. - 아래 while문에 mIsPlaying는 없어도 될듯. - 아 재생 중간에 정지되려면 while문 안에 있어야 할지도..?
            }

            //재생 중 화면 처리
            Message message = PlayActivity.vhandler.obtainMessage(1, alarmTime[i] + ":" + returnedValue[i] + ":" + i);
            PlayActivity.vhandler.sendMessage(message);

            try {
                //Toast.makeText(context.getApplicationContext(),"현재 재생중인 파일 " + fileName[i] +"",Toast.LENGTH_SHORT).show();
                FileInputStream fis = context.openFileInput(fileName[i]);
                DataInputStream dis = new DataInputStream(fis);
                int minBufferSize = AudioTrack.getMinBufferSize(SampleRate, CHANNEL, ENCODING);
//                audioTrack = new AudioTrack(AudioManager.STREAM_VOICE_CALL, SampleRate, CHANNEL, ENCODING, minBufferSize, AudioTrack.MODE_STREAM);
                audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, SampleRate, CHANNEL, ENCODING, minBufferSize, AudioTrack.MODE_STREAM);
                audioTrack.play();
//                audioTrack.setVolume()

                //progress bar의 범위인 0 ~ 100 에 맞도록 한 번에 채울 바의 크기를 계산
                int totalLength = dis.available(); //스트림으로부터 읽어들일 수 있는 바이트의 추정치 리턴
                int barVal = 0;
                PlayActivity.progress.setMax(totalLength); // progress bar의 최대범위 설정

                while (((count = dis.read(data, 0, mBufferSize)) > -1)&&mIsPlaying) {
                    if(PlayListActivity.phandler != null) {
                        //재생 중인 파일 하이라이트하기 위해 position정보를 보낸다.(phandler이용)
                        //여기다가 쓴 이유는 파일이 실행중일 때 목록버튼을 누르는 경우에도 하이라이트가 되도록 하기 위함이다.
                        Message message3 = PlayListActivity.phandler.obtainMessage(1, cnt - 1 - i);
                        PlayListActivity.phandler.sendMessage(message3);
                    }

                    SharedPreferences preference = context.getSharedPreferences("volume", context.MODE_PRIVATE);
                    float volume = preference.getFloat("volume", 1f);
                    audioTrack.setVolume(volume);
                    audioTrack.write(data, 0, count);

                    //progress bar 채우기
                    barVal += mBufferSize;
                    PlayActivity.progress.setProgress(barVal);
                }
                audioTrack.stop();
                audioTrack.release();
                dis.close();
                fis.close();

                //progress bar 초기화
                PlayActivity.progress.setProgress(0);

                if(mIsPlaying == false) { //재생 중간에 종료한 경우 -> 아래쪽 핸들러를 피함으로써 버튼 변경이 부드럽게 되도록 한다.
                    return;
                }

                /* 연속 재생 방식에서 하나 재생하고 멈추는 방식으로 바꿀 것인데, 연속 재생하는 방식을
                * 나중에 다시 사용할 수 있으니, mIsPlaying = false; 이렇게 추가해보자. */
                mIsPlaying = false;

                //한 파일의 재생이 끝나면, 중지 버튼이 재생 버튼으로 변경되도록 handler를 이용해 끝났음을 알려준다.
                //재생 화면에게 재생이 끝났음을 알려줌
                Message message2 = PlayActivity.vhandler.obtainMessage(1, "stop");
                PlayActivity.vhandler.sendMessage(message2);
                //재생 목록 화면에게 재생이 끝났음을 알려줌
                if(PlayListActivity.phandler != null) { //PlayListActivity를 호출하지 않았다면 phandler가 생성되지 않았을 수 있으므로
                    Message message3 = PlayListActivity.phandler.obtainMessage(1, cnt - 1 - i);
                    PlayListActivity.phandler.sendMessage(message3);
                }

                if(!mIsPlaying) break;

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if(i == -1) {
            mIsPlaying = false; //재생이 끝났음을 체크.
            Message message2 = PlayActivity.vhandler.obtainMessage(1, "stop");
            PlayActivity.vhandler.sendMessage(message2);
        }
    }

    /**
     * 이 메소드는 현재 재생 중인지 아닌지를 판단하기 위해 호출된다.
     *
     * @return boolean    현재 재생 중 여부 (True : 재생 중, False : 재생 중 아님)
     */
    public boolean isPlaying()
    {
        return mIsPlaying;
    }

    /**
     * 이 메소드는 알림 시각이 되면 그 알림에 해당하는 녹음 내용을 반복해서 재생하는 역할을 한다.
     * playWaveFile 메소드와 유사하다.
     *
     * @param SampleRate     녹음 시 사용된 sample rate(Hertz)
     * @param mBufferSize    재생 시 음성 파일에서 한 번에 읽어오는 음성 데이터의 최대 크기
     * @param filename        재생할 음성 파일의 이름
     *
     *
     * @exception FileNotFoundException
     * @exeption IOException
     *
     */
    public void playWaveFileAlarm(int SampleRate,int mBufferSize, String filename) { //해당 알람내용을 한 번만 재생하도록 한다.
        mIsPlaying2 = true;

//        boolean isFinished = false;
//        long startTime = System.currentTimeMillis();

//        while(true) {
//            if(isFinished) {
//                mIsPlaying2 = false;
//            }
            int count = 0;
            byte[] data = new byte[mBufferSize];

            try {
                FileInputStream fis = context.openFileInput(filename);
                DataInputStream dis = new DataInputStream(fis);
                int minBufferSize = AudioTrack.getMinBufferSize(SampleRate, CHANNEL, ENCODING);
//                audioTrack = new AudioTrack(AudioManager.STREAM_VOICE_CALL, SampleRate, CHANNEL, ENCODING, minBufferSize, AudioTrack.MODE_STREAM);
                audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, SampleRate, CHANNEL, ENCODING, minBufferSize, AudioTrack.MODE_STREAM);
                audioTrack.play();

                while (((count = dis.read(data, 0, mBufferSize)) > -1)&&mIsPlaying2) {
//                    long endTime = System.currentTimeMillis();
//                    if((endTime - startTime) / 1000.0f >= 60) {
//                        isFinished = true;
//                        break;
//                    }
                    SharedPreferences preference = context.getSharedPreferences("volume", context.MODE_PRIVATE);
                    float volume = preference.getFloat("volume", 1f);
                    audioTrack.setVolume(volume);
                    audioTrack.write(data, 0, count);
                }
                audioTrack.stop();
                audioTrack.release();
                dis.close();
                fis.close();

                if(!mIsPlaying2) {
                    return;
                }

//                if(!mIsPlaying2) {
//                    //알람이 끝나면 AlarmActivity로 알람이 끝났음을 알린다.
//                    Message message = AlarmActivity.ahandler.obtainMessage(1, "stop");
//                    AlarmActivity.ahandler.sendMessage(message);
//                    break;
//                }

                // 버튼을 play_btn2로 변경할 수 있도록 재생이 끝나면 알려준다.
                System.out.println("ahandler 여기서 콜하나");
                mIsPlaying2 = false;
                Message message = AlarmActivity.ahandler.obtainMessage(1, "stop");
                AlarmActivity.ahandler.sendMessage(message);

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
//        }
    }

    public void stopPlaying2() {
        mIsPlaying2 = false;
    }
}