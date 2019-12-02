package com.example.coolweather.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;



import java.util.ArrayList;
import java.util.List;

import com.example.coolweather.db.CoolWeatherDB;
import com.example.coolweather.model.City;
import com.example.coolweather.model.County;
import com.example.coolweather.model.Province;
import com.example.coolweather.util.HttpCallbackListener;
import com.example.coolweather.util.Httputil;
import com.example.coolweather.util.Utillity;

/**
 * Created by chencong on 2016/7/13.
 */
public class ChooseAreaActivity extends Activity {
    public static final int LEVEL_PROVINCE = 0;
    public static final int LEVEL_CITY = 1;
    public static final int LEVEL_COUNTY = 2;

    private ProgressDialog progressDialog;
    private TextView titleText;
    private ListView listView;
    private ArrayAdapter<String> adapter;
    private CoolWeatherDB coolWeatherDB;
    private List<String> dataList = new ArrayList<String>();

    //鐪佸垪琛�
    private List<Province> provinceList;

    //甯傚垪琛�
    private List<City> cityList;

    //鍘垮垪琛�
    private  List<County> countyList;

    //閫変腑鐨勭渷浠�
    private Province selectedProvince;

    //閫変腑鐨勫競
    private City selectedCity;

    //褰撳墠閫変腑鐨勭骇鍒�
    private int currentLevel;


    /*
    * 鏄惁浠嶹eatherActivity涓烦杞繃鏉�*/
    private boolean isFromWeatherActivity;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        isFromWeatherActivity = getIntent().getBooleanExtra("from_weather_activity",false);
        //浠嶤hooseAreaActivity璺宠浆鍒癢eatherActivity
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

