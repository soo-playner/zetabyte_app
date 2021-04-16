package com.oneClick.webview.Retrofit;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

public interface RetrofitInterface {

    @FormUrlEncoded
    @POST("lib/fcm_push/registerFcmToken.php")
    Call<retrofitData> setFcmToken(@Field("userId") String userId, @Field("fcmToken") String fcmToken, @Field("phoneNumber") String phoneNumber);

    @FormUrlEncoded
    @POST("check_finger_pwd.php")
    Call<retrofitData> checkPwd(@Field("user_id") String userId,@Field("user_pwd") String userPwd);

}

