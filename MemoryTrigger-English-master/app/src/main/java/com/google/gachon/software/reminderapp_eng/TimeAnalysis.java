package com.google.gachon.software.reminderapp_eng;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 이 클래스는 음성 녹음 후에 시간 표현을 추출하기 위하여 main activity에서 불러진다.
 * 정규식을 이용하여 음성 파일에서 추출한 텍스트로부터 시간 표현을 추출한다.
 * 시간표현은 Analysis 메소드 안에서 각 extract 메소드를 이용하여 추출하게 된다.
 */
public class TimeAnalysis {
    public HashMap<String, Integer> hMap;
    public HashMap<String, Integer> wMap;
    public HashMap<String, Integer> mMap;

    public int curYear, curMonth, curDay, curHour, curMinute; //현재 날짜
    public int calYear, calMonth, calDay, calHour, calMinute; //계산 후 알람 날짜
    public int curtime;

    String curA = new String(); //curA : 오전 / 오후
    String calA = new String(); //calA : 오전 / 오후

    public String curDayOfWeek;
    public int[] days = new int[]{0, 31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};

    boolean isNextDay; //다음날로 넘어가게 하는 함수인지 확인
    boolean isTime; //시간을 말한 정규식인지 확인
    boolean isNote; //일반메모인지 확인

    public TimeAnalysis() {
        hMap = new HashMap<String, Integer>();
        wMap = new HashMap<String, Integer>();
        mMap = new HashMap<String, Integer>();

        hMap.put("today", 0);
        hMap.put("tomorrow", 1);
        hMap.put("next day", 1);
        hMap.put("next week", 1);

        wMap.put("sunday", 1);
        wMap.put("monday", 2);
        wMap.put("tuesday", 3);
        wMap.put("wednesday", 4);
        wMap.put("thursday", 5);
        wMap.put("friday", 6);
        wMap.put("saturday", 7);

        mMap.put("january", 1);
        mMap.put("february", 2);
        mMap.put("march", 3);
        mMap.put("april", 4);
        mMap.put("may", 5);
        mMap.put("june", 6);
        mMap.put("july", 7);
        mMap.put("august", 8);
        mMap.put("september", 9);
        mMap.put("october", 10);
        mMap.put("november", 11);
        mMap.put("december", 12);
    }

    /**
     * 이 메소드는 초기 시간 값을 설정해주며, extract 메소드를 불러 음성으로 설정하고자 하는 시간 값을 계산하게 된다.
     *
     * @param target 사용자로 부터 입력받은 음성에서 텍스트를 변환한 String 값
     * @return boolean 해당 정규식을 통해서 시간표현이 추출되었는지 여부
     */
    public String Analysis(String target) {
        isNote = false;
        isNextDay = false;
        isTime = false;

        System.out.println("extract " + target);
        Calendar cal = Calendar.getInstance();
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yy:MM:dd:hh:mm:ss:a"); //현재 날짜
        String tempTime = sdf.format(date);
        String temp[] = tempTime.split(":");

        //현재 날짜를 초기화해준다.
        curYear = Integer.parseInt(temp[0]);
        curMonth = Integer.parseInt(temp[1]);
        curDay = Integer.parseInt(temp[2]);
        curHour = Integer.parseInt(temp[3]);
        curMinute = Integer.parseInt(temp[4]);
        //curSecond = Integer.parseInt(temp[5]);

        curA = temp[6];
        //calA = temp[6]; //오전을 기본값으로 두고 있었기에 에러가 났으므로 수정함.

        //24시간 기준으로 오전과 오후를 나눈다
        if (curA.equals("오전") && curHour == 12) {
            curHour = 0; //0시
        }

        if (curA.equals("오후") && curHour == 12) {
            curHour = 12;
        } else if (curA.equals("오후")) {
            curHour += 12;
        }

        //현재 요일을 가져온다.
        cal.set(Calendar.YEAR, curYear);
        cal.set(Calendar.MONTH, curMonth);
        cal.set(Calendar.DATE, curDay);

        switch (cal.get(Calendar.DAY_OF_WEEK) - 1) {
            case 1:
                curDayOfWeek = "sunday";
                break;
            case 2:
                curDayOfWeek = "monday";
                break;
            case 3:
                curDayOfWeek = "tuesday";
                break;
            case 4:
                curDayOfWeek = "wednesday";
                break;
            case 5:
                curDayOfWeek = "thursday";
                break;
            case 6:
                curDayOfWeek = "friday";
                break;
            case 0:
                curDayOfWeek = "saturday";
                break;
        }

        //계산값을 지금 현재 날짜로 초기화후
        calYear = curYear;
        calMonth = curMonth;
        calDay = curDay;
        calHour = curHour;
        calMinute = curMinute;

        String curTime = curYear + ":" + curMonth + ":" + curDay + ":" + curHour + ":" + curMinute;
        System.out.println("extract curTime: " + curTime + ":" + curA + ":" + curDayOfWeek);
        //정규식 표현식에서 계산 값 추출
        extractManager(target);

        String calTime = calYear + ":" + calMonth + ":" + calDay + ":" + calHour + ":" + calMinute;
        //추출한 표현값 리턴

        System.out.println("extract calTime: " + calTime + ":" + calA + ":");

        //시간표현이 없을 때 일반 메모로 인식하기 위해 note라는 문자열을 리턴함.
        if (calTime.equals(curTime))
            isNote = true;

        if (isNote == true)
            return "note";

        //추출하고 연산한 값
        calTime = calYear + ":" + calMonth + ":" + calDay + ":" + calHour + ":" + calMinute;
        return calTime;
    }

