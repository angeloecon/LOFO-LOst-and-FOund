package com.shruti.lofo.api;

import com.shruti.lofo.data.model.Item;
import com.shruti.lofo.data.model.LoginRequest;
import com.shruti.lofo.data.model.LoginResponse;
import com.shruti.lofo.data.model.RegisterRequest;
import com.shruti.lofo.data.model.RegisterResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;


public interface ApiService {
    @GET("/api/items")
    Call<List<Item>> getItems(@Query("type") String type);

    @POST("/api/items")
    Call<Item> createItem(@Body Item newItem);

    @POST("/api/login")
    Call<LoginResponse> loginUser(@Body LoginRequest loginRequest);

    @POST("/api/register")
    Call<RegisterResponse> registerUser(@Body RegisterRequest registerRequest);
}
