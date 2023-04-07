package com.example.myapplication;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class PriceEntity{
    public float holding ;
    public String name="";
    public String small_name="";
    PriceEntity(){}
    PriceEntity(String name, String small_name, int holding){
        this.name = name;
        this.small_name = small_name;
        this.holding = holding;

        Log.e("CWallet", "Entity " + name + " created");
    }
}