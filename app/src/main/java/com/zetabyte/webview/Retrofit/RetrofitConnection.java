package com.zetabyte.webview.Retrofit;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitConnection {
    String URL = "https://zetabyte.ai/";

    Retrofit retrofit = new Retrofit.Builder()
            .baseUrl(URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build();

    public RetrofitInterface  server = retrofit.create(RetrofitInterface.class);
}
