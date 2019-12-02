package com.example.coolweather.service;

import com.example.coolweather.util.HttpCallbackListener;
import com.example.coolweather.util.Httputil;
import com.example.coolweather.util.Utillity;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.util.Log;

public class AutoupdateService extends Service{

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		
		return null;
	}
	@Override
	public int onStartCommand(Intent intent,int flags,int startId){
		new Thread(new Runnable(){

			@Override
			public void run() {
				// TODO Auto-generated method stub
				updateWeather();
				
			}
			
		}).start();
		AlarmManager manager =(AlarmManager)getSystemService(ALARM_SERVICE);
		int anHour =8*60*60*1000;
		long triggerAtTime =SystemClock.elapsedRealtime()+anHour;
		Intent i =new Intent(this,AutoupdateService.class);
		PendingIntent pi =PendingIntent.getBroadcast(this, 0, i, 0);
		manager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAtTime, pi);
		return super.onStartCommand(intent, flags, startId);
		
	}
	private void updateWeather(){
		 SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		String weatherCode =prefs.getString("weather-code", "");
		String address ="http://www.weather.com.cn/data/list3/cityinfo/"+weatherCode+".html";
		Httputil.sendHttpRequest(address, new HttpCallbackListener(){
			@Override
			public void onFinish(String response){
				Log.d("Tag", response);
				Utillity.handleWeatherResponse(AutoupdateService.this, response);
				
			}
			@Override
			public void onError(Exception e){
				e.printStackTrace();
			}
		});
		
		
	}

}
