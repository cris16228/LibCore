package com.github.cris16228.libcore.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.graphics.drawable.DrawableCompat;

import com.github.cris16228.libcore.R;
import com.github.cris16228.libcore.StringUtils;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.stream.Collectors;

public class Passcode extends FrameLayout implements View.OnClickListener {

    private final ArrayList<String> numbers_list = new ArrayList<>();
    private final int buttonColor = 0x00000000;
    public String passcode = "";
    private String secondInput = "";
    private String firstInput = "";
    private onPasswordListener onPasswordListener;
    private View dot_1, dot_2, dot_3, dot_4;
    private MaterialButton btn_number_1, btn_number_2, btn_number_3, btn_number_4, btn_number_5, btn_number_6, btn_number_7, btn_number_8, btn_number_9, btn_number_0,
            btn_clear;
    private ImageView lock;
    private char[] code;
    private int backgroundColor = 0xFFAAAAAA;
    private int overlayColor = 0xFF448AFF;
    private int errorColor = 0xFFF24055;
    private String firstInputTip = "Enter a passcode of 4 digits";
    private String secondInputTip = "Re-enter new passcode";
    private String wrongInputTip = "Passcode do not match";
    private String correctInputTip = "Passcode is correct";
    private Drawable code_background;
    private Drawable code_overlay;
    private Drawable code_error;
    private TextView message;

    public Passcode(@NonNull Context context) {
        super(context);
        initView(context);
    }

