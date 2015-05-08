package com.kkweather.app.activity;

import com.kkweather.app.R;
import com.kkweather.app.R.id;
import com.kkweather.app.util.HttpCallbackListener;
import com.kkweather.app.util.HttpUtil;
import com.kkweather.app.util.Utility;

import android.R.array;
import android.app.Activity;
import android.app.SearchManager.OnCancelListener;
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

public class WeatherActivty extends Activity implements OnClickListener{

	private LinearLayout weatherInfoLayout;
	private TextView cityNameText;
	private TextView publishText;
	private TextView weatherDespText;
	private TextView temp1Text;
	private TextView temp2Text;
	private TextView currentDateText;
	private Button switchCtity;
	private Button refresh;
	
	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.weather_layout);
		weatherInfoLayout = (LinearLayout) findViewById(R.id.weather_info_layout);
		temp1Text = (TextView) findViewById(R.id.temp1);
		temp2Text = (TextView) findViewById(R.id.temp2);
		cityNameText = (TextView) findViewById(R.id.city_name);
		publishText = (TextView) findViewById(R.id.publish_text);
		weatherDespText = (TextView) findViewById(R.id.weather_desp);
		currentDateText = (TextView) findViewById(R.id.current_data);
		String countyCode = getIntent().getStringExtra("county_code");
		switchCtity = (Button) findViewById(R.id.switch_city);
		switchCtity.setOnClickListener(this);
		refresh = (Button) findViewById(R.id.refresh_weather);
		refresh.setOnClickListener(this);
		
		if (!TextUtils.isEmpty(countyCode)) {
			//有县级代号就去查询天气
			publishText.setText("同步中咯。。。");
			weatherInfoLayout.setVisibility(View.INVISIBLE);
			cityNameText.setVisibility(View.INVISIBLE);
			qureyWeatherCode(countyCode);
		} else {
			showWeather();
		}
		
	}

	/*
	查询县级代号所对应的天气代码
	*/
	private void qureyWeatherCode(String countyCode) {
		// TODO Auto-generated method stub
		String address = "http://www.weather.com.cn/data/list3/city"+countyCode+".xml";
		qureyFromServer(address,"countyCode");
	}
	
	
	private void queryWeatherInfo(String weatherCode) {
		// TODO Auto-generated method stub
		String address = "http://www.weather.com.cn/data/cityinfo/"+weatherCode+".html";
		qureyFromServer(address, "weatherCode");
	}
	
	/*
	根据传入的地址和类型去服务器查询天气代号或天气信息
	*/
	private void qureyFromServer(final String address, final String type) {
		// TODO Auto-generated method stub
		System.out.println("----->>"+address);
		HttpUtil.sendHttpRequest(address, new HttpCallbackListener() {
			
			@Override
			public void onFinish(final String response) {
				// TODO Auto-generated method stub
				if ("countyCode".equals(type)) {
					if (!TextUtils.isEmpty(response)) {
						String[] array = response.split("\\|");
						if (array != null && array.length == 2) {
							String weatherCode = array[1];
							queryWeatherInfo(weatherCode);
						}
					}
				} else if ("weatherCode".equals(type)){
					//处理服务器返回的天气信息
					Utility.handleWeatherResponse(WeatherActivty.this, response);
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
				// TODO Auto-generated method stub
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						publishText.setText("同步失败喔。。。");
					}
				});
			}
		});
	}

	
	/*
	从ShardPreference文件中读取存储的天气信息，并显示到界面上
	*/
	private void showWeather() {
		// TODO Auto-generated method stub
		SharedPreferences wc = PreferenceManager.getDefaultSharedPreferences(this);
		cityNameText.setText(wc.getString("city_name", ""));
		temp1Text.setText(wc.getString("temp1", ""));
		temp2Text.setText(wc.getString("temp2", ""));
		weatherDespText.setText(wc.getString("weather_desp", ""));
		publishText.setText("今天"+wc.getString("publish_time", "")+"发布");
		currentDateText.setText(wc.getString("current_date", ""));
		weatherInfoLayout.setVisibility(View.VISIBLE);
		cityNameText.setVisibility(View.VISIBLE);
		
		
	}

	@Override
	public void onClick(View aa) {
		// TODO Auto-generated method stub
		switch (aa.getId()) {
		case R.id.switch_city:
			Intent intent = new Intent(this,ChooseAreaAcitity.class);
			intent.putExtra("from_weather_activity", true);
			startActivity(intent);
			finish();
			break;
		case R.id.refresh_weather:
			publishText.setText("重新同步咯。。。");
			SharedPreferences pp = PreferenceManager.getDefaultSharedPreferences(this);
			String weatherCode = pp.getString("weather_code", "");
			if (!TextUtils.isEmpty(weatherCode)) {
				queryWeatherInfo(weatherCode);
			}
			break;

		default:
			break;
		}
	}
	
}
