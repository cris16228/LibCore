package com.github.cris16228.libcore.view;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class WateryProgressBar extends View {

    Path wavePath;
    private Paint wavePaint;
    private float progress = 0.12f;
    private int max = 100;
    private int waveColor = Color.BLUE;
    private ValueAnimator waveAnimator;
    private float xOffset = 0; // Offset for wave animation


    public WateryProgressBar(Context context) {
        super(context);
        init();
    }

    public WateryProgressBar(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public WateryProgressBar(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        wavePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        wavePaint.setColor(waveColor);
        wavePaint.setStyle(Paint.Style.FILL);
        wavePath = new Path();
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);
        int waveFrequency = 600;
        if (waveAnimator == null) {
            waveAnimator = ValueAnimator.ofFloat(0, waveFrequency);
            waveAnimator.setDuration(3000);
            waveAnimator.setRepeatCount(ValueAnimator.INFINITE);
            waveAnimator.setInterpolator(new LinearInterpolator());
            waveAnimator.addUpdateListener(animation -> {
                setXOffset((float) animation.getAnimatedValue());
            });
            waveAnimator.start();
        }
        int width = getWidth();
        int height = getHeight();

        float waveHeight = height * (1 - progress);


        wavePath.moveTo(-width + xOffset, waveHeight);

        float amplitude = 12;
        for (int x = -width; x <= width; x++) {
            float y = (float) (waveHeight + amplitude * Math.sin((2 * Math.PI / waveFrequency) * (x + xOffset)));
            wavePath.lineTo(x, y);
        }

        wavePath.lineTo(width, height);
        wavePath.lineTo(-width, height);
        wavePath.close();

        canvas.drawPath(wavePath, wavePaint);
    }

    public void setProgress(float progress) {
        this.progress = Math.max(0, Math.min(progress, max));
        invalidate();
    }

    public void setMax(int max) {
        this.max = max;
    }

    public void setColor(int color) {
        this.waveColor = color;
        wavePaint.setColor(color);
    }

    public float getXOffset() {
        return xOffset;
    }

    public void setXOffset(float xOffset) {
        this.xOffset = xOffset;
        invalidate();
    }
}
