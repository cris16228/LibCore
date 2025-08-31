package com.github.cris16228.libcore.view;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;

import com.github.cris16228.libcore.R;

import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class PieChartView extends View {

    private final Paint slicePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint labelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint clearPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final TextPaint legendTextPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
    private final Paint legendSwatchPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF drawingRect = new RectF();
    private List<Slice> slices = Collections.emptyList();
    private float strokeWidth = 0f;
    private boolean showLabels = true;
    private float labelTextSize = spToPx(12);
    private int labelColor = Color.BLACK;
    private float holeRadiusPercent = 0f;
    private float animationProgress = 1f;
    private boolean legendEnabled = true;
    private float legendTextSize = dpToPx(12);
    private int legendTextColor = Color.WHITE;
    private float legendSwatchSize = dpToPx(12);
    private float legendSpacing = dpToPx(8);
    private float legendRowSpacing = dpToPx(4);
    private int legendMaxRows = Integer.MAX_VALUE;
    private final String[] defaultColors = {"#AF7AC5", "#1ABC9C", "#DC7633", "#8E44AD", "#F7DC6F", "#D35400", "#3498DB", "#F0B27A", "#A569BD", "#fc3f8f", "#7FFF00", "#F1948A", "#2E86C1", "#9ACD32", "#c11b7b", "#FF6F61", "#1F618D", "#FF5733", "#28B463", "#76D7C4", "#D98880", "#6C3483", "#5499C7", "#C0392B", "#E67E22", "#900C3F", "#bc4017", "#FFD700", "#00FA9A", "#9B59B6", "#F1C40F", "#C70039", "#FF4500", "#E74C3C", "#7D3C98", "#00CED1", "#E59866", "#C39BD3", "#2874A6", "#FF69B4", "#F39C12", "#27AE60", "#FFC300", "#2ECC71", "#F8C471", "#16A085", "#2980B9", "#8A2BE2", "#5D6D7E", "#E9967A"};


    public PieChartView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initAttrs(context, attrs);
        clearPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
    }

    private void initAttrs(Context context, AttributeSet attrs) {
        TypedArray attributes = context.obtainStyledAttributes(attrs, R.styleable.PieChartView);
        strokeWidth = attributes.getDimension(R.styleable.PieChartView_strokeWidth, dpToPx(2));
        showLabels = attributes.getBoolean(R.styleable.PieChartView_showLabels, true);
        labelTextSize = attributes.getDimensionPixelSize(R.styleable.PieChartView_labelTextSize, spToPx(12));
        labelColor = attributes.getColor(R.styleable.PieChartView_labelColor, Color.BLACK);
        holeRadiusPercent = attributes.getFloat(R.styleable.PieChartView_holeRadiusPercent, 0f);
        legendEnabled = attributes.getBoolean(R.styleable.PieChartView_legendEnabled, true);
        legendTextSize = attributes.getDimensionPixelSize(R.styleable.PieChartView_legendTextSize, spToPx(12));
        legendTextColor = attributes.getColor(R.styleable.PieChartView_legendTextColor, Color.WHITE);
        legendSwatchSize = attributes.getDimensionPixelSize(R.styleable.PieChartView_legendSwatchSize, dpToPx(12));
        legendSpacing = attributes.getDimensionPixelSize(R.styleable.PieChartView_legendSpacing, dpToPx(8));
        legendRowSpacing = attributes.getDimensionPixelSize(R.styleable.PieChartView_legendRowSpacing, dpToPx(4));
        legendMaxRows = attributes.getInt(R.styleable.PieChartView_legendMaxRows, Integer.MAX_VALUE);
        attributes.recycle();
        labelPaint.setTextSize(labelTextSize);
        labelPaint.setColor(labelColor);
        legendTextPaint.setTextSize(legendTextSize);
        legendTextPaint.setColor(legendTextColor);
        legendTextPaint.setTypeface(Typeface.DEFAULT_BOLD);
    }

    public void setData(List<Slice> data) {
        this.slices = data != null ? data : Collections.emptyList();
        requestLayout();
        invalidate();
    }

    public void animateChart(long durationMs) {
        ValueAnimator anim = ValueAnimator.ofFloat(0f, 1f);
        anim.setDuration(durationMs);
        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(@NonNull ValueAnimator animation) {
                animationProgress = (float) animation.getAnimatedValue();
                invalidate();
            }
        });
        anim.start();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        int baseSize = Math.min(width, height);

        int legendRows = Math.min(slices.size(), legendMaxRows);
        int legendHeight = 0;
        if (legendEnabled && legendRows > 0) {
            legendHeight = (int) (dpToPx(12) + legendRows * (legendSwatchSize + legendRowSpacing) + dpToPx(12));
        }
        int desiredWidth = resolveSize(baseSize, widthMeasureSpec);
        int desiredHeight = resolveSize(baseSize + legendHeight, heightMeasureSpec);
        setMeasuredDimension(desiredWidth, desiredHeight);
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);
        if (slices.isEmpty()) {
            return;
        }

        float totalValue = 0;
        for (Slice slice : slices) {
            totalValue += slice.value;
        }

        if (totalValue == 0) {
            return;
        }

        float left = getPaddingLeft();
        float top = getPaddingTop();
        float right = getWidth() - getPaddingRight();
        float bottom = getHeight() - getPaddingBottom();
        drawingRect.set(left, top, right, bottom);

        float size = Math.min(drawingRect.width(), drawingRect.height());
        float dx = (drawingRect.width() - size) / 2f;
        float dy = (drawingRect.height() - size) / 2f;
        drawingRect.inset(dx, dy);

        float startAngle = -90f;
        for (Slice slice : slices) {
            if (slice.value <= 0) continue;
            float sweepAngle = (slice.value / totalValue) * 360 * animationProgress;
            if (sweepAngle <= 0) continue;

            slicePaint.setColor(slice.color);
            canvas.drawArc(drawingRect, startAngle, sweepAngle, true, slicePaint);

            if (holeRadiusPercent > 0f) {
                float inset = drawingRect.width() * holeRadiusPercent / 2f;
                drawingRect.inset(inset, inset);
                canvas.drawArc(drawingRect, startAngle, sweepAngle, true, clearPaint);
                drawingRect.inset(-inset, -inset);
            }

            if (legendEnabled && !slices.isEmpty()) {

                drawLegend(canvas);
            }

            if (showLabels) {
                String txt = String.format(Locale.getDefault(), "%d%%", Math.round((slice.value / totalValue) * 100));
                drawLabel(canvas, txt, startAngle, sweepAngle);
            }
            startAngle += sweepAngle;
        }
    }

    private void drawLegend(@NonNull Canvas canvas) {
        long total = total();
        if (total == 0) total = 1;

        float legendMargin = dpToPx(12);
        float size = Math.min(getWidth() - getPaddingLeft() - getPaddingRight(), getHeight() - getPaddingTop() - getPaddingBottom());
        float startY = size + legendMargin;

        int viewWidth = getWidth() - getPaddingLeft() - getPaddingRight();

        int rowsDrawn = 0;
        for (int i = 0; i < slices.size() && rowsDrawn < legendMaxRows; i++) {
            Slice s = slices.get(i);

            float pct = (float) s.value / total * 100f;
            String percent = String.format(Locale.getDefault(), "%.1f%%", pct);

            float swatchLeft = getPaddingLeft();
            float swatchTop = startY + rowsDrawn * (legendSwatchSize + legendSpacing);
            float swatchRight = swatchLeft + legendSwatchSize;
            float swatchBottom = swatchTop + legendSwatchSize;

            legendSwatchPaint.setColor(s.color);
            canvas.drawRect(swatchLeft, swatchTop, swatchRight, swatchBottom, legendSwatchPaint);

            float textX = swatchRight + legendSpacing;
            Paint.FontMetrics fontMetrics = legendTextPaint.getFontMetrics();
            float textBaseLine = swatchTop - fontMetrics.ascent;

            float maxLabelWidth = viewWidth - (textX - getPaddingLeft()) - legendSpacing - legendTextPaint.measureText(percent) - getPaddingRight();
            String label = TextUtils.ellipsize(s.label, legendTextPaint, maxLabelWidth, TextUtils.TruncateAt.END).toString();
            canvas.drawText(label, textX, textBaseLine, legendTextPaint);

            float pctX = getPaddingLeft() + viewWidth - legendTextPaint.measureText(percent);
            canvas.drawText(percent, pctX, textBaseLine, legendTextPaint);

            rowsDrawn++;
        }
    }

    private void drawLabel(Canvas canvas, String txt, float startAngle, float sweepAngle) {

        float angle = startAngle + sweepAngle / 2f;
        double rad = Math.toRadians(angle);
        float radius = drawingRect.width() / 2f * (1 - holeRadiusPercent) * 0.75f;
        float cx = (float) (drawingRect.centerX() + radius * Math.cos(rad));
        float cy = (float) (drawingRect.centerY() + radius * Math.sin(rad));

        Paint.FontMetrics fontMetrics = labelPaint.getFontMetrics();
        float yOffset = (fontMetrics.descent + fontMetrics.ascent) / 2f;
        canvas.drawText(txt, cx, cy - yOffset, labelPaint);
    }

    private int dpToPx(float dp) {
        return (int) (dp * getResources().getDisplayMetrics().density + 0.5f);
    }

    private int spToPx(float sp) {
        return (int) (sp * getResources().getDisplayMetrics().scaledDensity + 0.5f);
    }

    @SuppressLint("GetContentDescriptionOverride")
    @Override
    public CharSequence getContentDescription() {
        if (slices.isEmpty()) return "Empty pie chart";
        StringBuilder sb = new StringBuilder("Pie chart: ");
        for (Slice slice : slices) {
            sb.append(slice.label).append(" ").append(Math.round((slice.value / total()) * 100)).append("%, ");
        }
        return sb.toString();
    }

    private long total() {
        long total = 0;
        for (Slice slice : slices) {
            total += slice.value;
        }
        return total;
    }

    public static class Slice {
        public final String label;
        public final long value;
        public final int color;

        public Slice(String label, long value, int color) {
            this.label = label;
            this.value = value;
            this.color = color;
        }
    }
}