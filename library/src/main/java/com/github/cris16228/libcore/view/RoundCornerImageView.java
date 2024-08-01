package com.github.cris16228.libcore.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.widget.ImageView;

import androidx.annotation.Nullable;

import com.github.cris16228.libcore.R;

public class RoundCornerImageView extends ImageView {

    public static final int CORNER_NONE = 0;
    public static final int CORNER_TOP_LEFT = 1;
    public static final int CORNER_TOP_RIGHT = 2;
    public static final int CORNER_BOTTOM_RIGHT = 4;
    public static final int CORNER_BOTTOM_LEFT = 8;
    public static final int CORNER_ALL = 15;

    private final RectF cornerRect = new RectF();
    private final Path path = new Path();
    private int cornerRadius;
    private int roundedCorners;

    public RoundCornerImageView(Context context) {
        super(context);
    }

    public RoundCornerImageView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public RoundCornerImageView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.RoundCornerImageView);
        cornerRadius = a.getDimensionPixelSize(R.styleable.RoundCornerImageView_cornerRadius, 0);
        roundedCorners = a.getInt(R.styleable.RoundCornerImageView_roundedCorners, CORNER_NONE);
        a.recycle();
    }

    public int getCornerRadius() {
        return cornerRadius;
    }

    public void setCornerRadius(int cornerRadius) {
        this.cornerRadius = cornerRadius;
        setPath();
        invalidate();
    }

    public int getRoundedCorners() {
        return roundedCorners;
    }

    public void setRoundedCorners(int roundedCorners) {
        this.roundedCorners = roundedCorners;
        setPath();
        invalidate();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        setPath();
    }

    private void setPath() {
        path.rewind();

        if (cornerRadius >= 1f && roundedCorners != CORNER_NONE) {
            final int width = getWidth();
            final int height = getHeight();
            final float twoRounds = cornerRadius * 2;
            cornerRect.set(-cornerRadius, -cornerRadius, cornerRadius, cornerRadius);
            if (isRounded(CORNER_TOP_LEFT)) {
                cornerRect.offsetTo(0f, 0f);
                path.arcTo(cornerRect, 180f, 90f);
            } else {
                path.moveTo(0f, 0f);
            }
            if (isRounded(CORNER_TOP_RIGHT)) {
                cornerRect.offsetTo(width - twoRounds, 0f);
                path.arcTo(cornerRect, 270f, 90f);
            } else {
                path.lineTo(width, 0f);
            }
            if (isRounded(CORNER_BOTTOM_RIGHT)) {
                cornerRect.offsetTo(width - twoRounds, height - twoRounds);
                path.arcTo(cornerRect, 0f, 90f);
            } else {
                path.lineTo(0f, height);
            }
            if (isRounded(CORNER_BOTTOM_LEFT)) {
                cornerRect.offsetTo(0f, height - twoRounds);
                path.arcTo(cornerRect, 90f, 90f);
            } else {
                path.lineTo(0f, height);
            }
            path.close();
        }
    }

    private boolean isRounded(int corner) {
        return (roundedCorners & corner) == corner;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (!path.isEmpty())
            canvas.clipPath(path);
        super.onDraw(canvas);
    }
}
