package com.gracecode.tracker.activity;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import com.baidu.mapapi.*;
import com.gracecode.tracker.R;
import com.gracecode.tracker.dao.Archive;
import com.gracecode.tracker.util.UIHelper;
import com.markupartist.android.widget.ActionBar;

import java.io.File;
import java.util.ArrayList;

public class BaiduMap extends MapActivity {
    private static final String ARCHIVE_FILE_NAME = "archiveName";
    private String archiveFileName;
    private Archive archive;
    private MapView mapView;
    private MapController mapViewController;
    private Context context;
    private ArrayList<Location> locations;
    private BMapManager bMapManager = null;
    private ActionBar actionBar;
    private UIHelper uiHelper;

    @Override
    protected boolean isRouteDisplayed() {
        return false;
    }

    // 常用事件监听，用来处理通常的网络错误，授权验证错误等
    class MyGeneralListener implements MKGeneralListener {
        @Override
        public void onGetNetworkState(int iError) {
            Toast.makeText(context, "您的网络出错啦！", Toast.LENGTH_LONG).show();
        }

        @Override
        public void onGetPermissionState(int iError) {
            if (iError == MKEvent.ERROR_PERMISSION_DENIED) {
                Toast.makeText(context, "请输入正确的授权 KEY！",
                    Toast.LENGTH_LONG).show();
            }
        }
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.baidu_map);

        context = this;

        bMapManager = new BMapManager(getApplication());
        bMapManager.init("30183AD8A6AFE7CE8F649ED4CD258211E8DE78D7", new MyGeneralListener());

        super.initMapActivity(bMapManager);

        mapView = (MapView) findViewById(R.id.bmapsView);
        actionBar = (ActionBar) findViewById(R.id.action_bar);

        mapView.setBuiltInZoomControls(true);
        mapViewController = mapView.getController();

        uiHelper = new UIHelper(context);
        archiveFileName = getIntent().getStringExtra(ARCHIVE_FILE_NAME);
        archive = new Archive(getApplicationContext(), archiveFileName);

        locations = archive.fetchAll();


        actionBar.addAction(new ActionBar.Action() {
            @Override
            public int getDrawable() {
                return R.drawable.ic_menu_stop;
            }

            @Override
            public void performAction(View view) {
                uiHelper.showConfirmDialog("Delete", "Sure?", new Runnable() {
                        @Override
                        public void run() {
                            File archiveFile = new File(archiveFileName);
                            archiveFile.delete();
                            finish();
                        }
                    }, new Runnable() {
                        @Override
                        public void run() {

                        }
                    }
                );
            }
        });

    }

    @Override
    public void onResume() {
        if (bMapManager != null) {
            bMapManager.start();
        }
        super.onResume();
    }

    @Override
    public void onStart() {
        super.onStart();

        int size = locations.size();
        Location firstLocation = locations.get(0);
//        Location lastLocation = locations.get(size - 1);
//        Location centerLocation = locations.get(size / 2);

        mapView.getOverlays().add(new RouteOverlay());

        GeoPoint firstLocationPoint = new GeoPoint(
            (int) (firstLocation.getLatitude() * 1E6),
            (int) (firstLocation.getLongitude() * 1E6)
        );

        //float distance = firstLocation.distanceTo(lastLocation);

        // @todo 自动计算默认缩放的地图界面
        mapViewController.setCenter(firstLocationPoint);
        mapViewController.setZoom(13);
    }

    @Override
    public void onPause() {
        if (bMapManager != null) {
            bMapManager.stop();
        }
        super.onResume();
    }

    @Override
    public void onDestroy() {
        if (bMapManager != null) {
            bMapManager.destroy();
        }

        archive.close();
        super.onDestroy();
    }

    private class RouteOverlay extends Overlay {
        @Override
        public void draw(Canvas canvas, MapView mapView, boolean shadow) {
            Projection projection = mapView.getProjection();

            Location loc = archive.getLastRecord();
            GeoPoint geoPoint = new GeoPoint((int) (loc.getLatitude() * 1E6), (int) (loc.getLongitude() * 1E6));
            geoPoint = CoordinateConvert.bundleDecode(CoordinateConvert.fromWgs84ToBaidu(geoPoint));

            Paint paint = new Paint();
            paint.setAntiAlias(true);
            paint.setColor(Color.RED);
            paint.setStrokeWidth(4);

            int offset = 0;
            GeoPoint lastGeoPoint = geoPoint;
            for (int i = 0; i < locations.size(); i++) {
                Location x = locations.get(i);

                geoPoint = new GeoPoint((int) (x.getLatitude() * 1E6), (int) (x.getLongitude() * 1E6));
                geoPoint = CoordinateConvert.bundleDecode(CoordinateConvert.fromWgs84ToBaidu(geoPoint));

                Point last = projection.toPixels(lastGeoPoint, null);

                Point current = projection.toPixels(geoPoint, null);
                canvas.drawLine(last.x + offset, last.y + offset, current.x + offset, current.y + offset, paint);

                lastGeoPoint = geoPoint;
            }

            super.draw(canvas, mapView, shadow);
        }
    }

}
