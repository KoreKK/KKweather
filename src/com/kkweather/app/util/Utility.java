package com.kkweather.app.util;

import android.text.TextUtils;

import com.kkweather.app.db.KKWeatherDB;
import com.kkweather.app.model.City;
import com.kkweather.app.model.County;
import com.kkweather.app.model.Province;

public class Utility {

	/*
	 * 解析和处理服务器返回的省级数据
	 */
	public synchronized static boolean handleProvincesResponse(
			KKWeatherDB kkWeatherDB, String response) {
		if (!TextUtils.isEmpty(response)) {
			String[] allProvinces = response.split(",");
			if (allProvinces != null && allProvinces.length > 0) {
				for (String p : allProvinces) {
					String[] array = p.split("\\|");
					Province province = new Province();
					province.setProvinceCode(array[0]);
					province.setProvinceName(array[1]);
					kkWeatherDB.saveProvince(province);
				}
				return true;
			}
		}
		return false;
	}

	/*
	 * 解析和处理服务器返回的市级数据
	 */
	public synchronized static boolean handleCitiesResponse(
			KKWeatherDB kkWeatherDB, String response,int provinceId) {
		if (!TextUtils.isEmpty(response)) {
			String[] allcities = response.split(",");
			if (allcities != null && allcities.length > 0) {
				for (String c : allcities) {
					String[] array = c.split("\\|");
					City city = new City();
					city.setCityCode(array[0]);
					city.setCityName(array[1]);
					city.setProvinceId(provinceId);
					kkWeatherDB.saveCity(city);
				}
				return true;
			}
		}
		return false;
	}

	/*
	 * 解析和处理服务器返回县级数据
	 */
	public synchronized static boolean handleCountiesResponse(
			KKWeatherDB kkWeatherDB, String response,int cityId) {
		if (!TextUtils.isEmpty(response)) {
			String[] allCounties = response.split(",");
			if (allCounties != null && allCounties.length > 0) {
				for (String c : allCounties) {
					String[] array = c.split("\\|");
					County county = new County();
					county.setCountyCode(array[0]);
					county.setCountyName(array[1]);
					county.setCityId(cityId);
					kkWeatherDB.saveCounty(county);
				}
				return true;
			}
		}
		return false;
	}

}
