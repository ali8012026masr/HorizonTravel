package com.example.horizontravel.data.api;

import com.example.horizontravel.BuildConfig;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {
    private static ApiService apiService;
    private static Retrofit retrofit;
    private static Gson gson;

    public static synchronized void init() {
        if (apiService == null) {
            if (gson == null) {
                gson = new GsonBuilder()
                        .serializeNulls()
                        .create();
            }

            OkHttpClient.Builder okHttpBuilder = new OkHttpClient.Builder()
                    .connectTimeout(15, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .writeTimeout(30, TimeUnit.SECONDS)
                    .addInterceptor(new AuthInterceptor());

            if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
                logging.setLevel(HttpLoggingInterceptor.Level.BODY);
                okHttpBuilder.addInterceptor(logging);
            }

            retrofit = new Retrofit.Builder()
                    .baseUrl(getBaseUrl())
                    .client(okHttpBuilder.build())
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .build();

            apiService = retrofit.create(ApiService.class);
        }
    }

    public static String getBaseUrl() {
        return BuildConfig.BASE_URL;
    }

    public static synchronized ApiService getApiService() {
        if (apiService == null) {
            init();
        }
        return apiService;
    }

    public static synchronized Retrofit getRetrofit() {
        if (retrofit == null) {
            init();
        }
        return retrofit;
    }

    public static synchronized Gson getGson() {
        if (gson == null) {
            init();
        }
        return gson;
    }
}
