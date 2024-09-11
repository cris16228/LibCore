package com.github.cris16228.libcore.view;

import android.content.Context;
import android.graphics.Typeface;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StrikethroughSpan;
import android.text.style.StyleSpan;
import android.text.style.SuperscriptSpan;
import android.text.style.UnderlineSpan;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.widget.AppCompatTextView;

import com.github.cris16228.libcore.StringUtils;

import java.lang.reflect.Field;

public class MarkdownTextView extends AppCompatTextView {

    private char escapeCharacter = '\\';
    private String originalText;

    public MarkdownTextView(@NonNull Context context) {
        super(context);
    }

    public MarkdownTextView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public MarkdownTextView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public char getEscapeCharacter() {
        return escapeCharacter;
    }

    public void setEscapeCharacter(char escapeCharacter) {
        this.escapeCharacter = escapeCharacter;
    }

    public void setMarkdownText(String text) {
        setText(text);
    }

    public void setMarkdownText(@StringRes int resId) {
        setText(getResources().getString(resId));
    }

    public String getPlainText() {
        return originalText.replace("\n", "\\n");
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        CharSequence text = getText();
        if (!StringUtils.isEmpty(text)) {
            originalText = text.toString();
            Spannable markdownText = parseMarkdown(text.toString());
            try {
                Field mTextField = AppCompatTextView.class.getDeclaredField("mText");
                mTextField.setAccessible(true);
                mTextField.set(this, markdownText);
            } catch (NoSuchFieldException | IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void setText(CharSequence text, BufferType type) {
        originalText = text.toString();
        if (StringUtils.isEmpty(text)) {
            super.setText(text, type);
            return;
        }
        Spannable processedText = parseMarkdown(text.toString());
        super.setText(processedText, type);
    }

    private Spannable parseMarkdown(String text) {
        SpannableStringBuilder spannable = new SpannableStringBuilder(text);
        applyLists(spannable);
        applySuperScript(spannable);
        applyHeader(spannable);
        applySpan(spannable, "**", new StyleSpan(Typeface.BOLD));
        applySpan(spannable, "__", new StyleSpan(Typeface.ITALIC));
        applySpan(spannable, "_", new UnderlineSpan());
        applySpan(spannable, "~~", new StrikethroughSpan());
        return spannable;
    }

    private void applyHeader(SpannableStringBuilder spannable) {
        String text = spannable.toString();
        String[] lines = text.split("\n");
        int start;
        for (String line : lines) {
            int headerLevel = countLevel(line);
            if (headerLevel > 0) {
                if (line.length() <= headerLevel) {
                    return;
                }
                start = text.indexOf(line);
                float sizeMultiplier = getMultiplier(headerLevel);
                spannable.setSpan(new RelativeSizeSpan(sizeMultiplier), start, start + line.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                if (sizeMultiplier > 1.0f)
                    spannable.setSpan(new StyleSpan(Typeface.BOLD), start, start + line.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                spannable.replace(start, start + headerLevel, "");
                text = spannable.toString();
            }
        }
    }

    private float getMultiplier(int headerLevel) {
        switch (headerLevel) {
            case 1:
                return 3.0f;
            case 2:
                return 2.0f;
            case 3:
                return 1.2f;
            default:
                return 1.0f;
        }
    }

    private int countLevel(String line) {
        int count = 0;
        while (count < line.length() && line.charAt(count) == '#') {
            count++;
        }
        return count;
    }

    private void applySuperScript(SpannableStringBuilder spannable) {
        String text = spannable.toString();
        int start = text.indexOf("^");
        while (start != -1) {
            int end = text.indexOf("^", start + 1);
            if (end != -1) {
                spannable.setSpan(new SuperscriptSpan(), start, end + 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                spannable.setSpan(new RelativeSizeSpan(.8f), start, end + 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                spannable.delete(end, end + 1);
                spannable.delete(start, start + 1);
                text = spannable.toString();
                start = text.indexOf("^", start);
            } else {
                break;
            }
        }
    }

    private void applySpan(SpannableStringBuilder spannable, String delimiter, Object span) {
        String text = spannable.toString();
        int start = text.indexOf(delimiter);
        while (start != -1) {
            if (start > 0 && text.charAt(start - 1) == escapeCharacter) {
                start = text.indexOf(delimiter, start + delimiter.length());
                continue;
            }
            int end = text.indexOf(delimiter, start + delimiter.length());
            if (end != -1) {
                if (text.substring(start + delimiter.length(), end).isEmpty()) {
                    return;
                }
                spannable.setSpan(span, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                spannable.delete(end, end + delimiter.length());
                spannable.delete(start, start + delimiter.length());
                text = spannable.toString();
                start = text.indexOf(delimiter, start);
            } else {
                break;
            }
        }
    }

    private void applyLists(SpannableStringBuilder spannable) {
        String text = spannable.toString();
        String[] lines = text.split("\n");
        StringBuilder updatedText = new StringBuilder();
        int currentOrderedList = 1;
        for (String line : lines) {
            if (line.startsWith("- ")) {
                int start = text.indexOf(line);
                spannable.setSpan(new ForegroundColorSpan(0xFF888888), start, start + line.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                updatedText.append("• ").append(line.substring(2)).append("\n");
            } else if (line.startsWith("* ")) {
                int start = text.indexOf(line);
                spannable.setSpan(new ForegroundColorSpan(0xFF888888), start, start + line.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                updatedText.append(currentOrderedList).append(". ").append(line.substring(2)).append("\n");
                currentOrderedList++;
            } else {
                updatedText.append(line).append("\n");
            }
        }
        spannable.clear();
        spannable.append(updatedText.toString().trim());
    }
}
