package com.yltang.runnings.search;

import android.app.Activity;
import android.os.Bundle;
import android.text.Editable;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.geocode.GeoCodeOption;
import com.baidu.mapapi.search.geocode.GeoCodeResult;
import com.baidu.mapapi.search.geocode.GeoCoder;
import com.baidu.mapapi.search.geocode.OnGetGeoCoderResultListener;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeOption;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;
import com.yltang.runnings.R;

/**
 * 此demo用来展示如何进行地理编码搜索（用地址检索坐标）、反地理编码搜索（用坐标检索地址）
 */
public class GeoCoderDemo extends Activity implements OnGetGeoCoderResultListener {
    GeoCoder mSearch = null; // 搜索模块，也可去掉地图模块独立使用
    BaiduMap mBaiduMap = null;
    MapView mMapView = null;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_geocoder);
        CharSequence titleLable = "地理编码功能";
        setTitle(titleLable);

        // 地图初始化
        mMapView = (MapView) findViewById(R.id.bmapView);
        mBaiduMap = mMapView.getMap();

        // 初始化搜索模块，注册事件监听
        mSearch = GeoCoder.newInstance();
        mSearch.setOnGetGeoCodeResultListener(this);
    }

    /**
     * 发起搜索
     * 
     * @param v
     */
    public void searchButtonProcess(View v) {
        if (v.getId() == R.id.reversegeocode) {
            int version  = 0;
            EditText lat = (EditText) findViewById(R.id.lat);
            EditText lon = (EditText) findViewById(R.id.lon);
            CheckBox cb = (CheckBox) findViewById(R.id.newVersion);
            Editable latEditable = lat.getText();
            Editable lonEditable = lon.getText();

            if (latEditable == null || lonEditable == null) {
                return;
            }

            String latString = latEditable.toString();
            String lonString = lonEditable.toString();

            if (latString.isEmpty() || lonString.isEmpty()) {
                return;
            }

            LatLng ptCenter = new LatLng((Float.valueOf(latString)), (Float.valueOf(lonString)));

            // 反Geo搜索
            if(cb.isChecked()){
                version=1;
            }
            mSearch.reverseGeoCode(new ReverseGeoCodeOption().location(ptCenter).newVersion(version).radius(500));
        } else if (v.getId() == R.id.geocode) {
            EditText editCity = (EditText) findViewById(R.id.city);
            EditText editGeoCodeKey = (EditText) findViewById(R.id.geocodekey);

            // Geo搜索
            mSearch.geocode(new GeoCodeOption()
                .city(editCity.getText().toString())
                .address(editGeoCodeKey.getText().toString()));
        }
    }

    @Override
    protected void onPause() {
        mMapView.onPause();
        super.onPause();
    }

    @Override
    protected void onResume() {
        mMapView.onResume();
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        mMapView.onDestroy();
        mSearch.destroy();
        super.onDestroy();
    }

    @Override
    public void onGetGeoCodeResult(GeoCodeResult result) {
        if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
            Toast.makeText(GeoCoderDemo.this, "抱歉，未能找到结果", Toast.LENGTH_LONG).show();
            return;
        }

        mBaiduMap.clear();
        mBaiduMap.addOverlay(new MarkerOptions()
                                 .position(result.getLocation())
                                 .icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_marka)));

        mBaiduMap.setMapStatus(MapStatusUpdateFactory.newLatLng(result.getLocation()));
        String strInfo = String.format("纬度：%f 经度：%f",
                                        result.getLocation().latitude,
                                        result.getLocation().longitude);

        Toast.makeText(GeoCoderDemo.this, strInfo, Toast.LENGTH_LONG).show();

        Log.e("GeoCodeDemo", "onGetGeoCodeResult = " + result.toString());
    }

    @Override
    public void onGetReverseGeoCodeResult(ReverseGeoCodeResult result) {
        if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
            Toast.makeText(GeoCoderDemo.this, "抱歉，未能找到结果", Toast.LENGTH_LONG).show();
            return;
        }

        mBaiduMap.clear();
        mBaiduMap.addOverlay(new MarkerOptions()
                                .position(result.getLocation())
                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_marka)));

        mBaiduMap.setMapStatus(MapStatusUpdateFactory.newLatLng(result.getLocation()));

        Toast.makeText(GeoCoderDemo.this, result.getAddress() + " adcode: " + result.getAdcode(), Toast.LENGTH_LONG).show();

        Log.e("GeoCodeDemo", "ReverseGeoCodeResult = " + result.toString());
    }

}
