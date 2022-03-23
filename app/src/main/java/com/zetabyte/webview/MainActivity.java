package com.zetabyte.webview;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.net.Uri;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Environment;

import android.telephony.TelephonyManager;
import android.util.Log;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;


import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import com.google.android.play.core.appupdate.AppUpdateInfo;
import com.google.android.play.core.appupdate.AppUpdateManager;
import com.google.android.play.core.appupdate.AppUpdateManagerFactory;
import com.google.android.play.core.install.model.AppUpdateType;
import com.google.android.play.core.install.model.UpdateAvailability;

import com.google.firebase.dynamiclinks.ShortDynamicLink;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.zetabyte.webview.FingerPrint.FingerPrintDialogActivity;
import com.zetabyte.webview.QrScan.QrScannerActivity;


import com.zetabyte.webview.Retrofit.RetrofitConnection;
import com.zetabyte.webview.Retrofit.retrofitData;
import com.zetabyte.webview.SharedPreferences.PreferenceManager;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.dynamiclinks.DynamicLink;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;
import com.google.firebase.dynamiclinks.PendingDynamicLinkData;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class MainActivity extends AppCompatActivity implements MainToJavaScriptInterfaceData {
    String telNumber,device_key;
    private String state;
    private WebView webView;
    private static final int REQUEST_FINGER_PRINT_CODE = 999;
    private static final int REQUEST_QR_SCANNER_CODE = 888;
    private static final int REQUEST_CODE = 366;
    private AppUpdateManager appUpdateManager;

//    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 앱 업데이트 매니저 초기화
        appUpdateManager = AppUpdateManagerFactory.create(this);

        // 업데이트를 체크하는데 사용되는 인텐트를 리턴한다.
        com.google.android.play.core.tasks.Task<AppUpdateInfo> appUpdateInfoTask = appUpdateManager.getAppUpdateInfo();

        // Checks that the platform will allow the specified type of update.
        appUpdateInfoTask.addOnSuccessListener(appUpdateInfo -> { // appUpdateManager이 추가되는데 성공하면 발생하는 이벤트
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE // UpdateAvailability.UPDATE_AVAILABLE == 2 이면 앱 true
                    && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)) { // 허용된 타입의 앱 업데이트이면 실행 (AppUpdateType.IMMEDIATE || AppUpdateType.FLEXIBLE)
                // 업데이트가 가능하고, 상위 버전 코드의 앱이 존재하면 업데이트를 실행한다.
                requestUpdate (appUpdateInfo);
            }
        });

        webView = (WebView) findViewById(R.id.webView);

        WebSettings webSettings = webView.getSettings();

        webSettings.setJavaScriptEnabled(true);
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true); // 팝업
        webSettings.setLoadsImagesAutomatically(true); // 이미지 리소스 다운
        webSettings.setUseWideViewPort(false); // 가로모드
        webSettings.setSupportZoom(true); // 확대,축소
        webSettings.setDomStorageEnabled(false); // 로컬스토리지
        webSettings.setAppCacheEnabled(false); // 앱 캐시사용
        webSettings.setAllowFileAccessFromFileURLs(true); // 파일
        webSettings.setSaveFormData(false);
        webSettings.setSupportMultipleWindows(true);
        webSettings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);

        webView.addJavascriptInterface(new JavaScriptInterface(this), "App");
        webView.getSettings().setUserAgentString(
                this.webView.getSettings().getUserAgentString()
                        + " "
                        + getString(R.string.user_agent_suffix)
        );
        webView.loadUrl(this.getResources().getString(R.string.home));
        webView.setWebViewClient(new WebViewClient());
        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onJsAlert(WebView view, String url, String message, final JsResult result) {  // alert
//                new AlertDialog.Builder(MainActivity.this, R.style.MyAlertDialogTheme).setTitle(R.string.alert).setMessage("\n\t" + message).setIcon(R.drawable.ic_priority_high_black_24dp)
//                        .setPositiveButton(android.R.string.ok, new AlertDialog.OnClickListener() {
//                            public void onClick(DialogInterface dialog, int which) {
                                 Toast.makeText(MainActivity.this, ""+message, Toast.LENGTH_LONG).show();
                                result.confirm();
//                            }
//                        }).setCancelable(false).create().show();
                return true;
            }
        });
        getNumber();
        handleDeepLink();


    }

    // 업데이트 요청
    private void requestUpdate (AppUpdateInfo appUpdateInfo) {
        try {
            appUpdateManager.startUpdateFlowForResult(
                    // 'getAppUpdateInfo()' 에 의해 리턴된 인텐트
                    appUpdateInfo,
                    // 'AppUpdateType.FLEXIBLE': 사용자에게 업데이트 여부를 물은 후 업데이트 실행 가능
                    // 'AppUpdateType.IMMEDIATE': 사용자가 수락해야만 하는 업데이트 창을 보여줌
                    AppUpdateType.IMMEDIATE,
                    // 현재 업데이트 요청을 만든 액티비티, 여기선 MainActivity.
                    this,
                    // onActivityResult 에서 사용될 REQUEST_CODE.
                    REQUEST_CODE);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void onBackPressed() {
        if (webView.canGoBack()) webView.goBack();
        else finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE) {
            Toast myToast = Toast.makeText(this.getApplicationContext(), "MY_REQUEST_CODE", Toast.LENGTH_SHORT);
            myToast.show();

            // 업데이트가 성공적으로 끝나지 않은 경우
            if (resultCode != RESULT_OK) {
                Log.d("tag", "Update flow failed! Result code: " + resultCode);
                // 업데이트가 취소되거나 실패하면 업데이트를 다시 요청할 수 있다.,
                // 업데이트 타입을 선택한다 (IMMEDIATE || FLEXIBLE).
                com.google.android.play.core.tasks.Task<AppUpdateInfo> appUpdateInfoTask = appUpdateManager.getAppUpdateInfo();

                appUpdateInfoTask.addOnSuccessListener(appUpdateInfo -> {
                    if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                            // flexible한 업데이트를 위해서는 AppUpdateType.FLEXIBLE을 사용한다.
                            && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)) {
                        // 업데이트를 다시 요청한다.
                        requestUpdate (appUpdateInfo);
                    }
                });
            }
        }else {
            if (resultCode == RESULT_OK) {
                if (requestCode == REQUEST_FINGER_PRINT_CODE) {  // finger print
                    String getFingerPrintResult = "OK";
                    Log.d("TAG", "getFingerPrintResult: " + getFingerPrintResult);
                    webView.loadUrl("javascript:getFingerPrintResult('" + getFingerPrintResult + "')");
                }

                if (requestCode == REQUEST_QR_SCANNER_CODE) {  // qr scanner
                    String getQrScannerResult = data.getStringExtra("QrScannerResult");
                    Log.d("TAG", "QrScannerResult: " + getQrScannerResult);
                    webView.loadUrl("javascript:getQrScannerResult('" + getQrScannerResult + "')");
                }
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        appUpdateManager
                .getAppUpdateInfo()
                .addOnSuccessListener(
                        appUpdateInfo -> {
                            if (appUpdateInfo.updateAvailability()
                                    == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS) {
                                // If an in-app update is already running, resume the update.
                                try {
                                    appUpdateManager.startUpdateFlowForResult(
                                            appUpdateInfo,
                                            AppUpdateType.IMMEDIATE,
                                            this,
                                            REQUEST_CODE);
                                }
                                catch (Exception e)
                                {
                                    e.printStackTrace();
                                }
                            }
                        });
    }

    @Override
    public void moveToFingerPrintDialogActivity(boolean check) {
        Intent intent = new Intent(this, FingerPrintDialogActivity.class);
        intent.putExtra("check", check);
        startActivityForResult(intent, REQUEST_FINGER_PRINT_CODE);
    }

    @Override
    public void moveToQrScannerActivity() {  // 퍼미션 여부
        Dexter.withActivity(this)
                .withPermission(Manifest.permission.CAMERA)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse response) {
                        startActivityForResult(new Intent(MainActivity.this, QrScannerActivity.class), REQUEST_QR_SCANNER_CODE);  // 수락 후 QrScannerActivity 로 이동
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse response) {

                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                }).check();

    }

    @Override
    public void setFcmToken(String userId) {
        Log.d("TAG", "setFcmToken: " + userId);
        sendFcmTokenToServer(userId);
    }

    @Override
    public void setMoveToWebPage(String url) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        Uri uri = Uri.parse(url);
        intent.setData(uri);
        startActivity(intent);
    }

    @Override
    public void setShareData(String toWallet) { // 공유하기

        Task<ShortDynamicLink> shortDynamicLinkTask = FirebaseDynamicLinks.getInstance().createDynamicLink()
                .setLink(Uri.parse("http://wallet.willsoft.kr/wallet/send.php?to_wallet=" + toWallet))
                .setDomainUriPrefix("https://willsoft.page.link/")
                .setAndroidParameters(new DynamicLink.AndroidParameters.Builder("com.willsoft.webview").build())
                .buildShortDynamicLink()
                .addOnCompleteListener(this, new OnCompleteListener<ShortDynamicLink>() {
                    @Override
                    public void onComplete(@NonNull Task<ShortDynamicLink> task) {
                        if (task.isSuccessful()) {
                            // Short link created
                            Uri shortLink = task.getResult().getShortLink();
                            Uri flowchartLink = task.getResult().getPreviewLink();

                            Log.d("TAG", "onComplete: " + shortLink);
                            Log.d("TAG", "onComplete: " + flowchartLink);

                            Intent intent = new Intent(Intent.ACTION_SEND);
                            intent.setType("text/plain");

                            intent.putExtra(Intent.EXTRA_TEXT, shortLink.toString());
                            Intent chooser = Intent.createChooser(intent, "공유하기");
                            startActivity(chooser);

                        } else {
                            // Error
                            // ...
                        }
                    }
                });
    }

    @Override
    public void setUserId(String userId) {
        PreferenceManager.setString(this, "userId", userId);
    }

    @Override
    public void setUserPhoneNumber() {
        Log.d("TAG", "setUserPhone: "+telNumber);
        webView.post(new Runnable() {
            @Override public void run() {
            webView.loadUrl("javascript:getUserPhoneNumber('" + telNumber + "')");
        } });

    }

    @Override
    public void setPrivateKey(String key) {
        String userId = PreferenceManager.getString(this,"userId");
       try{

           File path = new File(Environment.getExternalStorageDirectory().getAbsoluteFile()+"/Wallet");
           if(!path.exists()){
               path.mkdir();
           }
           File file = new File(path,userId+"_PRIVATE_KEY.txt");
           FileWriter fileWriter = new FileWriter(file,false);
           PrintWriter printWriter = new PrintWriter(fileWriter);
           printWriter.println(key);
           printWriter.close();

           Toast.makeText(this, "내장메모리(Wallet 폴더)에\nPrivate Key 가 저장되었습니다.\n휴대폰변경시 같이 옮겨주세요.", Toast.LENGTH_LONG).show();
           webView.post(new Runnable() {
               @Override public void run() {
                   String result = "OK";
                   webView.loadUrl("javascript:setPrivateKeyResult('" + result + "')");
               } });
       }catch (Exception e){}
    }

    @Override
    public void getPrivateKey() {
        checkExternalStorage();
        try{
            String userId = PreferenceManager.getString(this,"userId");
            File path = new File(Environment.getExternalStorageDirectory().getAbsoluteFile()+"/Wallet/"+userId+"_PRIVATE_KEY.txt");
            if(path.exists()) {
                BufferedReader bufferedReader = new BufferedReader(new FileReader(path));
                device_key = bufferedReader.readLine();

                webView.post(new Runnable() {
                    @Override
                    public void run() {
                        webView.loadUrl("javascript:getPrivateKeyResult('" + device_key + "')");
                    }
                });
                bufferedReader.close();
            }
        }catch (Exception e){}

    }

    void sendFcmTokenToServer(String userId) {
        String fcmToken = PreferenceManager.getString(this, "fcmToken");
        if (!fcmToken.isEmpty()) {  // 새로운 fcm token이 있다면 서버로 저장 / 그렇지 않으면 retrofit 호출안함 (shared에 값이 있느냐 없느냐로 결정) / 매번 DB의 값을 불러와 대조 하는 것은 비효율적
            Log.d("TAG", "onResponse: sendFcmTokenToServer");
            String phoneNumber = telNumber;
            Log.d("TAG", "sendFcmTokenToServer: "+phoneNumber);
            RetrofitConnection retrofitConnection = new RetrofitConnection();
            Call<retrofitData> call = retrofitConnection.server.setFcmToken(userId, fcmToken,phoneNumber);
            call.enqueue(new Callback<retrofitData>() {
                @Override
                public void onResponse(Call<retrofitData> call, Response<retrofitData> response) {
                    Log.d("TAG", "onResponse:" + response);
                    if (response.body().getResult() != null && response.body().getResult().equals("OK")) {
                        PreferenceManager.setString(MainActivity.this, "fcmToken", null);
                        Log.d("TAG", "onResponse: 저장 성공");
                    } else if (response.body().getResult() != null && response.body().getResult().equals("FAILED")) {
                        Log.d("TAG", "onResponse: 저장 실패");
                    }
                }

                @Override
                public void onFailure(Call<retrofitData> call, Throwable t) {
                    Log.d("TAG", "onFailure: " + t);
                }
            });
        }
    }

    private void handleDeepLink() { // 공유하기 링크를 타고 들어왔을때
        FirebaseDynamicLinks.getInstance()
                .getDynamicLink(getIntent())
                .addOnSuccessListener(this, new OnSuccessListener<PendingDynamicLinkData>() {
                    @Override
                    public void onSuccess(PendingDynamicLinkData pendingDynamicLinkData) {

                        if (pendingDynamicLinkData != null) {
                            Uri deepLink = pendingDynamicLinkData.getLink();
                            Uri data = getIntent().getData();
                            Log.d("TAG", "onSuccess: " + deepLink);
                            Log.d("TAG", "onSuccess: " + deepLink.getQueryParameter("toWallet"));
                            Log.d("TAG", "onSuccess: " + data.getQueryParameter("toWallet"));
                            webView.loadUrl(String.valueOf(deepLink));
                        }

                    }
                })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d("TAG", "onFailure: " + e);
                    }
                });
    }

    @SuppressLint("MissingPermission")
    private void getNumber() {

        Dexter.withActivity(this)
                .withPermissions(Manifest.permission.READ_PHONE_STATE,Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {

                        if(report.getDeniedPermissionResponses().size() > 0){
                            Toast.makeText(MainActivity.this, "전화 권한: 연락처를 지갑주소로 이용 가능\n사진, 미디어, 파일 권한: 지갑 개인 키 저장", Toast.LENGTH_LONG).show();
                            finish();
                        }

                        TelephonyManager mTelephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
                        if (mTelephonyManager == null) {
//            Toast.makeText(MainActivity.this, "전화번호를 가져올 수 없습니다.", Toast.LENGTH_SHORT).show();
                        } else {

                            try {
                                int simState = mTelephonyManager.getSimState();
                                switch (simState) {
                                    // 유심 상태를 알 수 없는 경우
                                    case TelephonyManager.SIM_STATE_UNKNOWN:
                                        // 유심이 없는 경우
                                    case TelephonyManager.SIM_STATE_ABSENT:
                                        // 유심 오류, 영구적인 사용 중지 상태
                                    case TelephonyManager.SIM_STATE_PERM_DISABLED:
                                        // 유심이 있지만 오류 상태인 경우
                                    case TelephonyManager.SIM_STATE_CARD_IO_ERROR:
                                        // 유심이 있지만 통신사 제한으로 사용 불가
                                    case TelephonyManager.SIM_STATE_CARD_RESTRICTED:
//                        Toast.makeText(MainActivity.this, "단말기의 유심이 존재하지 않거나 오류가 있는 경우, 앱을 사용할 수 없습니다.", Toast.LENGTH_LONG).show();
                                        break;
                                    default:

                                        telNumber = mTelephonyManager.getLine1Number();
                                        telNumber = telNumber.replace("+82", "0");
                                        break;

                                }
                            } catch (Exception e) {
//                Toast.makeText(MainActivity.this, "단말기의 정보를 가져오는 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요.", Toast.LENGTH_LONG).show();
                                Log.e("simCheck", "Exception: " + e.toString());

                            }
                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                }).check();




    }

    /**
     * 외부메모리 상태 확인 메서드
     */
    boolean checkExternalStorage() {
        state = Environment.getExternalStorageState();
        // 외부메모리 상태
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            // 읽기 쓰기 모두 가능
            Log.d("TAG", "외부메모리 읽기 쓰기 모두 가능");
            return true;
        } else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)){
            //읽기전용
            Log.d("TAG", "외부메모리 읽기만 가능");
            return false;
        } else {
            // 읽기쓰기 모두 안됨
            Log.d("TAG", "외부메모리 읽기쓰기 모두 안됨 : "+ state);
            Toast.makeText(this, "외부메모리 문제로 인해 고객님의 Private Key를 저장 할 수 없습니다.", Toast.LENGTH_LONG).show();
            return false;
        }
    }


}
