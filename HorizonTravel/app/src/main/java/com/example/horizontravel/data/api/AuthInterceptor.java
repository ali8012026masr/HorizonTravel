package com.example.horizontravel.data.api;

import androidx.annotation.NonNull;

import com.example.horizontravel.util.SessionManager;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class AuthInterceptor implements Interceptor {
    public interface SessionExpiredListener {
        void onSessionExpired();
    }

    private static SessionExpiredListener sessionExpiredListener;

    public static void setSessionExpiredListener(SessionExpiredListener listener) {
        sessionExpiredListener = listener;
    }

    @NonNull
    @Override
    public Response intercept(@NonNull Chain chain) throws IOException {
        Request originalRequest = chain.request();
        String path = originalRequest.url().encodedPath();

        Request.Builder requestBuilder = originalRequest.newBuilder();

        // Attach auth headers if not a public auth endpoint
        boolean isPublicAuth = path.contains("/auth/login") ||
                path.contains("/auth/register/") ||
                path.contains("/auth/forgot-password") ||
                path.contains("/auth/reset-password");

        SessionManager sessionManager = SessionManager.getInstance();
        if (!isPublicAuth && sessionManager.isLoggedIn()) {
            String accessToken = sessionManager.getAccessToken();
            if (accessToken != null && !accessToken.isEmpty()) {
                requestBuilder.header("Authorization", "Bearer " + accessToken);
            }
            String refreshToken = sessionManager.getRefreshToken();
            if (refreshToken != null && !refreshToken.isEmpty()) {
                requestBuilder.header("x-refresh-token", refreshToken);
            }
        }

        Response response = chain.proceed(requestBuilder.build());

        // Read token refreshed in header by backend
        String refreshedAccessToken = response.header("x-access-token");
        if (refreshedAccessToken != null && !refreshedAccessToken.trim().isEmpty()) {
            sessionManager.updateAccessToken(refreshedAccessToken);
        }

        // Handle 401 Unauthorized globally
        if (response.code() == 401 && !isPublicAuth) {
            sessionManager.clear();
            if (sessionExpiredListener != null) {
                sessionExpiredListener.onSessionExpired();
            }
        }

        return response;
    }
}
