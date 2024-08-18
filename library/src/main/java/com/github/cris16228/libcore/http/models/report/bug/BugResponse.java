package com.github.cris16228.libcore.http.models.report.bug;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class BugResponse {
    @SerializedName("status_message")
    @Expose
    public String statusMessage;
    @SerializedName("status")
    @Expose
    private String status;
    @SerializedName("error")
    @Expose
    private String error;
    @SerializedName("data")
    @Expose
    private BugData data;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getStatusMessage() {
        return statusMessage;
    }

    public void setStatusMessage(String statusMessage) {
        this.statusMessage = statusMessage;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public BugData getData() {
        return data;
    }

    public void setData(BugData bugData) {
        this.data = bugData;
    }

}