    /**
     * 이 메소드는 정규식을 이용하여 음성 파일에서 추출한 텍스트로부터 시간표현을 추출한다.
     * 메소드 시작과 동시에 공백을 없애주며, 유사 단어를 하나로 통합하여 관리한다.
     * 예를 들어 새벽은 오전, 아침도 오전, 저녁은 오후, 낮은 오후 등
     *
     * @param searchTarget 사용자로 부터 입력받은 음성에서 텍스트를 변환한 String 값
     * @return boolean 해당 정규식을 통해서 시간표현이 추출되었는지 여부
     */
    public boolean extractManager(String searchTarget) {

        searchTarget = searchTarget.toLowerCase();

        searchTarget = searchTarget.replaceAll(" one", " 1");
        searchTarget = searchTarget.replaceAll(" two", " 2");
        searchTarget = searchTarget.replaceAll(" three", " 3");
        searchTarget = searchTarget.replaceAll(" for", " 4");
        searchTarget = searchTarget.replaceAll(" four", " 4");
        searchTarget = searchTarget.replaceAll(" five", " 5");
        searchTarget = searchTarget.replaceAll(" six", " 6");
        searchTarget = searchTarget.replaceAll(" seven", " 7");
        searchTarget = searchTarget.replaceAll(" eight", " 8");
        searchTarget = searchTarget.replaceAll(" nine", " 9");
        searchTarget = searchTarget.replaceAll(" ten", " 10");
        searchTarget = searchTarget.replaceAll(" eleven", " 11");
        searchTarget = searchTarget.replaceAll(" tweleve", " 12");

        searchTarget = searchTarget.replaceAll(" in the morning", " a.m.");
        searchTarget = searchTarget.replaceAll(" in the evening", " p.m.");
        searchTarget = searchTarget.replaceAll(" midnight", " 12:00 a.m.");
        searchTarget = searchTarget.replaceAll(" noon", " 12:00 p.m.");


        searchTarget = searchTarget.replaceAll("after ", "in ");
        searchTarget = searchTarget.replaceAll(" an ", " 1 ");
        searchTarget = searchTarget.replaceAll("upcoming ", "");
        searchTarget = searchTarget.replaceAll("on ", "");
        searchTarget = searchTarget.replaceAll("this ", "");
        searchTarget = searchTarget.replaceAll("the ", "");
        searchTarget = searchTarget.replaceAll("o'clock ", "p.m. ");
        searchTarget = searchTarget.replaceAll("and ", "");
        searchTarget = searchTarget.replaceAll("at ", "");
        searchTarget = searchTarget.replaceAll("th", "");
        //searchTarget = searchTarget.replaceAll(" to", " 2");
        searchTarget = searchTarget.replaceAll("rd", "");
        searchTarget = searchTarget.replaceAll("st", "");

        String regex = new String();

        while (true) {

            System.out.println("extract " + searchTarget);

            regex = "(january|february|march|april|may|june|july|august|september|october|november|december) ([1-3]?[0-9]) ([1]?[0-9]):([0-5]?[0-9]) (a.m.|p.m.|am|pm)"; //on May 4 at 3:50 p.m.
            if (checkExtract1(searchTarget, regex)) return true;

            regex = "(january|february|march|april|may|june|july|august|september|october|november|december) ([1-3]?[0-9]) ([1]?[0-9]) (a.m.|p.m.|am|pm)"; //on May 4 at 3 p.m.
            if (checkExtract1_2(searchTarget, regex)) return true;

            regex = "([1]?[0-9]):([0-5]?[0-9]) (a.m.|p.m.|am|pm) (january|february|march|april|may|june|july|august|september|october|november|december) ([1-3]?[0-9])"; //at 3:50 p.m on May 4 .
            if (checkExtract1_3(searchTarget, regex)) return true;

            regex = "([1]?[0-9]) (a.m.|p.m.|am|pm) (january|february|march|april|may|june|july|august|september|october|november|december) ([1-3]?[0-9])"; //at 3 p.m on May 4 .
            if (checkExtract1_4(searchTarget, regex)) return true;

            regex = "([1-3]?[0-9]) ([1]?[0-9]):([0-5]?[0-9]) (a.m.|p.m.|am|pm)"; // on the 4th at 3:50 p.m.
            if (checkExtract1_5(searchTarget, regex)) return true;

            regex = "([1-3]?[0-9]) ([1]?[0-9]) (a.m.|p.m.|am|pm)"; // on the 4th at 3p.m.
            if (checkExtract1_6(searchTarget, regex)) return true;

            regex = "([1]?[0-9]):([0-5]?[0-9]) (a.m.|p.m.|am|pm) ([1-3]?[0-9])"; // at 3:50 p.m. on the 4th
            if (checkExtract1_7(searchTarget, regex)) return true;

            regex = "([1]?[0-9]) (a.m.|p.m.|am|pm) ([1-3]?[0-9])"; // at 3 p.m. on the 4th
            if (checkExtract1_8(searchTarget, regex)) return true;

            regex = "([1]?[0-9]):([0-5]?[0-9]) (a.m.|p.m.|am|pm) next (sunday|monday|tuesday|wednesday|thursday|friday|saturday)"; //12:59 a.m. p.m next 요일
            if (checkExtract2(searchTarget, regex)) return true;

            regex = "([1]?[0-9]) (a.m.|p.m.|am|pm) next (sunday|monday|tuesday|wednesday|thursday|friday|saturday)"; //12 a.m. p.m next 요일
            if (checkExtract2_2(searchTarget, regex)) return true;

            regex = "([1]?[0-9]):([0-5]?[0-9]) (a.m.|p.m.|am|pm) (sunday|monday|tuesday|wednesday|thursday|friday|saturday)"; //12:59 a.m. p.m 요일 or 요일
            if (checkExtract3(searchTarget, regex)) return true;

            regex = "([1]?[0-9]) (a.m.|p.m.|am|pm) (sunday|monday|tuesday|wednesday|thursday|friday|saturday)"; //12 a.m. p.m 요일 or 요일
            if (checkExtract3_2(searchTarget, regex)) return true;

            regex = "([1]?[0-9]):([0-5]?[0-9]) (a.m.|p.m.|am|pm) tomorrow"; //12:59 a.m. p.m tomorrow
            if (checkExtract4_1(searchTarget, regex)) return true;

            regex = "([1]?[0-9]):([0-5]?[0-9]) (a.m.|p.m.|am|pm)"; //12:59 a.m. p.m
            if (checkExtract4(searchTarget, regex)) return true;

            regex = "([1]?[0-9]) (a.m.|p.m.|am|pm) tomorrow"; //12 a.m. p.m tomorrow
            if (checkExtract5_1(searchTarget, regex)) return true;

            regex = "([1]?[0-9]) (a.m.|p.m.|am|pm)"; //12 a.m. p.m
            if (checkExtract5(searchTarget, regex)) return true;

            regex = "in ([0-9]+) (hour|hours) ([0-9]+) (minute|minutes)"; //in 1 hour 30 minutes
            if (checkExtract6(searchTarget, regex)) return true;

            regex = "in ([0-9]+) (hour|hours)"; //in 1 hour
            if (checkExtract6_2(searchTarget, regex)) return true;

            regex = "([0-9]+) (minute|minutes) later"; //30 minutes later
            if (checkExtract6_3(searchTarget, regex)) return true;

            regex = "in ([0-9]+) (minute|minutes)"; //in 30 minutes
            if (checkExtract6_4(searchTarget, regex)) return true;

            regex = "in ([0-9]+) (week|weeks)"; //in 2 weeks
            if (checkExtract7(searchTarget, regex)) return true;

            regex = "in ([0-9]+) (day|days)"; //in 2 days
            if (checkExtract7_2(searchTarget, regex)) return true;


            regex = "다? ?다음 ?주 ?(월|화|수|목|금|토|일) ?요 ?일 ?(오 ?전|오 ?후)? ?([1-2]?[0-9]) ?시 ?([1-5]?[0-9]) ?분"; // 다다음주 월요일 오전/오후 1시 30분
            if (extract15(searchTarget, regex)) break;

            regex = "다? ?다음 ?주 ?(월|화|수|목|금|토|일) ?요 ?일 ?(오 ?전|오 ?후)? ?([1-2]?[0-9]) ?시 ?반"; // 다다음주 월요일 오전/오후 1시 반
            if (extract16(searchTarget, regex)) break;

            regex = "다? ?다음 ?주 ?(월|화|수|목|금|토|일) ?요 ?일 ?(오 ?전|오 ?후)? ?([1-2]?[0-9]) ?시"; //다다음주 월요일 1시
            if (extract17(searchTarget, regex)) break;

            //잘 안쓸 것 같은 정규식
            //regex = "다? ?다음 ?주 ?(1?[0-9]) ?월 ?([1-3]?[0-9]) ?일 ?(오 ?전|오 ?후)? ?([1-2]?[0-9]) ?시 ?([1-5]?[0-9]) ?분"; // 다다음주 ~월 ~일 오전/오후/1시 30분
            //if (extract24(searchTarget, regex)) break;

            regex = "(월|화|수|목|금|토|일) ?요 ?일 ?(오 ?전|오 ?후)? ?([1-2]?[0-9]) ?시 ?([1-5]?[0-9]) ?분"; //월요일 오전/오후 1시 30분
            if (extract18(searchTarget, regex)) break;

            regex = "(월|화|수|목|금|토|일) ?요 ?일 ?(오 ?전|오 ?후)? ?([1-2]?[0-9]) ?시 ?반"; //월요일 오전/오후 1시 반
            if (extract19(searchTarget, regex)) break;

            regex = "(월|화|수|목|금|토|일) ?요 ?일 ?(오 ?전|오 ?후)? ?([1-2]?[0-9]) ?시"; // 월요일 오전/오후 1시
            if (extract20(searchTarget, regex)) break;

            regex = "(오 ?전|오 ?후)? ?([1-2]?[0-9]) ?시 ?([1-5]?[0-9]) ?분 ?(월|화|수|목|금|토|일) ?요 ?일"; //오전/오후 1시 30분 월요일
            if (extract21(searchTarget, regex)) break;

            regex = "(오 ?전|오 ?후)? ?([1-2]?[0-9]) ?시 ?반 ?(월|화|수|목|금|토|일) ?요 ?일"; //오전/오후 1시 반 월요일
            if (extract22(searchTarget, regex)) break;

            regex = "(오 ?전|오 ?후)? ?([1-2]?[0-9]) ?시 ?(월|화|수|목|금|토|일) ?요 ?일"; //오전/오후 1시 월요일
            if (extract23(searchTarget, regex)) break;

            regex = "(1?[0-9]) ?월 ?([1-3]?[0-9]) ?일 ?(오 ?전|오 ?후)? ?([1-2]?[0-9]) ?시 ?([1-5]?[0-9]) ?분"; // 7월 5일 오전/오후 1시 30분
            if (extract24(searchTarget, regex)) break;

            regex = "(1?[0-9]) ?월 ?([1-3]?[0-9]) ?일 ?(오 ?전|오 ?후)? ?([1-2]?[0-9]) ?시 ?반"; // 7월 5일 오전/오후 1시 반
            if (extract28(searchTarget, regex)) break;

            regex = "(1?[0-9]) ?월 ?([1-3]?[0-9]) ?일 ?(오 ?전|오 ?후)? ?([1-2]?[0-9]) ?시"; // 7월 5일 오전/오후 1시
            if (extract25(searchTarget, regex)) break;

            regex = "(1?[0-9]) ?월 ?([1-3]?[0-9]) ?일"; // 7월 4일
            if (extract8(searchTarget, regex)) break;

            regex = "(1?[0-9]) ?월"; // 8월
            if (extract9(searchTarget, regex)) break;

            regex = "([1-3]?[0-9]) ?일 ?(오 ?전|오 ?후)? ?([1-2]?[0-9]) ?시 ?([1-5]?[0-9]) ?분"; // 7일 오전/오후 1시 30분
            if (extract26(searchTarget, regex)) break;

            regex = "([1-3]?[0-9]) ?일 ?(오 ?전|오 ?후)? ?([1-2]?[0-9]) ?시 ?반";  //7일 오전/오후 1시 반
            if (extract29(searchTarget, regex)) break;

            regex = "([1-3]?[0-9]) ?일 ?(오 ?전|오 ?후)? ?([1-2]?[0-9]) ?시"; //7일 오전/오후 1시
            if (extract27(searchTarget, regex)) break;

            regex = "([0-9]+) ?시간 ?([0-9]+) ?분 ?(후|뒤|있다가)"; // 5시간 30분 있다가 알려줘
            if (extract1(searchTarget, regex)) break;

            regex = "([0-9]+) ?시간 ?반 ?(후|뒤|있다가)"; // 5시간 반 있다가 알려줘
            if (extract30(searchTarget, regex)) break;

            regex = "([0-9]+) ?시 ?간? ?만? ?(후|뒤|있다가)"; // 5시간 있다가 알려줘
            if (extract2(searchTarget, regex)) {
                return true; //break;
            }

            regex = "(오 ?전|오 ?후)? ?([1-2]?[0-9]) ?시 ?([1-5]?[0-9])분"; //오전/오후 1시 30분
            if (extract3(searchTarget, regex)) {
                break;
            }

            regex = "(오 ?전|오 ?후)? ?([1-2]?[0-9]) ?시 ?반"; //오전 오후 1시 반
            if (extract4(searchTarget, regex)) {
                break;
            }
            regex = "(오 ?전|오 ?후)? ?([1-2]?[0-9]) ?시"; //오전 오후 1시
            if (extract5(searchTarget, regex)) {
                break;
            }
            regex = "([0-9]+) ?분 ?(후|뒤|있다가)"; // 5분뒤에
            if (extract6(searchTarget, regex)) {
                return true; //break;
            }

            regex = "(^| )([0-9]+) ?주 ?(후|뒤|있다가)"; //3주뒤에
            if (extract7(searchTarget, regex)) {
                return true; //break;
            }

            regex = "(^| )([0-9]+) ?일 ?(후|뒤|있다가)"; // 5일 뒤에
            if (extract11(searchTarget, regex)) {
                return true; //break;
            }

            regex = "([1-3]?[0-9]) ?일"; // 3일 미팅
            if (extract10(searchTarget, regex)) break;

            regex = "다? ?다음 ?주 ?(월|화|수|목|금|토|일) ?요 ?일"; //다음주 토요일
            if (extract13(searchTarget, regex)) break;

            regex = "(월|화|수|목|금|토|일) ?요 ?일"; //토요일
            if (extract14(searchTarget, regex)) break;

            regex = "(다? ?다음 ?주)"; //다음주
            if (extract12(searchTarget, regex)) break;

            break;
        }

        regex = "[다다음날|다음날|내일|낼|명일|모레|내일모레|내일모래|낼모레|낼모래|모래|글피|익일|명일]+";
        int ret = extract100(searchTarget, regex);
        System.out.println("extract ret " + ret + calA);
        if (ret > 0) {
            isNextDay = true;
            calDay += ret;
            int day_num = days[calMonth];
            calMonth += calDay == day_num ? 0 : calDay / day_num;
            calDay = calDay % day_num == 0 ? day_num : calDay % day_num;

            if (!isTime) {
                calHour = 8;
                calMinute = 0;
            }
        }

        //하루 이틀 사흘 나흘 닷새 엿새 이레의 표현이 있을 경우
        regex = "[하루|이틀|사흘|나흘|닷새|닷세|엿새|엿세|이레|열흘|보름]+(후|뒤|있다가)";
        extract103(searchTarget, regex);

        //오늘이라고 말할 경우
        regex = "(오늘)+";
        extract102(searchTarget, regex);

        //오전 오후를 식별해주는 정규식
        regex = "오전|오후";
        extract101(searchTarget, regex);

        return true;
    }

    /**
     * 이 메소드는 음성에서 추출한 시간표현에 순서대로 "월" "일" "시" "분" 대한 정보가 있을 시, 시간 표현을 추출하여 설정 시간 값에 할당해준다.
     *
     * @param searchTarget 사용자로 부터 입력받은 음성에서 텍스트를 변환한 String 값
     * @param regex        정규식 표현
     * @return boolean 해당 정규식을 통해서 시간표현이 추출되었는지 여부
     */

    public boolean checkExtract1(String searchTarget, String regex) //on May 4 at 3:50 p.m.
    {
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(searchTarget);
        String result = "";
        String[] temp = new String[4];

        boolean ischeckedExtract = false;

        while (matcher.find()) {
            System.out.println("checkExtract1");

            result = matcher.group(0);
            result = result.replaceAll(" (a.m.|p.m.)", "");

            temp = result.split(" ");

            String[] temp_time = new String[2];
            temp_time = temp[2].split(":");

            String temp_regex = "(a.m.|p.m.)";
            int ap = checkAMPM(searchTarget, temp_regex);

            if (ap == 1) {
                calA = "오전";
                calYear = curYear;
                calMonth = mMap.get(temp[0]);
                calDay = Integer.parseInt(temp[1]);
                calHour = Integer.parseInt(temp_time[0]) % 12;
                calMinute = Integer.parseInt(temp_time[1]);
            } else if (ap == 2) {
                calA = "오후";
                calYear = curYear;
                calMonth = mMap.get(temp[0]);
                calDay = Integer.parseInt(temp[1]);
                calHour = Integer.parseInt(temp_time[0]) % 12 + 12;
                calMinute = Integer.parseInt(temp_time[1]);
            }

            compareMonth();
            changeDate();

            ischeckedExtract = true;
        }
        return ischeckedExtract;
    }

    public boolean checkExtract1_2(String searchTarget, String regex) //on May 4 at 3 p.m.
    {
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(searchTarget);
        String result = "";
        String[] temp = new String[4];

        boolean ischeckedExtract = false;

        while (matcher.find()) {
            System.out.println("checkExtract1_2");

            result = matcher.group(0);
            result = result.replaceAll(" (a.m.|p.m.)", "");

            temp = result.split(" ");

            String[] temp_time = new String[2];
            temp_time[0] = temp[2];

            String temp_regex = "(a.m.|p.m.)";
            int ap = checkAMPM(searchTarget, temp_regex);

            if (ap == 1) {
                calA = "오전";
                calYear = curYear;
                calMonth = mMap.get(temp[0]);
                calDay = Integer.parseInt(temp[1]);
                calHour = Integer.parseInt(temp_time[0]) % 12;
                calMinute = 0;
            } else if (ap == 2) {
                calA = "오후";
                calYear = curYear;
                calMonth = mMap.get(temp[0]);
                calDay = Integer.parseInt(temp[1]);
                calHour = Integer.parseInt(temp_time[0]) % 12 + 12;
                calMinute = 0;
            }

            compareMonth();
            changeDate();

            ischeckedExtract = true;
        }
        return ischeckedExtract;
    }

    public boolean checkExtract1_3(String searchTarget, String regex) //at 3:50 p.m. on May 4
    {
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(searchTarget);
        String result = "";
        String[] temp = new String[4];

        boolean ischeckedExtract = false;

        while (matcher.find()) {
            System.out.println("checkExtract1_3");

            result = matcher.group(0);
            result = result.replaceAll(" (a.m.|p.m.)", "");

            temp = result.split(" ");

            String[] temp_time = new String[2];
            temp_time = temp[0].split(":");

            String temp_regex = "(a.m.|p.m.)";
            int ap = checkAMPM(searchTarget, temp_regex);

            if (ap == 1) {
                calA = "오전";
                calYear = curYear;
                calMonth = mMap.get(temp[1]);
                calDay = Integer.parseInt(temp[2]);
                calHour = Integer.parseInt(temp_time[0]) % 12;
                calMinute = Integer.parseInt(temp_time[1]);
            } else if (ap == 2) {
                calA = "오후";
                calYear = curYear;
                calMonth = mMap.get(temp[1]);
                calDay = Integer.parseInt(temp[2]);
                calHour = Integer.parseInt(temp_time[0]) % 12 + 12;
                calMinute = Integer.parseInt(temp_time[1]);
            }

            compareMonth();
            changeDate();

            ischeckedExtract = true;
        }
        return ischeckedExtract;
    }

