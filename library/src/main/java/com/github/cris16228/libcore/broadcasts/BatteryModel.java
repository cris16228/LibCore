package com.github.cris16228.libcore.broadcasts;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class BatteryModel {

    @SerializedName("chargingStatus")
    @Expose
    public String chargingStatus;

    @SerializedName("batteryCondition")
    @Expose
    public String batteryCondition;

    @SerializedName("powerSource")
    @Expose
    public String powerSource;

    @SerializedName("batteryTechnology")
    @Expose
    public String batteryTechnology;

    @SerializedName("voltageString")
    @Expose
    public String voltageString;

    @SerializedName("batteryLevel")
    @Expose
    public int batteryLevel;

    @SerializedName("batteryScale")
    @Expose
    public int batteryScale;

    @SerializedName("batteryHealth")
    @Expose
    public int batteryHealth;

    @SerializedName("temperatureC")
    @Expose
    public int temperatureC;

    @SerializedName("temperatureF")
    @Expose
    public int temperatureF;

    @SerializedName("batterySource")
    @Expose
    public int batterySource;

    @SerializedName("batteryStatus")
    @Expose
    public int batteryStatus;

    @SerializedName("currentMAh")
    @Expose
    public long currentMAh;

    @SerializedName("batteryVoltage")
    @Expose
    public int batteryVoltage;

    @SerializedName("energyIONow")
    @Expose
    public int energyIONow;

    @SerializedName("energyIOAverage")
    @Expose
    public int energyIOAverage;

    public String getChargingStatus() {
        return chargingStatus;
    }

    public void setChargingStatus(String chargingStatus) {
        this.chargingStatus = chargingStatus;
    }

    public String getBatteryCondition() {
        return batteryCondition;
    }

    public void setBatteryCondition(String batteryCondition) {
        this.batteryCondition = batteryCondition;
    }

    public String getPowerSource() {
        return powerSource;
    }

    public void setPowerSource(String powerSource) {
        this.powerSource = powerSource;
    }

    public String getBatteryTechnology() {
        return batteryTechnology;
    }

    public void setBatteryTechnology(String batteryTechnology) {
        this.batteryTechnology = batteryTechnology;
    }

    public String getVoltageString() {
        return voltageString;
    }

    public void setVoltageString(String voltageString) {
        this.voltageString = voltageString;
    }

    public int getBatteryLevel() {
        return batteryLevel;
    }

    public void setBatteryLevel(int batteryLevel) {
        this.batteryLevel = batteryLevel;
    }

    public int getBatteryScale() {
        return batteryScale;
    }

    public void setBatteryScale(int batteryScale) {
        this.batteryScale = batteryScale;
    }

    public int getBatteryHealth() {
        return batteryHealth;
    }

    public void setBatteryHealth(int batteryHealth) {
        this.batteryHealth = batteryHealth;
    }

    public int getTemperatureC() {
        return temperatureC;
    }

    public void setTemperatureC(int temperatureC) {
        this.temperatureC = temperatureC;
    }

    public int getTemperatureF() {
        return temperatureF;
    }

    public void setTemperatureF(int temperatureF) {
        this.temperatureF = temperatureF;
    }

    public int getBatterySource() {
        return batterySource;
    }

    public void setBatterySource(int batterySource) {
        this.batterySource = batterySource;
    }

    public int getBatteryStatus() {
        return batteryStatus;
    }

    public void setBatteryStatus(int batteryStatus) {
        this.batteryStatus = batteryStatus;
    }

    public long getCurrentMAh() {
        return currentMAh;
    }

    public void setCurrentMAh(long currentMAh) {
        this.currentMAh = currentMAh;
    }

    public int getBatteryVoltage() {
        return batteryVoltage;
    }

    public void setBatteryVoltage(int batteryVoltage) {
        this.batteryVoltage = batteryVoltage;
    }

    public int getEnergyIONow() {
        return energyIONow;
    }

    public void setEnergyIONow(int energyIONow) {
        this.energyIONow = energyIONow;
    }

    public int getEnergyIOAverage() {
        return energyIOAverage;
    }

    public void setEnergyIOAverage(int energyIOAverage) {
        this.energyIOAverage = energyIOAverage;
    }
}