        //瀵瑰凡缁忛�夋嫨浜嗗煄甯傚苟涓斾笉鏄粠WeatherActivity璺宠浆杩囨潵锛屾墠浼氱洿鎺ヨ烦杞埌WeatherActivity
        if (prefs.getBoolean("city_selected",false) && !isFromWeatherActivity){
            Intent intent = new Intent(this,WeatherActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.choose_area);
        listView = (ListView)findViewById(R.id.listView);
        titleText = (TextView) findViewById(R.id.textView);

        adapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,dataList);
        listView.setAdapter(adapter);
        coolWeatherDB = CoolWeatherDB.getInstance(this);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (currentLevel == LEVEL_PROVINCE){
                    selectedProvince = provinceList.get(position);  //index鎹osition
                    queryCities();
                }else if (currentLevel == LEVEL_CITY){
                    selectedCity = cityList.get(position);
                    queryCounties();

                    //浠嶤hooseAreaActivity璺宠浆鍒癢eatherActivity
                }else if (currentLevel == LEVEL_COUNTY){
                    String countyCode = countyList.get(position).getCountyCode();
                    Intent intent = new Intent(ChooseAreaActivity.this,WeatherActivity.class);
                    intent.putExtra("county_code",countyCode);
                    startActivity(intent);
                    finish();
                }


            }
        });
        queryProvinces();   //鍔犺浇鐪佺骇鏁版嵁
    }

   
    private void queryProvinces (){
        provinceList = coolWeatherDB.loadProvinces();
        if (provinceList.size() > 0){
            dataList.clear();
            for (Province province : provinceList){
                dataList.add(province.getProvinceName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            titleText.setText("中国");
            currentLevel = LEVEL_PROVINCE;
        }else {
            queryFromServer(null,"province");
        }
    }

    /*鏌ヨ閫変腑鐪佺殑鎵�鏈夊競锛屽厛浠庡競鏁版嵁搴撲腑鏌ヨ锛屽鏋滄病鏈夊啀浠庢湇鍔″櫒涓煡璇�*/
    private  void queryCities(){
        cityList = coolWeatherDB.loadCities(selectedProvince.getId());
        if (cityList.size() > 0){
            dataList.clear();
            for (City city : cityList){
                dataList.add(city.getCityName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            titleText.setText(selectedProvince.getProvinceName());
            currentLevel = LEVEL_CITY;
        }else {
            queryFromServer(selectedProvince.getProvinceCode(),"city");
        }
    }

    /*鏌ヨ閫変腑甯傜殑鎵�鏈夊幙锛屽厛浠庢暟鎹簱涓煡璇紝濡傛灉娌℃湁鍐嶄粠鏈嶅姟鍣ㄤ腑鏌ヨ*/
    private  void queryCounties(){
        countyList = coolWeatherDB.loadCounties(selectedCity.getId());
        if (countyList.size() >0 ){
            dataList.clear();
            for (County county : countyList){
                dataList.add(county.getCountyName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            titleText.setText(selectedCity.getCityName());
            currentLevel = LEVEL_COUNTY;
        }else{
            queryFromServer(selectedCity.getCityCode(),"county");
        }
    }

    /*鏍规嵁浼犲叆鐨勪唬鍙峰拰绫诲瀷浠庢湇鍔″櫒涓婃煡璇㈢渷甯傚幙鐨勬暟鎹�*/
    private void queryFromServer(final String code,final String type){
        String address;
        if (!TextUtils.isEmpty(code)){
            address = "http://www.weather.com.cn/data/list3/city"+code+".xml";
        }else{
            address = "http://www.weather.com.cn/data/list3/city.xml";
        }
        showProgressDialog();   //寮�鍚嚎绋�
        Httputil.sendHttpRequest(address, new HttpCallbackListener() {
            //handle**Response()鏂规硶閮芥槸boolean绫诲瀷鐨勶紝鍒ゆ柇鏄惁灏嗘暟鎹瓨鍌ㄥ埌琛ㄤ腑
            @Override
            public void onFinish(String response) {
                boolean result = false;
                if ("province".equals(type)){
                    result = Utillity.handleProvincesResponse(coolWeatherDB,response);
                }else if ("city".equals(type)){
                    result = Utillity.handleCitiesResponse(coolWeatherDB,response,selectedProvince.getId());
                }else if ("county".equals(type)){
                    result = Utillity.handleCountiesResponse(coolWeatherDB,response,selectedCity.getId());
                }

                if (result){
                    //閫氳繃runOnUiThread()鏂规硶鍥炲埌涓荤嚎绋嬪鐞嗛�昏緫
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            closeProgressDialog();
                            if ("province".equals(type)){
                                queryProvinces();
                            }else if ("city".equals(type)){
                                queryCities();
                            }else if ("county".equals(type)){
                                queryCounties();
                            }
                        }
                    });
                }
            }

            @Override
            public void onError(Exception e) {
                //閫氳繃runOnUiThread()鏂规硶鍥炶皟涓荤嚎绋嬪鐞嗛�昏緫
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        closeProgressDialog();
                        Toast.makeText(ChooseAreaActivity.this,"鍔犺浇澶辫触鍟�(鈺モ暞^鈺扳暐)",Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    /*鏄剧ず杩涘害瀵硅瘽妗�*/
    private void showProgressDialog(){
        if (progressDialog == null){
            progressDialog = new ProgressDialog(this);
            progressDialog.setMessage("鍔姏鍔犺浇涓�.... 鈫�(^蠅^)鈫�");
            progressDialog.setCanceledOnTouchOutside(false);
       }
        progressDialog.show();
    }

    /*
    * 鍏抽棴杩涘害瀵硅瘽妗�*/
    private void closeProgressDialog(){
        if (progressDialog != null){
            progressDialog.dismiss();
        }
    }

    /*
    * 鎹曡幏Back閿紝鏍规嵁褰撳墠绾у埆鏉ュ垽鏂紝鏄簲璇ュ競鍒楄〃銆佺渷鍒楄〃锛岃繕鏄洿鎺ラ��鍑�*/

    @Override
    public void onBackPressed() {
        if (currentLevel == LEVEL_COUNTY){
            queryCities();
        }else if (currentLevel == LEVEL_CITY){
            queryProvinces();
        }else {
            if (isFromWeatherActivity){
                Intent intent = new Intent(this,WeatherActivity.class);
                startActivity(intent);
            }
        }
            finish();
    }
}
            	
            	
            
