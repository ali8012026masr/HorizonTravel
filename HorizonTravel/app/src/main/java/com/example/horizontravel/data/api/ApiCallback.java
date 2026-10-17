package com.example.horizontravel.data.api;

import androidx.annotation.NonNull;

import com.example.horizontravel.data.model.ErrorResponse;
import com.google.gson.Gson;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public abstract class ApiCallback<T> implements Callback<T> {

    public abstract void onSuccess(T data);

    public abstract void onError(String message, int statusCode);

    public void onError(String message, int statusCode, Map<String, String> fieldErrors) {
        onError(message, statusCode);
    }

    @Override
    public void onResponse(@NonNull Call<T> call, @NonNull Response<T> response) {
        if (response.isSuccessful()) {
            onSuccess(response.body());
            return;
        }

        int statusCode = response.code();
        String errorMessage = "Request failed (" + statusCode + ")";
        Map<String, String> fieldErrors = null;

        if (response.errorBody() != null) {
            try {
                String errorBodyString = response.errorBody().string();
                Gson gson = ApiClient.getGson();
                ErrorResponse errorResponse = gson.fromJson(errorBodyString, ErrorResponse.class);
                if (errorResponse != null) {
                    if (errorResponse.getMessage() != null && !errorResponse.getMessage().trim().isEmpty()) {
                        errorMessage = errorResponse.getMessage().trim();
                    }
                    if (errorResponse.getErrors() != null && !errorResponse.getErrors().isEmpty()) {
                        fieldErrors = errorResponse.getErrors();
                    }
                }
            } catch (Exception ignored) {
            }
        }

        if (statusCode == 423) {
            if ("Request failed (423)".equals(errorMessage)) {
                errorMessage = "Account is locked. Please contact support.";
            }
        } else if (statusCode == 429) {
            if ("Request failed (429)".equals(errorMessage)) {
                errorMessage = "Rate limit reached. Please wait a moment before trying again.";
            }
        } else if (statusCode == 401) {
            if ("Request failed (401)".equals(errorMessage)) {
                errorMessage = "Invalid credentials or session expired.";
            }
        } else if (statusCode == 409) {
            if ("Request failed (409)".equals(errorMessage)) {
                errorMessage = "Email already registered or resource conflict.";
            }
        }

        onError(errorMessage, statusCode, fieldErrors);
    }

    @Override
    public void onFailure(@NonNull Call<T> call, @NonNull Throwable t) {
        int statusCode = -1;
        String errorMessage;
        if (t instanceof SocketTimeoutException) {
            errorMessage = "Connection timed out. Please try again.";
        } else if (t instanceof UnknownHostException) {
            errorMessage = "Unable to reach server. Please check your network connection.";
        } else if (t instanceof IOException) {
            errorMessage = "Network error occurred. Please check your connection.";
        } else {
            errorMessage = t.getMessage() != null ? t.getMessage() : "An unexpected error occurred.";
        }
        onError(errorMessage, statusCode);
    }
}
