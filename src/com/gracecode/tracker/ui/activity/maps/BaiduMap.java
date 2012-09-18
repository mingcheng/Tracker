package com.gracecode.tracker.ui.activity.maps;

import android.content.Context;
import android.content.Intent;
import android.graphics.*;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.SeekBar;
import android.widget.ToggleButton;
import com.baidu.mapapi.MapView;
import com.baidu.mapapi.Overlay;
import com.baidu.mapapi.Projection;
import com.gracecode.tracker.R;
import com.gracecode.tracker.dao.ArchiveMeta;
import com.gracecode.tracker.dao.Archiver;
import com.gracecode.tracker.ui.activity.Detail;
import com.gracecode.tracker.ui.activity.Records;
import com.gracecode.tracker.ui.activity.base.MapActivity;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class BaiduMap extends MapActivity implements SeekBar.OnSeekBarChangeListener {
    private Archiver archiver;

    private Context context;

    private String archiveFileName;
    private SeekBar mSeeker;
    private SimpleDateFormat dateFormat;
    private ToggleButton mSatellite;
    private View mapController;
    private PathOverlay pathOverlay;
    private PointMarkLayout currentMarkLayout;


    @Override
    public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
        // Helper.Logger.i("onProgressChanged");
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        // Helper.Logger.i("onStartTrackingTouch");
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        try {
            Location location = locations.get(seekBar.getProgress() - 1);
            String recordsFormatter = getString(R.string.records_formatter);

            helper.showShortToast(
                dateFormat.format(location.getTime()) + "\n" +
                    String.format(recordsFormatter, location.getSpeed() * ArchiveMeta.KM_PER_HOUR_CNT) + "km/h"
            );

            if (currentMarkLayout != null) {
                mapView.getOverlays().remove(currentMarkLayout);
            }
            currentMarkLayout = new PointMarkLayout(location, R.drawable.point);
            mapView.getOverlays().add(currentMarkLayout);

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
        mSeeker = (SeekBar) findViewById(R.id.seek);

        // Default date format.
        dateFormat = new SimpleDateFormat(getString(R.string.time_format), Locale.getDefault());

        archiver = new Archiver(getApplicationContext(), archiveFileName);
        locations = archiver.fetchAll();

        pathOverlay = new PathOverlay();

        // 计算边界
        getBoundary();
    }


    @Override
    public void onResume() {
//        actionBar.removeAllActions();
        super.onResume();
    }

    @Override
    public void onStart() {
        super.onStart();

        Intent intent = getIntent();
        if (intent.getBooleanExtra(Detail.INSIDE_TABHOST, false)) {
            if (actionBar != null) {
                actionBar.setVisibility(View.GONE);
            }
            mapController.setVisibility(View.GONE);
        }

        mSeeker.setMax(locations.size());
        mSeeker.setProgress(0);
        mSeeker.setOnSeekBarChangeListener(this);

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

        mapView.getOverlays().add(pathOverlay);
        mapView.getOverlays().add(new PointMarkLayout(archiver.getFirstRecord(), R.drawable.point_start));
        mapView.getOverlays().add(new PointMarkLayout(archiver.getLastRecord(), R.drawable.point_end));

        // @todo 自动计算默认缩放的地图界面
        mapViewController.setCenter(mapCenterPoint);
        mapViewController.setZoom(getFixedZoomLevel());
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
        archiver.close();
        super.onDestroy();
    }

    private class PathOverlay extends Overlay {
        private Paint paint;
        private Projection projection;
        private static final int MIN_POINT_SPAN = 5;

        public PathOverlay() {
            setPaint();
        }

        private void setPaint() {
            paint = new Paint();
            paint.setAntiAlias(true);
            paint.setDither(true);

            paint.setColor(getResources().getColor(R.color.highlight));
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeJoin(Paint.Join.ROUND);
            paint.setStrokeCap(Paint.Cap.ROUND);
            paint.setStrokeWidth(5);
            paint.setAlpha(188);
        }


        @Override
        public void draw(final Canvas canvas, final MapView mapView, boolean shadow) {
            this.projection = mapView.getProjection();

            if (!shadow) {
                synchronized (canvas) {
                    final Path path = new Path();
                    final int maxWidth = mapView.getWidth();
                    final int maxHeight = mapView.getHeight();

                    Point lastGeoPoint = null;
                    for (Location location : locations) {
                        Point current = projection.toPixels(getRealGeoPointFromLocation(location), null);

                        if (lastGeoPoint != null && (lastGeoPoint.y < maxHeight && lastGeoPoint.x < maxWidth)) {
/*                            if (Math.abs(current.x - lastGeoPoint.x) < MIN_POINT_SPAN
                                || Math.abs(current.y - lastGeoPoint.y) < MIN_POINT_SPAN) {
                                continue;
                            } else {*/
                            path.lineTo(current.x, current.y);
                            /*                   }*/
                        } else {
                            path.moveTo(current.x, current.y);
                        }
                        lastGeoPoint = current;
                    }

                    canvas.drawPath(path, paint);
                }
            }
        }
    }

    private class PointMarkLayout extends Overlay {
        private Location location;
        private int drawable;
        private Projection projection;

        PointMarkLayout(Location location, int drawable) {
            this.location = location;
            this.drawable = drawable;
        }

        @Override
        public void draw(final Canvas canvas, final MapView mapView, boolean shadow) {
            super.draw(canvas, mapView, shadow);

            this.projection = mapView.getProjection();
            Point point = projection.toPixels(getRealGeoPointFromLocation(location), null);

            Bitmap markerImage = BitmapFactory.decodeResource(getResources(), drawable);

            // 根据实际的条目而定偏移位置
            canvas.drawBitmap(markerImage,
                point.x - Math.round(markerImage.getWidth() * 0.4),
                point.y - Math.round(markerImage.getHeight() * 0.9), null);

            return;
        }
    }
}
