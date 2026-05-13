package com.example.myapplication.data;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class ApiError {
    @SerializedName("code")
    private String code;

    @SerializedName("details")
    private List<ValidationDetail> details;

    public String getCode() {
        return code;
    }

    public List<ValidationDetail> getDetails() {
        return details;
    }
}

