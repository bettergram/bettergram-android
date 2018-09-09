package ru.johnlife.lifetools.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;

import ru.johnlife.lifetools.tools.Constrain;

/**
 * Layout that provides pinch-zooming of content. This view should have exactly one child
 * view containing the content.
 */
public class ZoomLayout extends FrameLayout {

    private ScaleGestureDetector scaleDetector;
    private boolean animate = false;

    private enum Mode {
        NONE,
        DRAG,
        ZOOM
    }

    private static final String TAG = "ZoomLayout";
    private static final float MIN_ZOOM = 1.0f;
    private static final float MAX_ZOOM = 4.0f;

    private Mode mode = Mode.NONE;
    private float scale = 1.0f;
    private float lastScaleFactor = 0f;

    // Where the finger first  touches the screen
    private float startX = 0f;
    private float startY = 0f;

    // How much to translate the canvas
    private float dx = 0f;
    private float dy = 0f;
    private float prevDx = 0f;
    private float prevDy = 0f;

    public ZoomLayout(Context context) {
        super(context);
        init(context);
    }

    public ZoomLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public ZoomLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    @SuppressLint("ClickableViewAccessibility")
    private void init(Context context) {
        // added logic to adjust dx and dy for pinch/zoom pivot point
        scaleDetector = new ScaleGestureDetector(context, new ScaleGestureDetector.OnScaleGestureListener() {
            @Override
            public boolean onScaleBegin(ScaleGestureDetector scaleDetector) {
                Log.i(TAG, "onScaleBegin");
                return true;
            }

            @Override
            public boolean onScale(ScaleGestureDetector scaleDetector) {
                zoom(scaleDetector.getScaleFactor());
                return true;
            }

            @Override
            public void onScaleEnd(ScaleGestureDetector scaleDetector) {
                Log.i(TAG, "onScaleEnd");
            }
        });
        GestureDetector detector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener(){
            @Override
            public boolean onDoubleTap(MotionEvent e) {
                animate = true;
                mode = Mode.ZOOM;
                zoom(scale > (MAX_ZOOM * .9) ? MIN_ZOOM / MAX_ZOOM : 2f);
                return super.onDoubleTap(e);
            }
        });
        this.setOnTouchListener((view, motionEvent) -> {
            animate = false;
            switch (motionEvent.getAction() & MotionEvent.ACTION_MASK) {
                case MotionEvent.ACTION_DOWN:
                    if (scale > MIN_ZOOM) {
                        mode = Mode.DRAG;
                        startX = motionEvent.getX() - prevDx;
                        startY = motionEvent.getY() - prevDy;
                    }
                    break;
                case MotionEvent.ACTION_MOVE:
                    if (mode == Mode.DRAG) {
                        dx = motionEvent.getX() - startX;
                        dy = motionEvent.getY() - startY;
                    }
                    break;
                case MotionEvent.ACTION_POINTER_DOWN:
                    mode = Mode.ZOOM;
                    break;
                case MotionEvent.ACTION_POINTER_UP:
                    mode = Mode.NONE; // changed from DRAG, was messing up zoom
                    break;
                case MotionEvent.ACTION_UP:
                    mode = Mode.NONE;
                    prevDx = dx;
                    prevDy = dy;
                    break;
            }
            scaleDetector.onTouchEvent(motionEvent);
            detector.onTouchEvent(motionEvent);
            if ((mode == Mode.DRAG && scale >= MIN_ZOOM) || mode == Mode.ZOOM) {
                getParent().requestDisallowInterceptTouchEvent(true);
                applyToChildren(child-> {
                    float maxDx = child.getWidth() * (scale - 1);  // adjusted for zero pivot
                    float maxDy = child.getHeight() * (scale - 1);  // adjusted for zero pivot
                    dx = Math.min(Math.max(dx, -maxDx), 0);  // adjusted for zero pivot
                    dy = Math.min(Math.max(dy, -maxDy), 0);  // adjusted for zero pivot
                    child.setPivotX(0f);  // default is to pivot at view center
                    child.setPivotY(0f);  // default is to pivot at view center
                    if (animate) {
                        child.animate().setInterpolator(new DecelerateInterpolator())
                            .scaleX(scale)
                            .scaleY(scale)
                            .translationX(dx)
                            .translationY(dy)
                            .start();
                    } else {
                        child.setScaleX(scale);
                        child.setScaleY(scale);
                        child.setTranslationX(dx);
                        child.setTranslationY(dy);
                    }
                });
                animate = false;
            }

            return true;
        });
    }

    private void zoom(float scaleFactor) {
        Log.i(TAG, "onScale(), scaleFactor = " + scaleFactor);
        if (lastScaleFactor == 0 || (Math.signum(scaleFactor) == Math.signum(lastScaleFactor))) {
            float prevScale = scale;
            scale *= scaleFactor;
            scale = Constrain.range(scale, MIN_ZOOM, MAX_ZOOM);
            lastScaleFactor = scaleFactor;
            float adjustedScaleFactor = scale / prevScale;
            // added logic to adjust dx and dy for pinch/zoom pivot point
            Log.d(TAG, "onScale, adjustedScaleFactor = " + adjustedScaleFactor);
            Log.d(TAG, "onScale, BEFORE dx/dy = " + dx + "/" + dy);
            float focusX = scaleDetector.getFocusX();
            float focusY = scaleDetector.getFocusY();
            Log.d(TAG, "onScale, focusX/focusy = " + focusX + "/" + focusY);
            dx += (dx - focusX) * (adjustedScaleFactor - 1);
            dy += (dy - focusY) * (adjustedScaleFactor - 1);
            Log.d(TAG, "onScale, dx/dy = " + dx + "/" + dy);
        } else {
            lastScaleFactor = 0;
        }
    }

    private interface ChildVisitor {
        void applyTo(View child);
    }

    private void applyToChildren(ChildVisitor visitor) {
        for (int i = 0; i < getChildCount(); i++) {
            visitor.applyTo(getChildAt(i));
        }
    }

    public void resetZoom() {
        scale = 1f;
        dx = 0;
        dy = 0;
        applyToChildren(child-> {
            child.setScaleX(scale);
            child.setScaleY(scale);
            child.setPivotX(0f);  // default is to pivot at view center
            child.setPivotY(0f);  // default is to pivot at view center
            child.setTranslationX(dx);
            child.setTranslationY(dy);
        });
    }
}