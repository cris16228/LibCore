package com.github.cris16228.libcore.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

import androidx.annotation.NonNull;

import com.github.cris16228.libcore.R;

public class ZoomImageView extends androidx.appcompat.widget.AppCompatImageView {

    // We can be in one of these 3 states
    static final int NONE = 0;
    static final int DRAG = 1;
    static final int ZOOM = 2;
    static final int CLICK = 3;
    private final int CLICK_THRESHOLD = 20;
    public int mode = NONE;
    protected float origWidth, origHeight;
    Matrix matrix;
    PointF last = new PointF();
    PointF start = new PointF();
    float minScale = 1f;
    float maxScale = 5f;
    float maxDifference = 0.15f;
    float[] m;
    int viewWidth, viewHeight;
    float currentScale = 1f;
    int oldMeasuredWidth, oldMeasuredHeight;
    private OnTouchEvent onTouchEvent;
    private GestureDetector gestureDetector;
    ScaleGestureDetector mScaleDetector;
    Context context;

    public ZoomImageView(Context context) {
        super(context);
        sharedConstructing(context);
    }

    public ZoomImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.ZoomImageView);
        try {
            minScale = typedArray.getFloat(R.styleable.ZoomImageView_minScale, minScale);
            maxScale = typedArray.getFloat(R.styleable.ZoomImageView_maxScale, maxScale);
            maxDifference = typedArray.getFloat(R.styleable.ZoomImageView_maxDifference, maxDifference);
        } finally {
            typedArray.recycle();
        }
        sharedConstructing(context);
    }

    public void setOnTouchEvent(com.github.cris16228.libcore.view.ZoomImageView.OnTouchEvent onTouchEvent) {
        this.onTouchEvent = onTouchEvent;
    }

    private void stopInterceptEvent() {
        if (getParent() != null) getParent().requestDisallowInterceptTouchEvent(true);
    }

    private void startInterceptEvent() {
        if (getParent() != null) getParent().requestDisallowInterceptTouchEvent(false);
    }

    private void sharedConstructing(Context context) {
        super.setClickable(true);
        this.context = context;
        mScaleDetector = new ScaleGestureDetector(context, new ScaleListener());
        matrix = new Matrix();
        m = new float[9];
        setImageMatrix(matrix);
        setScaleType(ScaleType.MATRIX);

        gestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onDoubleTap(@NonNull MotionEvent e) {
                if (currentScale == minScale) {
                    currentScale = maxScale;
                    matrix.setScale(maxScale, maxScale, viewWidth / 2f, viewHeight / 2f);
                } else {
                    currentScale = minScale;
                    matrix.setScale(minScale, minScale, viewWidth / 2f, viewHeight / 2f);
                }
                fixTrans();
                setImageMatrix(matrix);
                invalidate();
                return true;
            }
        });

        setOnTouchListener((v, event) -> {
            mScaleDetector.onTouchEvent(event);
            gestureDetector.onTouchEvent(event);
            PointF curr = new PointF(event.getX(), event.getY());
            if (onTouchEvent != null) {
                onTouchEvent.onTouchEvent(event);
            }
            switch (event.getAction() & event.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                    last.set(curr);
                    start.set(last);
                    mode = DRAG;
                    stopInterceptEvent();
                    performClick();
                    break;

                case MotionEvent.ACTION_MOVE:
                    stopInterceptEvent();
                    if (mode == DRAG) {
                        float deltaX = curr.x - last.x;
                        float deltaY = curr.y - last.y;
                        float fixTransX = getFixDragTrans(deltaX, viewWidth,
                                origWidth * currentScale);
                        float fixTransY = getFixDragTrans(deltaY, viewHeight,
                                origHeight * currentScale);
                        matrix.postTranslate(fixTransX, fixTransY);
                        fixTrans();
                        last.set(curr.x, curr.y);
                        if (mScaleDetector.getScaleFactor() == currentScale)
                            startInterceptEvent();
                        else
                            stopInterceptEvent();
                    }
                    break;

                case MotionEvent.ACTION_UP:
                    mode = NONE;
                    if (currentScale <= minScale + maxDifference) {
                        currentScale = minScale;
                        Drawable drawable = getDrawable();
                        if (drawable != null) {
                            int bmWidth = drawable.getIntrinsicWidth();
                            int bmHeight = drawable.getIntrinsicHeight();
                            float scaleX = (float) viewWidth / bmWidth;
                            float scaleY = (float) viewHeight / bmHeight;
                            float scale = Math.min(scaleX, scaleY);
                            matrix.setScale(scale, scale);
                            float scaledWidth = scale * bmWidth;
                            float scaledHeight = scale * bmHeight;
                            float translateX = (viewWidth - scaledWidth) / 2;
                            float translateY = (viewHeight - scaledHeight) / 2;
                            matrix.postTranslate(translateX, translateY);
                        }
                    }
                    int xDiff = (int) Math.abs(curr.x - start.x);
                    int yDiff = (int) Math.abs(curr.y - start.y);
                    if (xDiff < CLICK_THRESHOLD || yDiff < CLICK_THRESHOLD) {
                        View parent = (View) getParent();
                        parent.performClick();
                    }
                    startInterceptEvent();
                    break;
                case MotionEvent.ACTION_POINTER_UP:
                    mode = NONE;
                    break;
            }

            setImageMatrix(matrix);
            invalidate();
            return true; // indicate event was handled
        });
    }

    void fixTrans() {
        matrix.getValues(m);
        float transX = m[Matrix.MTRANS_X];
        float transY = m[Matrix.MTRANS_Y];

        float fixTransX = getFixTrans(transX, viewWidth, origWidth * currentScale);
        float fixTransY = getFixTrans(transY, viewHeight, origHeight
                * currentScale);

        if (fixTransX != 0 || fixTransY != 0)
            matrix.postTranslate(fixTransX, fixTransY);
    }

    float getFixTrans(float trans, float viewSize, float contentSize) {
        float minTrans, maxTrans;

        if (contentSize <= viewSize) {
            minTrans = 0;
            maxTrans = viewSize - contentSize;
        } else {
            minTrans = viewSize - contentSize;
            maxTrans = 0;
        }

        if (trans < minTrans)
            return -trans + minTrans;
        if (trans > maxTrans)
            return -trans + maxTrans;
        return 0;
    }

    float getFixDragTrans(float delta, float viewSize, float contentSize) {
        if (contentSize <= viewSize) {
            return 0;
        }
        return delta;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        viewWidth = MeasureSpec.getSize(widthMeasureSpec);
        viewHeight = MeasureSpec.getSize(heightMeasureSpec);

        if (oldMeasuredHeight == viewWidth && oldMeasuredHeight == viewHeight
                || viewWidth == 0 || viewHeight == 0)
            return;
        oldMeasuredHeight = viewHeight;
        oldMeasuredWidth = viewWidth;

        if (currentScale == 1) {
            // Fit to screen.
            float scale;

            Drawable drawable = getDrawable();
            if (drawable == null || drawable.getIntrinsicWidth() == 0
                    || drawable.getIntrinsicHeight() == 0)
                return;
            int bmWidth = drawable.getIntrinsicWidth();
            int bmHeight = drawable.getIntrinsicHeight();

            float scaleX = (float) viewWidth / (float) bmWidth;
            float scaleY = (float) viewHeight / (float) bmHeight;
            scale = Math.min(scaleX, scaleY);
            matrix.setScale(scale, scale);

            //Center the image
            float redundantYSpace = (float) viewHeight - (scale * (float) bmHeight);
            float redundantXSpace = (float) viewWidth - (scale * (float) bmWidth);
            redundantYSpace /= (float) 2;
            redundantXSpace /= (float) 2;

            matrix.postTranslate(redundantXSpace, redundantYSpace);

            origWidth = viewWidth - 2 * redundantXSpace;
            origHeight = viewHeight - 2 * redundantYSpace;
            setImageMatrix(matrix);
        }
        fixTrans();
    }

    public interface OnTouchEvent {
        void onTouchEvent(MotionEvent event);
    }

    private class ScaleListener extends
            ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScaleBegin(@NonNull ScaleGestureDetector detector) {
            mode = ZOOM;
            return true;
        }

        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            float mScaleFactor = detector.getScaleFactor();
            float origScale = currentScale;
            currentScale *= mScaleFactor;
            if (currentScale > maxScale) {
                currentScale = maxScale;
                mScaleFactor = maxScale / origScale;
            } else if (currentScale < minScale) {
                currentScale = minScale;
                mScaleFactor = minScale / origScale;
            }

            if (origWidth * currentScale <= viewWidth
                    || origHeight * currentScale <= viewHeight)
                matrix.postScale(mScaleFactor, mScaleFactor, viewWidth / 2f,
                        viewHeight / 2f);
            else
                matrix.postScale(mScaleFactor, mScaleFactor,
                        detector.getFocusX(), detector.getFocusY());

            fixTrans();
            return true;
        }
    }
}