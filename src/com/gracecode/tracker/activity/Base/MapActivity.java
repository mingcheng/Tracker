package com.gracecode.tracker.activity.base;

import android.location.Location;
import android.os.Bundle;
import com.baidu.mapapi.*;
import com.mobclick.android.MobclickAgent;


public abstract class MapActivity extends com.baidu.mapapi.MapActivity implements MKGeneralListener {
    protected MapView mapView = null;
    protected MapController mapViewController;
    static protected BMapManager bMapManager;
    private static final String BAIDU_MAP_KEY = "30183AD8A6AFE7CE8F649ED4CD258211E8DE78D7";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bMapManager = new BMapManager(getApplication());
        bMapManager.init(BAIDU_MAP_KEY, this);

        MobclickAgent.onError(this);
    }

    @Override
    public void onStart() {
        super.onStart();

        if (mapView == null) {
            return;
        }

        mapView.removeAllViews();
        super.initMapActivity(bMapManager);

        mapViewController = mapView.getController();
        bMapManager.start();
    }

    @Override
    public void onStop() {
        super.onStop();
        bMapManager.stop();
    }

    @Override
    public void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
    }

    @Override
    protected boolean isRouteDisplayed() {
        return false;
    }

    protected void setCenterPoint(Location location, boolean animate) {
        if (mapView == null) {
            return;
        }

        GeoPoint geoPoint = new GeoPoint(
            (int) (location.getLatitude() * 1E6),
            (int) (location.getLongitude() * 1E6)
        );

        // 计算地图偏移
        geoPoint = CoordinateConvert.bundleDecode(CoordinateConvert.fromWgs84ToBaidu(geoPoint));

        // @todo 自动计算默认缩放的地图界面
        if (animate) {
            mapViewController.animateTo(geoPoint);
        } else {
            mapViewController.setCenter(geoPoint);
        }
    }

    protected void setCenterPoint(Location location) {
        setCenterPoint(location, false);
    }

    @Override
    public void onDestroy() {
        bMapManager.destroy();
        super.onDestroy();
    }

    @Override
    public void onGetNetworkState(int i) {

    }

    @Override
    public void onGetPermissionState(int i) {

    }
}
