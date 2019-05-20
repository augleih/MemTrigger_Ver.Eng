package com.google.gachon.software.reminderapp_eng;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.provider.CalendarContract;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.tsengvn.typekit.TypekitContextWrapper;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class PlayListActivity extends AppCompatActivity {

    public static Activity PLactivity; //PlayActivity에서 사용됨
    DataBase db;

    int SampleRate = 16000;
    int BufferSize = 1024;

    //listing
    ListView listView;
    PlaylistAdapter adapter;
    ImageButton homeBtn;

    //삭제하는 버튼들
    ImageButton deleteBtn;
    ImageButton deletefinalBtn;
    CheckBox allSeleteBtn;

    //header
    TextView header;

    //취소버튼
    ImageButton cancelBtn;
    TextView countText;

    //cancelBtn과 deletefinalBtn을 포함하고 있는 linearlayout
    LinearLayout underBtnsLinear;
    RelativeLayout underBtnsRelative;

    //삭제 다이얼로그
    boolean delBtnClicked = false;
    AlertDialog alertDialog;

    int count;
    int tempPos = -1, tempPos2 = -1;

    boolean nowStarted = false;
    boolean isAfterOnPause = false; //onPause 상태였다가 onStart하는 것인지 아닌지 체크하기 위함.
    boolean isBackPressed = false;
    boolean wasPlaying = false; //onPause전에 재생 중이었는지 아닌지 체크

    int playingPos = -1;
    public static Handler phandler; // 재생중인 리스트 처리 핸들러

    //삭제하기 상태들
    boolean deleteState = false;
    boolean deleteFinalState = false;
    boolean allSeleteState = false;

    //삭제 리스트
    ArrayList<Integer> deleteList;

    //리스트 뷰의 속성들을 저장
    PlaylistView viewArr[] = new PlaylistView[100]; //list의 각 아이템들의 view값을 담고 있다.
    Playlist listArr[] = new Playlist[100];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_list);

        PLactivity = this; //재생이 모두 끝나면 list화면도 같이 종료하는 데에 사용됨.
//        imageButton = (ImageButton) findViewById(R.id.imageButton);

        homeBtn = (ImageButton) findViewById(R.id.homeBtn);
        deleteBtn = (ImageButton) findViewById(R.id.deleteBtn);
        deletefinalBtn = (ImageButton) findViewById(R.id.deletefinalBtn);
        cancelBtn = (ImageButton)findViewById(R.id.cancelBtn);
        countText = (TextView)findViewById(R.id.countText);
        allSeleteBtn = (CheckBox) findViewById(R.id.allSeleteBtn);
        listView = (ListView) findViewById(R.id.listView);
        header = (TextView)findViewById(R.id.header);

        //cancelBtn과 deletefinalBtn을 포함하고 있는 linearlayout
        underBtnsRelative = (RelativeLayout) findViewById(R.id.underBtnsRelative);

//        textView = (TextView) findViewById(R.id.text);
//        textView.setMovementMethod(new ScrollingMovementMethod());


        db = Main2Activity.getDBInstance();
        count = db.getAllPlayListNum();

//        test = new String[count];
//        test = db.getAllContent();


//        //재생 중인지 아닌지에 따라 재생/중지 버튼 표시 -> onStart()에서 해주는 걸로...
//        if(Main2Activity.mVoicePlayer.mIsPlaying) {
//            imageButton.setImageResource(R.drawable.stop_btn2);
//        }
//        else {
//            imageButton.setImageResource(R.drawable.play_btn2);
//        }

        phandler = new Handler() {
            public void handleMessage(Message msg) {
                //재생 중이면
                if (Main2Activity.mVoicePlayer.isPlaying()) {
                    int position = (int) msg.obj;
                    tempPos2 = position;
                    if (nowStarted) { //list버튼을 눌러 처음 list화면이 뜰 때,
                        if (tempPos2 > 4)
                            listView.setSelection(tempPos2 - 4); //하이라이트 된 부분이 가운데로 오는 위치(-4)로 이동해주기(처음에만...! 찾기 편하도록!)
                        nowStarted = false;
                    }

                    //같은 position이 여러번 들어오면 한번만 색깔을 바꾸도록(textView에는 한번만 출력) 하기 위함.
                    if (tempPos != position) {
                        tempPos = position;

                        //String[] contentNameArr = db.getAllContent();
                        String[] contentNameArr = db.getAllContent();
                        int cnt = contentNameArr.length;
//                        textView.setText(contentNameArr[cnt -1 - position]);
                    } else {
                        return;
                    }

                    adapter.notifyDataSetChanged(); //adapter 내용 변경 - 리스트뷰 갱신 ***
                } else { //한 파일의 재생이 끝났다면
//                    imageButton.setImageResource(R.drawable.play_btn2);
                }
            }
        };

