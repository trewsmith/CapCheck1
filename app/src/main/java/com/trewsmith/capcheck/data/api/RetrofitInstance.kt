package com.trewsmith.capcheck.data.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance {
    val api: StockApi by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.massive.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(StockApi::class.java)
    }
}