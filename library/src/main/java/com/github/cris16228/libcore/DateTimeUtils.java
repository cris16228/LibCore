package com.github.cris16228.libcore;

import android.content.Context;
import android.text.TextUtils;
import android.widget.AutoCompleteTextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

public class DateTimeUtils {

    private String DEFAULT_FORMAT_DATE = "dd/MM/yyyy";
    private String DEFAULT_FORMAT_TIME = "HH:mm";
    private String DEFAULT_FORMAT = "EE MMM dd HH:mm:ss z yyyy";
    private long date;
    private long time;
    private onDateTimeSet onDateTimeSet;

    public String getDefaultFormat() {
        return DEFAULT_FORMAT;
    }

    public void setDefaultFormat(String defaultFormat) {
        this.DEFAULT_FORMAT = defaultFormat;
    }

    public String getDefaultFormatDate() {
        return DEFAULT_FORMAT_DATE;
    }

    public void setDefaultFormatDate(String defaultFormatDate) {
        this.DEFAULT_FORMAT_DATE = defaultFormatDate;
    }

    public String getDefaultFormatTime() {
        return DEFAULT_FORMAT_TIME;
    }

    public void setDefaultFormatTime(String defaultFormatTime) {
        this.DEFAULT_FORMAT_TIME = defaultFormatTime;
    }

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public void setOnDateTimeSet(onDateTimeSet onDateTimeSet) {
        this.onDateTimeSet = onDateTimeSet;
    }

