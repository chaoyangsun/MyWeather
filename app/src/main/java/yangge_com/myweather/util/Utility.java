package yangge_com.myweather.util;

import android.text.TextUtils;

import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import yangge_com.myweather.db.City;
import yangge_com.myweather.db.Country;
import yangge_com.myweather.db.Province;

/**
 * Created by charming-yin on 2017/10/19.
 */

public class Utility {
    public static boolean handleProvinceResponse(String response){
        if(!TextUtils.isEmpty(response)) {
            try{
                JSONArray allProvinces = new JSONArray(response);
                for (int i = 0; i < allProvinces.length(); i++) {
                    JSONObject jsonObject = allProvinces.getJSONObject(i);
                    Province province = new Gson().fromJson(jsonObject.toString(), Province.class);
                    province.save();
                }
                return true;
            }catch (JSONException e){
                e.printStackTrace();
            }
        }
        return false;
    }
    public static boolean handleCityResponse(String response, int provinceId){
        if(!TextUtils.isEmpty(response)) {
            try{
                JSONArray allCitys = new JSONArray(response);
                for (int i = 0; i < allCitys.length(); i++) {
                    JSONObject jsonObject = allCitys.getJSONObject(i);
                    City city = new Gson().fromJson(jsonObject.toString(), City.class);
                    city.save();
                }
                return true;
            }catch (JSONException e){
                e.printStackTrace();
            }
        }
        return false;
    }
    public static boolean handleCountryResponse(String response, int cityId){
        if(!TextUtils.isEmpty(response)) {
            try{
                JSONArray allCountrys = new JSONArray(response);
                for (int i = 0; i < allCountrys.length(); i++) {
                    JSONObject jsonObject = allCountrys.getJSONObject(i);
                    Country country = new Gson().fromJson(jsonObject.toString(), Country.class);
                    country.save();
                }
                return true;
            }catch (JSONException e){
                e.printStackTrace();
            }
        }
        return false;
    }
}