    public boolean checkExtract1_4(String searchTarget, String regex) //at 3 p.m. on May 4
    {
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(searchTarget);
        String result = "";
        String[] temp = new String[4];

        boolean ischeckedExtract = false;

        while (matcher.find()) {
            System.out.println("checkExtract1_4");

            result = matcher.group(0);
            result = result.replaceAll(" (a.m.|p.m.)", "");

            temp = result.split(" ");

            String[] temp_time = new String[2];
            temp_time[0] = temp[0];

            String temp_regex = "(a.m.|p.m.)";
            int ap = checkAMPM(searchTarget, temp_regex);

            if (ap == 1) {
                calA = "오전";
                calYear = curYear;
                calMonth = mMap.get(temp[1]);
                calDay = Integer.parseInt(temp[2]);
                calHour = Integer.parseInt(temp_time[0]) % 12;
                calMinute = 0;
            } else if (ap == 2) {
                calA = "오후";
                calYear = curYear;
                calMonth = mMap.get(temp[1]);
                calDay = Integer.parseInt(temp[2]);
                calHour = Integer.parseInt(temp_time[0]) % 12 + 12;
                calMinute = 0;
            }

            compareMonth();
            changeDate();

            ischeckedExtract = true;
        }
        return ischeckedExtract;
    }

    public boolean checkExtract1_5(String searchTarget, String regex) //on the 4th at 3:50 p.m.
    {
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(searchTarget);
        String result = "";
        String[] temp = new String[4];

        boolean ischeckedExtract = false;

        while (matcher.find()) {
            System.out.println("checkExtract1_5");

            result = matcher.group(0);
            result = result.replaceAll(" (a.m.|p.m.)", "");

            temp = result.split(" ");

            String[] temp_time = new String[2];
            temp_time = temp[1].split(":");

            String temp_regex = "(a.m.|p.m.)";
            int ap = checkAMPM(searchTarget, temp_regex);

            if (ap == 1) {
                calA = "오전";
                calYear = curYear;
                calMonth = curMonth;
                calDay = Integer.parseInt(temp[0]);
                calHour = Integer.parseInt(temp_time[0]) % 12;
                calMinute = Integer.parseInt(temp_time[1]);
            } else if (ap == 2) {
                calA = "오후";
                calYear = curYear;
                calMonth = curMonth;
                calDay = Integer.parseInt(temp[0]);
                calHour = Integer.parseInt(temp_time[0]) % 12 + 12;
                calMinute = Integer.parseInt(temp_time[1]);
            }

            compareDay();
            changeDate();

            ischeckedExtract = true;
        }
        return ischeckedExtract;
    }

    public boolean checkExtract1_6(String searchTarget, String regex) //on the 4th at 3 p.m.
    {
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(searchTarget);
        String result = "";
        String[] temp = new String[4];

        boolean ischeckedExtract = false;

        while (matcher.find()) {
            System.out.println("checkExtract1_6");

            result = matcher.group(0);
            result = result.replaceAll(" (a.m.|p.m.)", "");

            temp = result.split(" ");

            String[] temp_time = new String[2];
            temp_time[0] = temp[1];

            String temp_regex = "(a.m.|p.m.)";
            int ap = checkAMPM(searchTarget, temp_regex);

            if (ap == 1) {
                calA = "오전";
                calYear = curYear;
                calMonth = curMonth;
                calDay = Integer.parseInt(temp[0]);
                calHour = Integer.parseInt(temp_time[0]) % 12;
                calMinute = 0;
            } else if (ap == 2) {
                calA = "오후";
                calYear = curYear;
                calMonth = curMonth;
                calDay = Integer.parseInt(temp[0]);
                calHour = Integer.parseInt(temp_time[0]) % 12 + 12;
                calMinute = 0;
            }

            compareDay();
            changeDate();

            ischeckedExtract = true;
        }
        return ischeckedExtract;
    }

    public boolean checkExtract1_7(String searchTarget, String regex) //at 3:50 p.m. on the 4th
    {
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(searchTarget);
        String result = "";
        String[] temp = new String[4];

        boolean ischeckedExtract = false;

        while (matcher.find()) {
            System.out.println("checkExtract1_7");

            result = matcher.group(0);
            result = result.replaceAll(" (a.m.|p.m.)", "");

            temp = result.split(" ");

            String[] temp_time = new String[2];
            temp_time = temp[0].split(":");

            String temp_regex = "(a.m.|p.m.)";
            int ap = checkAMPM(searchTarget, temp_regex);

            if (ap == 1) {
                calA = "오전";
                calYear = curYear;
                calMonth = curMonth;
                calDay = Integer.parseInt(temp[1]);
                calHour = Integer.parseInt(temp_time[0]) % 12;
                calMinute = Integer.parseInt(temp_time[1]);
            } else if (ap == 2) {
                calA = "오후";
                calYear = curYear;
                calMonth = curMonth;
                calDay = Integer.parseInt(temp[1]);
                calHour = Integer.parseInt(temp_time[0]) % 12 + 12;
                calMinute = Integer.parseInt(temp_time[1]);
            }

            compareDay();
            changeDate();

            ischeckedExtract = true;
        }
        return ischeckedExtract;
    }

    public boolean checkExtract1_8(String searchTarget, String regex) //at 3 p.m. on the 4th
    {
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(searchTarget);
        String result = "";
        String[] temp = new String[4];

        boolean ischeckedExtract = false;

        while (matcher.find()) {
            System.out.println("checkExtract1_8");

            result = matcher.group(0);
            result = result.replaceAll(" (a.m.|p.m.)", "");

            temp = result.split(" ");

            String[] temp_time = new String[2];
            temp_time[0] = temp[0];

            String temp_regex = "(a.m.|p.m.)";
            int ap = checkAMPM(searchTarget, temp_regex);

            if (ap == 1) {
                calA = "오전";
                calYear = curYear;
                calMonth = curMonth;
                calDay = Integer.parseInt(temp[1]);
                calHour = Integer.parseInt(temp_time[0]) % 12;
                calMinute = 0;
            } else if (ap == 2) {
                calA = "오후";
                calYear = curYear;
                calMonth = curMonth;
                calDay = Integer.parseInt(temp[1]);
                calHour = Integer.parseInt(temp_time[0]) % 12 + 12;
                calMinute = 0;
            }

            compareDay();
            changeDate();

            ischeckedExtract = true;
        }
        return ischeckedExtract;
    }

    public boolean checkExtract2(String searchTarget, String regex) //12:59 a.m. p.m next 요일
    {
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(searchTarget);
        String result = "";
        String[] temp = new String[4];

        boolean ischeckedExtract = false;

        while (matcher.find()) {
            System.out.println("checkExtract2");

            result = matcher.group(0);
            result = result.replaceAll(" (a.m.|p.m.)", "");
            result = result.replaceAll("next ", "");

            //System.out.println("checkExtract1 " + result );
            temp = result.split(" ");

            String[] temp_time = new String[2];
            temp_time = temp[0].split(":");

            String temp_regex = "(a.m.|p.m.)";
            int ap = checkAMPM(searchTarget, temp_regex);

            int calweekday = 7 + (wMap.get(temp[1]) - (wMap.get(curDayOfWeek)));

            if (ap == 1) {
                calA = "오전";
                calYear = curYear;
                calMonth = curMonth;
                calDay = curDay + calweekday;
                calHour = Integer.parseInt(temp_time[0]) % 12;
                calMinute = Integer.parseInt(temp_time[1]);
            } else if (ap == 2) {
                calA = "오후";
                calYear = curYear;
                calMonth = curMonth;
                calDay = curDay + calweekday;
                calHour = Integer.parseInt(temp_time[0]) % 12 + 12;
                calMinute = Integer.parseInt(temp_time[1]);
            }
            changeDate();

            ischeckedExtract = true;
        }
        return ischeckedExtract;
    }

    public boolean checkExtract2_2(String searchTarget, String regex) //12a.m. p.m next 요일
    {
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(searchTarget);
        String result = "";
        String[] temp = new String[4];

        boolean ischeckedExtract = false;

        while (matcher.find()) {
            System.out.println("checkExtract2_2");

            result = matcher.group(0);
            result = result.replaceAll(" (a.m.|p.m.)", "");
            result = result.replaceAll("next ", "");

            temp = result.split(" ");

            String temp_time[] = new String[2];
            temp_time[0] = temp[0];

            String temp_regex = "(a.m.|p.m.)";
            int ap = checkAMPM(searchTarget, temp_regex);

            int calweekday = 7 + (wMap.get(temp[1]) - (wMap.get(curDayOfWeek)));

            if (ap == 1) {
                calA = "오전";
                calYear = curYear;
                calMonth = curMonth;
                calDay = curDay + calweekday;
                calHour = Integer.parseInt(temp_time[0]) % 12;
                calMinute = 0;
            } else if (ap == 2) {
                calA = "오후";
                calYear = curYear;
                calMonth = curMonth;
                calDay = curDay + calweekday;
                calHour = Integer.parseInt(temp_time[0]) % 12 + 12;
                calMinute = 0;
            }
            changeDate();

            ischeckedExtract = true;
        }
        return ischeckedExtract;
    }

    public boolean checkExtract3(String searchTarget, String regex) //12:59 a.m. p.m 요일
    {
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(searchTarget);
        String result = "";
        String[] temp = new String[4];

        boolean ischeckedExtract = false;

        while (matcher.find()) {
            System.out.println("checkExtract3");

            result = matcher.group(0);
            result = result.replaceAll(" (a.m.|p.m.)", "");

            temp = result.split(" ");

            String[] temp_time = new String[2];
            temp_time = temp[0].split(":");

            String temp_regex = "(a.m.|p.m.)";
            int ap = checkAMPM(searchTarget, temp_regex);

            int calweekday;

            if (wMap.get(temp[1]) - (wMap.get(curDayOfWeek)) < 0)
                calweekday = 7 + (wMap.get(temp[1]) - (wMap.get(curDayOfWeek)));
            else
                calweekday = (wMap.get(temp[1]) - (wMap.get(curDayOfWeek)));

            if (ap == 1) {
                calA = "오전";
                calYear = curYear;
                calMonth = curMonth;
                calDay = curDay + calweekday;
                calHour = Integer.parseInt(temp_time[0]) % 12;
                calMinute = Integer.parseInt(temp_time[1]);
            } else if (ap == 2) {
                calA = "오후";
                calYear = curYear;
                calMonth = curMonth;
                calDay = curDay + calweekday;
                calHour = Integer.parseInt(temp_time[0]) % 12 + 12;
                calMinute = Integer.parseInt(temp_time[1]);
            }
            changeDate();

            ischeckedExtract = true;
        }
        return ischeckedExtract;
    }

    public boolean checkExtract3_2(String searchTarget, String regex) //12 a.m. p.m 요일
    {
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(searchTarget);
        String result = "";
        String[] temp = new String[4];

        boolean ischeckedExtract = false;

        while (matcher.find()) {
            System.out.println("checkExtract3_2");

            result = matcher.group(0);
            result = result.replaceAll(" (a.m.|p.m.)", "");

            temp = result.split(" ");

            String temp_time[] = new String[2];
            temp_time[0] = temp[0];

            String temp_regex = "(a.m.|p.m.)";
            int ap = checkAMPM(searchTarget, temp_regex);

            int calweekday;

            if (wMap.get(temp[1]) - (wMap.get(curDayOfWeek)) < 0)
                calweekday = 7 + (wMap.get(temp[1]) - (wMap.get(curDayOfWeek)));
            else
                calweekday = (wMap.get(temp[1]) - (wMap.get(curDayOfWeek)));

            if (ap == 1) {
                calA = "오전";
                calYear = curYear;
                calMonth = curMonth;
                calDay = curDay + calweekday;
                calHour = Integer.parseInt(temp_time[0]) % 12;
                calMinute = 0;
            } else if (ap == 2) {
                calA = "오후";
                calYear = curYear;
                calMonth = curMonth;
                calDay = curDay + calweekday;
                calHour = Integer.parseInt(temp_time[0]) % 12 + 12;
                calMinute = 0;
            }
            changeDate();

            ischeckedExtract = true;
        }
        return ischeckedExtract;
    }

    public boolean checkExtract4(String searchTarget, String regex) //12:59 a.m. p.m
    {
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(searchTarget);
        String result = "";
        String[] temp = new String[4];

        boolean ischeckedExtract = false;

        while (matcher.find()) {
            System.out.println("checkExtract4");

            result = matcher.group(0);
            result = result.replaceAll(" (a.m.|p.m.)", "");

            temp = result.split(" ");

            String[] temp_time = new String[2];
            temp_time = temp[0].split(":");

            String temp_regex = "(a.m.|p.m.)";

            int ap = checkAMPM(searchTarget, temp_regex);

            if (ap == 1) {
                calA = "오전";
                calYear = curYear;
                calMonth = curMonth;
                calDay = curDay;
                calHour = Integer.parseInt(temp_time[0]) % 12;
                calMinute = Integer.parseInt(temp_time[1]);
            } else if (ap == 2) {
                calA = "오후";
                calYear = curYear;
                calMonth = curMonth;
                calDay = curDay;
                calHour = Integer.parseInt(temp_time[0]) % 12 + 12;
                calMinute = Integer.parseInt(temp_time[1]);
            }

            compareHour();
            changeDate();

            ischeckedExtract = true;
        }
        return ischeckedExtract;
    }

