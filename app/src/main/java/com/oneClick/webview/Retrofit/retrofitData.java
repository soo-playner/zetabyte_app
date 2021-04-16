package com.oneClick.webview.Retrofit;

public class retrofitData {
    String userId,fcmToken,result;

    public retrofitData(String uerId, String fcmToken,String result) {
        this.userId = uerId;
        this.fcmToken = fcmToken;
        this.result = result;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getFcmToken() {
        return fcmToken;
    }

    public void setFcmToken(String fcmToken) {
        this.fcmToken = fcmToken;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }
}
