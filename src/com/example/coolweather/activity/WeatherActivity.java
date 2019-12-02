package com.example.coolweather.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;



import com.example.coolweather.service.AutoupdateService;
//import com.example.test.service.AutoUpdateService;
import com.example.coolweather.util.HttpCallbackListener;
import com.example.coolweather.util.Httputil;
import com.example.coolweather.util.Utillity;

public class WeatherActivity extends Activity implements OnClickListener{

	private LinearLayout weatherInfoLayout;
	/**
	 * 鐢ㄤ簬鏄剧ず鍩庡競鍚�
	 */
	private TextView cityNameText;
	/**
	 * 鐢ㄤ簬鏄剧ず鍙戝竷鏃堕棿
	 */
	private TextView publishText;
	/**
	 * 鐢ㄤ簬鏄剧ず澶╂皵鎻忚堪淇℃伅
	 */
	private TextView weatherDespText;
	/**
	 * 鐢ㄤ簬鏄剧ず姘旀俯1
	 */
	private TextView temp1Text;
	/**
	 * 鐢ㄤ簬鏄剧ず姘旀俯2
	 */
	private TextView temp2Text;
	/**
	 * 鐢ㄤ簬鏄剧ず褰撳墠鏃ユ湡
	 */
	private TextView currentDateText;
	/**
	 * 杩斿洖鍩庡競閫夋嫨鐣岄潰
	 */
	private Button switchCity;
	/**
	 * 鎵嬪姩鍒锋柊澶╂皵淇℃伅
	 */
	private Button refreshWeather;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.weather_layout);
		// 鍒濆鍖栧悇鎺т欢
		weatherInfoLayout = (LinearLayout) findViewById(R.id.weather_info_layout);
		cityNameText = (TextView) findViewById(R.id.city_name);
		publishText = (TextView) findViewById(R.id.publish_text);
		weatherDespText = (TextView) findViewById(R.id.weather_desp);
		temp1Text = (TextView) findViewById(R.id.temp1);
		temp2Text = (TextView) findViewById(R.id.temp2);
		currentDateText = (TextView) findViewById(R.id.current_date);
		switchCity = (Button) findViewById(R.id.switch_city);
		refreshWeather = (Button) findViewById(R.id.refresh_weather);
		String countyCode = getIntent().getStringExtra("county_code");
		if (!TextUtils.isEmpty(countyCode)) {
	
			publishText.setText("正在同步中");
			weatherInfoLayout.setVisibility(View.INVISIBLE);
			cityNameText.setVisibility(View.INVISIBLE);
			queryWeatherCode(countyCode);
		} else {
			
			showWeather();
		}
		switchCity.setOnClickListener(this);
		refreshWeather.setOnClickListener(this);
		//鐎圭偘绶ラ崠鏍х畭閸涘﹥娼�
	   // AdView adView = new AdView(this, AdSize.FIT_SCREEN);
	    //閼惧嘲褰囩憰浣哥サ閸忋儱绠嶉崨濠冩蒋閻ㄥ嫬绔风仦锟�
	   // LinearLayout adLayout=(LinearLayout)findViewById(R.id.adLayout);
	    //鐏忓棗绠嶉崨濠冩蒋閸旂姴鍙嗛崚鏉跨鐏烇拷娑擄拷
	   // adLayout.addView(adView);
	}
	
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.switch_city:
			Intent intent = new Intent(this, ChooseAreaActivity.class);
			intent.putExtra("from_weather_activity", true);
			startActivity(intent);
			finish();
			break;
		case R.id.refresh_weather:
			publishText.setText("同步中");
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
			String weatherCode = prefs.getString("weather_code", "");
			if (!TextUtils.isEmpty(weatherCode)) {
				queryWeatherInfo(weatherCode);
			}
			break;
		default:
			break;
		}
	}
	
	/**
	 * 鏌ヨ鍘跨骇浠ｇ爜鎵�瀵瑰簲鐨勫ぉ姘斾唬鍙�
	 */
	private void queryWeatherCode(String countyCode) {
		String address = "http://www.weather.com.cn/data/list3/city" + countyCode + ".xml";
		queryFromServer(address, "countyCode");
	}

	/**
	 * 鏌ヨ澶╂皵浠ｅ彿鎵�瀵瑰簲鐨勫ぉ姘�
	 */
	private void queryWeatherInfo(String weatherCode) {
		String address = "http://www.weather.com.cn/data/cityinfo/" + weatherCode + ".html";
		queryFromServer(address, "weatherCode");
	}
	
	/**
	 * 鏍规嵁浼犲叆鐨勫湴鍧�鍜岀被鍨嬪幓鍚戞湇鍔″櫒鏌ヨ澶╂皵浠ｅ彿鎴栬�呭ぉ姘斾俊鎭�
	 */
	private void queryFromServer(final String address, final String type) {
		Httputil.sendHttpRequest(address, new HttpCallbackListener() {
			@Override
			public void onFinish(final String response) {
				if ("countyCode".equals(type)) {
					if (!TextUtils.isEmpty(response)) {
						// 浠庢湇鍔″櫒杩斿洖鐨勬暟鎹腑瑙ｆ瀽鍑哄ぉ姘斾唬鍙�
						String[] array = response.split("\\|");
						if (array != null && array.length == 2) {
							String weatherCode = array[1];
							queryWeatherInfo(weatherCode);
						}
					}
				} else if ("weatherCode".equals(type)) {
					// 澶勭悊鏈嶅姟鍣ㄨ繑鍥炵殑澶╂皵淇℃伅
					Utillity.handleWeatherResponse(WeatherActivity.this, response);
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							showWeather();
						}
					});
				}
			}
			
			@Override
			public void onError(Exception e) {
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						publishText.setText("同步失败...(。・_・)/~~~");
					}
				});
			}
		});
	}
	
	/**
	 *浠嶴haredPreferences鏂囦欢涓鍙栧瓨鍌ㄧ殑澶╂皵淇℃伅锛屽苟鏄剧ず鍒扮晫闈笂
	 */
	private void showWeather() {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		cityNameText.setText( prefs.getString("city_name", ""));
		temp1Text.setText(prefs.getString("temp1", ""));
		temp2Text.setText(prefs.getString("temp2", ""));
		weatherDespText.setText(prefs.getString("weather_desp", ""));
		publishText.setText("今天" + prefs.getString("publish_time", "") + "发布");
		currentDateText.setText(prefs.getString("current_date", ""));
		weatherInfoLayout.setVisibility(View.VISIBLE);
		cityNameText.setVisibility(View.VISIBLE);
		Intent intent = new Intent(this, AutoupdateService.class);
		startService(intent);
	}

}