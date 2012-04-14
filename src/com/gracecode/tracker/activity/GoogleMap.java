package com.gracecode.tracker.activity;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.location.Location;
import android.os.Bundle;
import com.baidu.mapapi.*;
import com.gracecode.tracker.R;
import com.gracecode.tracker.dao.Archive;
import com.gracecode.tracker.util.UIHelper;

import java.io.IOException;
import java.util.ArrayList;

public class GoogleMap extends MapActivity {
    private static final String ARCHIVE_FILE_NAME = "archiveName";

    private MapView mapView;
    private MapController mapController;
    private Context context;
    private Archive archive;
    private ArrayList<Location> locations;
    private UIHelper uiHelper;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.google_map);

        mapView = (MapView) findViewById(R.id.google_mapview);
        mapController = mapView.getController();
        uiHelper = new UIHelper(context);

        context = getApplicationContext();
        try {
            archive = new Archive(context, getIntent().getStringExtra(ARCHIVE_FILE_NAME));
        } catch (IOException e) {
            uiHelper.showLongToast(getString(R.string.archive_not_exists));
            finish();
        }
        locations = archive.fetchAll();

        mapView.getOverlays().add(new WalkedOverlay());
    }

    @Override
    protected void onResume() {
        super.onResume();

        Location location = archive.getLastRecord();
        GeoPoint point = new GeoPoint((int) (location.getLatitude() * 1E6), (int) (location.getLongitude() * 1E6));

        mapView.setBuiltInZoomControls(true);

        mapController.setCenter(point);
        mapController.setZoom(14);
    }

    @Override
    protected boolean isRouteDisplayed() {
        return false;
    }

    private class WalkedOverlay extends Overlay {
        @Override
        public void draw(Canvas canvas, MapView mapView, boolean shadow) {
            Projection projection = mapView.getProjection();

            Location loc = archive.getLastRecord();
            GeoPoint geoPoint = new GeoPoint((int) (loc.getLatitude() * 1E6), (int) (loc.getLongitude() * 1E6));
            //geoPoint = CoordinateConvert.bundleDecode(CoordinateConvert.fromWgs84ToBaidu(geoPoint));

            Paint paint = new Paint();
            paint.setColor(Color.RED);
            paint.setStrokeWidth(4);

            int zoom = mapView.getZoomLevel();
            GeoPoint lastGeoPoint = geoPoint;
            for (int i = 0; i < locations.size(); i++) {
                Location x = locations.get(i);

                geoPoint = new GeoPoint((int) (x.getLatitude() * 1E6), (int) (x.getLongitude() * 1E6));
//                geoPoint = CoordinateConvert.bundleDecode(CoordinateConvert.fromWgs84ToBaidu(geoPoint));

                Point last = projection.toPixels(lastGeoPoint, null);

                Point current = projection.toPixels(geoPoint, null);
                canvas.drawLine(last.x, last.y, current.x, current.y, paint);

                lastGeoPoint = geoPoint;
            }

            //super.draw(canvas, mapView, shadow);
        }
    }


    public static double lngToPixel(double lng, int zoom) {
        return (lng + 180) * (256L << zoom) / 360;
    }

    public static double latToPixel(double lat, int zoom) {
        double siny = Math.sin(lat * Math.PI / 180);
        double y = Math.log((1 + siny) / (1 - siny));
        return (128 << zoom) * (1 - y / (2 * Math.PI));
    }
}