    public boolean checkExtract4_1(String searchTarget, String regex) //12:59 a.m. p.m tomorrow
    {
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(searchTarget);
        String result = "";
        String[] temp = new String[4];

        boolean ischeckedExtract = false;

        while (matcher.find()) {
            System.out.println("checkExtract4_1");

            result = matcher.group(0);
            result = result.replaceAll(" (a.m.|p.m.)", "");
            result = result.replaceAll(" tomorrow", "");

            temp = result.split(" ");

            String[] temp_time = new String[2];
            temp_time = temp[0].split(":");

            String temp_regex = "(a.m.|p.m.)";

            int ap = checkAMPM(searchTarget, temp_regex);

            if (ap == 1) {
                calA = "오전";
                calYear = curYear;
                calMonth = curMonth;
                calDay = curDay + 1;
                calHour = Integer.parseInt(temp_time[0]) % 12;
                calMinute = Integer.parseInt(temp_time[1]);
            } else if (ap == 2) {
                calA = "오후";
                calYear = curYear;
                calMonth = curMonth;
                calDay = curDay + 1;
                calHour = Integer.parseInt(temp_time[0]) % 12 + 12;
                calMinute = Integer.parseInt(temp_time[1]);
            }

            changeDate();

            ischeckedExtract = true;
        }
        return ischeckedExtract;
    }


    public boolean checkExtract5(String searchTarget, String regex) //12a.m. p.m
    {
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(searchTarget);
        String result = "";

        boolean ischeckedExtract = false;

        while (matcher.find()) {
            System.out.println("checkExtract5");

            result = matcher.group(0);
            result = result.replaceAll(" (a.m.|p.m.)", "");

            String[] temp_time = new String[2];
            temp_time = result.split(" ");

            String temp_regex = "(a.m.|p.m.)";

            int ap = checkAMPM(searchTarget, temp_regex);

            if (ap == 1) {
                calA = "오전";
                calYear = curYear;
                calMonth = curMonth;
                calDay = curDay;
                calHour = Integer.parseInt(temp_time[0]) % 12;
                calMinute = 0;
            } else if (ap == 2) {
                calA = "오후";
                calYear = curYear;
                calMonth = curMonth;
                calDay = curDay;
                calHour = Integer.parseInt(temp_time[0]) % 12 + 12;
                calMinute = 0;
            }

            compareHour();
            changeDate();

            ischeckedExtract = true;
        }
        return ischeckedExtract;
    }

    public boolean checkExtract5_1(String searchTarget, String regex) //12a.m. p.m tomorrow
    {
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(searchTarget);
        String result = "";

        boolean ischeckedExtract = false;

        while (matcher.find()) {
            System.out.println("checkExtract5_1");

            result = matcher.group(0);
            result = result.replaceAll(" (a.m.|p.m.)", "");
            result = result.replaceAll(" tomorrow", "");

            String[] temp_time = new String[2];
            temp_time = result.split(" ");

            String temp_regex = "(a.m.|p.m.)";

            int ap = checkAMPM(searchTarget, temp_regex);

            if (ap == 1) {
                calA = "오전";
                calYear = curYear;
                calMonth = curMonth;
                calDay = curDay + 1;
                calHour = Integer.parseInt(temp_time[0]) % 12;
                calMinute = 0;
            } else if (ap == 2) {
                calA = "오후";
                calYear = curYear;
                calMonth = curMonth;
                calDay = curDay + 1;
                calHour = Integer.parseInt(temp_time[0]) % 12 + 12;
                calMinute = 0;
            }

            changeDate();

            ischeckedExtract = true;
        }
        return ischeckedExtract;
    }

    public boolean checkExtract6(String searchTarget, String regex) //in 1 hour 45 minutes
    {
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(searchTarget);
        String result = "";

        boolean ischeckedExtract = false;

        while (matcher.find()) {
            System.out.println("checkExtract6");

            result = matcher.group(0);

            result = result.replaceAll(" (hours|hour|minutes|minute)", "");
            result = result.replaceAll("in ", "");

            System.out.println("checkExtract5 " + result);

            String[] temp_time = new String[2];
            temp_time = result.split(" ");

            int addhour = Integer.parseInt(temp_time[0]);
            int addminute = Integer.parseInt(temp_time[1]);

            afterTime(addminute, addhour, 0);

            changeDate();

            ischeckedExtract = true;
        }
        return ischeckedExtract;
    }

    public boolean checkExtract6_2(String searchTarget, String regex) //in 1 hour
    {
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(searchTarget);
        String result = "";

        boolean ischeckedExtract = false;

        while (matcher.find()) {
            System.out.println("checkExtract6_2");

            result = matcher.group(0);

            result = result.replaceAll(" (hours|hour)", "");
            result = result.replaceAll("in ", "");

            String[] temp_time = new String[2];
            temp_time = result.split(" ");

            int addhour = Integer.parseInt(temp_time[0]);
            int addminute = 0;

            afterTime(addminute, addhour, 0);

            changeDate();

            ischeckedExtract = true;
        }
        return ischeckedExtract;
    }

    public boolean checkExtract6_3(String searchTarget, String regex) //30 minutes later
    {
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(searchTarget);
        String result = "";

        boolean ischeckedExtract = false;

        while (matcher.find()) {
            System.out.println("checkExtract6_3");

            result = matcher.group(0);

            result = result.replaceAll(" (minutes|minute)", "");
            result = result.replaceAll(" later", "");

            String[] temp_time = new String[2];
            temp_time = result.split(" ");

            int addhour = 0;
            int addminute = Integer.parseInt(temp_time[0]);

            afterTime(addminute, addhour, 0);

            changeDate();

            ischeckedExtract = true;
        }
        return ischeckedExtract;
    }

    public boolean checkExtract6_4(String searchTarget, String regex) //in 45 minutes
    {
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(searchTarget);
        String result = "";

        boolean ischeckedExtract = false;

        while (matcher.find()) {
            System.out.println("checkExtract6_4");

            result = matcher.group(0);

            result = result.replaceAll(" (minutes|minute)", "");
            result = result.replaceAll("in ", "");

            String[] temp_time = new String[2];
            temp_time = result.split(" ");

            int addhour = 0;
            int addminute = Integer.parseInt(temp_time[0]);

            afterTime(addminute, addhour, 0);

            changeDate();

            ischeckedExtract = true;
        }
        return ischeckedExtract;
    }

    public boolean checkExtract7(String searchTarget, String regex) //in 2 weeks
    {
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(searchTarget);
        String result = "";

        boolean ischeckedExtract = false;

        while (matcher.find()) {
            System.out.println("checkExtract7");

            result = matcher.group(0);

            result = result.replaceAll(" (weeks|week)", "");
            result = result.replaceAll("in ", "");

            String[] temp_time = new String[2];
            temp_time = result.split(" ");

            int addday = Integer.parseInt(temp_time[0]) * 7;

            afterTime(0, 0, addday);

            changeDate();

            ischeckedExtract = true;
        }
        return ischeckedExtract;
    }

    public boolean checkExtract7_2(String searchTarget, String regex) //in 2 day
    {
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(searchTarget);
        String result = "";

        boolean ischeckedExtract = false;

        while (matcher.find()) {
            System.out.println("checkExtract7_2");

            result = matcher.group(0);

            result = result.replaceAll(" (days|day)", "");
            result = result.replaceAll("in ", "");

            String[] temp_time = new String[2];
            temp_time = result.split(" ");

            int addday = Integer.parseInt(temp_time[0]);

            afterTime(0, 0, addday);

            changeDate();

            ischeckedExtract = true;
        }
        return ischeckedExtract;
    }

    public void compareHour() {

        if (curHour * 60 + curMinute >= calHour * 60 + calMinute) {
            calDay = calDay + 1;
        }
    }

    public void compareDay() {

        if (curDay * 1440 + curHour * 60 + curMinute >= calDay * 1440 + calHour * 60 + calMinute) {
            calMonth = calMonth + 1;
        }
    }

    public void compareMonth() {
        if (curMonth * 43200 + curDay * 1440 + curHour * 60 + curMinute >= calMonth * 43200 + calDay * 1440 + calHour * 60 + calMinute) {
            calYear = calYear + 1;
        }
    }

    public int checkAMPM(String searchTarget, String regex) {

        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(searchTarget);
        String result;

        while (matcher.find()) {
            System.out.println("checkAMPM");
            result = matcher.group(0);

            if (result.equals("a.m.")) {
                return 1;
            } else if (result.equals("p.m.")) {
                return 2;
            }
        }
        return 3;
    }

    public void changeDate() {

        calHour += calMinute / 60;
        calMinute = calMinute % 60;

        calDay += calHour / 24;
        calHour = calHour % 24;

        int day_num = days[curMonth];
        calMonth += calDay == day_num ? 0 : calDay / day_num;
        calDay = calDay % day_num == 0 ? day_num : calDay % day_num;

        calYear += calMonth / 13;
        calMonth = calMonth % 12 == 0 ? 12 : calMonth % 12;
        calMonth = calMonth % 12 == 0 ? 12 : calMonth % 12;

    }

    public void afterTime(int m, int h, int d) {

        calMinute = curMinute + m;
        calHour = curHour + h;
        calDay = curDay + d;
        calMonth = curMonth;
        calYear = curYear;

        calHour += calMinute / 60;
        calMinute = calMinute % 60;
        calDay += calHour / 24;
        calHour = calHour % 24;

        int day_num = days[curMonth];
        calMonth += calDay == day_num ? 0 : calDay / day_num;
        calDay = calDay % day_num == 0 ? day_num : calDay % day_num;
        calYear += calMonth / 13;
        calMonth = calMonth % 12 == 0 ? 12 : calMonth % 12;

    }

    public boolean extract24(String searchTarget, String regex) { //월 일 시 분
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(searchTarget);

        boolean isExtracted = false;
        String result = "";
        String[] temp = new String[4];
        while (matcher.find()) {
            System.out.println("extract24");

            isNextDay = true;
            isExtracted = true;

            result = matcher.group(0);
            result = result.replaceAll("오전", "");
            result = result.replaceAll("오후", "");
            temp = result.replaceAll(" ", "").split("월|일|시|분");

            // 시간표현 초기화
            calMonth = Integer.parseInt(temp[0]);
            calDay = Integer.parseInt(temp[1]);
            calHour = Integer.parseInt(temp[2]);
            calMinute = Integer.parseInt(temp[3]);

            //시간바꿔주기 위한것
            regex = "오전|오후";
            extract101(searchTarget, regex);

            if (curMonth * 43200 + curDay * 1440 + curHour * 60 + curMinute >= calMonth * 43200 + calDay * 1440 + calHour * 60 + calMinute)
                atTime(curYear + 1, Integer.parseInt(temp[0]), Integer.parseInt(temp[1]), Integer.parseInt(temp[2]), Integer.parseInt(temp[3]));
            else
                atTime(curYear, Integer.parseInt(temp[0]), Integer.parseInt(temp[1]), Integer.parseInt(temp[2]), Integer.parseInt(temp[3]));
        }
        return isExtracted;
    }

    /**
     * 이 메소드는 음성에서 추출한 시간표현에 순서대로 "월" "일" "시"에 대한 정보가 있을 시, 시간 표현을 추출하여 설정 시간 값에 할당해준다.
     *
     * @param searchTarget 사용자로 부터 입력받은 음성에서 텍스트를 변환한 String 값
     * @param regex        정규식 표현
     * @return boolean 해당 정규식을 통해서 시간표현이 추출되었는지 여부
     */
    public boolean extract25(String searchTarget, String regex) { //월 일 시
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(searchTarget);

        boolean isExtracted = false;
        String result = "";
        String[] temp = new String[4];

        while (matcher.find()) {
            System.out.println("extract25");

            isNextDay = true;
            isExtracted = true;
            result = matcher.group(0);
            result = result.replaceAll("오전", "");
            result = result.replaceAll("오후", "");
            temp = result.replaceAll(" ", "").split("월|일|시");

            // 시간표현 초기화
            calMonth = Integer.parseInt(temp[0]);
            calDay = Integer.parseInt(temp[1]);
            calHour = Integer.parseInt(temp[2]);

            //시간바꿔주기 위한것
            regex = "오전|오후";
            extract101(searchTarget, regex);

            if (curMonth * 720 + curDay * 24 + curHour >= calMonth * 720 + calDay * 24 + calHour)
                atTime(curYear + 1, Integer.parseInt(temp[0]), Integer.parseInt(temp[1]), Integer.parseInt(temp[2]), 0);
            else
                atTime(curYear, Integer.parseInt(temp[0]), Integer.parseInt(temp[1]), Integer.parseInt(temp[2]), 0);
        }

        return isExtracted;
    }