    public Passcode(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView(context);
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.Passcode);
        try {
            passcode = typedArray.getString(R.styleable.Passcode_pass);
            backgroundColor = typedArray.getColor(R.styleable.Passcode_passcodeBackground, backgroundColor);
            overlayColor = typedArray.getColor(R.styleable.Passcode_passcodeOverlay, overlayColor);
            errorColor = typedArray.getColor(R.styleable.Passcode_passcodeError, errorColor);
            firstInputTip = typedArray.getString(R.styleable.Passcode_firstInputTip);
            secondInputTip = typedArray.getString(R.styleable.Passcode_secondInputTip);
            wrongInputTip = typedArray.getString(R.styleable.Passcode_wrongInputTip);
            correctInputTip = typedArray.getString(R.styleable.Passcode_correctInputTip);
        } finally {
            typedArray.recycle();
        }
        firstInputTip = firstInputTip == null ? context.getResources().getString(R.string.passcode_tip) : firstInputTip;
        secondInputTip = secondInputTip == null ? context.getResources().getString(R.string.passcode_enter_again) : secondInputTip;
        wrongInputTip = wrongInputTip == null ? context.getResources().getString(R.string.passcode_not_matching) : wrongInputTip;
        correctInputTip = correctInputTip == null ? context.getResources().getString(R.string.passcode_correct) : correctInputTip;
    }

    public Passcode(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.Passcode);
        try {
            passcode = typedArray.getString(R.styleable.Passcode_pass);
            backgroundColor = typedArray.getColor(R.styleable.Passcode_passcodeBackground, backgroundColor);
            overlayColor = typedArray.getColor(R.styleable.Passcode_passcodeOverlay, overlayColor);
            errorColor = typedArray.getColor(R.styleable.Passcode_passcodeError, errorColor);
            firstInputTip = typedArray.getString(R.styleable.Passcode_firstInputTip);
            secondInputTip = typedArray.getString(R.styleable.Passcode_secondInputTip);
            wrongInputTip = typedArray.getString(R.styleable.Passcode_wrongInputTip);
            correctInputTip = typedArray.getString(R.styleable.Passcode_correctInputTip);
        } finally {
            typedArray.recycle();
        }
        firstInputTip = firstInputTip == null ? context.getResources().getString(R.string.passcode_tip) : firstInputTip;
        secondInputTip = secondInputTip == null ? context.getResources().getString(R.string.passcode_enter_again) : secondInputTip;
        wrongInputTip = wrongInputTip == null ? context.getResources().getString(R.string.passcode_not_matching) : wrongInputTip;
        correctInputTip = correctInputTip == null ? context.getResources().getString(R.string.passcode_correct) : correctInputTip;
        initView(context);
    }

    public Passcode(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initView(context);
    }

    public int getButtonColor() {
        return buttonColor;
    }

    public ImageView getLock() {
        return lock;
    }

    public int getBackgroundColor() {
        return backgroundColor;
    }

    public void setBackgroundColor(int background) {
        this.backgroundColor = background;
    }

    public int getOverlayColor() {
        return overlayColor;
    }

    public void setOverlayColor(int overlay) {
        this.overlayColor = overlay;
    }

    public int getErrorColor() {
        return errorColor;
    }

    public void setErrorColor(int error) {
        this.errorColor = error;
    }

    public String getFirstInputTip() {
        return firstInputTip;
    }

    public void setFirstInputTip(String firstInputTip) {
        this.firstInputTip = firstInputTip;
    }

    public String getSecondInputTip() {
        return secondInputTip;
    }

    public void setSecondInputTip(String secondInputTip) {
        this.secondInputTip = secondInputTip;
    }

    public String getWrongInputTip() {
        return wrongInputTip;
    }

    public void setWrongInputTip(String wrongInputTip) {
        this.wrongInputTip = wrongInputTip;
    }

    public String getCorrectInputTip() {
        return correctInputTip;
    }

    public void setCorrectInputTip(String correctInputTip) {
        this.correctInputTip = correctInputTip;
    }

    public Drawable getCode_background() {
        return code_background;
    }

    public void setCode_background(Drawable code_background) {
        this.code_background = code_background;
    }

    public Drawable getCode_overlay() {
        return code_overlay;
    }

    public void setCode_overlay(Drawable code_overlay) {
        this.code_overlay = code_overlay;
    }

    public Drawable getCode_error() {
        return code_error;
    }

    public void setCode_error(Drawable code_error) {
        this.code_error = code_error;
    }

    public TextView getMessage() {
        return message;
    }

    public void setMessage(TextView message) {
        this.message = message;
    }

    private void initView(Context context) {
        code = new char[4];
        View view = inflate(context, R.layout.layout_passcode, this);

        code_background = ResourcesCompat.getDrawable(getResources(), R.drawable.passcode_background, null);
        if (code_background != null) {
            DrawableCompat.setTint(code_background, Color.parseColor(String.format("#%06X", (0xFFFFFF & backgroundColor))));
        }
        code_overlay = ResourcesCompat.getDrawable(getResources(), R.drawable.passcode_overlay, null);
        if (code_overlay != null) {
            DrawableCompat.setTint(code_overlay, Color.parseColor(String.format("#%06X", (0xFFFFFF & overlayColor))));
        }
        code_error = ResourcesCompat.getDrawable(getResources(), R.drawable.passcode_error, null);
        if (code_error != null) {
            DrawableCompat.setTint(code_error, Color.parseColor(String.format("#%06X", (0xFFFFFF & errorColor))));
        }

        message = view.findViewById(R.id.message);
        message.setText(firstInputTip);
        lock = view.findViewById(R.id.lock);
        dot_1 = view.findViewById(R.id.dot_1);
        dot_2 = view.findViewById(R.id.dot_2);
        dot_3 = view.findViewById(R.id.dot_3);
        dot_4 = view.findViewById(R.id.dot_4);
        btn_number_0 = view.findViewById(R.id.btn_number_0);
        btn_number_1 = view.findViewById(R.id.btn_number_1);
        btn_number_2 = view.findViewById(R.id.btn_number_2);
        btn_number_3 = view.findViewById(R.id.btn_number_3);
        btn_number_4 = view.findViewById(R.id.btn_number_4);
        btn_number_5 = view.findViewById(R.id.btn_number_5);
        btn_number_6 = view.findViewById(R.id.btn_number_6);
        btn_number_7 = view.findViewById(R.id.btn_number_7);
        btn_number_8 = view.findViewById(R.id.btn_number_8);
        btn_number_9 = view.findViewById(R.id.btn_number_9);
        btn_clear = view.findViewById(R.id.btn_clear);

        btn_number_0.setOnClickListener(this);
        btn_number_1.setOnClickListener(this);
        btn_number_2.setOnClickListener(this);
        btn_number_3.setOnClickListener(this);
        btn_number_4.setOnClickListener(this);
        btn_number_5.setOnClickListener(this);
        btn_number_6.setOnClickListener(this);
        btn_number_7.setOnClickListener(this);
        btn_number_8.setOnClickListener(this);
        btn_number_9.setOnClickListener(this);
        btn_clear.setOnClickListener(this);
        btn_number_0.setBackgroundColor(buttonColor);
        btn_number_1.setBackgroundColor(buttonColor);
        btn_number_2.setBackgroundColor(buttonColor);
        btn_number_3.setBackgroundColor(buttonColor);
        btn_number_4.setBackgroundColor(buttonColor);
        btn_number_5.setBackgroundColor(buttonColor);
        btn_number_6.setBackgroundColor(buttonColor);
        btn_number_7.setBackgroundColor(buttonColor);
        btn_number_8.setBackgroundColor(buttonColor);
        btn_number_9.setBackgroundColor(buttonColor);
        btn_clear.setBackgroundColor(buttonColor);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.btn_number_0) {
            numbers_list.add("0");
            passNumber(numbers_list);
        } else if (id == R.id.btn_number_1) {
            numbers_list.add("1");
            passNumber(numbers_list);
        } else if (id == R.id.btn_number_2) {
            numbers_list.add("2");
            passNumber(numbers_list);
        } else if (id == R.id.btn_number_3) {
            numbers_list.add("3");
            passNumber(numbers_list);
        } else if (id == R.id.btn_number_4) {
            numbers_list.add("4");
            passNumber(numbers_list);
        } else if (id == R.id.btn_number_5) {
            numbers_list.add("5");
            passNumber(numbers_list);
        } else if (id == R.id.btn_number_6) {
            numbers_list.add("6");
            passNumber(numbers_list);
        } else if (id == R.id.btn_number_7) {
            numbers_list.add("7");
            passNumber(numbers_list);
        } else if (id == R.id.btn_number_8) {
            numbers_list.add("8");
            passNumber(numbers_list);
        } else if (id == R.id.btn_number_9) {
            numbers_list.add("9");
            passNumber(numbers_list);
        } else if (id == R.id.btn_clear) {
            numbers_list.clear();
            passNumber(numbers_list);
        }
    }

    public void onPasswordListener(onPasswordListener _onPasswordListener) {
        onPasswordListener = _onPasswordListener;

    }

    public String getPasscode() {
        return passcode;
    }

    public void setPasscode(String passcode) {
        this.passcode = passcode;
    }

    private void passNumber(ArrayList<String> numbers_list) {
        if (numbers_list.size() <= 0) {
            dot_1.setBackgroundResource(R.drawable.passcode_background);
            dot_2.setBackgroundResource(R.drawable.passcode_background);
            dot_3.setBackgroundResource(R.drawable.passcode_background);
            dot_4.setBackgroundResource(R.drawable.passcode_background);
        } else {
            System.out.println(TextUtils.isEmpty(passcode));
            if (TextUtils.isEmpty(passcode)) {
                switch (numbers_list.size()) {
                    case 1:
                        dot_1.setBackgroundResource(R.drawable.passcode_overlay);
                        dot_2.setBackgroundResource(R.drawable.passcode_background);
                        dot_3.setBackgroundResource(R.drawable.passcode_background);
                        dot_4.setBackgroundResource(R.drawable.passcode_background);
                        break;
                    case 2:
                        dot_1.setBackgroundResource(R.drawable.passcode_overlay);
                        dot_2.setBackgroundResource(R.drawable.passcode_overlay);
                        dot_3.setBackgroundResource(R.drawable.passcode_background);
                        dot_4.setBackgroundResource(R.drawable.passcode_background);
                        break;
                    case 3:
                        dot_1.setBackgroundResource(R.drawable.passcode_overlay);
                        dot_2.setBackgroundResource(R.drawable.passcode_overlay);
                        dot_3.setBackgroundResource(R.drawable.passcode_overlay);
                        dot_4.setBackgroundResource(R.drawable.passcode_background);
                        break;
                    case 4:
                        dot_1.setBackgroundResource(R.drawable.passcode_overlay);
                        dot_2.setBackgroundResource(R.drawable.passcode_overlay);
                        dot_3.setBackgroundResource(R.drawable.passcode_overlay);
                        dot_4.setBackgroundResource(R.drawable.passcode_overlay);
                        if (TextUtils.isEmpty(firstInput) && TextUtils.isEmpty(secondInput)) {
                            firstInput = String.valueOf(numbers_list.stream().collect(Collectors.joining()).toCharArray());
                            message.setText(secondInputTip);
                            delayClear(200);
                            break;
                        }
                        if (!TextUtils.isEmpty(firstInput) && TextUtils.isEmpty(secondInput)) {
                            secondInput = String.valueOf(numbers_list.stream().collect(Collectors.joining()).toCharArray());
                            if (secondInput.equals(firstInput)) {
                                delayClear(100);
                                onPasswordListener.onPasswordCreated(secondInput);
                            } else {
                                message.setText(wrongInputTip);
                                firstInput = "";
                                secondInput = "";
                                delayClear(200);
                            }
                            break;
                        }
                        delayClear(200);
                        break;
                }
            } else
                switch (numbers_list.size()) {
                    case 1:
                        dot_1.setBackgroundResource(R.drawable.passcode_overlay);
                        dot_2.setBackgroundResource(R.drawable.passcode_background);
                        dot_3.setBackgroundResource(R.drawable.passcode_background);
                        dot_4.setBackgroundResource(R.drawable.passcode_background);
                        break;
                    case 2:
                        dot_1.setBackgroundResource(R.drawable.passcode_overlay);
                        dot_2.setBackgroundResource(R.drawable.passcode_overlay);
                        dot_3.setBackgroundResource(R.drawable.passcode_background);
                        dot_4.setBackgroundResource(R.drawable.passcode_background);
                        break;
                    case 3:
                        dot_1.setBackgroundResource(R.drawable.passcode_overlay);
                        dot_2.setBackgroundResource(R.drawable.passcode_overlay);
                        dot_3.setBackgroundResource(R.drawable.passcode_overlay);
                        dot_4.setBackgroundResource(R.drawable.passcode_background);
                        break;
                    case 4:
                        dot_1.setBackgroundResource(R.drawable.passcode_overlay);
                        dot_2.setBackgroundResource(R.drawable.passcode_overlay);
                        dot_3.setBackgroundResource(R.drawable.passcode_overlay);
                        dot_4.setBackgroundResource(R.drawable.passcode_overlay);
                        code = numbers_list.stream().collect(Collectors.joining()).toCharArray();
                        if (!TextUtils.isEmpty(passcode)) {
                            if (String.valueOf(code).equals(StringUtils.binaryToString(passcode, true))) {
                                lock.setBackgroundResource(R.drawable.lock_open);
                                message.setText(correctInputTip);
                                new Handler().postDelayed(() -> {
                                    numbers_list.clear();
                                    onPasswordListener.onPasswordMatch();
                                }, 200);
                            } else {
                                dot_1.setBackgroundResource(R.drawable.passcode_error);
                                dot_2.setBackgroundResource(R.drawable.passcode_error);
                                dot_3.setBackgroundResource(R.drawable.passcode_error);
                                dot_4.setBackgroundResource(R.drawable.passcode_error);
                                message.setText(wrongInputTip);
                                delayClear(600);
                                onPasswordListener.onPasswordNotMatch();
                                message.setText(firstInputTip);
                            }
                        }
                        break;
                }
        }
    }

    private void delayClear(long delay) {
        new Handler().postDelayed(() -> {
            numbers_list.clear();
            passNumber(numbers_list);
        }, delay);
    }

    public interface onPasswordListener {

        void onPasswordMatch();

        void onPasswordNotMatch();

        void onPasswordCreated(String password);
    }
}
