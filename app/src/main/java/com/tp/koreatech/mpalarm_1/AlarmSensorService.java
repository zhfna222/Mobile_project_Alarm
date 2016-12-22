package com.tp.koreatech.mpalarm_1;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import com.tp.koreatech.mpalarm.R;

import java.util.Calendar;

/**
 * Created by SANGGYUN on 2016-12-21.
 */

public class AlarmSensorService extends Service implements SensorEventListener {
    Alarm alarm = null;
    private int count=0;

    private long lastTime = System.currentTimeMillis();
    private long totalTime=0;
    private float speed;

    float[] gravity_data = new float[3];
    float[] accel_data = new float[3];
    float[] last_accel_data = new float[3];
    final float alpha = (float)0.8;

    private SensorManager mSensorManager;
    private Sensor mAccelerometer;

    private static final int SHAKE_THRESHOLD = 150;

    @Override
    public IBinder onBind(Intent intent) {

        return null;
    }
    @Override
    public void onCreate() {
        super.onCreate();
        mSensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        Intent intent = new Intent(this, AlarmActivity.class);
        PendingIntent pIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        Notification noti = new Notification.Builder(this)
                         .setContentTitle("Alarm service")
                         .setContentText("Service is running... start an activity")
                         .setSmallIcon(R.mipmap.ic_launcher)
                         .setContentIntent(pIntent)
                         .build();

        startForeground(123, noti);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mSensorManager.unregisterListener(this);
    }
    //알람 설정
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Bundle bundle = intent.getExtras();
        alarm = (Alarm) bundle.getSerializable("alarm");

        mSensorManager.registerListener(this,mAccelerometer,SensorManager.SENSOR_DELAY_NORMAL);

        return START_NOT_STICKY;
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if(sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER){
            long currentTime = System.currentTimeMillis();
            long gabOfTime = (currentTime - lastTime);

            if (gabOfTime > 500) {
                totalTime += gabOfTime;
                lastTime = currentTime;
                if(totalTime > alarm.getDelayTime()*60*1000){
                    Calendar c = alarm.getAlarmTime();
                    c.add(Calendar.MINUTE, alarm.getDelayTime()+1);
                    alarm.setAlarmTime(c);
                    Context context = getApplicationContext();
                    Intent mathAlarmServiceIntent = new Intent(context, AlarmAlertBroadcastReciever.class);
                    mathAlarmServiceIntent.putExtra("alarm", alarm);
                    context.sendBroadcast(mathAlarmServiceIntent, null);
                    stopSelf();
                }
                gravity_data[0] = alpha * gravity_data[0] + (1 - alpha) * sensorEvent.values[0]; //먼저 중력데이터를 계산함
                gravity_data[1] = alpha * gravity_data[1] + (1 - alpha) * sensorEvent.values[1];
                gravity_data[2] = alpha * gravity_data[2] + (1 - alpha) * sensorEvent.values[2];
                accel_data[0] = sensorEvent.values[0] - gravity_data[0]; // 순수 가속도센서값에 중력값을 빼줌
                accel_data[1] = sensorEvent.values[1] - gravity_data[1]; // 아니면 약 9.81 어쩌고 하는값이 더해짐
                accel_data[2] = sensorEvent.values[2] - gravity_data[2];

                speed = Math.abs(accel_data[0] + accel_data[0] + accel_data[0] - last_accel_data[0] - last_accel_data[1] - last_accel_data[2]) / gabOfTime * 10000;

                Log.i("aaaa", "Sensor " + speed + ", " + count + ", " + totalTime + ", " + alarm.getDelayTime()*60*1000);
                if (speed > SHAKE_THRESHOLD) {
                    count++;
                }
                if(alarm.getAccCount()<=count){
                    StaticWakeLock.lockOff(this);
                    stopSelf();
                }

                last_accel_data[0] = accel_data[0];
                last_accel_data[1] = accel_data[1];
                last_accel_data[2] = accel_data[2];
            }

        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
}
