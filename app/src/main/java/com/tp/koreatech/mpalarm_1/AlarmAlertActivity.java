package com.tp.koreatech.mpalarm_1;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Vibrator;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.tp.koreatech.mpalarm.R;

public class AlarmAlertActivity extends Activity implements OnClickListener {

	private Alarm alarm;
	private MediaPlayer mediaPlayer;

	private StringBuilder answerBuilder = new StringBuilder();

	private Vibrator vibrator;

	private boolean alarmActive; //활동 취소버튼 잠금

	private TextView answerView;
	private Button extBtn;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//창화면 생성 추가 플래그
		final Window window = getWindow();
		window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
				| WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
				| WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);

		setContentView(R.layout.show_layout);


		Bundle bundle = this.getIntent().getExtras();
		alarm = (Alarm) bundle.getSerializable("alarm");

		//이름 설정
		this.setTitle(alarm.getAlarmName());

		answerView = (TextView) findViewById(R.id.textViewAlarmedTime);
		answerView.setText(alarm.getAlarmName() + "\n" + alarm.getAlarmTimeString()
				+ "\n" + alarm.getRepeatDaysString()+"\n"+alarm.getAlarmTonePath() +"\n"+
				alarm.getAccelerometer() +"\n"+alarm.getAccCount()+"\n"+alarm.getDelayTime());

		// Toast.makeText(this, answerString, Toast.LENGTH_LONG).show();


		TelephonyManager telephonyManager = (TelephonyManager) this
				.getSystemService(Context.TELEPHONY_SERVICE);

		PhoneStateListener phoneStateListener = new PhoneStateListener() {
			@Override
			public void onCallStateChanged(int state, String incomingNumber) {
				switch (state) {
					case TelephonyManager.CALL_STATE_RINGING:
						Log.d(getClass().getSimpleName(), "Incoming call: "
								+ incomingNumber);
						try {
							mediaPlayer.pause();
						} catch (IllegalStateException e) {

						}
						break;
					case TelephonyManager.CALL_STATE_IDLE:
						Log.d(getClass().getSimpleName(), "Call State Idle");
						try {
							mediaPlayer.start();
						} catch (IllegalStateException e) {

						}
						break;
				}
				super.onCallStateChanged(state, incomingNumber);
			}
		};

		telephonyManager.listen(phoneStateListener,
				PhoneStateListener.LISTEN_CALL_STATE);

		startAlarm();

	}

	@Override
	protected void onResume() {
		super.onResume();
		//alarmActive = true;
	}

	private void startAlarm() {

		if (alarm.getAlarmTonePath() != "") {
			mediaPlayer = new MediaPlayer();
			//진동 설정
			if (alarm.getVibrate()) {
				vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
				long[] pattern = { 1000, 200, 200, 200 };
				vibrator.vibrate(pattern, 0);
			}
			try {
				//알람음 설정
				mediaPlayer.setVolume(1.0f, 1.0f);
				mediaPlayer.setDataSource(this,
						Uri.parse(alarm.getAlarmTonePath()));
				mediaPlayer.setAudioStreamType(AudioManager.STREAM_ALARM);
				mediaPlayer.setLooping(true);
				mediaPlayer.prepare();
				mediaPlayer.start();

			} catch (Exception e) {
				mediaPlayer.release();
				//alarmActive = false;
			}
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onBackPressed()
	 */
	@Override
	public void onBackPressed() {

		//if (!alarmActive)
			super.onBackPressed();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onPause()
	 */
	@Override
	protected void onPause() {
		super.onPause();
		StaticWakeLock.lockOff(this);
	}

	@Override
	protected void onDestroy() {
		try {
			if (vibrator != null)
				vibrator.cancel();
		} catch (Exception e) {

		}
		try {
			mediaPlayer.stop();
		} catch (Exception e) {

		}
		try {
			mediaPlayer.release();
		} catch (Exception e) {

		}

		if(alarm.getAccelerometer()) {
			Intent intent = new Intent(this, AlarmSensorService.class);
			intent.putExtra("alarm", alarm);
			startService(intent);
		}

		super.onDestroy();
	}

	@Override
	public void onClick(View v) {

	}
}
