package com.github.cris16228.libcore.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.github.cris16228.libcore.R;

public class StrokeTextView extends TextView {

    private float strokeWidth;
    private Integer strokeColor;
    private Paint.Join strokeJoin;
    private float strokeMiter;


    public StrokeTextView(Context context) {
        super(context);
        init(null);
    }

    public StrokeTextView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public StrokeTextView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    public StrokeTextView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(attrs);
    }

    public void init(AttributeSet attrs) {
        if (attrs != null) {
            TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.StrokeTextView);
            if (a.hasValue(R.styleable.StrokeTextView_strokeColor)) {
                float strokeWidth = a.getDimensionPixelSize(R.styleable.StrokeTextView_strokeWidth, 1);
                int strokeColor = a.getColor(R.styleable.StrokeTextView_strokeColor, 0xff000000);
                float strokeMiter = a.getDimensionPixelSize(R.styleable.StrokeTextView_strokeMiter, 10);
                Paint.Join strokeJoin = null;
                switch (a.getInt(R.styleable.StrokeTextView_strokeJoinStyle, 0)) {
                    case (0):
                        strokeJoin = Paint.Join.MITER;
                        break;
                    case (1):
                        strokeJoin = Paint.Join.BEVEL;
                        break;
                    case (2):
                        strokeJoin = Paint.Join.ROUND;
                        break;
                }
                this.setStroke(strokeWidth, strokeColor, strokeJoin, strokeMiter);
            }
        }
    }

    private void setStroke(float strokeWidth, int strokeColor, Paint.Join strokeJoin, float strokeMiter) {
        this.strokeWidth = strokeWidth;
        this.strokeColor = strokeColor;
        this.strokeJoin = strokeJoin;
        this.strokeMiter = strokeMiter;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int restoreColor = this.getCurrentTextColor();
        if (strokeColor != null) {
            TextPaint textPaint = this.getPaint();
            textPaint.setStyle(Paint.Style.STROKE);
            textPaint.setStrokeJoin(strokeJoin);
            textPaint.setStrokeMiter(strokeMiter);
            this.setTextColor(strokeColor);
            textPaint.setStrokeWidth(strokeWidth);
            super.onDraw(canvas);
            textPaint.setStyle(Paint.Style.FILL);
            this.setTextColor(restoreColor);
        }
    }
}
