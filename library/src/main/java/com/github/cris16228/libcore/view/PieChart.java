package com.github.cris16228.libcore.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Scroller;

import com.github.cris16228.libcore.FloatUtils;
import com.github.cris16228.libcore.R;
import com.github.cris16228.libcore.models.PieChartModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class PieChart extends View {

    double totalAmount = 0;
    private List<PieChartModel> pieChartModels = new ArrayList<>();
    private List<String> titles = new ArrayList<>();
    private int dataSize;
    private Scroller scroller;
    private int legendScrollY = 0;
    private float startY = 0f;
    private int pieChartSize;
    private int pieChartXOffset;
    private int pieChartYOffset;
    private int legendWidth;
    private boolean enablePercent;
    private boolean enableLegend;
    private boolean useDefaultColors;
    private List<Integer> colors;
    private boolean legendOnRight;
    private int legendX;
    private int legendY;

    private String[] colorString = {
            "#5574a6ff",
            "#3366ccff",
            "#dc3912ff",
            "#ff9900ff",
            "#109618ff",
            "#990099ff",
            "#5574a6ff",
            "#0099c6ff",
            "#dd4477ff",
            "#66aa00ff",
            "#b82e2eff",
            "#316395ff",
            "#994499ff",
            "#22aa99ff",
            "#aaaa11ff",
            "#6633ccff",
            "#e67300ff",
            "#8b0707ff",
            "#651067ff",
            "#329262ff",
            "#63a8c0ff",
            "#3b3eacff",
            "#b77322ff",
            "#16d620ff",
            "#b91383ff",
            "#f4359eff",
            "#9c5935ff",
            "#a9c413ff",
            "#2a778dff",
            "#668d1cff",
            "#bea413ff",
            "#0c5922ff",
            "#3366ccff",
            "#dc3912ff",
            "#ff9900ff",
            "#109618ff",
            "#990099ff",
            "#dd4477ff",
            "#66aa00ff",
            "#b82e2eff",
            "#316395ff",
            "#994499ff",
            "#22aa99ff",
            "#6633ccff",
            "#e67300ff",
            "#8b0707ff",
            "#651067ff",
            "#329262ff"
    };

    public PieChart(Context context, AttributeSet attrs) {
        super(context, attrs);
        colors = new ArrayList<>();
        buildColors();
        scroller = new Scroller(context);
        TypedArray attributes = context.obtainStyledAttributes(attrs, R.styleable.PieChart);
        pieChartSize = attributes.getDimensionPixelSize(R.styleable.PieChart_pieChartSize, 200);
        pieChartSize = Math.min(pieChartSize, 475);
        pieChartXOffset = attributes.getDimensionPixelSize(R.styleable.PieChart_pieChartXOffset, 0);
        pieChartYOffset = attributes.getDimensionPixelSize(R.styleable.PieChart_pieChartYOffset, 0);
        enablePercent = attributes.getBoolean(R.styleable.PieChart_enablePercent, true);
        enableLegend = attributes.getBoolean(R.styleable.PieChart_enableLegend, true);
        useDefaultColors = attributes.getBoolean(R.styleable.PieChart_useDefaultColors, true);
        legendOnRight = attributes.getBoolean(R.styleable.PieChart_legendOnRight, true);
        attributes.recycle();
    }

    private void buildColors() {
        for (int i = 0; i < pieChartModels.size(); i++) {
            try {
                colors.add(Color.parseColor(colorString[i]));
            } catch (IndexOutOfBoundsException e) {
                colors.add(Color.parseColor(colorString[new Random().nextInt(colorString.length)]));
            }
        }
    }

    public void setData(List<PieChartModel> pieChartModels) {
        this.pieChartModels.clear();
        this.titles.clear();
        this.pieChartModels = pieChartModels;
        dataSize = pieChartModels.size();
    }

    public void setEnablePercent(boolean enablePercent) {
        this.enablePercent = enablePercent;
    }

    public void setEnableLegend(boolean enableLegend) {
        this.enableLegend = enableLegend;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);


        int width = 0;
        int height = 0;

        if (widthMode == MeasureSpec.EXACTLY) {
            width = widthSize;
        } else if (widthMode == MeasureSpec.AT_MOST) {
            width = Math.min(pieChartSize, widthSize);
        }
        setMeasuredDimension(width, height);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (pieChartModels == null || pieChartModels.isEmpty()) {
            return;
        }

        for (PieChartModel model : pieChartModels) {
            titles.add(model.getTitle());
            totalAmount += model.getValue();
        }


        float startAngle = 0;
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setStyle(Paint.Style.FILL);

        int availableWidth = getWidth();
        pieChartSize = Math.min(pieChartSize, 475);
        legendWidth = availableWidth - pieChartSize;
        if (!legendOnRight) {
            pieChartXOffset = pieChartXOffset + legendWidth;
            legendX = 0;
        } else {
            legendX = getWidth() - legendWidth + 50;
        }
        RectF rect = new RectF(
                pieChartXOffset,
                pieChartYOffset,
                pieChartXOffset + pieChartSize,
                pieChartYOffset + pieChartSize
        );

        // Draw pie slices
        for (PieChartModel model : pieChartModels) {
            paint.setColor(model.getColor());
            float sweepAngle = (float) (model.getValue() / totalAmount * 360);
            canvas.drawArc(rect, startAngle, sweepAngle, true, paint);
            startAngle += sweepAngle;
        }
        canvas.save();
        canvas.translate(0, legendScrollY);
        float legendY = rect.top + 50;
        paint.setTextSize(40);
        for (int i = 0; i < pieChartModels.size(); i++) {
            PieChartModel model = pieChartModels.get(i);
            paint.setColor(Color.parseColor(String.format("#%06X", (0xFFFFFF & (model.getColor() == 0 ? colors.get(i) : model.getColor())))));
            canvas.drawRect(legendX, legendY, getWidth() - legendWidth + 100, legendY + 40, paint);
            paint.setColor(Color.WHITE);
            String percent = FloatUtils.getNumberFormat(((float) (model.getValue() / totalAmount) * 100), 2);
            if (enableLegend) {
                if (enablePercent) {
                    canvas.drawText(getResources().getString(R.string.legend_text_percent, model.getTitle(), percent), pieChartSize + 120, legendY + 30, paint);
                } else {
                    canvas.drawText(getResources().getString(R.string.legend_text_no_percent, model.getTitle()), pieChartSize + 120, legendY + 30, paint);
                }
            }
            legendY += 60;

        }
        canvas.restore();
        if (!scroller.isFinished()) {
            invalidate();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (pieChartModels.size() * 60 <= getHeight()) {
            return super.onTouchEvent(event);
        }

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                startY = event.getY();
                return true;
            case MotionEvent.ACTION_MOVE:
                float currentY = event.getY();
                int dy = (int) (currentY - startY);
                scrollLegend(dy);
                startY = currentY;
                return true;
        }

        return super.onTouchEvent(event);
    }

    private void scrollLegend(int dy) {

        int totalLegendHeight = pieChartModels.size() * 60 + 90; // Total height of the legend
        int maxScrollY = Math.max(0, totalLegendHeight - getHeight());
        if (dy > 0) {
            if (legendScrollY - dy < 0) {
                legendScrollY = 0;
            } else {
                legendScrollY -= dy;
            }
        } else {
            if ((legendScrollY + dy) * -1 > maxScrollY) {
                legendScrollY = maxScrollY * -1;
            } else {
                legendScrollY += dy;
            }
        }
        invalidate();
    }
}