package com.example.vlad.paint;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by vlad on 24/10/17.
 */

public class PaintView extends View {
    private Context context;

    private int tool = Tool.BRUSH;

    private float xZoomCenter;
    private float yZoomCenter;
    private ScaleGestureDetector mScaleDetector;
    private float mScaleFactor = 1.f;


    private static int color = Color.BLACK;
    private int strokeWidth = 5;
    private int pathId = 0;

    private Point helpPoint;
    private Path helpPath;

    private Bitmap bitmap;
    private Canvas bitmapCanvas;
    private final Paint paintScreen;

    private final Map<Integer, Path> pathMap = new HashMap<>();
    private final Map<Integer, Paint> paintMap = new HashMap<>();



    public PaintView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;

        paintScreen = new Paint();

        mScaleDetector = new ScaleGestureDetector(context, new ScaleListener());
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        // if app was started by launch icon
        if (bitmap == null) {
            clearBitmap();
        }

        bitmap = Bitmap.createScaledBitmap(bitmap, getWidth(), getHeight(), true);
        bitmapCanvas = new Canvas(bitmap);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.save();
        canvas.scale(mScaleFactor, mScaleFactor, xZoomCenter, yZoomCenter);

        canvas.drawBitmap(bitmap, 0, 0, paintScreen);
        Object[] keys = pathMap.keySet().toArray();
        Arrays.sort(keys);
        for (Object key : keys) {
            Path path = pathMap.get(key);
            Paint paintLine = paintMap.get(key);
            canvas.drawPath(path, paintLine);
        }

        canvas.restore();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getActionMasked();

        if (tool == Tool.MOVE) {
            mScaleDetector.onTouchEvent(event);
            return true;
        }

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                touchStarted(event);
                break;
            case MotionEvent.ACTION_MOVE:
                touchMoved(event);
                break;
            case MotionEvent.ACTION_UP:
                touchEnded(event);
                break;
            case MotionEvent.ACTION_CANCEL:
                undo();
                break;
        }

        invalidate();
        return true;
    }

    private void touchStarted(MotionEvent event) {
        // calculate current center coordinates
        xZoomCenter = getWidth() / 2;
        yZoomCenter = getHeight() / 2;

        Path path = new Path();
        pathId++;

        Paint paintLine = new Paint();
        paintLine.setAntiAlias(true);
        paintLine.setColor(color);
        paintLine.setStyle(Paint.Style.STROKE);
        paintLine.setStrokeWidth(strokeWidth);
        paintLine.setStrokeCap(Paint.Cap.ROUND);

        float deltaCenterX = event.getX() - getWidth() / 2;
        float deltaCenterY = event.getY() - getHeight() / 2;

        switch (tool) {
            case Tool.BRUSH:
                helpPath = new Path();
                helpPath.moveTo(xZoomCenter + deltaCenterX / mScaleFactor, yZoomCenter + deltaCenterY / mScaleFactor);
                break;
            default:
                Point point = new Point();
                point.set((int) (xZoomCenter + deltaCenterX / mScaleFactor),
                        (int) (yZoomCenter + deltaCenterY / mScaleFactor));
                helpPoint = point;

        }

        pathMap.put(pathId, path);
        paintMap.put(pathId, paintLine);
    }

    private void touchMoved(MotionEvent event) {
        Path path = new Path();

        float deltaCenterX = event.getX() - getWidth() / 2;
        float deltaCenterY = event.getY() - getHeight() / 2;

        switch (tool) {
            case Tool.BRUSH:
                path = helpPath;

                path.lineTo(xZoomCenter + deltaCenterX / mScaleFactor,
                        yZoomCenter + deltaCenterY / mScaleFactor);
                pathMap.put(pathId, path);
                break;
            case Tool.OVAL:
                path.addOval(helpPoint.x, helpPoint.y,
                        xZoomCenter + deltaCenterX / mScaleFactor,
                        yZoomCenter + deltaCenterY / mScaleFactor,
                        Path.Direction.CW);
                pathMap.put(pathId, path);
                break;
            case Tool.RECTANGLE:
                float endPointX = xZoomCenter + deltaCenterX / mScaleFactor;
                float endPointY = yZoomCenter + deltaCenterY / mScaleFactor;

                if (endPointX > helpPoint.x && endPointY > helpPoint.y) {
                    path.addRect(helpPoint.x, helpPoint.y,
                            endPointX,
                            endPointY,
                            Path.Direction.CW);
                    pathMap.put(pathId, path);
                } else if (endPointX > helpPoint.x) {
                    path.addRect(helpPoint.x, endPointY,
                            endPointX,
                            helpPoint.y,
                            Path.Direction.CW);
                    pathMap.put(pathId, path);
                } else if (endPointY > helpPoint.y) {
                    path.addRect(endPointX, helpPoint.y,
                            helpPoint.x,
                            endPointY,
                            Path.Direction.CW);
                    pathMap.put(pathId, path);
                } else if (endPointY < helpPoint.y) {
                    path.addRect(endPointX, endPointY,
                            helpPoint.x,
                            helpPoint.y,
                            Path.Direction.CW);
                    pathMap.put(pathId, path);
                }


                break;
        }
    }


    private void touchEnded(MotionEvent event) {
        // may be in future
    }

    public void undo() {
        pathMap.remove(pathId);
        pathId--;
        if (pathId < 0) pathId = 0;

        invalidate();
    }

    public void setTool(int toolCode) {
        tool = toolCode;
    }

    public void setColor(int colorCode) {
        color = colorCode;
    }

    public void setBackground(Bitmap image) {
        if (getHeight() > 0 && getWidth() > 0)
            image = Bitmap.createScaledBitmap(image, getWidth(), getHeight(), true);
        bitmap = image;
        bitmapCanvas = new Canvas(bitmap);

        // clear old
        paintMap.clear();
        pathMap.clear();
        pathId = 0;
    }

    public Bitmap getImage() {
        for (Integer key : pathMap.keySet()) {
            Path path = pathMap.get(key);
            Paint paintLine = paintMap.get(key);
            bitmapCanvas.drawPath(path, paintLine);
        }
        return bitmap;
    }

    private class ScaleListener
            extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {

            xZoomCenter = detector.getFocusX();
            yZoomCenter = detector.getFocusY();

            mScaleFactor *= detector.getScaleFactor();

            // Don't let the object get too small or too large.
            mScaleFactor = Math.max(1f, Math.min(mScaleFactor, 5.0f));

            invalidate();
            return true;
        }
    }

    public void clear() {
        pathId = 0;
        pathMap.clear();
        paintMap.clear();
        mScaleFactor = 1f;
        clearBitmap();
        invalidate();
    }

    private void clearBitmap() {
        bitmap = Bitmap.createBitmap(getWidth(), getHeight(),
                Bitmap.Config.ARGB_8888);
        bitmap.eraseColor(Color.WHITE);
        bitmapCanvas = new Canvas(bitmap);
    }

    public int getColor() {
        return color;
    }

    public int getTool() {
        return tool;
    }

    public void setStrokeWidth(int width) {
        strokeWidth = width;
    }

    public int getStrokeWidth() {
        return strokeWidth;
    }

}
