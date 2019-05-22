package com.assettracker.Interfaces;

import com.assettracker.models.Result;

import java.util.Map;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.QueryMap;

public interface RetrofitInterface {
    @GET("/maps/api/directions/json")
    Call<Result> getpoint(@QueryMap Map<String, String> params);
}
