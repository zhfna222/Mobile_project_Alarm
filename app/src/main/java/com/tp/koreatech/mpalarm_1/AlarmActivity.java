package com.tp.koreatech.mpalarm_1;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.HapticFeedbackConstants;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.Toast;

import com.tp.koreatech.mpalarm.R;

import java.util.List;

public class AlarmActivity extends BaseActivity{
    ListView mathAlarmListView; //리스트 뷰
    AlarmListAdapter alarmListAdapter; //알람 리스트 어탭터

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.alarm_activity);

        mathAlarmListView = (ListView) findViewById(android.R.id.list);

        //롱클릭 설정
        mathAlarmListView.setLongClickable(true);

        //롱클릭시 삭제 설정
        mathAlarmListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position, long id) {
                view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
                final Alarm alarm = (Alarm) alarmListAdapter.getItem(position);
                AlertDialog.Builder dialog = new AlertDialog.Builder(AlarmActivity.this);
                dialog.setTitle("삭제 확인");
                dialog.setMessage("알람을 삭제 하시겠습니까?");
                dialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        //데이터 베이스 삭제 중요!!!!~!~!~!~!~!~!
                        Database.init(AlarmActivity.this);
                        Database.deleteEntry(alarm);
                        //알람 서비스 호출 알람 상태 변화 를 체크 한다
                        AlarmActivity.this.callMathAlarmScheduleService();

                        //리스트 목록 재설정
                        updateAlarmList();
                    }
                });
                //취소 버튼 따로 설정 중요~!
                dialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

                dialog.show();

                return true;
            }
        });
        //알람 서비스 인텐드 설정
        callMathAlarmScheduleService();

        //어댑터 선언및 리스트 뷰 설정
        alarmListAdapter = new AlarmListAdapter(this);
        this.mathAlarmListView.setAdapter(alarmListAdapter);

        //리스트뷰 클릭시 설정
        mathAlarmListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View v, int position, long id) {
                v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
                //알람 아이템 값 가져 오기
                Alarm alarm = (Alarm) alarmListAdapter.getItem(position);

                //인텐트 후 인텐트 로 아이템 올림
                Intent intent = new Intent(AlarmActivity.this, AlarmPreferencesActivity.class);
                intent.putExtra("alarm", alarm);
                startActivity(intent);
            }

        });
    }

    @Override
    protected void onPause() {
        // setListAdapter(null);
        Database.deactivate();
        super.onPause();
    }

    //화면 재생성
    @Override
    protected void onResume() {
        super.onResume();
        updateAlarmList();
    }

    //리스트뷰 재생성
    public void updateAlarmList(){
        Database.init(AlarmActivity.this);
        //알람 리스트 alarms 에 db의 내용 모두 가졍오기
        final List<Alarm> alarms = Database.getAll();


        alarmListAdapter.setMathAlarms(alarms);

        runOnUiThread(new Runnable() {
            public void run() {
                // reload content
                //리스트부 리로드 할것
                AlarmActivity.this.alarmListAdapter.notifyDataSetChanged();
                if(alarms.size() > 0){
                    findViewById(android.R.id.empty).setVisibility(View.INVISIBLE);
                }else{
                    findViewById(android.R.id.empty).setVisibility(View.VISIBLE);
                }
            }
        });
    }

    //체크 박스 클릭시
    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.checkBox_alarm_active) {

            CheckBox checkBox = (CheckBox) v;
            //아이템 가져오기
            Alarm alarm = (Alarm) alarmListAdapter.getItem((Integer) checkBox.getTag());
            //체크박스 값 설정
            alarm.setAlarmActive(checkBox.isChecked());
            //데이터 베이스 갱신
            Database.update(alarm);
            AlarmActivity.this.callMathAlarmScheduleService();
            if (checkBox.isChecked()) {
                Toast.makeText(AlarmActivity.this, alarm.getTimeUntilNextAlarmMessage(), Toast.LENGTH_LONG).show();
            }
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        boolean result = super.onCreateOptionsMenu(menu);
        menu.findItem(R.id.menu_item_save).setVisible(false);
        menu.findItem(R.id.menu_item_delete).setVisible(false);
        return result;
    }

}
