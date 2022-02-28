package com.samwoo.webview;

import android.util.Log;
import android.webkit.JavascriptInterface;

public class JavaScriptInterface {
    MainToJavaScriptInterfaceData mainToJavaScriptInterfaceData;

    public JavaScriptInterface(MainToJavaScriptInterfaceData mainToJavaScriptInterfaceData) {
        this.mainToJavaScriptInterfaceData = mainToJavaScriptInterfaceData;
    }

    @JavascriptInterface
    public void callQrScanner() {
        Log.d("TAG", "callQrScanner: ");
        mainToJavaScriptInterfaceData.moveToQrScannerActivity();
    }

    @JavascriptInterface
    public void callFingerPrint(boolean check) {
        Log.d("TAG", "callFingerPrint: " + check);
        mainToJavaScriptInterfaceData.moveToFingerPrintDialogActivity(check);
    }

    @JavascriptInterface
    public void setFcmToken(String userId){
        mainToJavaScriptInterfaceData.setFcmToken(userId);
        mainToJavaScriptInterfaceData.setUserId(userId);
    }

    @JavascriptInterface
    public void openNewWebView(String url){
        Log.d("TAG", "openNewWebView: "+url);
        mainToJavaScriptInterfaceData.setMoveToWebPage(url);
    }

    @JavascriptInterface
    public void share(String toWallet){
        Log.d("TAG", "toWallet: "+toWallet);
        mainToJavaScriptInterfaceData.setShareData(toWallet);
    }

    @JavascriptInterface
    public void setUserPhoneNumber(){
        Log.d("TAG", "setUserPhoneNumber: ");
        mainToJavaScriptInterfaceData.setUserPhoneNumber();
    }

    @JavascriptInterface
    public void setPrivateKey(String key){
        mainToJavaScriptInterfaceData.setPrivateKey(key);
    }

    @JavascriptInterface
    public void getPrivateKey(){
        mainToJavaScriptInterfaceData.getPrivateKey();
    }
}