    /**
     * 이 메소드는 음성에서 추출한 시간표현에 순서대로 "일" "시" "분" 대한 정보가 있을 시, 시간 표현을 추출하여 설정 시간 값에 할당해준다.
     *
     * @param searchTarget 사용자로 부터 입력받은 음성에서 텍스트를 변환한 String 값
     * @param regex        정규식 표현
     * @return boolean 해당 정규식을 통해서 시간표현이 추출되었는지 여부
     */
    public boolean extract26(String searchTarget, String regex) { //일 시 분
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(searchTarget);

        boolean isExtracted = false;
        String result = "";
        String[] temp = new String[4];

        while (matcher.find()) {
            System.out.println("extract26");

            isNextDay = true;
            isExtracted = true;

            result = matcher.group(0);
            result = result.replaceAll("오전", "");
            result = result.replaceAll("오후", "");
            temp = result.replaceAll(" ", "").split("일|시|분");

            // 시간표현 초기화
            calDay = Integer.parseInt(temp[0]);
            calHour = Integer.parseInt(temp[1]);
            calMinute = Integer.parseInt(temp[2]);

            //시간바꿔주기 위한것
            regex = "오전|오후";
            extract101(searchTarget, regex);

            if (curDay * 1440 + curHour * 60 + curMinute >= calDay * 1440 + calHour * 60 + calMinute)
                atTime(curYear, curMonth + 1, Integer.parseInt(temp[0]), Integer.parseInt(temp[1]), Integer.parseInt(temp[2]));
            else
                atTime(curYear, curMonth, Integer.parseInt(temp[0]), Integer.parseInt(temp[1]), Integer.parseInt(temp[2]));
        }

        return isExtracted;
    }

    /**
     * 이 메소드는 음성에서 추출한 시간표현에 순서대로 "일" "시"에 대한 정보가 있을 시, 시간 표현을 추출하여 설정 시간 값에 할당해준다.
     *
     * @param searchTarget 사용자로 부터 입력받은 음성에서 텍스트를 변환한 String 값
     * @param regex        정규식 표현
     * @return boolean 해당 정규식을 통해서 시간표현이 추출되었는지 여부
     */
    public boolean extract27(String searchTarget, String regex) { //일 시
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(searchTarget);

        boolean isExtracted = false;

        String result = "";
        String[] temp = new String[4];
        while (matcher.find()) {
            System.out.println("extract27");

            isNextDay = true;
            isExtracted = true;

            result = matcher.group(0);
            result = result.replaceAll("오전", "");
            result = result.replaceAll("오후", "");
            temp = result.replaceAll(" ", "").split("일|시");
            System.out.println("temp : " + temp[0] + temp[1]);

            // 시간표현 초기화
            calDay = Integer.parseInt(temp[0]);
            calHour = Integer.parseInt(temp[1]);

            //시간바꿔주기 위한것
            regex = "오전|오후";
            extract101(searchTarget, regex);

            if (curDay * 24 + curHour >= calDay * 24 + calHour)
                atTime(curYear, curMonth + 1, Integer.parseInt(temp[0]), Integer.parseInt(temp[1]), 0);
            else
                atTime(curYear, curMonth, Integer.parseInt(temp[0]), Integer.parseInt(temp[1]), 0);
        }

        return isExtracted;
    }

    /**
     * 이 메소드는 음성에서 추출한 시간표현에 순서대로 "월" "일" "시" "반"에 대한 정보가 있을 시, 시간 표현을 추출하여 설정 시간 값에 할당해준다.
     *
     * @param searchTarget 사용자로 부터 입력받은 음성에서 텍스트를 변환한 String 값
     * @param regex        정규식 표현
     * @return boolean 해당 정규식을 통해서 시간표현이 추출되었는지 여부
     */
    public boolean extract28(String searchTarget, String regex) { //월 일 시 반
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(searchTarget);

        boolean isExtracted = false;
        String result = "";
        String[] temp = new String[4];

        while (matcher.find()) {
            System.out.println("extract28");

            isNextDay = true;
            isExtracted = true;

            result = matcher.group(0);
            result = result.replaceAll("오전", "");
            result = result.replaceAll("오후", "");
            temp = result.replaceAll(" ", "").split("월|일|시");

            // 시간표현 초기화
            calMonth = Integer.parseInt(temp[0]);
            calDay = Integer.parseInt(temp[1]);
            calHour = Integer.parseInt(temp[2]);
            calMinute = 30;

            //시간바꿔주기 위한것
            regex = "오전|오후";
            extract101(searchTarget, regex);

            if (curMonth * 43200 + curDay * 1440 + curHour * 60 + curMinute >= calMonth * 43200 + calDay * 1440 + calHour * 60 + calMinute)
                atTime(curYear + 1, Integer.parseInt(temp[0]), Integer.parseInt(temp[1]), Integer.parseInt(temp[2]), 30);
            else
                atTime(curYear, Integer.parseInt(temp[0]), Integer.parseInt(temp[1]), Integer.parseInt(temp[2]), 30);
        }

        return isExtracted;
    }

    /**
     * 이 메소드는 음성에서 추출한 시간표현에 순서대로 "일" "시" "반"에 대한 정보가 있을 시, 시간 표현을 추출하여 설정 시간 값에 할당해준다.
     *
     * @param searchTarget 사용자로 부터 입력받은 음성에서 텍스트를 변환한 String 값
     * @param regex        정규식 표현
     * @return boolean 해당 정규식을 통해서 시간표현이 추출되었는지 여부
     */
    public boolean extract29(String searchTarget, String regex) { //일 시 반
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(searchTarget);

        boolean isExtracted = false;

        String result = "";
        String[] temp = new String[4];
        while (matcher.find()) {
            System.out.println("extract29");

            isNextDay = true;
            isExtracted = true;
            result = matcher.group(0);
            result = result.replaceAll("오전", "");
            result = result.replaceAll("오후", "");
            temp = result.replaceAll(" ", "").split("일|시");

            // 시간표현 초기화
            calMinute = curMonth;
            calDay = Integer.parseInt(temp[0]);
            calHour = Integer.parseInt(temp[1]);
            calMinute = 30;

            //시간바꿔주기 위한것
            regex = "오전|오후";
            extract101(searchTarget, regex);

            if (curMonth * 43200 + curDay * 1440 + curHour * 60 + curMinute >= calMonth * 43200 + calDay * 1440 + calHour * 60 + calMinute)
                atTime(curYear, calMonth + 1, Integer.parseInt(temp[0]), Integer.parseInt(temp[1]), 30);
            else
                atTime(curYear, calMonth, Integer.parseInt(temp[0]), Integer.parseInt(temp[1]), 30);
        }

        return isExtracted;
    }

    /**
     * 이 메소드는 음성에서 추출한 시간표현에 순서대로 "시간" "분" "후, 뒤, 또는 있다가"에 대한 정보가 있을 시, 시간 표현을 추출하여 설정 시간 값에 할당해준다.
     *
     * @param searchTarget 사용자로 부터 입력받은 음성에서 텍스트를 변환한 String 값
     * @param regex        정규식 표현
     * @return boolean 해당 정규식을 통해서 시간표현이 추출되었는지 여부
     */
    public boolean extract1(String searchTarget, String regex) { //~시간 ~분 후|뒤|있다가
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(searchTarget);

        boolean isExtracted = false;

        String result = "";
        String[] temp = new String[4];

        while (matcher.find()) {
            System.out.println("extract1");
            isExtracted = true;
            result = matcher.group(0);

            temp = result.replaceAll(" ", "").split("시간|분");

            addTime(0, Integer.parseInt(temp[0].replaceAll(" ", "")), Integer.parseInt(temp[1].replaceAll(" ", "")));

            if (calHour < 12)
                calA = "오전";
        }

        return isExtracted;
    }

    /**
     * 이 메소드는 음성에서 추출한 시간표현에 순서대로 "시간" "반" "후, 뒤, 또는 있다가"에 대한 정보가 있을 시, 시간 표현을 추출하여 설정 시간 값에 할당해준다.
     *
     * @param searchTarget 사용자로 부터 입력받은 음성에서 텍스트를 변환한 String 값
     * @param regex        정규식 표현
     * @return boolean 해당 정규식을 통해서 시간표현이 추출되었는지 여부
     */
    public boolean extract30(String searchTarget, String regex) { //~시간 ~반 후|뒤|있다가
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(searchTarget);
        boolean isExtracted = false;
        String result = "";
        String[] temp = new String[4];
        while (matcher.find()) {
            System.out.println("extract30");
            isExtracted = true;
            result = matcher.group(0);

            temp = result.replaceAll(" ", "").split("시간");
            addTime(0, Integer.parseInt(temp[0].replaceAll(" ", "")), 30);

            if (calHour < 12)
                calA = "오전";
        }

        return isExtracted;
    }

    /**
     * 이 메소드는 음성에서 추출한 시간표현에 순서대로 "시간" "후, 뒤, 또는 있다가"에 대한 정보가 있을 시, 시간 표현을 추출하여 설정 시간 값에 할당해준다.
     *
     * @param searchTarget 사용자로 부터 입력받은 음성에서 텍스트를 변환한 String 값
     * @param regex        정규식 표현
     * @return boolean 해당 정규식을 통해서 시간표현이 추출되었는지 여부
     */
    public boolean extract2(String searchTarget, String regex) { //~시간 후
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(searchTarget);
        boolean isExtracted = false;
        String result = "";
        String[] temp = new String[4];
        while (matcher.find()) {
            System.out.println("extract2");
            isExtracted = true;
            result = matcher.group(0);
            temp = result.split("시간");
            addTime(0, Integer.parseInt(temp[0].replaceAll(" ", "")), 0);

            if (calHour < 12)
                calA = "오전";
        }
        return isExtracted;
    }

    /**
     * 이 메소드는 음성에서 추출한 시간표현에 순서대로 "시" "분"에 대한 정보가 있을 시, 시간 표현을 추출하여 설정 시간 값에 할당해준다.
     *
     * @param searchTarget 사용자로 부터 입력받은 음성에서 텍스트를 변환한 String 값
     * @param regex        정규식 표현
     * @return boolean 해당 정규식을 통해서 시간표현이 추출되었는지 여부
     */
    public boolean extract3(String searchTarget, String regex) { //~시 ~분
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(searchTarget);
        boolean isExtracted = false;
        String result = "";
        String[] temp = new String[4];
        while (matcher.find()) {
            System.out.println("extract3");
            isExtracted = true;
            isTime = true;
            result = matcher.group(0);
            result = result.replaceAll("오전", "");
            result = result.replaceAll("오후", "");
            temp = result.split("시|분");

            atTime(curYear, curMonth, curDay, Integer.parseInt(temp[0].replaceAll(" ", "")), Integer.parseInt(temp[1].replaceAll(" ", "")));
        }
        return isExtracted;
    }

    /**
     * 이 메소드는 음성에서 추출한 시간표현에 순서대로 "시" "반"에 대한 정보가 있을 시, 시간 표현을 추출하여 설정 시간 값에 할당해준다.
     *
     * @param searchTarget 사용자로 부터 입력받은 음성에서 텍스트를 변환한 String 값
     * @param regex        정규식 표현
     * @return boolean 해당 정규식을 통해서 시간표현이 추출되었는지 여부
     */
    public boolean extract4(String searchTarget, String regex) { //~시 반
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(searchTarget);
        boolean isExtracted = false;
        String result = "";
        String[] temp = new String[4];
        while (matcher.find()) {
            System.out.println("extract4");
            isExtracted = true;
            isTime = true;
            isNextDay = false;
            result = matcher.group(0);
            result = result.replaceAll("오전", "");
            result = result.replaceAll("오후", "");
            temp = result.split("시");

            atTime(curYear, curMonth, curDay, Integer.parseInt(temp[0].replaceAll(" ", "")), 30);
            System.out.println("extract :result" + curYear + calA);
        }
        return isExtracted;
    }

    /**
     * 이 메소드는 음성에서 추출한 시간표현에 순서대로 "시"에 대한 정보가 있을 시, 시간 표현을 추출하여 설정 시간 값에 할당해준다.
     *
     * @param searchTarget 사용자로 부터 입력받은 음성에서 텍스트를 변환한 String 값
     * @param regex        정규식 표현
     * @return boolean 해당 정규식을 통해서 시간표현이 추출되었는지 여부
     */
    public boolean extract5(String searchTarget, String regex) { //~시
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(searchTarget);
        boolean isExtracted = false;
        String result = "";
        String[] temp = new String[4];
        while (matcher.find()) {
            System.out.println("extract5");
            isExtracted = true;
            isTime = true;
            result = matcher.group(0);
            result = result.replaceAll("오전", "");
            result = result.replaceAll("오후", "");
            temp = result.split("시");
            // atTime(curYear, curMonth, curDay, Integer.parseInt(temp[0].trim()), 0);
            atTime(curYear, curMonth, curDay, Integer.parseInt(temp[0].replaceAll(" ", "")), 0);
        }
        return isExtracted;
    }

    /**
     * 이 메소드는 음성에서 추출한 시간표현에 순서대로 "분" "후, 뒤, 또는 있다가"에 대한 정보가 있을 시, 시간 표현을 추출하여 설정 시간 값에 할당해준다.
     *
     * @param searchTarget 사용자로 부터 입력받은 음성에서 텍스트를 변환한 String 값
     * @param regex        정규식 표현
     * @return boolean 해당 정규식을 통해서 시간표현이 추출되었는지 여부
     */
    public boolean extract6(String searchTarget, String regex) { //~분 후|뒤|있다가
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(searchTarget);
        boolean isExtracted = false;
        String result = "";
        String[] temp = new String[4];
        while (matcher.find()) {
            System.out.println("extract6");
            isExtracted = true;
            isTime = true;
            result = matcher.group(0);
            temp = result.split("분");
            //addTime(0, 0, Integer.parseInt(temp[0].trim()));
            addTime(0, 0, Integer.parseInt(temp[0].replaceAll(" ", "")));
        }
        return isExtracted;
    }

