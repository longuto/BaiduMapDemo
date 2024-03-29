package com.yltang.runnings.search;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.MapBaseIndoorMapInfo;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.overlayutil.IndoorPoiOverlay;
import com.baidu.mapapi.search.poi.OnGetPoiSearchResultListener;
import com.baidu.mapapi.search.poi.PoiDetailResult;
import com.baidu.mapapi.search.poi.PoiDetailSearchResult;
import com.baidu.mapapi.search.poi.PoiIndoorInfo;
import com.baidu.mapapi.search.poi.PoiIndoorOption;
import com.baidu.mapapi.search.poi.PoiIndoorResult;
import com.baidu.mapapi.search.poi.PoiResult;
import com.baidu.mapapi.search.poi.PoiSearch;
import com.yltang.runnings.R;
import com.yltang.runnings.indoorview.BaseStripAdapter;
import com.yltang.runnings.indoorview.StripListView;

public class IndoorSearchDemo extends Activity implements OnGetPoiSearchResultListener,
        BaiduMap.OnBaseIndoorMapListener {
    private MapView mMapView;
    private BaiduMap mBaiduMap;
    private PoiSearch mPoiSearch = null;
    Button searchBtn;
    EditText searchContent;
    StripListView stripView;
    BaseStripAdapter mFloorListAdapter;
    MapBaseIndoorMapInfo mMapBaseIndoorMapInfo = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_indoor_search);

        mMapView = (MapView) findViewById(R.id.bmapView);
        mBaiduMap = mMapView.getMap();

        /* 因为室内图支持最大缩放级别为22.0f，要做特殊处理，所以要先打开室内图开关，然后做地图刷新，以防设置
         * 缩放级别为22.0f时不生效
         */
        mBaiduMap.setIndoorEnable(true);

        LatLng centerpos = new LatLng(39.871281,116.385306); // 北京南站
        MapStatus.Builder builder = new MapStatus.Builder();
        builder.target(centerpos).zoom(19.0f);
        mBaiduMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));

     //   stripView = (StripListView) findViewById(R.id.stripView);
        stripView = new StripListView(this);
        RelativeLayout layout = (RelativeLayout) findViewById(R.id.viewStub);
        layout.addView(stripView);
        mFloorListAdapter = new BaseStripAdapter(this);
        mBaiduMap.setOnBaseIndoorMapListener(this);

        mPoiSearch = PoiSearch.newInstance();
        mPoiSearch.setOnGetPoiSearchResultListener(this);

        searchContent = (EditText) findViewById(R.id.content);
        searchBtn = (Button) findViewById(R.id.indoorSearch);
        searchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MapBaseIndoorMapInfo indoorInfo = mBaiduMap.getFocusedBaseIndoorMapInfo();
                if (indoorInfo == null) {
                    Toast.makeText(IndoorSearchDemo.this, "当前无室内图，无法搜索" , Toast.LENGTH_LONG).show();
                    return;
                }
                PoiIndoorOption option = new PoiIndoorOption().poiIndoorBid(
                        indoorInfo.getID()).poiIndoorWd(searchContent.getText().toString());
                mPoiSearch.searchPoiIndoor( option);
            }
        });

        stripView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                if (mMapBaseIndoorMapInfo == null) {
                    return;
                }
                String floor = (String) mFloorListAdapter.getItem(position);
                mBaiduMap.switchBaseIndoorMapFloor(floor, mMapBaseIndoorMapInfo.getID());
                mFloorListAdapter.setSelectedPostion(position);
                mFloorListAdapter.notifyDataSetInvalidated();
            }
        });

    }

    @Override
    public void onGetPoiResult(PoiResult result) {

    }

    /**
     * V5.2.0版本之后，还方法废弃，使用{@link #onGetPoiDetailResult(PoiDetailSearchResult)}
     * 代替
     */
    @Override
    public void onGetPoiDetailResult(PoiDetailResult result) {

    }

    @Override
    public void onGetPoiDetailResult(PoiDetailSearchResult result) {

    }

    /**
     * 获取室内图搜索结果，得到searchPoiIndoor返回的结果
     * @param result
     */
    @Override
    public void onGetPoiIndoorResult(PoiIndoorResult result) {
        mBaiduMap.clear();
        if (result == null  || result.error == PoiIndoorResult.ERRORNO.RESULT_NOT_FOUND) {
            Toast.makeText(IndoorSearchDemo.this, "无结果" , Toast.LENGTH_LONG).show();
            return;
        }
        if (result.error == PoiIndoorResult.ERRORNO.NO_ERROR) {
            IndoorPoiOverlay overlay = new MyIndoorPoiOverlay(mBaiduMap);
            mBaiduMap.setOnMarkerClickListener(overlay);
            overlay.setData(result);
            overlay.addToMap();
            overlay.zoomToSpan();
        }
    }

    @Override
    public void onBaseIndoorMapMode(boolean b, MapBaseIndoorMapInfo mapBaseIndoorMapInfo) {

        if (b) {
            stripView.setVisibility(View.VISIBLE);
            if (mapBaseIndoorMapInfo == null) {
                return;
            }
            mFloorListAdapter.setmFloorList(mapBaseIndoorMapInfo.getFloors());
            stripView.setStripAdapter(mFloorListAdapter);

        } else {
            stripView.setVisibility(View.GONE);
        }
        mMapBaseIndoorMapInfo = mapBaseIndoorMapInfo;
    }

    private class MyIndoorPoiOverlay extends IndoorPoiOverlay {

        /**
         * 构造函数
         *
         * @param baiduMap 该 IndoorPoiOverlay 引用的 BaiduMap 对象
         */
        public MyIndoorPoiOverlay(BaiduMap baiduMap) {
            super(baiduMap);
        }

        @Override
        /**
         * 响应点击室内POI点事件
         * @param i
         *            被点击的poi在
         *            {@link com.baidu.mapapi.search.poi.PoiIndoorResult#getmArrayPoiInfo()} } 中的索引
         * @return
         */
        public boolean onPoiClick(int i) {
            PoiIndoorInfo info =  getIndoorPoiResult().getmArrayPoiInfo().get(i);
            Toast.makeText(IndoorSearchDemo.this, info.name + ",在" + info.floor + "层", Toast.LENGTH_LONG).show();
            return false;
        }

    }

    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mPoiSearch.destroy();
        mMapView.onDestroy();
    }

}
