package com.github.cris16228.libcore.models;

import java.util.List;
import java.util.stream.IntStream;

public class PieChartModel {

    private String title;
    private double value;
    private String color;

    public String getTitle() {
        return title;
    }

    public PieChartModel(String title, double value) {
        this.title = title;
        this.value = value;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    public PieChartModel(String title, double value, String color) {
        this.title = title;
        this.value = value;
        this.color = color;
    }

    public PieChartModel() {
    }

    public static int getIndex(List<PieChartModel> pieChartModels, String title) {
        return IntStream.range(0, pieChartModels.size()).filter(i -> pieChartModels.get(i).getTitle().equals(title)).findFirst().orElse(-1);
    }

    public void setTile(String title) {
        this.title = title;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }
}