    /**
     * 이 메소드는 음성에서 추출한 시간표현에 순서대로 "주" "후, 뒤, 또는 있다가"에 대한 정보가 있을 시, 시간 표현을 추출하여 설정 시간 값에 할당해준다.
     *
     * @param searchTarget 사용자로 부터 입력받은 음성에서 텍스트를 변환한 String 값
     * @param regex        정규식 표현
     * @return boolean 해당 정규식을 통해서 시간표현이 추출되었는지 여부
     */
    public boolean extract7(String searchTarget, String regex) { //~주 후
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(searchTarget);

        boolean isExtracted = false;
        String result = "";
        String[] temp = new String[4];

        while (matcher.find()) {
            System.out.println("extract7");
            isExtracted = true;
            result = matcher.group(0);
            temp = result.split("주");
            // addTime(7*Integer.parseInt(temp[0].trim()), 0, 0);
            addTime(7 * Integer.parseInt(temp[0].replaceAll(" ", "")), 0, 0);
        }
        return isExtracted;
    }

    /**
     * 이 메소드는 음성에서 추출한 시간표현에 순서대로 "월" "일"에 대한 정보가 있을 시, 시간 표현을 추출하여 설정 시간 값에 할당해준다.
     *
     * @param searchTarget 사용자로 부터 입력받은 음성에서 텍스트를 변환한 String 값
     * @param regex        정규식 표현
     * @return boolean 해당 정규식을 통해서 시간표현이 추출되었는지 여부
     */
    public boolean extract8(String searchTarget, String regex) { //~월 ~일
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(searchTarget);

        boolean isExtracted = false;
        String result = "";
        String[] temp = new String[4];

        while (matcher.find()) {
            System.out.println("extract8");
            isExtracted = true;
            isNextDay = true;
            result = matcher.group(0);
            temp = result.split("월|일");

            // 시간표현 초기화
            calMonth = Integer.parseInt(temp[0]);
            calDay = Integer.parseInt(temp[1]);
            calHour = 8;
            calMinute = 0;

            //시간바꿔주기 위한것
            regex = "오전|오후";
            extract101(searchTarget, regex);

            //atTime(curYear, Integer.parseInt(temp[0].trim()), Integer.parseInt(temp[1].trim()), curHour, curMinute);
            if (curMonth * 720 + curDay * 24 + curHour >= calMonth * 720 + calDay * 24 + calHour)
                calYear = curYear + 1; // 이전을 얘기한다면
        }
        return isExtracted;
    }

    /**
     * 이 메소드는 음성에서 추출한 시간표현에 순서대로 "월"에 대한 정보가 있을 시, 시간 표현을 추출하여 설정 시간 값에 할당해준다.
     *
     * @param searchTarget 사용자로 부터 입력받은 음성에서 텍스트를 변환한 String 값
     * @param regex        정규식 표현
     * @return boolean 해당 정규식을 통해서 시간표현이 추출되었는지 여부
     */
    public boolean extract9(String searchTarget, String regex) { //~월
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(searchTarget);
        boolean isExtracted = false;
        String result = "";
        String[] temp = new String[4];
        while (matcher.find()) {
            System.out.println("extract9");
            isExtracted = true;
            result = matcher.group(0);
            temp = result.split("월");
            // atTime(curYear, Integer.parseInt(temp[0].trim()), 1, 0, 0); //그 달의 1일 0시 0분
            atTime(curYear, Integer.parseInt(temp[0].replaceAll(" ", "")), 1, 0, 0); //그 달의 1일 0시 0분
            if (curMonth >= Integer.parseInt(temp[0].replaceAll(" ", "")))
                calYear = curYear + 1; // 이전을 얘기한다면

            calHour = 8;
            calMinute = 0; //working time의 초기 시간으로 설정
        }
        return isExtracted;
    }

    /**
     * 이 메소드는 음성에서 추출한 시간표현에 순서대로 "일"에 대한 정보가 있을 시, 시간 표현을 추출하여 설정 시간 값에 할당해준다.
     *
     * @param searchTarget 사용자로 부터 입력받은 음성에서 텍스트를 변환한 String 값
     * @param regex        정규식 표현
     * @return boolean 해당 정규식을 통해서 시간표현이 추출되었는지 여부
     */
    public boolean extract10(String searchTarget, String regex) { //~일
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(searchTarget);
        boolean isExtracted = false;
        String result = "";
        String[] temp = new String[4];
        while (matcher.find()) {
            System.out.println("extract10");
            isExtracted = true;
            result = matcher.group(0);
            temp = result.split("일");
            //atTime(curYear, curMonth, Integer.parseInt(temp[0].trim()), 0, 0); //그 달, 그 일의 0시 0분
            atTime(curYear, curMonth, Integer.parseInt(temp[0].replaceAll(" ", "")), 0, 0); //그 달, 그 일의 0시 0분
            if (curDay >= Integer.parseInt(temp[0].replaceAll(" ", "")))
                calMonth = curMonth + 1; // 이전을 얘기한다면

            calHour = 8;
            calMinute = 0; //working time의 초기 시간으로 설정
        }
        return isExtracted;
    }

    /**
     * 이 메소드는 음성에서 추출한 시간표현에 순서대로 "일" "후, 뒤, 또는 있다가"에 대한 정보가 있을 시, 시간 표현을 추출하여 설정 시간 값에 할당해준다.
     *
     * @param searchTarget 사용자로 부터 입력받은 음성에서 텍스트를 변환한 String 값
     * @param regex        정규식 표현
     * @return boolean 해당 정규식을 통해서 시간표현이 추출되었는지 여부
     */
    public boolean extract11(String searchTarget, String regex) { //~일 후(뒤)
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(searchTarget);
        boolean isExtracted = false;
        String result = "";
        String[] temp = new String[4];
        while (matcher.find()) {
            System.out.println("extract11");
            isExtracted = true;
            result = matcher.group(0);
            temp = result.split("일");
            //addTime(Integer.parseInt(temp[0].trim()), 0, 0);
            addTime(Integer.parseInt(temp[0].replaceAll(" ", "")), 0, 0);
        }
        return isExtracted;
    }

    public int extract103(String searchTarget, String regex) { //하루/이틀 뒤..
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(searchTarget);
        int maxi = 0;
        String temp[] = new String[2];
        while (matcher.find()) {
            isNextDay = true;
            System.out.println("extract103");
            String match = matcher.group(0);
            String result = match;
            //result 값을 바꿔주기
            result = result.replaceAll("하루", "1일");
            result = result.replaceAll("이틀", "2일");
            result = result.replaceAll("사흘", "3일");
            result = result.replaceAll("나흘", "4일");
            result = result.replaceAll("닷새|닷세", "5일");
            result = result.replaceAll("엿새|엿세", "6일");
            result = result.replaceAll("이레|이래", "7일");
            result = result.replaceAll("열흘", "10일");
            result = result.replaceAll("보름", "15일");
            temp = result.split("일");

            int plusDay = Integer.parseInt(temp[0].replaceAll(" ", ""));
            calDay = calDay + plusDay;
            int day_num = days[curMonth];
            calMonth += calDay == day_num ? 0 : calDay / day_num;
            calDay = calDay % day_num == 0 ? day_num : calDay % day_num;
            calYear += calMonth / 13;
            calMonth = calMonth % 12 == 0 ? 12 : calMonth % 12;

        }
        return 1;
    }

    /**
     * 이 메소드는 음성에서 추출한 시간표현에 순서대로 "오늘", "다음날", 다다음날", 내일", 낼", 명일", 모레", 글피", "익일"에 대한 정보가 있을 시, 시간 표현을 추출하여 설정 시간 값에 할당해준다.
     *
     * @param searchTarget 사용자로 부터 입력받은 음성에서 텍스트를 변환한 String 값
     * @param regex        정규식 표현
     * @return int 해당 정규식을 통해서 추출된 시간표현중 일수가 가장 최대치인 값
     */
    public int extract100(String searchTarget, String regex) { //[오늘|다음날|다다음날|다다음날|내일|낼|명일|모레|글피|익일|명일]+
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(searchTarget);
        int maxi = 0;
        while (matcher.find()) {
            System.out.println("extract100");
            String match = matcher.group(0);
            Integer num = 0;
            num = hMap.get(match);
            if (num == null) num = 0;

            if (maxi < num) maxi = num;
        }
        return maxi;
    }

    /**
     * 이 메소드는 음성에서 추출한 시간표현에 순서대로 "다음주" 또는 "다다음주"에 대한 정보가 있을 시, 시간 표현을 추출하여 설정 시간 값에 할당해준다.
     *
     * @param searchTarget 사용자로 부터 입력받은 음성에서 텍스트를 변환한 String 값
     * @param regex        정규식 표현
     * @return boolean 해당 정규식을 통해서 시간표현이 추출되었는지 여부
     */
    public boolean extract12(String searchTarget, String regex) { //다음주, 다다음주
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(searchTarget);
        boolean isExtracted = false;
        String result = "";
        String[] temp = new String[4];
        while (matcher.find()) {
            System.out.println("extract12");
            isExtracted = true;
            result = matcher.group(0).replaceAll(" ", "");
            addTime(hMap.get(result) * 7, 0, 0);
            calHour = 8;
            calMinute = 0; //working time의 초기 시간으로 설정
        }
        return isExtracted;
    }

    /**
     * 이 메소드는 음성에서 추출한 시간표현에 순서대로 "다음주" 또는 "다다음주" 그리고 "요일"에 대한 정보가 있을 시, 시간 표현을 추출하여 설정 시간 값에 할당해준다.
     *
     * @param searchTarget 사용자로 부터 입력받은 음성에서 텍스트를 변환한 String 값
     * @param regex        정규식 표현
     * @return boolean 해당 정규식을 통해서 시간표현이 추출되었는지 여부
     */
    public boolean extract13(String searchTarget, String regex) { //다음주, 다다음주 월,화~일요일
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(searchTarget);

        boolean isExtracted = false;
        String result = "";
        String week = "";
        String dayofweek = "";
        String[] temp = new String[4];

        while (matcher.find()) {
            System.out.println("extract13");
            isNextDay = true;
            isExtracted = true;
            result = matcher.group(0).replaceAll(" ", "");

            if (result.length() == 7) {
                week = result.substring(0, 4); //다다음주
                dayofweek = result.substring(4); //월~일요일
            } else if (result.length() == 6) {
                week = result.substring(0, 3); //다음주
                dayofweek = result.substring(3); //월~일요일
            }

            System.out.println("주: " + week + " 요일 : " + dayofweek + " 더해야할 일수 : ");
            int calweekday = hMap.get(week) * 7 + (wMap.get(dayofweek) - (wMap.get(curDayOfWeek)));
            calDay += calweekday;

            calHour = 8;
            calMinute = 0; //working time의 초기 시간으로 설정

            int day_num = days[curMonth];
            calMonth += calDay / day_num;
            calDay = calDay % day_num == 0 ? day_num : calDay % day_num;
            calYear += calMonth / 13;
            calMonth = calMonth % 12 == 0 ? 12 : calMonth % 12;
        }
        return isExtracted;
    }

    /**
     * 이 메소드는 음성에서 추출한 시간표현에 순서대로 "요일"에 대한 정보가 있을 시, 시간 표현을 추출하여 설정 시간 값에 할당해준다.
     *
     * @param searchTarget 사용자로 부터 입력받은 음성에서 텍스트를 변환한 String 값
     * @param regex        정규식 표현
     * @return boolean 해당 정규식을 통해서 시간표현이 추출되었는지 여부
     */
    public boolean extract14(String searchTarget, String regex) { //일요일,월요일. 혹시 현재 화요일인데 월요일이라고하면 다음주가됨
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(searchTarget);
        // System.out.println("요일을 하고 있다");
        boolean isExtracted = false;
        String result = "";
        String[] temp = new String[4];
        while (matcher.find()) {
            isNextDay = true;
            System.out.println("extract14");
            isExtracted = true;
            result = matcher.group(0).replaceAll(" ", "");
            String dayofweek = result;

            calHour = 8;
            calMinute = 0; //working time의 초기 시간으로 설정

            if (wMap.get(curDayOfWeek) < wMap.get(dayofweek)) {
                //int calweekday = (-1 * (wMap.get(curDayOfWeek) - 1) + (0 * 7 + wMap.get(dayofweek) - 1));
                int calweekday = 0 * 7 + (wMap.get(dayofweek) - (wMap.get(curDayOfWeek)));

                calDay += calweekday;

            } else if (wMap.get(curDayOfWeek) >= wMap.get(dayofweek)) {
                //int calweekday = (-1 * (wMap.get(curDayOfWeek) - 1) + (1 * 7 + wMap.get(dayofweek) - 1));
                int calweekday = 1 * 7 + (wMap.get(dayofweek) - (wMap.get(curDayOfWeek)));
                calDay += calweekday;
            }

            //TODO 요일에 관련된 함수들을 아래의 식을 적용시켜야한다. 추가 요망
            int day_num = days[curMonth];
            if (day_num != calDay) {
                calMonth += calDay / day_num;
            }
            calDay = calDay % day_num == 0 ? day_num : calDay % day_num;
            calYear += calMonth / 13;
            calMonth = calMonth % 12 == 0 ? 12 : calMonth % 12;

        }
        return isExtracted;
    }

