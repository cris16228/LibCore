package com.github.cris16228.libcore.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

public class UUIDS implements Serializable {

    private final static long serialVersionUID = -5957354940189877332L;
    @SerializedName("uuids")
    @Expose
    private List<UUID> uuids = null;

    public List<UUID> getUuids() {
        return uuids;
    }

    public void setUuids(List<UUID> uuids) {
        this.uuids = uuids;
    }

}