    public void getDateTime(AppCompatActivity activity, Object datetime, long initValue, boolean isHint, onDateTimeSet onDateTimeSet) {
        Context context = activity.getBaseContext();
        StringBuilder dateTime = new StringBuilder();
        AtomicLong date_ms = new AtomicLong(initValue);
        MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker().setTitleText("Select date").setSelection(date_ms.get() == 0L ? MaterialDatePicker.todayInUtcMilliseconds() : date_ms.get()).build();
        datePicker.addOnPositiveButtonClickListener(selection -> {
            dateTime.append(new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date(selection)));
            dateTime.append(" ");
            date_ms.set(selection);
            Calendar calendar = Calendar.getInstance(Locale.getDefault());
            MaterialTimePicker timePicker = new MaterialTimePicker.Builder()
                    .setTitleText("Select time")
                    .setTimeFormat(TimeFormat.CLOCK_24H)
                    .setHour(calendar.get(Calendar.HOUR_OF_DAY)).setMinute(calendar.get(Calendar.MINUTE)).build();
            timePicker.addOnPositiveButtonClickListener(v -> {
                dateTime.append(timePicker.getHour()).append(":").append(timePicker.getMinute() == 0 ? "00" : (timePicker.getMinute() <= 9 ? "0" + timePicker.getMinute() : timePicker.getMinute()));
                if (isHint) {
                    if (datetime instanceof AutoCompleteTextView) {
                        try {
                            AutoCompleteTextView autoCompleteTextView = (AutoCompleteTextView) datetime;
                            autoCompleteTextView.setHint(dateTime.toString());
                        } catch (Exception e) {
                            e.printStackTrace();
                            FileUtils.with(context).debugLog(e.toString());
                        }
                    } else if (datetime instanceof TextInputEditText) {
                        try {
                            TextInputEditText textInputEditText = (TextInputEditText) datetime;
                            textInputEditText.setHint(dateTime.toString());
                        } catch (Exception e) {
                            e.printStackTrace();
                            FileUtils.with(context).debugLog(e.toString());
                        }
                    }
                } else {
                    if (datetime instanceof AutoCompleteTextView) {
                        try {
                            AutoCompleteTextView autoCompleteTextView = (AutoCompleteTextView) datetime;
                            autoCompleteTextView.setText(dateTime.toString());
                        } catch (Exception e) {
                            e.printStackTrace();
                            FileUtils.with(context).debugLog(e.toString());
                        }
                    } else if (datetime instanceof TextInputEditText) {
                        try {
                            TextInputEditText textInputEditText = (TextInputEditText) datetime;
                            textInputEditText.setText(dateTime.toString());
                        } catch (Exception e) {
                            e.printStackTrace();
                            FileUtils.with(context).debugLog(e.toString());
                        }
                    }
                }
                if (onDateTimeSet != null) {
                    onDateTimeSet.onConfirm(dateTime.toString());
                }
            });
            timePicker.show(activity.getSupportFragmentManager(), context.getClass().getSimpleName());
        });
        datePicker.show(activity.getSupportFragmentManager(), context.getClass().getSimpleName());
    }

    public LocalDateTime convertToDateTime(long milliseconds) {
        ZonedDateTime zonedDateTime = ZonedDateTime.ofInstant(Instant.ofEpochMilli(milliseconds), ZoneId.systemDefault());
        return zonedDateTime.with(LocalTime.MIDNIGHT).toLocalDateTime();
    }

    public long calculateDaysLeft(long nextBillingDateInMillis) {
        Instant currentDateInstant = Instant.now();
        currentDateInstant = currentDateInstant.atZone(ZoneOffset.systemDefault()).withHour(0).withMinute(0).withSecond(0).withNano(0).toInstant();
        Instant nextBillingDateInstant = Instant.ofEpochMilli(nextBillingDateInMillis);
        Duration duration = Duration.between(currentDateInstant, nextBillingDateInstant);
        return duration.toDays();
    }

    public long convertToMillis(LocalDateTime dateTime) {
        return dateTime.toInstant(ZoneOffset.UTC).toEpochMilli();
    }

    public boolean isAfter(long checked, long toCheck) {
        ZonedDateTime checkedTime = ZonedDateTime.ofInstant(Instant.ofEpochMilli(checked), ZoneId.of("UTC"));
        ZonedDateTime toCheckTime = ZonedDateTime.ofInstant(Instant.ofEpochMilli(toCheck), ZoneId.of("UTC"));
        return checkedTime.isAfter(toCheckTime);
    }

    public void getDate(AppCompatActivity activity, Object datetime, long initValue, boolean isHint, onDateTimeSet onDateTimeSet) {
        Context context = activity.getBaseContext();
        AtomicLong date_ms = new AtomicLong(initValue);
        StringBuilder dateTime = new StringBuilder();
        MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker().setTitleText("Select date").setSelection(date_ms.get() == 0L ? MaterialDatePicker.todayInUtcMilliseconds() : date_ms.get()).build();
        datePicker.addOnPositiveButtonClickListener(selection -> {
            dateTime.append(new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date(selection)));
            date_ms.set(selection);
            if (isHint) {
                if (datetime instanceof AutoCompleteTextView) {
                    try {
                        AutoCompleteTextView autoCompleteTextView = (AutoCompleteTextView) datetime;
                        autoCompleteTextView.setHint(dateTime.toString());
                    } catch (Exception e) {
                        e.printStackTrace();
                        FileUtils.with(context).debugLog(e.toString());
                    }
                } else if (datetime instanceof TextInputEditText) {
                    try {
                        TextInputEditText textInputEditText = (TextInputEditText) datetime;
                        textInputEditText.setHint(dateTime.toString());
                    } catch (Exception e) {
                        e.printStackTrace();
                        FileUtils.with(context).debugLog(e.toString());
                    }
                }
            } else {
                if (datetime instanceof AutoCompleteTextView) {
                    try {
                        AutoCompleteTextView autoCompleteTextView = (AutoCompleteTextView) datetime;
                        autoCompleteTextView.setText(dateTime.toString());
                    } catch (Exception e) {
                        e.printStackTrace();
                        FileUtils.with(context).debugLog(e.toString());
                    }
                } else if (datetime instanceof TextInputEditText) {
                    try {
                        TextInputEditText textInputEditText = (TextInputEditText) datetime;
                        textInputEditText.setText(dateTime.toString());
                    } catch (Exception e) {
                        e.printStackTrace();
                        FileUtils.with(context).debugLog(e.toString());
                    }
                }
            }
            if (onDateTimeSet != null) {
                onDateTimeSet.onConfirm(dateTime.toString());
            }
        });
        datePicker.show(activity.getSupportFragmentManager(), context.getClass().getSimpleName());
    }

    public void getDate(AppCompatActivity activity, Object datetime, long initValue, String dateFormat, boolean isHint, onDateTimeSet onDateTimeSet) {
        Context context = activity.getBaseContext();
        AtomicLong date_ms = new AtomicLong(initValue);
        StringBuilder dateTime = new StringBuilder();
        MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker().setTitleText("Select date").setSelection(date_ms.get() == 0L ? MaterialDatePicker.todayInUtcMilliseconds() : date_ms.get()).build();
        datePicker.addOnPositiveButtonClickListener(selection -> {
            dateTime.append(new SimpleDateFormat(StringUtils.isEmpty(dateFormat) ? "dd/MM/yyyy" : dateFormat, Locale.getDefault()).format(new Date(selection)));
            date_ms.set(selection);
            if (isHint) {
                if (datetime instanceof AutoCompleteTextView) {
                    try {
                        AutoCompleteTextView autoCompleteTextView = (AutoCompleteTextView) datetime;
                        autoCompleteTextView.setHint(dateTime.toString());
                    } catch (Exception e) {
                        e.printStackTrace();
                        FileUtils.with(context).debugLog(e.toString());
                    }
                } else if (datetime instanceof TextInputEditText) {
                    try {
                        TextInputEditText textInputEditText = (TextInputEditText) datetime;
                        textInputEditText.setHint(dateTime.toString());
                    } catch (Exception e) {
                        e.printStackTrace();
                        FileUtils.with(context).debugLog(e.toString());
                    }
                } else if (datetime instanceof TextInputLayout) {
                    try {
                        TextInputLayout textInputLayout = (TextInputLayout) datetime;
                        textInputLayout.setHint(dateTime.toString());
                    } catch (Exception e) {
                        e.printStackTrace();
                        FileUtils.with(context).debugLog(e.toString());
                    }
                }
            } else {
                if (datetime instanceof AutoCompleteTextView) {
                    try {
                        AutoCompleteTextView autoCompleteTextView = (AutoCompleteTextView) datetime;
                        autoCompleteTextView.setText(dateTime.toString());
                    } catch (Exception e) {
                        e.printStackTrace();
                        FileUtils.with(context).debugLog(e.toString());
                    }
                } else if (datetime instanceof TextInputEditText) {
                    try {
                        TextInputEditText textInputEditText = (TextInputEditText) datetime;
                        textInputEditText.setText(dateTime.toString());
                    } catch (Exception e) {
                        e.printStackTrace();
                        FileUtils.with(context).debugLog(e.toString());
                    }
                } else if (datetime instanceof TextInputLayout) {
                    try {
                        TextInputLayout textInputLayout = (TextInputLayout) datetime;
                        textInputLayout.setHint(dateTime.toString());
                    } catch (Exception e) {
                        e.printStackTrace();
                        FileUtils.with(context).debugLog(e.toString());
                    }
                }
            }
            if (onDateTimeSet != null) {
                onDateTimeSet.onConfirm(dateTime.toString());
            }
        });
        datePicker.show(activity.getSupportFragmentManager(), context.getClass().getSimpleName());
    }

    public String getDateTime(long millis, String formatter) {
        if (TextUtils.isEmpty(formatter))
            formatter = DEFAULT_FORMAT;
        return new SimpleDateFormat(formatter, Locale.getDefault()).format(millis);
    }

    public long dateToExact(String date, String dateFormat) {
        Calendar current = Calendar.getInstance();
        current.setTime(new Date());
        Calendar exactCal = Calendar.getInstance();
        exactCal.setTime(new Date(getMilliseconds(date, StringUtils.isEmpty(dateFormat) ? "dd/MM/yyyy" : dateFormat)));
        exactCal.set(Calendar.HOUR_OF_DAY, current.get(Calendar.HOUR_OF_DAY));
        exactCal.set(Calendar.HOUR, 0);
        exactCal.set(Calendar.MINUTE, 0);
        exactCal.set(Calendar.SECOND, 0);
        exactCal.set(Calendar.MILLISECOND, 0);
        return exactCal.getTimeInMillis();
    }

    private String timePicker(MaterialTimePicker timePicker) {
        return timePicker.getHour() + ":" + (timePicker.getMinute() == 0 ? "00" : timePicker.getMinute());
    }

    public String getDateFormat(MaterialTimePicker timePicker) {
        StringBuilder dateTime = new StringBuilder();
        dateTime.append(new SimpleDateFormat(DEFAULT_FORMAT_DATE, Locale.getDefault()).format(new Date(getDate())));
        dateTime.append(timePicker(timePicker));
        return dateTime.toString();
    }

    public String videoTimeConvert(String millis) {
        if (StringUtils.isEmpty(millis)) {
            return String.format("%02d:%02d", 0, 0);
        } else {
            long value = Long.parseLong(millis);
            long hours = TimeUnit.MILLISECONDS.toHours(value);
            long minutes = TimeUnit.MILLISECONDS.toMinutes(value) % 60;
            long seconds = TimeUnit.MILLISECONDS.toSeconds(value) % 60;

            if (hours > 0) {
                return String.format("%02d:%02d:%02d", hours, minutes, seconds);
            } else {
                return String.format("%02d:%02d", minutes, seconds);
            }
        }
    }

    public long getMilliseconds(String date) {
        return getMilliseconds(date, DEFAULT_FORMAT);
    }

    public long getMilliseconds(String date, String formatting) {
        SimpleDateFormat sdf = new SimpleDateFormat(formatting,
                Locale.getDefault());
        Date temp_date = null;
        try {
            temp_date = sdf.parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return temp_date != null ? temp_date.getTime() : 0;
    }

    public interface onDateTimeSet {

        String onConfirm(String datetime);
    }
}
