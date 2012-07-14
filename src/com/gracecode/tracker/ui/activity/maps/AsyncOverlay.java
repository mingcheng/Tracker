package com.gracecode.tracker.ui.activity.maps;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.location.Location;
import android.os.Handler;
import com.baidu.mapapi.*;
import com.gracecode.tracker.R;

import java.util.ArrayList;

public abstract class AsyncOverlay extends Overlay {
    private static final int OFFSET = 20;

    protected Context context;

    private Paint paint;
    private Bitmap mCalculationBitmap;
    private Canvas mCalculationCanvas;
    private Canvas mActiveCanvas;

    private MapView mapView;
    private Projection mProjection;

    private GeoPoint mGeoTopLeft;
    private GeoPoint mGeoBottomRight;

    private int mCalculationWidth = 1;
    private int mCalculationHeight = 1;

    protected ArrayList<Location> locations = new ArrayList<Location>();
    private Handler mHandler;

    private int mActiveZoomLevel = 0;

    public AsyncOverlay(Context context, MapView mapView, ArrayList<Location> locations, Handler handler) {
        this.context = context;
        this.locations = locations;
        this.mapView = mapView;
        this.mHandler = handler;
    }

    protected void reset() {
        synchronized (mapView) {
            mProjection = mapView.getProjection();

            mCalculationWidth = mapView.getWidth();
            mCalculationHeight = mapView.getHeight();

            mGeoTopLeft = mProjection.fromPixels(0, 0);
            mGeoBottomRight = new GeoPoint(mCalculationWidth, mCalculationHeight);

            mCalculationBitmap = Bitmap.createBitmap(mCalculationWidth, mCalculationHeight, Bitmap.Config.ARGB_8888);
            mCalculationCanvas = new Canvas(mCalculationBitmap);
        }
    }

    private boolean isOutAlignment(Point point) {
        return point.x < 0 || point.y < 0 ||
            point.x > mCalculationWidth || point.y > mCalculationHeight;
    }

    private void setPaint() {
        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setDither(true);

        paint.setColor(context.getResources().getColor(R.color.highlight));
        paint.setStyle(Paint.Style.FILL_AND_STROKE);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeWidth(8);
        paint.setAlpha(200);
    }

    protected boolean considerRedrawOffscreen() {
        int oldZoomLevel = mActiveZoomLevel;
        mActiveZoomLevel = mapView.getZoomLevel();

        boolean needNewCalculation = false;

        if (mCalculationBitmap == null || mCalculationBitmap.getWidth() != mCalculationWidth
            || mCalculationBitmap.getHeight() != mCalculationHeight) {
            reset();
            needNewCalculation = true;
        }

        if (needNewCalculation || mActiveZoomLevel != oldZoomLevel) {
            return true;
        }

        return false;
    }

    @Override
    public void draw(Canvas canvas, MapView mapView, boolean shadow) {
        this.mActiveCanvas = canvas;
        this.mapView = mapView;

        if (!shadow && considerRedrawOffscreen()) {
            synchronized (canvas) {
                new Thread(drawPath).run();
            }
        }
    }

    private Runnable drawPath = new Runnable() {
        @Override
        public void run() {
            Point lastGeoPoint = null;

            for (Location location : locations) {
                Point current =
                    mProjection.toPixels(getRealGeoPointFromLocation(location), null);

                if (lastGeoPoint != null && !isOutAlignment(current)) {
                    mActiveCanvas.drawLine(lastGeoPoint.x, lastGeoPoint.y, current.x, current.y, paint);
                } else {
                    mActiveCanvas.drawPoint(current.x, current.y, paint);
                }
                lastGeoPoint = current;
            }

            mActiveCanvas.drawBitmap(mCalculationBitmap, 0, 0, null);
        }
    };


    protected static GeoPoint getGeoPoint(Location location) {
        GeoPoint geoPoint = new GeoPoint(
            (int) (location.getLatitude() * 1E6),
            (int) (location.getLongitude() * 1E6)
        );

        return geoPoint;
    }

    protected static GeoPoint getRealGeoPointFromLocation(Location location) {
        GeoPoint geoPoint = getGeoPoint(location);
        return CoordinateConvert.bundleDecode(CoordinateConvert.fromWgs84ToBaidu(geoPoint));
    }
}
