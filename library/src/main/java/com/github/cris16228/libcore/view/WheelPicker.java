package com.github.cris16228.libcore.view;

import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.NumberPicker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.github.cris16228.libcore.R;

public class WheelPicker extends FrameLayout {

    String[] wheelData;

    private OnItemSelect onItemSelect;

    public WheelPicker(@NonNull Context context) {
        super(context);
        initView(context);
    }

    public WheelPicker(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    public WheelPicker(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    public WheelPicker(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initView(context);
    }

    public OnItemSelect getOnItemSelect() {
        return onItemSelect;
    }

    public void setOnItemSelect(OnItemSelect onItemSelect) {
        this.onItemSelect = onItemSelect;
    }

    public String[] getWheelData() {
        return wheelData;
    }

    public void setWheelData(String[] wheelData) {
        this.wheelData = wheelData;
    }

    private void initView(Context context) {
        View view = inflate(context, R.layout.layout_wheel_picker, this);
        NumberPicker numberPicker = view.findViewById(R.id.wheelPicker);
        numberPicker.setMinValue(0);
        numberPicker.setMaxValue(wheelData.length - 1);
        numberPicker.setDisplayedValues(wheelData);
        numberPicker.setShowDividers(LinearLayout.SHOW_DIVIDER_NONE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            numberPicker.setTextColor(context.getColor(R.color.colorText));
        }
        numberPicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                onItemSelect.OnSelection(newVal, wheelData[newVal]);
            }
        });
    }

    public interface OnItemSelect {

        void OnSelection(int valI, String valS);
    }
}
