package com.samwoo.webview.QrScan;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.ViewGroup;

import com.samwoo.webview.R;
import com.google.zxing.Result;


import me.dm7.barcodescanner.zxing.ZXingScannerView;

public class QrScannerActivity extends AppCompatActivity implements ZXingScannerView.ResultHandler {

    private ZXingScannerView mScannerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr_scanner);

        ViewGroup contentFrame = (ViewGroup) findViewById(R.id.content_frame);
        mScannerView = new ZXingScannerView(this);
        contentFrame.addView(mScannerView);

    }

    @Override
    public void handleResult(Result rawResult) {

        Intent intent = new Intent();
        intent.putExtra("QrScannerResult", rawResult.getText());
        setResult(RESULT_OK, intent);
        finish();
        // Note:
        // * Wait 2 seconds to resume the preview.
        // * On older devices continuously stopping and resuming camera preview can result in freezing the app.
        // * I don't know why this is the case but I don't have the time to figure out.
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mScannerView.resumeCameraPreview(QrScannerActivity.this);
            }
        }, 2000);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mScannerView == null) {
            mScannerView = new ZXingScannerView(this);
            setContentView(mScannerView);
        }
        mScannerView.setResultHandler(this);
        mScannerView.startCamera();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mScannerView.stopCamera();
    }

}
