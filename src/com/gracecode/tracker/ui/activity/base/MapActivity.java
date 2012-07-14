package com.gracecode.tracker.ui.activity.base;

import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import com.baidu.mapapi.*;
import com.gracecode.tracker.R;
import com.gracecode.tracker.util.Helper;
import com.markupartist.android.widget.ActionBar;
import com.umeng.analytics.MobclickAgent;

import java.util.ArrayList;

public abstract class MapActivity extends com.baidu.mapapi.MapActivity implements MKGeneralListener {
    protected MapView mapView = null;
    protected MapController mapViewController;
    protected BMapManager bMapManager;
    private static final String BAIDU_MAP_KEY = "30183AD8A6AFE7CE8F649ED4CD258211E8DE78D7";
    protected Helper helper;
    protected Context context;
    protected ActionBar actionBar;

    protected ArrayList<Location> locations;

    protected double topBoundary;
    protected double leftBoundary;
    protected double rightBoundary;
    protected double bottomBoundary;

    protected Location locationTopLeft;
    protected Location locationBottomRight;
    protected float maxDistance;
    protected GeoPoint mapCenterPoint;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;

        bMapManager = new BMapManager(getApplication());
        bMapManager.init(BAIDU_MAP_KEY, this);

        actionBar = (ActionBar) findViewById(R.id.action_bar);

        helper = new Helper(context);
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

        GeoPoint geoPoint = getRealGeoPointFromLocation(location);
        if (animate) {
            mapViewController.animateTo(geoPoint);
        } else {
            mapViewController.setCenter(geoPoint);
        }
    }

    protected void setCenterPoint(Location location) {
        setCenterPoint(location, false);
    }

    protected GeoPoint getGeoPoint(Location location) {
        GeoPoint geoPoint = new GeoPoint(
            (int) (location.getLatitude() * 1E6),
            (int) (location.getLongitude() * 1E6)
        );

        return geoPoint;
    }

    protected GeoPoint getRealGeoPointFromLocation(Location location) {
        GeoPoint geoPoint = getGeoPoint(location);
        return CoordinateConvert.bundleDecode(CoordinateConvert.fromWgs84ToBaidu(geoPoint));
    }

    protected GeoPoint getRealGeoPointFromGeo(GeoPoint geoPoint) {
        return CoordinateConvert.bundleDecode(CoordinateConvert.fromWgs84ToBaidu(geoPoint));
    }

    @Override
    public void onDestroy() {
        bMapManager.destroy();
        super.onDestroy();
    }

    @Override
    public void onGetNetworkState(int iError) {
    }

    @Override
    public void onGetPermissionState(int iError) {
        if (iError == MKEvent.ERROR_PERMISSION_DENIED) {

        }
    }

    protected void getBoundary() {
        leftBoundary = locations.get(0).getLatitude();
        bottomBoundary = locations.get(0).getLongitude();

        rightBoundary = locations.get(0).getLatitude();
        topBoundary = locations.get(0).getLongitude();

        for (Location location : locations) {
            if (leftBoundary > location.getLatitude()) {
                leftBoundary = location.getLatitude();
            }

            if (rightBoundary < location.getLatitude()) {
                rightBoundary = location.getLatitude();
            }

            if (topBoundary < location.getLongitude()) {
                topBoundary = location.getLongitude();
            }

            if (bottomBoundary > location.getLongitude()) {
                bottomBoundary = location.getLongitude();
            }
        }

        locationTopLeft = new Location("");
        locationTopLeft.setLongitude(topBoundary);
        locationTopLeft.setLatitude(leftBoundary);

        locationBottomRight = new Location("");
        locationBottomRight.setLongitude(bottomBoundary);
        locationBottomRight.setLatitude(rightBoundary);

        maxDistance = locationTopLeft.distanceTo(locationBottomRight);
        mapCenterPoint = getRealGeoPointFromGeo(new GeoPoint(
            (int) ((leftBoundary + (rightBoundary - leftBoundary) / 2) * 1e6),
            (int) ((bottomBoundary + (topBoundary - bottomBoundary) / 2) * 1e6)
        ));
    }

    protected int getFixedZoomLevel() {
        int fixedLatitudeSpan = (int) ((rightBoundary - leftBoundary) * 1e6);
        int fixedLongitudeSpan = (int) ((topBoundary - bottomBoundary) * 1e6);

        for (int i = mapView.getMaxZoomLevel(); i > 0; i--) {
            mapViewController.setZoom(i);
            int latSpan = mapView.getLatitudeSpan();
            int longSpan = mapView.getLongitudeSpan();

            if (latSpan > fixedLatitudeSpan && longSpan > fixedLongitudeSpan) {
                return i;
            }
        }

        return mapView.getMaxZoomLevel();
    }
}