//        imageButton.setOnClickListener(new View.OnClickListener() {
//            public void onClick(View v) {
//                if(Main2Activity.mVoicePlayer.mIsPlaying) { //재생 중
//                    playingPos = Main2Activity.mVoicePlayer.stopPlaying();
//                    imageButton.setImageResource(R.drawable.play_btn2);
//                }
//                else {
//                    System.out.println("왜 종료가 되는기야?" + playingPos);
//                    Main2Activity.mVoicePlayer.startPlaying(SampleRate, BufferSize, playingPos + 1);
//                    imageButton.setImageResource(R.drawable.stop_btn2);
//                }
//            }
//        });

        //홈버튼
        homeBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (PlayActivity.Pactivity != null) {
                    PlayActivity.Pactivity.finish();
                }
                finish();
            }
        });

        //휴지통 버튼 누를시
        deleteBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                listView.setSelection(db.getAllPlayListNum() - 1);
                listView.setAdapter(adapter);
                //휴지통 버튼을 누를시, 전체 삭제와, 삭제하기 버튼이 등장
                // 처음엔 숨긴다.

                //휴지통 버튼이 눌렸는지 상태를 체크
                deleteState = !deleteState;

                if (deletefinalBtn.getVisibility() == View.VISIBLE) {
                    underBtnsRelative.setVisibility(View.GONE); //리니어 레이아웃으로 cancel과 deletefinal묶음
                    countText.setVisibility(View.GONE);
                    cancelBtn.setVisibility(View.GONE);
                    deletefinalBtn.setVisibility(View.GONE);
                    header.setText("Memo List");
                    allSeleteBtn.setVisibility(View.INVISIBLE);

                    //모두 선택을 초기화
                    deleteFinalState = false;
                    allSeleteState = false;
                    allSeleteBtn.setChecked(false);
                    allSeleteBtn.setText("Select all");

                    for(int i = 0; i < count; i++)
                    {
                        listArr[i].isCheckedState = false;
                    }


                    checkCount();
                    onStart();

                } else if (deletefinalBtn.getVisibility() == View.GONE) {
                    underBtnsRelative.setVisibility(View.VISIBLE); //리니어 레이아웃으로 cancel과 deletefinal묶음
                    countText.setVisibility(View.VISIBLE);
                    cancelBtn.setVisibility(View.VISIBLE);
                    deletefinalBtn.setVisibility(View.VISIBLE);
                    allSeleteBtn.setVisibility(View.VISIBLE);
                    header.setText("");
                }

                //새로 갱신
                adapter.notifyDataSetChanged();
            }
        });

        //마지막 삭제 작업
        deletefinalBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //딜리트할 리스트를 넣어준다.
                deleteList = new ArrayList<Integer>();

                for (int i = 0; i < db.getAllPlayListNum(); i++) {

                    if (listArr[i].isCheckedState == true) {
                        deleteList.add(count - i - 1);
                    }
                    Log.d("여기도냐", viewArr[i].getTitle() + " " + viewArr[i].getIsChecked());
                }

                //최종 삭제 버튼을 클릭했다는 걸 표시
                delBtnClicked = true;

                //현재 재생중이었다면 중지
                if (Main2Activity.mVoicePlayer.mIsPlaying)
                    Main2Activity.mVoicePlayer.stopPlaying();

                //참조1 : http://mainia.tistory.com/2017
                //참조2 : http://pluu.github.io/blog/rxjava/2017/02/04/android-alertdialog/

                //다이얼 로그 실행
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(PlayListActivity.this);

                // AlertDialog setting
                alertDialogBuilder
                        .setTitle("Delete")
                        .setMessage("Would you like to delete files that you selected?")
                        .setIcon(R.drawable.del_btn)
                        .setCancelable(false)
                        .setPositiveButton("Yes",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        delBtnClicked = false;
                                        //해당파일을 삭제한다.
                                        for (int i = 0; i < deleteList.size(); i++) {
                                            delFunction(deleteList.get(i));
                                            Log.d("어때", i + " " + deleteList.get(i));
                                        }

                                        int cnt = db.getAllPlayListNum();
                                        if (cnt == 0) { //파일이 없으면 끝낸다.
                                            if (PlayActivity.Pactivity != null) {
                                                PlayActivity.Pactivity.finish();
                                            }
                                            finish();
                                        }

                                        deleteBtn.callOnClick();
                                        onStart();
//                                        //다시 재생 시작
//                                        onPause();
//                                        onResume();
                                    }
                                })
                        .setNegativeButton("No",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        delBtnClicked = false;
                                        //dialog를 취소
                                        dialog.cancel();
//                                        //다시 재생 시작
//                                        onPause();
//                                        onResume();
                                        deleteBtn.callOnClick();
                                        onStart();

                                    }
                                });

                // Dialog 생성
                alertDialog = alertDialogBuilder.create();

                // show Dialog
                alertDialog.show();

                // 메시지 택스트의 크기 와 가운데 정렬한다.
                TextView textView = (TextView) alertDialog.findViewById(android.R.id.message);
                textView.setTextSize(20.0f);
                textView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                Button cancelBtn = (Button) alertDialog.findViewById(android.R.id.button1);
                Button okBtn = (Button) alertDialog.findViewById(android.R.id.button2);
                cancelBtn.setTextSize(17.0f);
                okBtn.setTextSize(17.0f);
            }
        });

        //전체 선택 작업
        allSeleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //전체 선택을 누를 경우 전체 선택 취소기능 구현
                //전체 선택 취소 누를 경우 전체선택 기능 구현
                allSeleteState = !allSeleteState;

                Log.d("어때", db.getAllPlayListNum() + " " + viewArr.length);

                if (allSeleteState) {
                    allSeleteBtn.setText("Unselect all");
                    for (int i = 0; i < db.getAllPlayListNum(); i++) {
                        System.out.println("Select all" + i +" "+ viewArr[i].checkbox.isChecked());
                        if (listArr[i].isCheckedState == false) {
                            viewArr[i].checkbox.callOnClick();
                        }
                    }
                } else {
                    allSeleteBtn.setText("Sellect all");
                    for (int i = 0; i < db.getAllPlayListNum(); i++) {
                        System.out.println("전체 선택" + i +" "+ viewArr[i].checkbox.isChecked());
                        if (listArr[i].isCheckedState == true) {
                            viewArr[i].checkbox.callOnClick();
                        }
                    }
                }
                adapter.notifyDataSetChanged(); //adapter 내용 변경 - 리스트뷰 갱신 ***
            }
        });

        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteBtn.callOnClick();
            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Main2Activity.mVoicePlayer.stopPlaying();
                int playCount = db.getAllPlayListNum();

                //PlayActivity 호출하면 될 듯...
                Intent intent = new Intent(getApplicationContext(), PlayActivity.class);
                intent.putExtra("playcount", playCount - position);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); //PlayActivity위의 activity 모두 삭제
                System.out.println("stopPoint in PlayListActivity : " + (playCount - position));
                startActivity(intent);

