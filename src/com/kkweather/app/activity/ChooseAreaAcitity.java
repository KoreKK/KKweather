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
	 * ʡ�����б�
	 */
	private List<Province> provinceList;
	private List<City> cityList;
	private List<County> countyList;

	/*
	 * ѡ�е�ʡ�ݡ�����
	 */
	private Province selectProvince;
	private City selectCity;

	/*
	 * ��ǰѡ�еļ���
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
	 * ��ѯȫ�����е�ʡ�����ȴ����ݿ��ѯ�����û�е���������ѯ
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
			titleText.setText("�й�");
			currentLevel = LEVEL_PROVINCE;
		} else {
			queryFromServer(null, "province");
		}

	}

	/*
	 * ��ѯѡ��ʡ�����е��У��������ݿ��ѯ����û����ȥ�������ϲ�ѯ��
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
	 * ��ѯѡ���������е��أ����ȴ����ݿ��ѯ�����û�в�ѯ����ȥ�������ϲ�ѯ��
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
	 * ���ݴ���Ĵ��ź����ʹӷ������ϲ�ѯʡ�������ݡ�
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
					//ͨ��runOnUIThread���������ص����̴߳����߼�
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
				// ͨ��runOnUIThread���������ص����̴߳����߼�
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						closeProgressDialog();
						Toast.makeText(ChooseAreaAcitity.this, "����ʧ��", Toast.LENGTH_SHORT).show();
					}
				});

			}
		});

	}

	/*
	 * ��ʾ���ȶԻ���
	 */
	private void showProgressDialog() {
		// TODO Auto-generated method stub
		if (progressDialog == null) {
			progressDialog = new ProgressDialog(this);
			progressDialog.setMessage("���ڼ���....");
			progressDialog.setCanceledOnTouchOutside(false);
		}
		progressDialog.show();

	}
	
	/*
	�رս��ȶԻ���
	*/
	private void closeProgressDialog() {
		// TODO Auto-generated method stub
		if (progressDialog != null) {
			progressDialog.dismiss();
		}
	}
	
	/*
	����Back�������ݵ�ǰ�����жϣ���ʱ�������б�ʡ�б�����ֱ���˳���
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
