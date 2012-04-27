package com.gracecode.tracker.activity;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.SeekBar;
import android.widget.Toast;
import android.widget.ToggleButton;
import com.baidu.mapapi.*;
import com.gracecode.tracker.R;
import com.gracecode.tracker.dao.Archive;
import com.gracecode.tracker.util.UIHelper;
import com.markupartist.android.widget.ActionBar;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class BaiduMap extends MapActivity implements SeekBar.OnSeekBarChangeListener {
    private Archive archive;
    private MapView mapView;
    private MapController mapViewController;
    private Context context;
    private ArrayList<Location> locations;
    private BMapManager bMapManager = null;
    private ActionBar actionBar;
    private UIHelper uiHelper;
    private String archiveFileName;
    private SeekBar mSeeker;
    private SimpleDateFormat dateFormat;
    private ToggleButton mSatellite;

    @Override
    protected boolean isRouteDisplayed() {
        return false;
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        try {
            Location location = locations.get(seekBar.getProgress() - 1);
            uiHelper.showShortToast(dateFormat.format(location.getTime()));
            setCenterPoint(location, true);
        } catch (IndexOutOfBoundsException e) {
            return;
        }
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
//
        super.initMapActivity(bMapManager);

        mapView = (MapView) findViewById(R.id.bmapsView);
        actionBar = (ActionBar) findViewById(R.id.action_bar);

        mapView.setBuiltInZoomControls(true);
        //mapView.setDrawOverlayWhenZooming(false);
        mapViewController = mapView.getController();
        mapView.setSatellite(false);

        mSeeker = (SeekBar) findViewById(R.id.seek);
        mSatellite = (ToggleButton) findViewById(R.id.satellite);

        uiHelper = new UIHelper(context);
        archiveFileName = getIntent().getStringExtra(Records.INTENT_ARCHIVE_FILE_NAME);
        //archiveFileName = "/mnt/sdcard/tracker/201204/1334727127367.sqlite";

        dateFormat = new SimpleDateFormat(getString(R.string.time_format), Locale.CHINA);

        archive = new Archive(getApplicationContext(), archiveFileName);
        locations = archive.fetchAll();
    }

    @Override
    public void onResume() {
//        if (bMapManager != null) {
//            bMapManager.start();
//        }

        actionBar.removeAllActions();
//        actionBar.addAction(new ActionBar.Action() {
//            @Override
//            public int getDrawable() {
//                return android.R.drawable.ic_delete;
//            }
//
//            @Override
//            public void performAction(View view) {

//            }
//        });

        super.onResume();
    }

    @Override
    public void onStart() {
        super.onStart();

        int size = locations.size();
        if (size <= 0) {
            return;
        }

        MyLocationOverlay myLocationOverlay = new MyLocationOverlay(context, mapView);
        myLocationOverlay.disableMyLocation();
        myLocationOverlay.disableCompass();

        mSeeker.setMax(locations.size());
        mSeeker.setProgress(0);
        mSeeker.setOnSeekBarChangeListener(this);

        mSatellite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mSatellite.isChecked()) {
                    mapView.setSatellite(true);
                } else {
                    mapView.setSatellite(false);
                }

                bMapManager.stop();
                bMapManager.start();
                uiHelper.showShortToast(getString(R.string.toggle_satellite));
            }
        });

        Location firstLocation = locations.get(0);
//        Location lastLocation = locations.get(size - 1);
//        Location centerLocation = locations.get(size / 2);


        // mapView.getOverlays().add(new RouteOverlay());
        // mapView.getOverlays().add(myLocationOverlay);

        Drawable marker = getResources().getDrawable(R.drawable.mark);
        mapView.getOverlays().add(new RouteItemizedOverlay(marker, context));

        //float distance = firstLocation.distanceTo(lastLocation);
        // @todo 自动计算默认缩放的地图界面
        setCenterPoint(firstLocation, false);
        mapViewController.setZoom(16);
    }

    @Override
    public void onPause() {
//        if (bMapManager != null) {
//            bMapManager.stop();
//        }
        super.onResume();
    }

    @Override
    public void onStop() {
        mapView.getOverlays().clear();
        super.onStop();
    }

    @Override
    public void onDestroy() {
//        if (bMapManager != null) {
//            bMapManager.destroy();
//        }

        archive.close();
        super.onDestroy();
    }

    class RouteItemizedOverlay extends ItemizedOverlay<OverlayItem> {
        Bitmap bitmap = null;

        private List<OverlayItem> geoPointList = new ArrayList<OverlayItem>();
        private Paint paint;

        public RouteItemizedOverlay(Drawable marker, Context context) {
            super(boundCenterBottom(marker));

            for (int i = 0; i < locations.size(); i++) {
                Location x = locations.get(i);
                GeoPoint geoPoint = new GeoPoint((int) (x.getLatitude() * 1E6), (int) (x.getLongitude() * 1E6));
                geoPoint = CoordinateConvert.bundleDecode(CoordinateConvert.fromWgs84ToBaidu(geoPoint));

                geoPointList.add(new OverlayItem(geoPoint, x.getLatitude() + "", x.getLongitude() + ""));
            }


//            paint = new Paint();
//            paint.setAntiAlias(true);
//            paint.setColor(Color.RED);
//            paint.setAlpha(95);
//            paint.setStrokeWidth(6);

            populate();
        }

//        @Override
//        public void draw(Canvas canvas, MapView mapView, boolean shadow) {
//            Projection projection = mapView.getProjection();
//
//            GeoPoint lastGeoPoint = null;
//            int maxWidth = mapView.getWidth();
//            int maxHeight = mapView.getHeight();
//            bitmap = Bitmap.createBitmap(maxWidth, maxHeight, Bitmap.Config.ARGB_8888);
//
//            Canvas tmpCanvas = new Canvas(bitmap);
//            for (int i = 0; i < locations.size(); i++) {
//                Location x = locations.get(i);
//
//                GeoPoint geoPoint = new GeoPoint((int) (x.getLatitude() * 1E6), (int) (x.getLongitude() * 1E6));
//                geoPoint = CoordinateConvert.bundleDecode(CoordinateConvert.fromWgs84ToBaidu(geoPoint));
//
//                Point current = projection.toPixels(geoPoint, null);
//                if (lastGeoPoint != null) {
//                    Point last = projection.toPixels(lastGeoPoint, null);
//                    if (last.y < maxHeight && last.x < maxWidth) {
//                        tmpCanvas.drawLine(last.x, last.y, current.x, current.y, paint);
//                    }
//                } else {
//                    tmpCanvas.drawPoint(current.x, current.y, paint);
//                }
//
//                lastGeoPoint = geoPoint;
//            }
//
//            canvas.drawBitmap(bitmap, 0, 0, null);
//        }


        @Override
        protected OverlayItem createItem(int i) {
            return geoPointList.get(i);
        }

        @Override
        public int size() {
            return geoPointList.size();
        }


        @Override
        protected boolean onTap(int i) {
            Location location = locations.get(i);
            uiHelper.showShortToast(dateFormat.format(location.getTime()));
            mSeeker.setProgress(i);
            setCenterPoint(location, true);
            return true;
        }
    }


    private void setCenterPoint(Location location, boolean animate) {
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

    private void setCenterPoint(Location location) {
        setCenterPoint(location, false);
    }
}