//                finish();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        deleteState = false;
        deleteFinalState = false;
        allSeleteState = false;

        //재생 중인지 아닌지에 따라 재생/중지 버튼 표시
//        if(Main2Activity.mVoicePlayer.mIsPlaying) {
//            imageButton.setImageResource(R.drawable.stop_btn2);
//        }
//        else {
//            imageButton.setImageResource(R.drawable.play_btn2);
//        }

        if (!isAfterOnPause && !Main2Activity.mVoicePlayer.mIsPlaying) { //재생 중지 상태의 playActivity에서 넘어왔을 경우
            String[] contentNameArr = db.getAllContent();
            int playCount = db.getAllPlayListNum();
            Intent intent = getIntent();
            playingPos = intent.getIntExtra("playingpos", playCount - 1); //시작화면에서 재생버튼 클릭해서 리스트로 넘어올 때, 맨 위의 것을 하이라이트.
            //목록에 highlight
            tempPos2 = playCount - playingPos - 1;
            //textView에 내용 출력
//            textView.setText(contentNameArr[playingPos].replaceAll(" ", ""));
        } else { //추가했음... 재생 화면에서 재생 중에 리스트로 넘어올 경우, playingPos가 초기화가 안 되길래 여기서 초기화 함.
            System.out.println("list tempPos2 왔어 : " + tempPos2);
            Intent intent = getIntent();
            playingPos = intent.getIntExtra("playingpos", 0);
        }

        makeList2();
        listView.setAdapter(adapter);
        listView.setSelection(db.getAllPlayListNum() - 1);
        nowStarted = true;

        //onPause상태에 있었다가 onStart된다면 -> 홈버튼을 눌러서 onPause가 되었다가 다시 실행시킨 경우.
        //backButton이나, 리스트의 파일을 선택하는 경우는 finish()되므로, 이 경우는 홈버튼을 눌렀다가 다시 실행한 경우이다.
        if (isAfterOnPause) {
            if (wasPlaying) { //onPause전에 재생 중이었다면
                Main2Activity.mVoicePlayer.startPlaying(SampleRate, BufferSize, playingPos + 1);
            }
            isAfterOnPause = false;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        System.out.println("백버튼 누르면 여기로 오나"); //finish()를 해도 이쪽으로 오는듯
        if (!isBackPressed) { //back button을 누른 경우는 제외. - back button을 누른 경우는 계속 진행되도록 할 것임
            playingPos = Main2Activity.mVoicePlayer.stopPlaying();

            //onPause상태에 들어왔다는 것을 체크
            isAfterOnPause = true;
            if (Main2Activity.mVoicePlayer.mIsPlaying) {
                wasPlaying = true; //재생 중에 onPause가 됐다면 다시 복귀할 때도 재생할 수 있도록 체크해놓는다.
//                playingPos = Main2Activity.mVoicePlayer.stopPlaying();
            } else {
                wasPlaying = false;
            }
        }
        isBackPressed = false;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (PlayActivity.Pactivity != null) {
            PlayActivity.Pactivity.finish();
        }
        finish();
        //super.onBackPressed() 전에 setResult()를 해줘야 resultCode가 전달됨!!
//        Intent intent = new Intent();
//        intent.putExtra("key", playingPos);
//        setResult(-100, intent);
//
//        System.out.println("여기에는 onback!!");
//
//        isBackPressed = true;
//        super.onBackPressed();

        //PlayActivity 호출 -> back button을 눌렀을 때는 intent에 -1을 보내서 처음부터 재생이 되도록 한다.
        //PlayActivity 호출 -> back button을 눌렀을 때는 intent에 -100을 보내서 현재 진행중인 상태 그대로 이어나가도록 한다
//        Intent intent = new Intent(getApplicationContext(), PlayActivity.class);
//        intent.putExtra("playcount", -100);
//        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); //PlayActivity위의 activity 모두 삭제
//        startActivity(intent);
        //finish();
    }

    public void checkCount() {
        int checkCount = 0;
        {
            for (int i = 0; i < count; i++) {

                if (listArr[i].isCheckedState == true) {
                    checkCount++;
                }
                Log.d("여기도냐2", checkCount + "");
            }

            if(checkCount > 0){
                deletefinalBtn.setEnabled(true);
                deletefinalBtn.setImageResource(R.drawable.del_btn_list);
                countText.setText(checkCount+" selected" );
            }
            else if(checkCount == 0)
            {
                deletefinalBtn.setEnabled(false);
                deletefinalBtn.setImageResource(R.drawable.del_btn_list2);
                countText.setText(checkCount+" selected");
            }
        }
    }

    /**
     * 재생 목록을 만드는 메소드, 재생 목록에는 각 컨텐츠들이 보여진다.
     */
    public void makeList2() {
        adapter = new PlaylistAdapter();
        count = db.getAllPlayListNum();
        ContentAnalysis contentAnalysis = new ContentAnalysis();

        String[] contentNameArr = db.getAllContent();
        String[] alarmTimeArr = db.getAllAlarmTime();
        String[] fileNameArr = db.getAllFileName();
        int playCount = contentNameArr.length;

        System.out.println("Play Count : " + playCount);
        for (int i = playCount - 1; i >= 0; i--) {
            //각 녹음 파일의 일정 내용을 목록에 출력하는 코드.
            //contentNameArr[i] = contentAnalysis.Analysis(contentNameArr[i]); 여기 주석 없애면 컨텐츠만 요약해서 나와요.

            //현재 시간과 알람 시간을 비교해서 종료된 알람과 앞으로 예정된 알람을 색깔로 구분하자.
            String strColor = new String();
            strColor = "#18392b";
            if (!alarmTimeArr[i].equals("일반 메모")) { //알람 시간이 존재한다면
                System.out.println("알람 시간 포멧 : " + alarmTimeArr[i]);
                //참조 : https://okky.kr/article/183785
                String words[] = alarmTimeArr[i].split(":");
                String date = "20" + words[0] + "-";
                date += (Integer.parseInt(words[1]) < 10 ? "0" + words[1] : words[1]) + "-";
                date += (Integer.parseInt(words[2]) < 10 ? "0" + words[2] : words[2]) + " ";
                date += (Integer.parseInt(words[3]) < 10 ? "0" + words[3] : words[3]) + ":";
                date += (Integer.parseInt(words[4]) < 10 ? "0" + words[4] : words[4]) + ":";
                date += "00";
                System.out.println("알람 포맷 : " + date);
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                try {
                    Date aDate = simpleDateFormat.parse(date);
                    long aMillis = aDate.getTime(); //alarm time
                    long cMillis = System.currentTimeMillis();//current time
                    if (aMillis > cMillis) { //미래의 알람
                        System.out.println("여기 미래의 알람");
                        strColor = "#FF0000";
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }

            if (contentNameArr[i].equals("")) {
                adapter.addItem(new Playlist((i + 1) + "." , "내용 없음", timeFormatFunc(alarmTimeArr[i]), R.drawable.alarm, strColor));
            } else {
                if (alarmTimeArr[i].equals("일반 메모"))
                    //adapter.addItem(new Playlist((i + 1) + "." , contentNameArr[i], "알람정보 없음", R.drawable.memo, strColor));
                    adapter.addItem(new Playlist((i + 1) + ".",  contentTime(contentNameArr[i], 20), "알람정보 없음", R.drawable.memo, strColor));
                else
                    //adapter.addItem(new Playlist((i + 1) + ".", contentNameArr[i], timeFormatFunc(alarmTimeArr[i]), R.drawable.alarm, strColor));
                    adapter.addItem(new Playlist((i + 1) + ".", contentTime(contentNameArr[i], 14), timeFormatFunc(alarmTimeArr[i]), R.drawable.alarm, strColor));
            }
        }
    }

    // 목록을 관리해주는 adapter
    class PlaylistAdapter extends BaseAdapter {
        ArrayList<Playlist> items = new ArrayList<Playlist>();

        @Override
        public int getCount() {
            return items.size();
        }

        public void addItem(Playlist item) {
            items.add(item);
        }

        @Override
        public Object getItem(int position) {
            return items.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup viewGroup) {
            System.out.println("getView: " + position + " " + convertView + " " + viewGroup);
            PlaylistView view = new PlaylistView(getApplicationContext());
            Playlist item = items.get(position);
            listArr[position] = items.get(position);
            final int position2 = position;

            view.setNumber(item.getNumStr());
            view.setContent(item.getContent());
            if (!(item.getAlarmTime()).equals("알람정보 없음")) { //알람시간이 있는 경우에만
                view.setAlarmTime(item.getAlarmTime());
                view.setImage(item.getResId());
                view.setAlarmTimeColor(item.getStrColor());
            }

            if (position == tempPos2) {
                view.setBackgroundColor(Color.parseColor("#20556080"));
//                view.setBackgroundColor(Color.rgb(92, 224, 189)); // #5CE0BD
            } else {
                view.textView.setBackgroundColor(Color.parseColor("#00000000"));
            }

            //휴지통 버튼을 누를시 true값이 되고, 체크 박스가 등장
            if (deleteState == true) {
                Log.d("Tag", "deleteState" + deleteState + position);
                view.setVisible();

                //휴지통 버튼을 다시 누르면 체크 박스 사라짐
            } else if (deleteState == false) {
                deleteFinalState = false;
                view.setGone();
            }

            view.checkbox.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    System.out.println("position2" + position2 + " " + items.get(position2).isCheckedState );
                    if (items.get(position2).isCheckedState == false) {
                        items.get(position2).setCheckedState();
                        checkCount();
                    } else if(items.get(position2).isCheckedState == true) {
                        items.get(position2).setUnCheckedState();
                        checkCount();
                    }
                }
            });

            if (items.get(position2).isCheckedState == true) {
                view.checkbox.setChecked(true);
            } else if(items.get(position2).isCheckedState == false)
                view.checkbox.setChecked(false);

            //모든 리스트들의 view를 arr로 저장한다.
            System.out.println("position: " + position);
            viewArr[position] = view;

            return view;
        }
    }

    // make list2에서 사용 (컨텐츠 명으로 나타내기 위해서)
    public String contentTime(String contentName, int len) {
        //contentName = contentName.replaceAll(" ", "");
        if (contentName.length() > len) return contentName.substring(0, len) + "..";
        else return contentName;
    }

    public String timeFormatFunc(String rawTime) {
        String words[] = rawTime.split(":");

        if (words[0].equals("일반 메모"))
            return rawTime;
        else {
            words[1] = Integer.parseInt(words[1]) < 10 ? "0" + words[1] : words[1];
            words[2] = Integer.parseInt(words[2]) < 10 ? "0" + words[2] : words[2];
            words[3] = Integer.parseInt(words[3]) < 10 ? "0" + words[3] : words[3];
            words[4] = Integer.parseInt(words[4]) < 10 ? "0" + words[4] : words[4];
//            return words[1] + "-" + words[2] + " " + words[3] + ":" + words[4];

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
            return words[3] + ":" + words[4] + "(" + words[2] + daySuffix + ")";
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

    public void delFunction(int deletePos) {
        String fileNameArr[] = db.getAllFileName();
        String alarmTimeArr[] = db.getAllAlarmTime();

        //db에서 파일이름 삭제
        db.delete(fileNameArr[deletePos]);

        //내부 저장소의 음성파일 삭제
        Context context = getApplicationContext();
        context.deleteFile(fileNameArr[deletePos]);

        //파일 이름에 해당하는 알람이 있으면 취소////////////////////////////////////////////
        SharedPreferences tempPref = getSharedPreferences("piPref", MODE_PRIVATE);
        int rCode = tempPref.getInt(fileNameArr[deletePos], -1); //fileNameArr[playingPos]에 해당하는 값이 없으면 -1을 받아온다.
        System.out.println("알람삭제 : " + rCode);
        if (rCode != -1) {
            AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            Intent intent = new Intent("com.google.cloud.android.reminderapp.ALARM_START");
            PendingIntent sender = PendingIntent.getBroadcast(getApplicationContext(), rCode, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            if (sender != null) {
                am.cancel(sender);
                sender.cancel();
            }
        }

        // 파일 이름에 해당하는 캘린더 일정 삭제
        SharedPreferences calTempPref = getSharedPreferences("calPref", MODE_PRIVATE);
        long eventID = calTempPref.getLong(fileNameArr[deletePos], -1); //fileNameArr[playingPos]에 해당하는 값이 없으면 -1을 받아온다.

        if(eventID != -1) {
            // delete calendar
            ContentResolver cr = getContentResolver();
            ContentValues values = new ContentValues();
            Uri deleteUri = null;
            deleteUri = ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, eventID);
            int rows = getContentResolver().delete(deleteUri, null, null);

            System.out.println("test : " + eventID + " 삭제");
        }
    }
}
