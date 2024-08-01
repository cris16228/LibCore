package com.github.cris16228.libcore.broadcasts;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.BatteryManager;

import com.github.cris16228.libcore.R;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.text.NumberFormat;
import java.util.Locale;

public class BatteryReceiver extends BroadcastReceiver {

    public String chargingStatus;
    public String batteryCondition;
    public String powerSource;
    public String batteryTechnology;
    public String batteryVoltageString;
    public int batteryLevel;
    public int batteryScale;
    public int batteryHealth;
    public int temperatureC;
    public int temperatureF;
    public int batterySource;
    public int batteryStatus;
    public long currentMAh;
    public int batteryVoltage;
    public int energyIONow;
    public int energyIOAverage;
    public onBatteryChange onBatteryChange;
    private Context _context;
    private BatteryModel batteryModel;

    private String batteryJson;

    public String getBatteryJson() {
        return batteryJson;
    }

    public void onBatteryChange(onBatteryChange _onBatteryChange) {
        onBatteryChange = _onBatteryChange;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (_context == null)
            _context = context;
        batteryLevel = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
        batteryScale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
        batteryHealth = intent.getIntExtra(BatteryManager.EXTRA_HEALTH, 0);
        switch (batteryHealth) {
            case BatteryManager.BATTERY_HEALTH_COLD:
                batteryCondition = context.getResources().getString(R.string.battery_condition_cold);
                break;
            case BatteryManager.BATTERY_HEALTH_DEAD:
                batteryCondition = context.getResources().getString(R.string.battery_condition_dead);
                break;
            case BatteryManager.BATTERY_HEALTH_GOOD:
                batteryCondition = context.getResources().getString(R.string.battery_condition_good);
                break;
            case BatteryManager.BATTERY_HEALTH_UNKNOWN:
                batteryCondition = context.getResources().getString(R.string.battery_condition_unknown);
                break;
            case BatteryManager.BATTERY_HEALTH_OVERHEAT:
                batteryCondition = context.getResources().getString(R.string.battery_condition_overheat);
                break;
            case BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE:
                batteryCondition = context.getResources().getString(R.string.battery_condition_high_voltage);
                break;
            case BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE:
                batteryCondition = context.getResources().getString(R.string.battery_condition_unspecified_failure);
                break;
        }
        temperatureC = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0) / 10;
        temperatureF = (int) (temperatureC * 1.8 + 32);
        batterySource = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
        switch (batterySource) {
            case 0:
                powerSource = context.getResources().getString(R.string.battery_power_source_unplugged);
                break;
            case BatteryManager.BATTERY_PLUGGED_AC:
                powerSource = context.getResources().getString(R.string.battery_power_source_ac);
                break;
            case BatteryManager.BATTERY_PLUGGED_USB:
                powerSource = context.getResources().getString(R.string.battery_power_source_usb);
                break;
            case BatteryManager.BATTERY_PLUGGED_WIRELESS:
                powerSource = context.getResources().getString(R.string.battery_power_source_wireless);
                break;
        }
        batteryStatus = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
        switch (batteryStatus) {
            case BatteryManager.BATTERY_STATUS_CHARGING:
                chargingStatus = context.getResources().getString(R.string.battery_status_charging);
                break;
            case BatteryManager.BATTERY_STATUS_DISCHARGING:
                chargingStatus = context.getResources().getString(R.string.battery_status_discharging);
                break;
            case BatteryManager.BATTERY_STATUS_UNKNOWN:
                chargingStatus = context.getResources().getString(R.string.battery_status_unknown);
                break;
            case BatteryManager.BATTERY_STATUS_NOT_CHARGING:
                chargingStatus = context.getResources().getString(R.string.battery_status_not_charging);
                break;
            case BatteryManager.BATTERY_STATUS_FULL:
                chargingStatus = context.getResources().getString(R.string.battery_status_full);
                break;
        }
        batteryTechnology = intent.getStringExtra(BatteryManager.EXTRA_TECHNOLOGY);
        batteryVoltage = intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0);
        batteryVoltageString = NumberFormat.getNumberInstance(Locale.US)
                .format(batteryVoltage)
                .replace(",", "،");
        BatteryManager mBatteryManager = (BatteryManager) context.getSystemService(Context.BATTERY_SERVICE);
        currentMAh = mBatteryManager.getLongProperty(BatteryManager.BATTERY_PROPERTY_CHARGE_COUNTER);
        energyIONow = mBatteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW);
        energyIOAverage = mBatteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_AVERAGE);
        if (onBatteryChange != null)
            onBatteryChange.update();
        batteryModel = new BatteryModel();
        batteryModel.setChargingStatus(chargingStatus);
        batteryModel.setBatteryCondition(batteryCondition);
        batteryModel.setPowerSource(powerSource);
        batteryModel.setBatteryTechnology(batteryTechnology);
        batteryModel.setVoltageString(batteryVoltageString);
        batteryModel.setBatteryLevel(batteryLevel);
        batteryModel.setBatteryScale(batteryScale);
        batteryModel.setBatteryHealth(batteryHealth);
        batteryModel.setTemperatureC(temperatureC);
        batteryModel.setTemperatureF(temperatureF);
        batteryModel.setBatterySource(batterySource);
        batteryModel.setBatteryStatus(batteryStatus);
        batteryModel.setCurrentMAh(currentMAh);
        batteryModel.setBatteryVoltage(batteryVoltage);
        batteryModel.setEnergyIONow(energyIONow);
        batteryModel.setEnergyIOAverage(energyIOAverage);
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        batteryJson = gson.toJson(batteryModel);
    }

    public interface onBatteryChange {
        void update();
    }
}