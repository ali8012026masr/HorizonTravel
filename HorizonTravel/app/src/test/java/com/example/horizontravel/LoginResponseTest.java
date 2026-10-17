package com.example.horizontravel;

import com.example.horizontravel.data.model.LoginResponse;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.junit.Test;

import static org.junit.Assert.*;

public class LoginResponseTest {
    @Test
    public void testAdminLoginResponseDeserialization() {
        String json = "{\"id\":1,\"fullName\":\"Horizon Admin\",\"email\":\"admin@horizon.demo\",\"role\":\"ADMIN\",\"roleDetails\":null,\"accessToken\":\"token123\",\"refreshToken\":\"ref123\",\"expiresInSeconds\":900}";
        Gson gson = new GsonBuilder().serializeNulls().create();
        try {
            LoginResponse response = gson.fromJson(json, LoginResponse.class);
            assertNotNull(response);
            assertEquals("ADMIN", response.getRole());
            System.out.println("SUCCESS: Deserialized successfully! roleDetails = " + response.getRoleDetails());
        } catch (Exception e) {
            System.err.println("FAILED WITH EXCEPTION: " + e.getClass().getName() + ": " + e.getMessage());
            e.printStackTrace();
            fail("Deserialization threw exception: " + e.getMessage());
        }
    }
}
