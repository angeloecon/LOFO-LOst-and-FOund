package com.shruti.lofo.api;

import com.shruti.lofo.data.model.Item;
import com.shruti.lofo.data.model.ItemRequest;
import com.shruti.lofo.data.model.LoginRequest;
import com.shruti.lofo.data.model.LoginResponse;
import com.shruti.lofo.data.model.RegisterRequest;
import com.shruti.lofo.data.model.RegisterResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;


public interface ApiService {
    // API CALLS IN CASE OF EMERGENCY CALL 911

    // GET : FETCHES ITEM DETAILS
    @GET("/api/items/{id}")
    Call<Item> getItemById(@Path("id") int id);

    // GET : FETCHES ALL ITEMS BY TYPE IF ETHER LOST/FOUND
    @GET("/api/items")
    Call<List<Item>> getItems(@Query("type") String type);

    // CREATE : CREATING NEW ITEM
    @POST("/api/items")
    Call<Item> createItem(@Body ItemRequest itemRequest);

    // AUTH

    // LOGIN
    @POST("/api/login")
    Call<LoginResponse> loginUser(@Body LoginRequest loginRequest);

    // REGISTER
    @POST("/api/register")
    Call<RegisterResponse> registerUser(@Body RegisterRequest registerRequest);


}
