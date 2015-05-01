package com.kkweather.app.activity;

import java.util.ArrayList;
import java.util.List;

import com.kkweather.app.db.KKWeatherDB;
import com.kkweather.app.model.City;
import com.kkweather.app.model.County;
import com.kkweather.app.model.Province;
import com.kkweather.app.util.HttpCallbackListener;
import com.kkweather.app.util.HttpUtil;
import com.kkweather.app.util.Utility;

import android.R;
import android.R.bool;
import android.R.integer;
import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class ChooseAreaAcitity extends Activity {

	public static final int LEVEL_PROVINCE = 0;
	public static final int LEVEL_CITI = 1;
	public static final int LEVEL_COUNTY = 2;

	private ProgressDialog progressDialog;
	private TextView titleText;
	private ListView listView;
	private ArrayAdapter<String> adapter;
	private KKWeatherDB kkWeatherDB;
	private List<String> dataList = new ArrayList<String>();

	/*
	 * 省市县列表
	 */
	private List<Province> provinceList;
	private List<City> cityList;
	private List<County> countyList;

	/*
	 * 选中的省份、城市
	 */
	private Province selectProvince;
	private City selectCity;

	/*
	 * 当前选中的级别
	 */
	private int currentLevel;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(com.kkweather.app.R.layout.choose_area);
		listView = (ListView) findViewById(com.kkweather.app.R.id.list_view);
		titleText = (TextView) findViewById(com.kkweather.app.R.id.title_text);
		adapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1, dataList);
		listView.setAdapter(adapter);
		kkWeatherDB = KKWeatherDB.getInstance(this);
		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View view, int index,
					long arg3) {
				// TODO Auto-generated method stub
				if (currentLevel == LEVEL_PROVINCE) {
					selectProvince = provinceList.get(index);
					queryCities();
				} else if (currentLevel == LEVEL_CITI) {
					selectCity = cityList.get(index);
					queryCounties();

				}

			}

		});
		queryProvinces();
	}

	/*
	 * 查询全国所有的省，优先从数据库查询，如果没有到服务器查询
	 */
	private void queryProvinces() {
		// TODO Auto-generated method stub
		provinceList = kkWeatherDB.loadProvinces();
		if (provinceList.size() > 0) {
			dataList.clear();
			for (Province province : provinceList) {
				dataList.add(province.getProvinceName());
			}
			adapter.notifyDataSetChanged();
			listView.setSelection(0);
			titleText.setText("中国");
			currentLevel = LEVEL_PROVINCE;
		} else {
			queryFromServer(null, "province");
		}

	}

	/*
	 * 查询选中省内所有的市，优先数据库查询，，没有再去服务器上查询。
	 */
	private void queryCities() {
		// TODO Auto-generated method stub
		cityList = kkWeatherDB.loadCities(selectProvince.getId());
		if (cityList.size() > 0) {
			dataList.clear();
			for (City city : cityList) {
				dataList.add(city.getCityName());
			}
			adapter.notifyDataSetChanged();
			listView.setSelection(0);
			titleText.setText(selectProvince.getProvinceName());
			currentLevel = LEVEL_CITI;
		} else {
			queryFromServer(selectProvince.getProvinceCode(), "city");

		}

	}

	/*
	 * 查询选中市内所有的县，优先从数据库查询，如果没有查询到再去服务器上查询。
	 */
	private void queryCounties() {
		// TODO Auto-generated method stub
		countyList = kkWeatherDB.loadCounties(selectCity.getId());
		if (countyList.size() > 0) {
			dataList.clear();
			for (County county : countyList) {
				dataList.add(county.getCountyName());
			}
			adapter.notifyDataSetChanged();
			listView.setSelection(0);
			titleText.setText(selectProvince.getProvinceName());
			currentLevel = LEVEL_COUNTY;
		} else {
			queryFromServer(selectCity.getCityCode(), "county");

		}
	}

	/*
	 * 根据传入的代号和类型从服务器上查询省市县数据。
	 */
	private void queryFromServer(final String code, final String type) {
		// TODO Auto-generated method stub
		String address;
		if (!TextUtils.isEmpty(code)) {
			address = "http://www.weather.com.cn/data/list3/city" + code
					+ ".xml";
		} else {
			address = "http://www.weather.com.cn/data/list3/city.xml";

		}
		showProgressDialog();
		HttpUtil.sendHttpRequest(address, new HttpCallbackListener() {

			@Override
			public void onFinish(String response) {
				// TODO Auto-generated method stub
				boolean result = false;
				if ("province".equals(type)) {
					result = Utility.handleProvincesResponse(kkWeatherDB,
							response);
				} else if ("city".equals(type)) {
					result = Utility.handleCitiesResponse(kkWeatherDB,
							response, selectProvince.getId());
				} else if ("county".equals(type)) {
					result = Utility.handleCountiesResponse(kkWeatherDB,
							response, selectCity.getId());
				}
				if (result) {
					//通过runOnUIThread（）方法回到主线程处理逻辑
					runOnUiThread(new Runnable() {
						public void run() {
							closeProgressDialog();
							if ("province".equals(type)) {
								queryProvinces();
							} else if ("city".equals(type)){
								queryCities();
							} else if ("county".equals(type)) {
								queryCounties();
							}
						}
					});
				}

			}

			@Override
			public void onError(Exception e) {
				// 通过runOnUIThread（）方法回到主线程处理逻辑
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						closeProgressDialog();
						Toast.makeText(ChooseAreaAcitity.this, "加载失败", Toast.LENGTH_SHORT).show();
					}
				});

			}
		});

	}

	/*
	 * 显示进度对话框
	 */
	private void showProgressDialog() {
		// TODO Auto-generated method stub
		if (progressDialog == null) {
			progressDialog = new ProgressDialog(this);
			progressDialog.setMessage("正在加载....");
			progressDialog.setCanceledOnTouchOutside(false);
		}
		progressDialog.show();

	}
	
	/*
	关闭进度对话框
	*/
	private void closeProgressDialog() {
		// TODO Auto-generated method stub
		if (progressDialog != null) {
			progressDialog.dismiss();
		}
	}
	
	/*
	捕获Back键，根据当前级别判断，此时返回市列表、省列表、还是直接退出。
	*/
	@Override
	public void onBackPressed(){
		if (currentLevel == LEVEL_COUNTY) {
			queryCities();
		} else if (currentLevel == LEVEL_CITI){
			queryProvinces();
		}else {
			finish();
		}
	}
	
}
