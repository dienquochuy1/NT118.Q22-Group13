package com.example.myapplication.auth;

import java.util.List;

public class AuthErrorInfo {
    private final String code;
    private final String message;
    private final List<String> fieldErrors;

    public AuthErrorInfo(String code, String message, List<String> fieldErrors) {
        this.code = code;
        this.message = message;
        this.fieldErrors = fieldErrors;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public List<String> getFieldErrors() {
        return fieldErrors;
    }
}

