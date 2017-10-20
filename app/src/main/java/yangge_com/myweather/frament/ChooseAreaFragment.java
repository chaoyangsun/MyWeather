package yangge_com.myweather.frament;


import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.litepal.crud.DataSupport;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import yangge_com.myweather.R;
import yangge_com.myweather.activity.WeatherActivity;
import yangge_com.myweather.db.City;
import yangge_com.myweather.db.Country;
import yangge_com.myweather.db.Province;
import yangge_com.myweather.util.HttpUtil;
import yangge_com.myweather.util.Utility;

/**
 * A simple {@link Fragment} subclass.
 */
public class ChooseAreaFragment extends Fragment {
    public static final int LEVEL_PROVINE = 0;
    public static final int LEVEL_CITY = 1;
    public static final int LEVEL_COUNTRY = 2;
    private ProgressDialog dialog;
    private TextView title_text;
    private Button back_button;
    private ListView list_view;
    private ArrayAdapter<String> adapter;
    private List<String> dataList = new ArrayList<>();
    private List<Province> provinveList;
    private List<City> cityList;
    private List<Country> countryList;
    private Province selectedProvince;
    private City selectedCity;
    private int currentLevel;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.choose_area, container, false);
        title_text = view.findViewById(R.id.title_text);
        back_button = view.findViewById(R.id.back_button);
        list_view = view.findViewById(R.id.list_view);
        adapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_expandable_list_item_1, dataList);
        list_view.setAdapter(adapter);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        list_view.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if(currentLevel == LEVEL_PROVINE) {
                    selectedProvince = provinveList.get(i);
                    queryCities();
                }else if(currentLevel == LEVEL_CITY) {
                    selectedCity = cityList.get(i);
                    queryCountrys();
                }else if(currentLevel == LEVEL_COUNTRY) {
                    String weatherId = countryList.get(i).getWeatherId();
                    Intent intent = new Intent(getActivity(), WeatherActivity.class);
                    intent.putExtra("weather_id", weatherId);
                    startActivity(intent);
                    getActivity().finish();
                }
            }
        });

        back_button.setOnClickListener(view -> {
            if(currentLevel == LEVEL_COUNTRY) {
                queryCities();
            }else if(currentLevel == LEVEL_CITY) {
                queryProvinces();
            }
        });
        queryProvinces();
    }

    private void queryProvinces() {
        title_text.setText("中国");
        back_button.setVisibility(View.GONE);
        provinveList = DataSupport.findAll(Province.class);
        if(provinveList.size() > 0) {
            dataList.clear();
            for (Province p: provinveList){
                dataList.add(p.getProvinceName());
            }
            adapter.notifyDataSetChanged();
            list_view.setSelection(0);
            currentLevel = LEVEL_PROVINE;
        }else {
            String address = "http://guolin.tech/api/china";
            queryFromServer(address, "province");
        }
    }

    private void queryCities() {
        title_text.setText(selectedProvince.getProvinceName());
        back_button.setVisibility(View.VISIBLE);
        cityList = DataSupport.where("provinceid = ?", String.valueOf(selectedProvince.getId())).find(City.class);
        if(cityList.size() > 0) {
            dataList.clear();
            for (City city : cityList){
                dataList.add(city.getCityName());
            }
            adapter.notifyDataSetChanged();
            list_view.setSelection(0);
            currentLevel = LEVEL_CITY;
        }else {
            int provinceCode = selectedProvince.getProvinceCode();
            String address = "http://guolin.tech/api/china" + "/" +provinceCode;
            queryFromServer(address, "city");
        }

    }


    private void queryCountrys() {
        title_text.setText(selectedCity.getCityName());
        back_button.setVisibility(View.VISIBLE);
        countryList = DataSupport.where("cityid = ?", String.valueOf(selectedCity.getId())).find(Country.class);
        if(countryList.size() > 0) {
            dataList.clear();
            for (Country c : countryList){
                dataList.add(c.getCountryName());
            }
            adapter.notifyDataSetChanged();
            list_view.setSelection(0);
            currentLevel = LEVEL_COUNTRY;
        }else {
            int provinceCode = selectedProvince.getProvinceCode();
            int cityCode = selectedCity.getCityCode();
            String address = "http://guolin.tech/api/china" +"/" + provinceCode + "/" + cityCode;
            queryFromServer(address, "country");
        }
    }

    private void queryFromServer(String address, String type) {
        Log.i("Http", " " + address);
        showProgressDialog();
        HttpUtil.sendOkHttpRequest(address, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                getActivity().runOnUiThread(() -> {
                    closeProgessDialog();
                    Toast.makeText(getContext(), "加载失败", Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseText = response.body().string();
                boolean result = false;
                if("province".equals(type)) {
                    result = Utility.handleProvinceResponse(responseText);
                }else if("city".equals(type)) {
                    result = Utility.handleCityResponse(responseText, selectedProvince.getId());
                }else if("country".equals(type)) {
                    result = Utility.handleCountryResponse(responseText, selectedCity.getId());
                }
                if(result) {
                    getActivity().runOnUiThread(() -> {
                        closeProgessDialog();
                        if("province".equals(type)) {
                            queryProvinces();
                        }else if("city".equals(type)) {
                            queryCities();
                        }else if("country".equals(type)) {
                            queryCountrys();
                        }
                    });
                }
            }
        });

    }

    private void closeProgessDialog() {
        if(dialog != null) {
            dialog.dismiss();
        }
    }

    private void showProgressDialog() {
        if(dialog == null) {
            dialog = new ProgressDialog(getActivity());
            dialog.setMessage("正在加载...");
            dialog.setCanceledOnTouchOutside(false);
        }
        dialog.show();
    }
}
