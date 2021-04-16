package com.oneClick.webview.FingerPrint;

import android.graphics.Color;
import android.app.Activity;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;


import com.oneClick.webview.R;

public class FingerPrintDialogActivity extends Activity implements FingerPrintAuthDialogFragment.SecretAuthorize {

    private FingerPrintAuthDialogFragment mFragment;
    boolean check;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_finger_print_dialog);

        init();
        //세팅부분
        mFragment = new FingerPrintAuthDialogFragment(check);

        mFragment.setCallback(this);
        mFragment.show(this.getFragmentManager(), "my_fragment");
        mFragment.setCancelable(false);

        Window window = getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(Color.parseColor("#000000"));
    }

    private void init() {
        check = getIntent().getBooleanExtra("check", false);
    }

    @Override
    public void success() {
        setResult(RESULT_OK);
        finish();
    }

    @Override
    public void fail() {
        Toast.makeText(this, "다시 시도해주세요", Toast.LENGTH_SHORT).show();
    }
}
