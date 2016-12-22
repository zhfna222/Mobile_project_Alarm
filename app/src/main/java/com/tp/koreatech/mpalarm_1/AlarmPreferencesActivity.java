
package com.tp.koreatech.mpalarm_1;

import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.app.TimePickerDialog.OnTimeSetListener;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnMultiChoiceClickListener;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Vibrator;
import android.support.v7.app.ActionBar;
import android.text.InputType;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.CheckedTextView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TimePicker;

import com.tp.koreatech.mpalarm.R;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class AlarmPreferencesActivity extends BaseActivity {

    ImageButton deleteButton;
    TextView okButton;
    TextView cancelButton;
    private Alarm alarm;
    private MediaPlayer mediaPlayer;

    private ListAdapter listAdapter;
    private ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // requestWindowFeature(Window.FEATURE_NO_TITLE);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        setContentView(R.layout.alarm_preferences);

        //번들 key hash 자료 구조중요!!!!
        //인텐트 한 값을 받는다.
        Bundle bundle = getIntent().getExtras();

        //알람 아이템 이 올라왔을시
        if (bundle != null && bundle.containsKey("alarm")) {
            //alarm; 에 리스트 뷰 항목에 맞게 변한 자료 집어넣음 중요!!!
            setMathAlarm((Alarm) bundle.getSerializable("alarm"));
        } else {
            setMathAlarm(new Alarm());
        }

        //어댑터 리스너 등록 !@중요
        if (bundle != null && bundle.containsKey("adapter")) {
            setListAdapter((AlarmPreferenceListAdapter) bundle.getSerializable("adapter"));
        } else {
            setListAdapter(new AlarmPreferenceListAdapter(this, getMathAlarm()));
        }

        //리스트 뷰 항목 클릭시 제일 중요~!~!!!~!!~!~!~!!
        getListView().setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> l, View v, int position, long id) {
                //프러퍼런스 리스트 뷰 어댑터 설정 @상수 변환 하지 않을시 오류
                final AlarmPreferenceListAdapter alarmPreferenceListAdapter = (AlarmPreferenceListAdapter) getListAdapter();
                final AlarmPreference alarmPreference = (AlarmPreference) alarmPreferenceListAdapter.getItem(position);

                AlertDialog.Builder alert;
                v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
                switch (alarmPreference.getType()) {
                    //체크 리스트
                    case BOOLEAN:
                        CheckedTextView checkedTextView = (CheckedTextView) v;
                        boolean checked = !checkedTextView.isChecked();
                        ((CheckedTextView) v).setChecked(checked);
                        switch (alarmPreference.getKey()) {
                            case ALARM_ACTIVE:
                                alarm.setAlarmActive(checked);
                                break;
                            case ALARM_VIBRATE:
                                alarm.setVibrate(checked);
                                if (checked) {
                                    Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
                                    vibrator.vibrate(1000);
                                }
                                break;
                            case ALARM_ACCELEROMETER:
                                alarm.setAccelerometer(checked);
                                break;
                        }
                        alarmPreference.setValue(checked);
                        break;
                    //메모
                    case STRING:

                        alert = new AlertDialog.Builder(AlarmPreferencesActivity.this);

                        alert.setTitle(alarmPreference.getTitle());
                        // alert.setMessage(message);

                        // Set an EditText view to get user input
                        final EditText input = new EditText(AlarmPreferencesActivity.this);

                        input.setText(alarmPreference.getValue().toString());

                        alert.setView(input);
                        alert.setPositiveButton("Ok", new OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {

                                alarmPreference.setValue(input.getText().toString());

                                if (alarmPreference.getKey() == AlarmPreference.Key.ALARM_NAME) {
                                    alarm.setAlarmName(alarmPreference.getValue().toString());
                                }

                                alarmPreferenceListAdapter.setMathAlarm(getMathAlarm());
                                alarmPreferenceListAdapter.notifyDataSetChanged();
                            }
                        });
                        alert.show();
                        break;
                    //셋 알람 톤 디피커리
                    case LIST:
                        alert = new AlertDialog.Builder(AlarmPreferencesActivity.this);

                        alert.setTitle(alarmPreference.getTitle());
                        // alert.setMessage(message);

                        CharSequence[] items = new CharSequence[alarmPreference.getOptions().length];
                        for (int i = 0; i < items.length; i++)
                            items[i] = alarmPreference.getOptions()[i];

                        alert.setItems(items, new OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                switch (alarmPreference.getKey()) {
                                /*
							case ALARM_DIFFICULTY:
								Alarm.Difficulty d = Alarm.Difficulty.values()[which];
								alarm.setDifficulty(d);
								break;*/
                                    case ALARM_TONE:
                                        alarm.setAlarmTonePath(alarmPreferenceListAdapter.getAlarmTonePaths()[which]);
                                        if (alarm.getAlarmTonePath() != null) {
                                            if (mediaPlayer == null) {
                                                mediaPlayer = new MediaPlayer();
                                            } else {
                                                if (mediaPlayer.isPlaying())
                                                    mediaPlayer.stop();
                                                mediaPlayer.reset();
                                            }
                                            try {
                                                // mediaPlayer.setVolume(1.0f, 1.0f);
                                                mediaPlayer.setVolume(0.2f, 0.2f);
                                                mediaPlayer.setDataSource(AlarmPreferencesActivity.this, Uri.parse(alarm.getAlarmTonePath()));
                                                mediaPlayer.setAudioStreamType(AudioManager.STREAM_ALARM);
                                                mediaPlayer.setLooping(false);
                                                mediaPlayer.prepare();
                                                mediaPlayer.start();

                                                // Force the mediaPlayer to stop after 3
                                                // seconds...
                                                if (alarmToneTimer != null)
                                                    alarmToneTimer.cancel();
                                                alarmToneTimer = new CountDownTimer(3000, 3000) {
                                                    @Override
                                                    public void onTick(long millisUntilFinished) {

                                                    }

                                                    @Override
                                                    public void onFinish() {
                                                        try {
                                                            if (mediaPlayer.isPlaying())
                                                                mediaPlayer.stop();
                                                        } catch (Exception e) {

                                                        }
                                                    }
                                                };
                                                alarmToneTimer.start();
                                            } catch (Exception e) {
                                                try {
                                                    if (mediaPlayer.isPlaying())
                                                        mediaPlayer.stop();
                                                } catch (Exception e2) {

                                                }
                                            }
                                        }
                                        break;
                                    default:
                                        break;
                                }
                                alarmPreferenceListAdapter.setMathAlarm(getMathAlarm());
                                alarmPreferenceListAdapter.notifyDataSetChanged();
                            }

                        });

                        alert.show();
                        break;
                    //요일 선택
                    case MULTIPLE_LIST:
                        alert = new AlertDialog.Builder(AlarmPreferencesActivity.this);

                        alert.setTitle(alarmPreference.getTitle());
                        // alert.setMessage(message);

                        CharSequence[] multiListItems = new CharSequence[alarmPreference.getOptions().length];
                        for (int i = 0; i < multiListItems.length; i++)
                            multiListItems[i] = alarmPreference.getOptions()[i];

                        boolean[] checkedItems = new boolean[multiListItems.length];
                        for (Alarm.Day day : getMathAlarm().getDays()) {
                            checkedItems[day.ordinal()] = true;
                        }
                        alert.setMultiChoiceItems(multiListItems, checkedItems, new OnMultiChoiceClickListener() {

                            @Override
                            public void onClick(final DialogInterface dialog, int which, boolean isChecked) {

                                Alarm.Day thisDay = Alarm.Day.values()[which];

                                if (isChecked) {
                                    alarm.addDay(thisDay);
                                } else {
                                    // Only remove the day if there are more than 1
                                    // selected
                                    if (alarm.getDays().length > 1) {
                                        alarm.removeDay(thisDay);
                                    } else {
                                        // If the last day was unchecked, re-check
                                        // it
                                        ((AlertDialog) dialog).getListView().setItemChecked(which, true);
                                    }
                                }

                            }
                        });
                        alert.setOnCancelListener(new OnCancelListener() {
                            @Override
                            public void onCancel(DialogInterface dialog) {
                                alarmPreferenceListAdapter.setMathAlarm(getMathAlarm());
                                alarmPreferenceListAdapter.notifyDataSetChanged();

                            }
                        });
                        alert.show();
                        break;
                    //시간 선택
                    case TIME:
                        switch (alarmPreference.getKey()) {
                            case ALARM_TIME:
                                TimePickerDialog timePickerDialog = new TimePickerDialog(AlarmPreferencesActivity.this, new OnTimeSetListener() {

                                    @Override
                                    public void onTimeSet(TimePicker timePicker, int hours, int minutes) {
                                        Calendar newAlarmTime = Calendar.getInstance();
                                        newAlarmTime.set(Calendar.HOUR_OF_DAY, hours);
                                        newAlarmTime.set(Calendar.MINUTE, minutes);
                                        newAlarmTime.set(Calendar.SECOND, 0);
                                        alarm.setAlarmTime(newAlarmTime);
                                        alarmPreferenceListAdapter.setMathAlarm(getMathAlarm());
                                        alarmPreferenceListAdapter.notifyDataSetChanged();
                                    }
                                }, alarm.getAlarmTime().get(Calendar.HOUR_OF_DAY), alarm.getAlarmTime().get(Calendar.MINUTE), true);
                                timePickerDialog.setTitle(alarmPreference.getTitle());
                                timePickerDialog.show();
                                break;
                        }
                    case INTEGER:
                        switch (alarmPreference.getKey()) {
                            case ALARM_ACCCOUNT:

                                alert = new AlertDialog.Builder(AlarmPreferencesActivity.this);

                                alert.setTitle(alarmPreference.getTitle());
                                // alert.setMessage(message);

                                // Set an EditText view to get user input
                                final EditText input1 = new EditText(AlarmPreferencesActivity.this);
                                input1.setInputType(InputType.TYPE_CLASS_NUMBER);

                                input1.setText(alarmPreference.getValue().toString());

                                alert.setView(input1);
                                alert.setPositiveButton("Ok", new OnClickListener() {
                                    public void onClick(DialogInterface dialog, int whichButton) {

                                        alarmPreference.setValue(input1.getText().toString());

                                        if (alarmPreference.getKey() == AlarmPreference.Key.ALARM_ACCCOUNT) {
                                            String temp = alarmPreference.getValue().toString();
                                            if (Integer.parseInt(temp) < 0)
                                                temp = "0";
                                            alarm.setAccCount(Integer.parseInt(temp));
                                        }

                                        alarmPreferenceListAdapter.setMathAlarm(getMathAlarm());
                                        alarmPreferenceListAdapter.notifyDataSetChanged();
                                    }
                                });
                                alert.show();
                                break;
                            case ALARM_DELAYTIME:

                                alert = new AlertDialog.Builder(AlarmPreferencesActivity.this);

                                alert.setTitle(alarmPreference.getTitle());
                                // alert.setMessage(message);

                                // Set an EditText view to get user input
                                final EditText input2 = new EditText(AlarmPreferencesActivity.this);
                                input2.setInputType(InputType.TYPE_CLASS_NUMBER);

                                input2.setText(alarmPreference.getValue().toString());

                                alert.setView(input2);
                                alert.setPositiveButton("Ok", new OnClickListener() {
                                    public void onClick(DialogInterface dialog, int whichButton) {

                                        alarmPreference.setValue(input2.getText().toString());

                                        if (alarmPreference.getKey() == AlarmPreference.Key.ALARM_DELAYTIME) {
                                            String temp = alarmPreference.getValue().toString();
                                            alarm.setDelayTime(Integer.parseInt(temp));
                                        }

                                        alarmPreferenceListAdapter.setMathAlarm(getMathAlarm());
                                        alarmPreferenceListAdapter.notifyDataSetChanged();
                                    }
                                });
                                alert.show();
                                break;
                        }
                        break;
                    default:
                        break;
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        boolean result = super.onCreateOptionsMenu(menu);
        menu.findItem(R.id.menu_item_new).setVisible(false);
        return result;
    }

    //저장 삭제 버튼 엑션바
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_save:
                Database.init(getApplicationContext());
                if (getMathAlarm().getId() < 1) {
                    // 디비 생성
                    Database.create(getMathAlarm());
                } else {
                    //디비 갱신
                    Database.update(getMathAlarm());
                }
                //알람 인텐드 불러오기
                callMathAlarmScheduleService();
                //Toast.makeText(AlarmPreferencesActivity.this, getMathAlarm().getTimeUntilNextAlarmMessage(), Toast.LENGTH_LONG).show();
                finish();
                break;
            //삭제
            case R.id.menu_item_delete:
                AlertDialog.Builder dialog = new AlertDialog.Builder(AlarmPreferencesActivity.this);
                dialog.setTitle("삭제");
                dialog.setMessage("삭제하시겠습니까?");
                dialog.setPositiveButton("Ok", new OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        Database.init(getApplicationContext());
                        if (getMathAlarm().getId() < 1) {
                            // Alarm not saved
                        } else {
                            //디비 삭제
                            Database.deleteEntry(alarm);
                            callMathAlarmScheduleService();
                        }
                        finish();
                    }
                });
                dialog.setNegativeButton("Cancel", new OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                dialog.show();

                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private CountDownTimer alarmToneTimer;

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putSerializable("alarm", getMathAlarm());
        outState.putSerializable("adapter", (AlarmPreferenceListAdapter) getListAdapter());
    }

    ;

    @Override
    protected void onPause() {
        super.onPause();
        try {
            if (mediaPlayer != null)
                mediaPlayer.release();
        } catch (Exception e) {
        }
        // setListAdapter(null);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    public Alarm getMathAlarm() {
        return alarm;
    }

    public void setMathAlarm(Alarm alarm) {
        this.alarm = alarm;
    }

    public ListAdapter getListAdapter() {
        return listAdapter;
    }

    public void setListAdapter(ListAdapter listAdapter) {
        this.listAdapter = listAdapter;
        getListView().setAdapter(listAdapter);

    }

    public ListView getListView() {
        if (listView == null)
            listView = (ListView) findViewById(android.R.id.list);
        return listView;
    }

    public void setListView(ListView listView) {
        this.listView = listView;
    }

    @Override
    public void onClick(View v) {
        // super.onClick(v);

    }


    public class AlarmPreferenceListAdapter extends BaseAdapter implements Serializable {

        private Context context;
        private Alarm alarm;
        private List<AlarmPreference> preferences = new ArrayList<AlarmPreference>();
        private final String[] repeatDays = {"일요일", "월요일", "화요일", "수요일", "목요일", "금요일", "토요일"};
        //private final String[] alarmDifficulties = {"Easy","Medium","Hard"};

        private String[] alarmTones;
        private String[] alarmTonePaths;


        public AlarmPreferenceListAdapter(Context context, Alarm alarm) {
            setContext(context);
//		(new Runnable(){
//
//			@Override
//			public void run() {
            //Log.d("AlarmPreferenceListAdapter", "Loading Ringtones...");

            //알람 톤 가져오기
            RingtoneManager ringtoneMgr = new RingtoneManager(getContext());
            ringtoneMgr.setType(RingtoneManager.TYPE_ALARM);
            Cursor alarmsCursor = ringtoneMgr.getCursor();

            alarmTones = new String[alarmsCursor.getCount() + 1];
            alarmTones[0] = "무음";
            alarmTonePaths = new String[alarmsCursor.getCount() + 1];
            alarmTonePaths[0] = "";

            if (alarmsCursor.moveToFirst()) {
                do {
                    alarmTones[alarmsCursor.getPosition() + 1] = ringtoneMgr.getRingtone(alarmsCursor.getPosition()).getTitle(getContext());
                    alarmTonePaths[alarmsCursor.getPosition() + 1] = ringtoneMgr.getRingtoneUri(alarmsCursor.getPosition()).toString();
                } while (alarmsCursor.moveToNext());
            }
            alarmsCursor.close();
//
//			}
//
//		}).run();
//
            //뷰 항목 등록
            setMathAlarm(alarm);
        }

        @Override
        public int getCount() {
            return preferences.size();
        }

        @Override
        public Object getItem(int position) {
            return preferences.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            //알람 프리퍼런스 아이템 항목 생성
            AlarmPreference alarmPreference = (AlarmPreference) getItem(position);
            //레이아웃읽어오는 변수 선언
            LayoutInflater layoutInflater = LayoutInflater.from(getContext());
            switch (alarmPreference.getType()) {
                case BOOLEAN:
                    if (null == convertView || convertView.getId() != android.R.layout.simple_list_item_checked)
                        convertView = layoutInflater.inflate(android.R.layout.simple_list_item_checked, null);

                    CheckedTextView checkedTextView = (CheckedTextView) convertView.findViewById(android.R.id.text1);
                    checkedTextView.setText(alarmPreference.getTitle());
                    checkedTextView.setChecked((Boolean) alarmPreference.getValue());
                    break;

                case INTEGER:
                case STRING:
                case LIST:
                case MULTIPLE_LIST:
                case TIME:
                default:
                    if (null == convertView || convertView.getId() != android.R.layout.simple_list_item_2)
                        convertView = layoutInflater.inflate(android.R.layout.simple_list_item_2, null);

                    TextView text1 = (TextView) convertView.findViewById(android.R.id.text1);
                    text1.setTextSize(18);
                    text1.setText(alarmPreference.getTitle());

                    TextView text2 = (TextView) convertView.findViewById(android.R.id.text2);
                    text2.setText(alarmPreference.getSummary());
                    break;
            }

            return convertView;
        }

        public Alarm getMathAlarm() {
            for (AlarmPreference preference : preferences) {
                switch (preference.getKey()) {
                    case ALARM_ACTIVE:
                        alarm.setAlarmActive((Boolean) preference.getValue());
                        break;
                    case ALARM_NAME:
                        alarm.setAlarmName((String) preference.getValue());
                        break;
                    case ALARM_TIME:
                        alarm.setAlarmTime((String) preference.getValue());
                        break;
                /*
				case ALARM_DIFFICULTY:
					alarm.setDifficulty(Alarm.Difficulty.valueOf((String)preference.getValue()));
					break;*/
                    case ALARM_TONE:
                        alarm.setAlarmTonePath((String) preference.getValue());
                        break;
                    case ALARM_VIBRATE:
                        alarm.setVibrate((Boolean) preference.getValue());
                        break;
                    case ALARM_REPEAT:
                        alarm.setDays((Alarm.Day[]) preference.getValue());
                        break;
                    case ALARM_ACCELEROMETER:
                        alarm.setAccelerometer((Boolean) preference.getValue());
                        break;
                    case ALARM_ACCCOUNT:
                        alarm.setAccCount((Integer) preference.getValue());
                        break;
                    case ALARM_DELAYTIME:
                        alarm.setDelayTime((Integer) preference.getValue());
                        break;
                }
            }

            return alarm;
        }

        //피르퍼런스리스트 뷰 항목 등록
        public void setMathAlarm(Alarm alarm) {
            this.alarm = alarm;
            preferences.clear();
            preferences.add(new AlarmPreference(AlarmPreference.Key.ALARM_ACTIVE, "활성화", null, null, alarm.getAlarmActive(), AlarmPreference.Type.BOOLEAN));
            preferences.add(new AlarmPreference(AlarmPreference.Key.ALARM_NAME, "제목", alarm.getAlarmName(), null, alarm.getAlarmName(), AlarmPreference.Type.STRING));
            preferences.add(new AlarmPreference(AlarmPreference.Key.ALARM_TIME, "시간 설정", alarm.getAlarmTimeString(), null, alarm.getAlarmTime(), AlarmPreference.Type.TIME));
            preferences.add(new AlarmPreference(AlarmPreference.Key.ALARM_REPEAT, "요일 설정", alarm.getRepeatDaysString(), repeatDays, alarm.getDays(), AlarmPreference.Type.MULTIPLE_LIST));
            //preferences.add(new AlarmPreference(AlarmPreference.Key.ALARM_DIFFICULTY,"Difficulty", alarm.getDifficulty().toString(), alarmDifficulties, alarm.getDifficulty(), Type.LIST));

            //알람음 나게 함
            Uri alarmToneUri = Uri.parse(alarm.getAlarmTonePath());
            Ringtone alarmTone = RingtoneManager.getRingtone(getContext(), alarmToneUri);

            //확인 요망 오류경고 1
            if (alarmTone instanceof Ringtone && !alarm.getAlarmTonePath().equalsIgnoreCase("")) {
                preferences.add(new AlarmPreference(AlarmPreference.Key.ALARM_TONE, "알람음", alarmTone.getTitle(getContext()), alarmTones, alarm.getAlarmTonePath(), AlarmPreference.Type.LIST));
            } else {
                preferences.add(new AlarmPreference(AlarmPreference.Key.ALARM_TONE, "알람음", getAlarmTones()[0], alarmTones, null, AlarmPreference.Type.LIST));
            }

            preferences.add(new AlarmPreference(AlarmPreference.Key.ALARM_VIBRATE, "진동 설정", null, null, alarm.getVibrate(), AlarmPreference.Type.BOOLEAN));
            preferences.add(new AlarmPreference(AlarmPreference.Key.ALARM_ACCELEROMETER, "지연 알람", null, null, alarm.getAccelerometer(), AlarmPreference.Type.BOOLEAN));
            preferences.add(new AlarmPreference(AlarmPreference.Key.ALARM_ACCCOUNT, "횟수", "" + alarm.getAccCount(), null, alarm.getAccCount(), AlarmPreference.Type.INTEGER));
            preferences.add(new AlarmPreference(AlarmPreference.Key.ALARM_DELAYTIME, "제한 시간(분)", "" + alarm.getDelayTime(), null, alarm.getDelayTime(), AlarmPreference.Type.INTEGER));

        }


        public Context getContext() {
            return context;
        }

        public void setContext(Context context) {
            this.context = context;
        }

        public String[] getRepeatDays() {
            return repeatDays;
        }
/*
	public String[] getAlarmDifficulties() {
		return alarmDifficulties;
	}*/

        public String[] getAlarmTones() {
            return alarmTones;
        }

        public String[] getAlarmTonePaths() {
            return alarmTonePaths;
        }

    }

	/*
	*
	*
	* */


}