    /**
     * 이 메소드는 음성에서 추출한 시간표현에 순서대로 "오전" ,"오후" 또는 오전 오후의 값이 명시되지 않는 값에 대해서, 추출한 시간 표현에  WorkTime 규칙을 적용하여 설정할 시간 값을 계산해준다.
     *
     * @param searchTarget 사용자로 부터 입력받은 음성에서 텍스트를 변환한 String 값
     * @param regex        정규식 표현
     * @return boolean 해당 정규식을 통해서 시간표현이 추출되었는지 여부
     */
    public boolean extract101(String searchTarget, String regex) { //오전, 오후
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(searchTarget);

        boolean isExtracted = false;
        String result = "";

        while (matcher.find()) { //같은 형식의 시간표현이 여러개인 경우 가장 마지막 시간 표현 사용
            System.out.println("extract101");
            isExtracted = true;
            result = matcher.group(0);
        }

        //if (result.equals("오후")) { //오후
        if (result.equals("오후") || calA.equals("오후")) {
            calA = "오후";
            if (calHour < 12)
                calHour += 12;
            if (calHour * 60 + calMinute < curHour * 60 + curMinute && !isNextDay) {
                calDay += 1;
                int day_num = days[calMonth];
                calMonth += calDay / day_num;
                calDay = calDay % day_num == 0 ? day_num : calDay % day_num;

                calYear += calMonth / 13;
                calMonth = calMonth % 12 == 0 ? 12 : calMonth % 12;
            }
        } else if (result.equals("오전") || calA.equals("오전")) { //오전
            System.out.println("extract result : " + result + "extract calA :  " + calA);

            calA = "오전";
            System.out.println("extract CALDAY & HOUR + MINUTE : " + calDay + ":" + calHour + ":" + calMinute);
            System.out.println("extract " + (calHour * 60 + calMinute) + " extract " + (curHour * 60 + curMinute) + " " + isNextDay);
            if (calHour * 60 + calMinute < curHour * 60 + curMinute && !isNextDay) {

                calDay += 1;
                System.out.println("extract day : " + calDay);
                int day_num = days[calMonth];

                System.out.println("extract daynum : " + day_num);
                calMonth += calDay / day_num;

                System.out.println("extract month : " + calMonth);
                calDay = calDay % day_num == 0 ? day_num : calDay % day_num;

                calYear += calMonth / 13;

                System.out.println("extract year : " + calYear);
                calMonth = calMonth % 12 == 0 ? 12 : calMonth % 12;


                System.out.println("extract Month : " + calMonth);
            }
        } else {
            /*
            * 이전에는 Working Time이라는 것을 두어 오전/오후가 입력되지 않았다면 아래와 같이 처리했으나
            * 이를 새로운 방식 (사용자에게 되묻는 방식)으로 변경하려고 하였기에 주석처리 했다.
            * 이를 다시 기존의 Working Time방식으로 남겨둘 수 있기 때문에 지우지 않았다.
            오전, 오후가 입력되지 않았다면, working time(8am - 8pm, 8 - 20) 범위내에서 처리
            System.out.println("extract200");
            if (calHour < 12) { //12이상이면 오후로 정해진 것. 1 ~ 11은 오전/오후 둘 다 가능
                calA = "오전";
                if (calHour < 8) { //0 ~ 7
                    calHour += 12;
                    calA = "오후";
                }
            }
            */
            isNote = true;

            if (calHour * 60 + calMinute < curHour * 60 + curMinute && !isNextDay && (calYear * 365 + calMonth * 30 + calDay) <= (curYear * 365 + curMonth * 30 + curDay)) {
                System.out.println("extract201 : " + calMonth + " " + calDay + " " + calHour + " " + calMinute);
                System.out.println("extract201");
                calDay += 1;
                int day_num = days[calMonth];
                calMonth += calDay == day_num ? 0 : calDay / day_num;
                calDay = calDay % day_num == 0 ? day_num : calDay % day_num;

                calYear += calMonth / 13;
                calMonth = calMonth % 12 == 0 ? 12 : calMonth % 12;
            }
        }

        return isExtracted;
    }

    /**
     * 이 메소드는 음성에서 추출한 시간표현에 순서대로 "다음주" 또는 "다다음주" 그리고 "요일", "시", "분"에 대한 정보가 있을 시, 시간 표현을 추출하여 설정 시간 값에 할당해준다.
     *
     * @param searchTarget 사용자로 부터 입력받은 음성에서 텍스트를 변환한 String 값
     * @param regex        정규식 표현
     * @return boolean 해당 정규식을 통해서 시간표현이 추출되었는지 여부
     */
    public boolean extract15(String searchTarget, String regex) { //다/다음주 ~요일 오전/오후 ~요일 ~시 ~분

        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(searchTarget);
        boolean isExtracted = false;

        String result = "";
        String week = "";
        String dayofweek = "";

        String[] temp = new String[4];

        while (matcher.find()) {
            isExtracted = true;
            isNextDay = true;
            System.out.println("extract15");

            result = matcher.group(0).replaceAll(" ", "");
            result = result.replaceAll("오전", "");
            result = result.replaceAll("오후", "");

            if (result.substring(0, 3).equals("다다음")) {
                week = result.substring(0, 4);
                dayofweek = result.substring(4, 7);
                result = result.substring(7);

                temp = result.split("시|분");
                atTime(curYear, curMonth, curDay, Integer.parseInt(temp[0].replaceAll(" ", "")), Integer.parseInt(temp[1].replaceAll(" ", "")));

                int calweekday = hMap.get(week) * 7 + (wMap.get(dayofweek) - (wMap.get(curDayOfWeek)));
                calDay += calweekday;
            } else if (result.substring(0, 3).equals("다음주")) {
                week = result.substring(0, 3);
                dayofweek = result.substring(3, 6);
                result = result.substring(6);

                temp = result.split("시|분");
                atTime(curYear, curMonth, curDay, Integer.parseInt(temp[0].replaceAll(" ", "")), Integer.parseInt(temp[1].replaceAll(" ", "")));

                int calweekday = hMap.get(week) * 7 + (wMap.get(dayofweek) - (wMap.get(curDayOfWeek)));
                calDay += calweekday;
            }

            int day_num = days[curMonth];
            calMonth += calDay / day_num;
            calDay = calDay % day_num == 0 ? day_num : calDay % day_num;
            calYear += calMonth / 13;
            calMonth = calMonth % 12 == 0 ? 12 : calMonth % 12;

        }
        return isExtracted;
    }

    /**
     * 이 메소드는 음성에서 추출한 시간표현에 순서대로 "다음주" 또는 "다다음주" 그리고 "요일", "시", "반"에 대한 정보가 있을 시, 시간 표현을 추출하여 설정 시간 값에 할당해준다.
     *
     * @param searchTarget 사용자로 부터 입력받은 음성에서 텍스트를 변환한 String 값
     * @param regex        정규식 표현
     * @return boolean 해당 정규식을 통해서 시간표현이 추출되었는지 여부
     */
    public boolean extract16(String searchTarget, String regex) { //다/다음주 ~요일 ~시 반
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(searchTarget);

        boolean isExtracted = false;

        String result = "";
        String week = "";
        String dayofweek = "";
        String[] temp = new String[4];

        while (matcher.find()) {
            isExtracted = true;
            isNextDay = true;
            System.out.println("In extract16");

            result = matcher.group(0).replaceAll(" ", "");
            result = result.replaceAll("오전", "");
            result = result.replaceAll("오후", "");

            if (result.substring(0, 3).equals("다다음")) {
                week = result.substring(0, 4);
                dayofweek = result.substring(4, 7);
                result = result.substring(7);

                temp = result.split("시");
                atTime(curYear, curMonth, curDay, Integer.parseInt(temp[0].replaceAll(" ", "")), 30);

                int calweekday = hMap.get(week) * 7 + (wMap.get(dayofweek) - (wMap.get(curDayOfWeek)));
                calDay += calweekday;
            } else if (result.substring(0, 3).equals("다음주")) {
                week = result.substring(0, 3);
                dayofweek = result.substring(3, 6);
                result = result.substring(6);

                temp = result.split("시");
                atTime(curYear, curMonth, curDay, Integer.parseInt(temp[0].replaceAll(" ", "")), 30);

                int calweekday = hMap.get(week) * 7 + (wMap.get(dayofweek) - (wMap.get(curDayOfWeek)));
                calDay += calweekday;
            }

            int day_num = days[curMonth];
            calMonth += calDay / day_num;
            calDay = calDay % day_num == 0 ? day_num : calDay % day_num;
            calYear += calMonth / 13;
            calMonth = calMonth % 12 == 0 ? 12 : calMonth % 12;
        }
        return isExtracted;
    }

    /**
     * 이 메소드는 음성에서 추출한 시간표현에 순서대로 "다음주" 또는 "다다음주" 그리고 "요일", "시"에 대한 정보가 있을 시, 시간 표현을 추출하여 설정 시간 값에 할당해준다.
     *
     * @param searchTarget 사용자로 부터 입력받은 음성에서 텍스트를 변환한 String 값
     * @param regex        정규식 표현
     * @return boolean 해당 정규식을 통해서 시간표현이 추출되었는지 여부
     */
    public boolean extract17(String searchTarget, String regex) { //다/다음주 ~요일 ~시

        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(searchTarget);

        boolean isExtracted = false;

        String result = "";
        String week = "";
        String dayofweek = "";
        String[] temp = new String[4];

        while (matcher.find()) {
            System.out.println("In extract17");
            isNextDay = true;
            isExtracted = true;
            result = matcher.group(0).replaceAll(" ", "");
            result = result.replaceAll("오전", "");
            result = result.replaceAll("오후", "");

            if (result.substring(0, 3).equals("다다음")) {
                week = result.substring(0, 4);
                dayofweek = result.substring(4, 7);
                result = result.substring(7);

                temp = result.split("시");
                atTime(curYear, curMonth, curDay, Integer.parseInt(temp[0].replaceAll(" ", "")), 0);

                int calweekday = hMap.get(week) * 7 + (wMap.get(dayofweek) - (wMap.get(curDayOfWeek)));
                calDay += calweekday;
            } else if (result.substring(0, 3).equals("다음주")) {
                week = result.substring(0, 3);
                dayofweek = result.substring(3, 6);
                result = result.substring(6);

                temp = result.split("시");
                atTime(curYear, curMonth, curDay, Integer.parseInt(temp[0].replaceAll(" ", "")), 0);

                int calweekday = hMap.get(week) * 7 + (wMap.get(dayofweek) - (wMap.get(curDayOfWeek)));
                calDay += calweekday;
            }

            int day_num = days[curMonth];
            calMonth += calDay / day_num;
            calDay = calDay % day_num == 0 ? day_num : calDay % day_num;
            calYear += calMonth / 13;
            calMonth = calMonth % 12 == 0 ? 12 : calMonth % 12;
        }
        return isExtracted;
    }

    /**
     * 이 메소드는 음성에서 추출한 시간표현에 순서대로 "요일", "시", "분"에 대한 정보가 있을 시, 시간 표현을 추출하여 설정 시간 값에 할당해준다.
     *
     * @param searchTarget 사용자로 부터 입력받은 음성에서 텍스트를 변환한 String 값
     * @param regex        정규식 표현
     * @return boolean 해당 정규식을 통해서 시간표현이 추출되었는지 여부
     */
    public boolean extract18(String searchTarget, String regex) { //~요일 ~시 ~분

        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(searchTarget);
        boolean isExtracted = false;

        String result = "";
        String week = "";
        String dayofweek = "";
        String[] temp = new String[4];

        while (matcher.find()) {
            System.out.println("In extract18");
            isExtracted = true;

            result = matcher.group(0).replaceAll(" ", "");
            result = result.replaceAll("오전", "");
            result = result.replaceAll("오후", "");

            dayofweek = result.substring(0, 3);
            result = result.substring(3);
            temp = result.split("시|분");

            atTime(curYear, curMonth, curDay, Integer.parseInt(temp[0].replaceAll(" ", "")), Integer.parseInt(temp[1].replaceAll(" ", "")));

            //시간바꿔주기 위한것
            regex = "오전|오후";
            extract101(searchTarget, regex);

            if (wMap.get(curDayOfWeek) <= wMap.get(dayofweek)) {
                int calweekday = 0 * 7 + (wMap.get(dayofweek) - (wMap.get(curDayOfWeek)));
                calDay += calweekday;

            } else if (wMap.get(curDayOfWeek) >= wMap.get(dayofweek)) {
                int calweekday = 1 * 7 + (wMap.get(dayofweek) - (wMap.get(curDayOfWeek)));
                calDay += calweekday;
            }

            int day_num = days[curMonth];
            calMonth += calDay / day_num;
            calDay = calDay % day_num == 0 ? day_num : calDay % day_num;
            calYear += calMonth / 13;
            calMonth = calMonth % 12 == 0 ? 12 : calMonth % 12;
        }
        return isExtracted;
    }

