/* Copyright 2014 Sheldon Neilson www.neilson.co.za
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */
package com.tp.koreatech.mpalarm_1;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;


public class AlarmAlertBroadcastReciever extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		Intent mathAlarmServiceIntent = new Intent(
				context,
				AlarmServiceBroadcastReciever.class);
		context.sendBroadcast(mathAlarmServiceIntent, null);
		
		StaticWakeLock.lockOn(context);

		Bundle bundle = intent.getExtras();
		bundle.size();
		final Alarm alarm = (Alarm) bundle.getSerializable("alarm");
		Intent alarmAlertActivityIntent;

		alarmAlertActivityIntent = new Intent(context, AlarmAlertActivity.class);

		alarmAlertActivityIntent.putExtra("alarm", alarm);
		//새로운 창에 실행 `
		alarmAlertActivityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

		//시작
		context.startActivity(alarmAlertActivityIntent);
	}

}
