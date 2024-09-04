package com.github.cris16228.libcore.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Scroller;

import com.github.cris16228.libcore.FloatUtils;
import com.github.cris16228.libcore.R;
import com.github.cris16228.libcore.StringUtils;
import com.github.cris16228.libcore.models.PieChartModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class PieChart extends View {

    double totalAmount = 0;
    private List<PieChartModel> pieChartModels = new ArrayList<>();
    private Map<String, Double> slices = new HashMap<>();
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
    private List<String> colors;
    private boolean legendOnRight;
    private int legendX;
    private int legendY;
    private String titleText = "";
    private int titleTextSize = 40;
    private int titleTextColor = Color.BLACK;
    private Paint.Align titleTextAlignment = Paint.Align.CENTER;
    private int titleMarginTop = 0;
    private int titleMarginBottom = 0;
    private int titleMarginLeft = 0;
    private int titleMarginRight = 0;
    private int titleMargin = 0;
    private String[] defaultColors = {"#AF7AC5", "#1ABC9C", "#DC7633", "#8E44AD", "#F7DC6F", "#D35400", "#3498DB", "#F0B27A", "#A569BD", "#fc3f8f", "#7FFF00", "#F1948A", "#2E86C1", "#9ACD32", "#c11b7b", "#FF6F61", "#1F618D", "#FF5733", "#28B463", "#76D7C4", "#D98880", "#6C3483", "#5499C7", "#C0392B", "#E67E22", "#900C3F", "#bc4017", "#FFD700", "#00FA9A", "#9B59B6", "#F1C40F", "#C70039", "#FF4500", "#E74C3C", "#7D3C98", "#00CED1", "#E59866", "#C39BD3", "#2874A6", "#FF69B4", "#F39C12", "#27AE60", "#FFC300", "#2ECC71", "#F8C471", "#16A085", "#2980B9", "#8A2BE2", "#5D6D7E", "#E9967A"};

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
        titleText = attributes.getString(R.styleable.PieChart_text);
        titleTextSize = attributes.getDimensionPixelSize(R.styleable.PieChart_textSize, 40);
        titleTextColor = attributes.getColor(R.styleable.PieChart_textColor, Color.BLACK);
        int alignment = attributes.getInt(R.styleable.PieChart_textAlignment, Paint.Align.CENTER.ordinal());
        switch (alignment) {
            case 0:
            default:
                titleTextAlignment = Paint.Align.LEFT;
                break;
            case 1:
                titleTextAlignment = Paint.Align.CENTER;
                break;
            case 2:
                titleTextAlignment = Paint.Align.RIGHT;
                break;
        }
        titleMarginTop = attributes.getDimensionPixelSize(R.styleable.PieChart_titleMarginTop, 0);
        titleMarginBottom = attributes.getDimensionPixelSize(R.styleable.PieChart_titleMarginBottom, 0);
        titleMarginLeft = attributes.getDimensionPixelSize(R.styleable.PieChart_titleMarginStart, 0);
        titleMarginRight = attributes.getDimensionPixelSize(R.styleable.PieChart_titleMarginEnd, 0);
        titleMargin = attributes.getDimensionPixelSize(R.styleable.PieChart_titleMargin, 0);
        if (titleMargin > 0) {
            titleMarginTop = titleMarginBottom = titleMarginLeft = titleMarginRight = titleMargin;
        }
        attributes.recycle();
    }

    private void buildColors() {
        int colorIndex = 0;
        for (int i = 0; i < pieChartModels.size(); i++) {
            if (pieChartModels.get(i).getColor().isEmpty()) {
                try {
                    colors.add(defaultColors[colorIndex]);
                    colorIndex++;
                } catch (IndexOutOfBoundsException e) {
                    colors.add(defaultColors[new Random().nextInt(defaultColors.length)]);
                }
            }
        }
    }

    public void setData(List<PieChartModel> pieChartModels) {
        this.pieChartModels.clear();
        this.slices.clear();
        this.totalAmount = 0;
        this.pieChartModels = pieChartModels;
        dataSize = pieChartModels.size();
        colors.clear();
    }

    public void setEnablePercent(boolean enablePercent) {
        this.enablePercent = enablePercent;
    }

    public void setEnableLegend(boolean enableLegend) {
        this.enableLegend = enableLegend;
    }

    public void setLegendOnRight(boolean legendOnRight) {
        this.legendOnRight = legendOnRight;
    }

    public void setTitleText(String titleText) {
        this.titleText = titleText;
    }

    public void setTitleTextSize(int titleTextSize) {
        this.titleTextSize = titleTextSize;
    }

    public void setTitleTextColor(int titleTextColor) {
        this.titleTextColor = titleTextColor;
    }

    public void setTitleTextAlignment(Paint.Align titleTextAlignment) {
        this.titleTextAlignment = titleTextAlignment;
    }

    public void setTitleMarginTop(int titleMarginTop) {
        this.titleMarginTop = titleMarginTop;
    }

    public void setTitleMarginBottom(int titleMarginBottom) {
        this.titleMarginBottom = titleMarginBottom;
    }

    public void setTitleMarginLeft(int titleMarginLeft) {
        this.titleMarginLeft = titleMarginLeft;
    }

    public void setTitleMarginRight(int titleMarginRight) {
        this.titleMarginRight = titleMarginRight;
    }

    public void setTitleMargin(int titleMargin) {
        titleMarginTop = titleMarginBottom = titleMarginLeft = titleMarginRight = titleMargin;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);


        int width = 0;
        int height = pieChartSize;
        if (!TextUtils.isEmpty(titleText)) {
            height += titleMarginTop + titleMarginBottom + titleTextSize;
        }

        if (enableLegend) {
            Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
            paint.setTextSize(40);
            float maxTextWidth = 0;
            for (Map.Entry<String, Double> entry : slices.entrySet()) {
                String text = getResources().getString(enablePercent ? R.string.legend_text_percent : R.string.legend_text_no_percent,
                        FloatUtils.getNumberFormat(((float) (entry.getValue() / totalAmount) * 100), 2));
                float textWidth = paint.measureText(text);
                maxTextWidth = Math.max(maxTextWidth, textWidth);
            }
            legendWidth = (int) (maxTextWidth + 120);
            if (legendOnRight) {
                width += pieChartSize + legendWidth;
            } else {
                width += pieChartSize;
            }
        } else {
            width += pieChartSize;
        }

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
        slices.clear();
        totalAmount = 0;
        if (colors.isEmpty()) {
            buildColors();
        }

        for (PieChartModel model : pieChartModels) {
            if (slices.containsKey(model.getTitle())) {
                slices.put(model.getTitle(), slices.get(model.getTitle()) + model.getValue());
            } else {
                slices.put(model.getTitle(), model.getValue());
            }
        }

        for (double value : slices.values()) {
            totalAmount += value;
        }

        float startAngle = 0;
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setStyle(Paint.Style.FILL);

        int availableWidth = getWidth();
        pieChartSize = Math.min(pieChartSize, 475);
        legendWidth = availableWidth - pieChartSize;
        if (enableLegend && !legendOnRight) {
            pieChartXOffset = legendWidth;
            legendX = 0;
        } else {
            pieChartXOffset = 0;
        }
        if (!StringUtils.isEmpty(titleText)) {
            float titleY = titleMarginTop + titleTextSize;
            TextPaint titlePaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
            float titleWidth = titlePaint.measureText(titleText);
            float availableTextWidth = getWidth() - titleMarginLeft - titleMarginRight;
            float titleX = titleMarginLeft;
            if (titleTextAlignment == Paint.Align.CENTER) {
                titleX += availableTextWidth / 2f - titleWidth / 2f;
            } else if (titleTextAlignment == Paint.Align.RIGHT) {
                titleX += availableTextWidth - titleWidth;
            }
            titlePaint.setColor(titleTextColor);
            titlePaint.setTextSize(titleTextSize);
            titlePaint.setTextAlign(titleTextAlignment);
            canvas.drawText(titleText, titleX, titleY, titlePaint);
        }
        int pieChartTop = titleMarginTop + titleTextSize + titleMarginBottom;
        RectF rect = new RectF(
                pieChartXOffset,
                pieChartTop,
                pieChartXOffset + pieChartSize,
                pieChartTop + pieChartSize
        );

        // Draw pie slices
        for (Map.Entry<String, Double> entry : slices.entrySet()) {
            int index = PieChartModel.getIndex(pieChartModels, entry.getKey());
            PieChartModel model = pieChartModels.get(index);
            paint.setColor(Color.parseColor(model.getColor().isEmpty() ? colors.get(index) : model.getColor()));
            float sweepAngle = (float) (entry.getValue() / totalAmount * 360);
            canvas.drawArc(rect, startAngle, sweepAngle, true, paint);
            startAngle += sweepAngle;
        }
        if (enableLegend) {
            canvas.save();
            int legendLeft = legendOnRight ? -pieChartSize + 50 : legendX;
            int legendTop = 0;
            int legendRight = legendOnRight ? getWidth() : legendWidth;
            int legendBottom = getHeight();

            /*canvas.clipRect(legendLeft, legendTop, legendRight, legendBottom);*/
            canvas.translate(legendOnRight ? 0 : -legendWidth, legendScrollY);
            float legendY = rect.top + 50;
            paint.setTextSize(40);
            for (Map.Entry<String, Double> entry : slices.entrySet()) {
                int index = PieChartModel.getIndex(pieChartModels, entry.getKey());
                PieChartModel model = pieChartModels.get(index);
                paint.setColor(Color.parseColor(model.getColor().isEmpty() ? colors.get(index) : model.getColor()));

                /*int rectLeft = legendOnRight ? legendLeft : 0;
                 */
                canvas.drawRect(legendLeft, legendY, legendLeft + 50, legendY + 40, paint);
                Log.e("TAG", "onDraw: " + legendOnRight + ": " + legendLeft + " " + legendLeft + 50);
                paint.setColor(titleTextColor);
                String percent = FloatUtils.getNumberFormat(((float) (entry.getValue() / totalAmount) * 100), 2);
                int textX = legendOnRight ? legendLeft + 70 : 70;
                if (enablePercent) {
                    canvas.drawText(getResources().getString(R.string.legend_text_percent, entry.getKey(), percent), pieChartSize + legendLeft + 150, legendY + 30, paint);
                } else {
                    canvas.drawText(getResources().getString(R.string.legend_text_no_percent, entry.getKey()), pieChartSize + legendLeft + 150, legendY + 30, paint);
                }
                legendY += 60;
            }
            canvas.restore();
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

        int totalLegendHeight = slices.size() * 60 + 50; // Total height of the legend
        int maxScrollY = Math.max(0, totalLegendHeight - getHeight());
        if (legendOnRight) {
            dy = -dy;
        }
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
                if (legendScrollY + dy > 0) {
                    legendScrollY -= dy;
                } else {
                    legendScrollY = 0;
                }
            }
        }
        int legendLeft = legendOnRight ? pieChartSize : 0;
        int legendTop = legendScrollY;
        int legendRight = legendOnRight ? getWidth() : legendWidth;
        int legendBottom = legendTop + totalLegendHeight;
        invalidate(legendLeft, legendTop, legendRight, legendBottom);
    }
}