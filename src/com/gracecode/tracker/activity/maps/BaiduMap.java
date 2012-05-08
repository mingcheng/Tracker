package com.gracecode.tracker.activity.maps;

import android.content.Context;
import android.graphics.*;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.SeekBar;
import android.widget.ToggleButton;
import com.baidu.mapapi.ItemizedOverlay;
import com.baidu.mapapi.MapView;
import com.baidu.mapapi.OverlayItem;
import com.baidu.mapapi.Projection;
import com.gracecode.tracker.R;
import com.gracecode.tracker.activity.Records;
import com.gracecode.tracker.activity.base.MapActivity;
import com.gracecode.tracker.dao.Archive;
import com.gracecode.tracker.util.Helper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class BaiduMap extends MapActivity implements SeekBar.OnSeekBarChangeListener {
    private Archive archive;

    private Context context;
    private ArrayList<Location> locations;

    private String archiveFileName;
    private SeekBar mSeeker;
    private SimpleDateFormat dateFormat;
    private ToggleButton mSatellite;
    private View mapController;

    @Override
    public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        try {
            Location location = locations.get(seekBar.getProgress() - 1);
            helper.showShortToast(dateFormat.format(location.getTime()));
            setCenterPoint(location, true);
        } catch (IndexOutOfBoundsException e) {
            return;
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.baidu_map);

        context = this;

        mapView = (MapView) findViewById(R.id.bmapsView);
        mapController = findViewById(R.id.map_controller);
        archiveFileName = getIntent().getStringExtra(Records.INTENT_ARCHIVE_FILE_NAME);

//        mapView.setBuiltInZoomControls(true);
//        mapView.setSatellite(false);

        mSeeker = (SeekBar) findViewById(R.id.seek);
        mSatellite = (ToggleButton) findViewById(R.id.satellite);

        dateFormat = new SimpleDateFormat(getString(R.string.time_format), Locale.CHINA);

        archive = new Archive(getApplicationContext(), archiveFileName);
        locations = archive.fetchAll();
    }

    @Override
    public void onResume() {
//        actionBar.removeAllActions();
        super.onResume();
    }

    @Override
    public void onStart() {
        super.onStart();

        if (actionBar != null) {
            actionBar.setVisibility(View.GONE);
        }
        mapController.setVisibility(View.GONE);

//
//        mSeeker.setMax(locations.size());
//        mSeeker.setProgress(0);
//        mSeeker.setOnSeekBarChangeListener(this);

//        mSatellite.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                if (mSatellite.isChecked()) {
//                    mapView.setSatellite(true);
//                } else {
//                    mapView.setSatellite(false);
//                }
//
//                bMapManager.stop();
//                bMapManager.start();
//                helper.showShortToast(getString(R.string.toggle_satellite));
//            }
//        });

        Location firstLocation = archive.getFirstRecord();

        Drawable marker = getResources().getDrawable(R.drawable.mark);
        mapView.getOverlays().add(new RouteItemizedOverlay(marker, context));

        //float distance = firstLocation.distanceTo(lastLocation);
        // @todo 自动计算默认缩放的地图界面
        setCenterPoint(firstLocation, false);
        mapViewController.setZoom(14);
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onStop() {
        mapView.getOverlays().clear();
        super.onStop();
    }

    @Override
    public void onDestroy() {
        archive.close();
        super.onDestroy();
    }

    class RouteItemizedOverlay extends ItemizedOverlay<OverlayItem> {
        Bitmap bitmap = null;

        private List<OverlayItem> geoPointList = new ArrayList<OverlayItem>();
        private Paint paint;
        private Projection projection;
        private Canvas canvas;

        public RouteItemizedOverlay(Drawable marker, Context context) {
            super(boundCenterBottom(marker));
//
//            for (int i = 0; i < locations.size(); i++) {
//                Location x = locations.get(i);
//                GeoPoint geoPoint = getRealGeoPointFromLocation(x);
//                geoPointList.add(new OverlayItem(geoPoint, x.getLatitude() + "", x.getLongitude() + ""));
//            }

            paint = new Paint();
            paint.setAntiAlias(true);
            paint.setDither(true);

            paint.setColor(getResources().getColor(R.color.highlight));
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeJoin(Paint.Join.ROUND);
            paint.setStrokeCap(Paint.Cap.ROUND);
            paint.setStrokeWidth(7);
            paint.setAlpha(188);
            populate();
        }

        @Override
        public void draw(Canvas canvas, MapView mapView, boolean shadow) {
            this.projection = mapView.getProjection();
            this.canvas = canvas;
            if (!shadow) {
                synchronized (canvas) {
                    int maxWidth = mapView.getWidth();
                    int maxHeight = mapView.getHeight();

                    Helper.Logger.e("Latitude Span: " + mapView.getLatitudeSpan());
                    Helper.Logger.e("Longitude Span: " + mapView.getLongitudeSpan());
                    Helper.Logger.e("Zoom Level: " + mapView.getZoomLevel());

                    Path path = new Path();
                    Point lastGeoPoint = null;
                    for (Location location : locations) {
                        Point current = projection.toPixels(getRealGeoPointFromLocation(location), null);
                        if (lastGeoPoint != null && (lastGeoPoint.y < maxHeight && lastGeoPoint.x < maxWidth)) {
                            path.lineTo(current.x, current.y);
                        } else {
                            path.moveTo(current.x, current.y);
                        }
                        lastGeoPoint = current;
                    }

                    canvas.drawPath(path, paint);
                }
            }
        }

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
            helper.showShortToast(dateFormat.format(location.getTime()));
            mSeeker.setProgress(i);
            setCenterPoint(location, true);
            return true;
        }
    }
}