    /**
     * 이 메소드는 음성에서 추출한 시간표현에 순서대로 "요일", "시", "반"에 대한 정보가 있을 시, 시간 표현을 추출하여 설정 시간 값에 할당해준다.
     *
     * @param searchTarget 사용자로 부터 입력받은 음성에서 텍스트를 변환한 String 값
     * @param regex        정규식 표현
     * @return boolean 해당 정규식을 통해서 시간표현이 추출되었는지 여부
     */
    public boolean extract19(String searchTarget, String regex) { //~요일 ~시 ~반

        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(searchTarget);

        boolean isExtracted = false;

        String result = "";
        String week = "";
        String dayofweek = "";
        String[] temp = new String[4];

        while (matcher.find()) {

            isNextDay = true;
            System.out.println("extract19");

            isExtracted = true;
            result = matcher.group(0).replaceAll(" ", "");
            result = result.replaceAll("오전", "");
            result = result.replaceAll("오후", "");

            dayofweek = result.substring(0, 3);
            result = result.substring(3);
            temp = result.split("시");

            atTime(curYear, curMonth, curDay, Integer.parseInt(temp[0].replaceAll(" ", "")), 30);

            //TODO 오류 수정 바람
            if (wMap.get(curDayOfWeek) <= wMap.get(dayofweek)) {
                int calweekday = 0 * 7 + (wMap.get(dayofweek) - (wMap.get(curDayOfWeek)));
                calDay += calweekday;
            } else if (wMap.get(curDayOfWeek) >= wMap.get(dayofweek)) {
                int calweekday = 1 * 7 + (wMap.get(dayofweek) - (wMap.get(curDayOfWeek)));
                calDay += calweekday;
            }


            int day_num = days[curMonth];
            calMonth += calDay / day_num;
            calDay = calDay % day_num == 0 ? day_num : calDay % day_num;
            calYear += calMonth / 13;
            calMonth = calMonth % 12 == 0 ? 12 : calMonth % 12;
        }
        return isExtracted;
    }

    /**
     * 이 메소드는 음성에서 추출한 시간표현에 순서대로 "요일", "시"에 대한 정보가 있을 시, 시간 표현을 추출하여 설정 시간 값에 할당해준다.
     *
     * @param searchTarget 사용자로 부터 입력받은 음성에서 텍스트를 변환한 String 값
     * @param regex        정규식 표현
     * @return boolean 해당 정규식을 통해서 시간표현이 추출되었는지 여부
     */
    public boolean extract20(String searchTarget, String regex) { //~요일 ~시

        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(searchTarget);

        boolean isExtracted = false;

        String result = "";
        String week = "";
        String dayofweek = "";
        String[] temp = new String[4];

        while (matcher.find()) {
            System.out.println("extract20");

            isNextDay = true;
            isExtracted = true;

            result = matcher.group(0).replaceAll(" ", "");
            result = result.replaceAll("오전", "");
            result = result.replaceAll("오후", "");

            dayofweek = result.substring(0, 3);
            result = result.substring(3);
            temp = result.split("시");

            atTime(curYear, curMonth, curDay, Integer.parseInt(temp[0].replaceAll(" ", "")), 0);

            //시간바꿔주기 위한것
            regex = "오전|오후";
            extract101(searchTarget, regex);


            System.out.println("extract wmap : " + wMap.get(curDayOfWeek) + " " + wMap.get(dayofweek) + " " + curHour + " " + calHour);

            if (wMap.get(curDayOfWeek) <= wMap.get(dayofweek)) {
                int calweekday = 0 * 7 + (wMap.get(dayofweek) - (wMap.get(curDayOfWeek)));
                calDay += calweekday;
            } else if (wMap.get(curDayOfWeek) >= wMap.get(dayofweek)) {
                int calweekday = 1 * 7 + (wMap.get(dayofweek) - (wMap.get(curDayOfWeek)));
                calDay += calweekday;
            }

            int day_num = days[curMonth];
            calMonth += calDay / day_num;
            calDay = calDay % day_num == 0 ? day_num : calDay % day_num;
            calYear += calMonth / 13;
            calMonth = calMonth % 12 == 0 ? 12 : calMonth % 12;
        }
        return isExtracted;
    }

    /**
     * 이 메소드는 음성에서 추출한 시간표현에 순서대로  "시", "분", "요일"에 대한 정보가 있을 시, 시간 표현을 추출하여 설정 시간 값에 할당해준다.
     *
     * @param searchTarget 사용자로 부터 입력받은 음성에서 텍스트를 변환한 String 값
     * @param regex        정규식 표현
     * @return boolean 해당 정규식을 통해서 시간표현이 추출되었는지 여부
     */
    public boolean extract21(String searchTarget, String regex) { // ~시 ~분 ~요일

        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(searchTarget);

        boolean isExtracted = false;

        String result = "";
        String week = "";
        String dayofweek = "";
        String[] temp = new String[4];

        while (matcher.find()) {
            System.out.println("extract21");
            isNextDay = true;
            isExtracted = true;

            result = matcher.group(0).replaceAll(" ", "");
            result = result.replaceAll("오전", "");
            result = result.replaceAll("오후", "");

            dayofweek = result.substring(result.length() - 3);
            result = result.substring(0, result.length() - 3);

            temp = result.split("시|분");
            atTime(curYear, curMonth, curDay, Integer.parseInt(temp[0].replaceAll(" ", "")), Integer.parseInt(temp[1].replaceAll(" ", "")));

            if (wMap.get(curDayOfWeek) <= wMap.get(dayofweek)) {
                int calweekday = 0 * 7 + (wMap.get(dayofweek) - (wMap.get(curDayOfWeek)));
                calDay += calweekday;

            } else if (wMap.get(curDayOfWeek) >= wMap.get(dayofweek)) {
                int calweekday = 1 * 7 + (wMap.get(dayofweek) - (wMap.get(curDayOfWeek)));
                calDay += calweekday;
            }

            int day_num = days[curMonth];
            calMonth += calDay / day_num;
            calDay = calDay % day_num == 0 ? day_num : calDay % day_num;
            calYear += calMonth / 13;
            calMonth = calMonth % 12 == 0 ? 12 : calMonth % 12;
        }
        return isExtracted;
    }

    /**
     * 이 메소드는 음성에서 추출한 시간표현에 순서대로  "시", "반", "요일"에 대한 정보가 있을 시, 시간 표현을 추출하여 설정 시간 값에 할당해준다.
     *
     * @param searchTarget 사용자로 부터 입력받은 음성에서 텍스트를 변환한 String 값
     * @param regex        정규식 표현
     * @return boolean 해당 정규식을 통해서 시간표현이 추출되었는지 여부
     */
    public boolean extract22(String searchTarget, String regex) { // ~시 ~반 ~요일

        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(searchTarget);

        boolean isExtracted = false;

        String result = "";
        String week = "";
        String dayofweek = "";
        String[] temp = new String[4];

        while (matcher.find()) {
            System.out.println("extract22");

            isNextDay = true;
            isExtracted = true;

            result = matcher.group(0).replaceAll(" ", "");
            result = result.replaceAll("오전", "");
            result = result.replaceAll("오후", "");

            dayofweek = result.substring(result.length() - 3);
            result = result.substring(0, result.length() - 3);
            temp = result.split("시");

            atTime(curYear, curMonth, curDay, Integer.parseInt(temp[0].replaceAll(" ", "")), 30);

            if (wMap.get(curDayOfWeek) <= wMap.get(dayofweek)) {
                int calweekday = 0 * 7 + (wMap.get(dayofweek) - (wMap.get(curDayOfWeek)));
                calDay += calweekday;

            } else if (wMap.get(curDayOfWeek) >= wMap.get(dayofweek)) {
                int calweekday = 1 * 7 + (wMap.get(dayofweek) - (wMap.get(curDayOfWeek)));
                calDay += calweekday;
            }

            int day_num = days[curMonth];
            calMonth += calDay / day_num;
            calDay = calDay % day_num == 0 ? day_num : calDay % day_num;
            calYear += calMonth / 13;
            calMonth = calMonth % 12 == 0 ? 12 : calMonth % 12;
        }
        return isExtracted;
    }

    /**
     * 이 메소드는 음성에서 추출한 시간표현에 순서대로  "시","요일"에 대한 정보가 있을 시, 시간 표현을 추출하여 설정 시간 값에 할당해준다.
     *
     * @param searchTarget 사용자로 부터 입력받은 음성에서 텍스트를 변환한 String 값
     * @param regex        정규식 표현
     * @return boolean 해당 정규식을 통해서 시간표현이 추출되었는지 여부
     */
    public boolean extract23(String searchTarget, String regex) { // ~시 ~요일

        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(searchTarget);

        boolean isExtracted = false;
        String result = "";
        String week = "";
        String dayofweek = "";
        String[] temp = new String[4];

        while (matcher.find()) {
            System.out.println("extract23");

            isNextDay = true;
            isExtracted = true;

            result = matcher.group(0).replaceAll(" ", "");
            result = result.replaceAll("오전", "");
            result = result.replaceAll("오후", "");

            dayofweek = result.substring(result.length() - 3);
            result = result.substring(0, result.length() - 3);
            temp = result.split("시");
            atTime(curYear, curMonth, curDay, Integer.parseInt(temp[0].replaceAll(" ", "")), 0);

            if (wMap.get(curDayOfWeek) <= wMap.get(dayofweek)) {
                int calweekday = 0 * 7 + (wMap.get(dayofweek) - (wMap.get(curDayOfWeek)));
                calDay += calweekday;

            } else if (wMap.get(curDayOfWeek) >= wMap.get(dayofweek)) {
                int calweekday = 1 * 7 + (wMap.get(dayofweek) - (wMap.get(curDayOfWeek)));
                calDay += calweekday;
            }

            int day_num = days[curMonth];
            calMonth += calDay / day_num;
            calDay = calDay % day_num == 0 ? day_num : calDay % day_num;
            calYear += calMonth / 13;
            calMonth = calMonth % 12 == 0 ? 12 : calMonth % 12;
        }
        return isExtracted;
    }

    /**
     * 이 메소드는 음성에서 추출한 시간표현에 "오늘"에 대한 정보가 있을 시, isNextDay 설정값을 변경해준다.
     * isNextDay 변수는 시간 계산에 있어서, 하루가 넘어가는 것을 막아주는 역할을 한다.
     *
     * @param searchTarget 사용자로 부터 입력받은 음성에서 텍스트를 변환한 String 값
     * @param regex        정규식 표현
     * @return boolean 해당 정규식을 통해서 시간표현이 추출되었는지 여부
     */
    public boolean extract102(String searchTarget, String regex) { //오늘

        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(searchTarget);

        while (matcher.find()) {
            System.out.println("extract102");
            isNextDay = true;
        }
        return true;
    }

    /**
     * 이 메소드는 현재 시간을 기준으로 시간 표현 값이 변화해야하는 일 경우 아래의 메소드 시간표현의 연산을 적용시킨다.
     * "후","있다가","지나고"를 예시로 들 수 있다.
     *
     * @param d 일
     * @param h 시
     * @param m 분
     */
    public void addTime(int d, int h, int m) {

        calMinute = curMinute + m;
        calHour = curHour + h;
        calDay = curDay + d;
        calMonth = curMonth;
        calYear = curYear;

        calHour += calMinute / 60;
        calMinute = calMinute % 60;
        calDay += calHour / 24;
        calHour = calHour % 24;

        int day_num = days[curMonth];
        calMonth += calDay == day_num ? 0 : calDay / day_num;
        calDay = calDay % day_num == 0 ? day_num : calDay % day_num;
        calYear += calMonth / 13;
        calMonth = calMonth % 12 == 0 ? 12 : calMonth % 12;
        if (calHour >= 12) calA = "오후";

        isNextDay = true; // 위에서 하루를 추가해주기 때문에 isNexDay로 하루를 넘겼다는 표시를 해줘야한다.
    }

    /**
     * 이 메소드는 현재 시간에 영향을 받지 않으며, 추출한 시간표현이 정해져있을시 이 메소드의 시간표현의 연산을 적용시킨다.
     * 몇월 몇일 몇시 몇분을 예시로 들 수 있다.
     *
     * @param y 년
     * @param M 월
     * @param d 일
     * @param h 시
     * @param m 분
     */
    public void atTime(int y, int M, int d, int h, int m) {

        calYear = y;
        calMonth = M;
        calDay = d;
        calHour = h;
        calMinute = m;

        if (calHour >= 12) calA = "오후";
    }
}