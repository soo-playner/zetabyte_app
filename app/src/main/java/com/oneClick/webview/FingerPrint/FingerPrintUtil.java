package com.oneClick.webview.FingerPrint;

import android.content.Context;

import androidx.core.hardware.fingerprint.FingerprintManagerCompat;

public class FingerPrintUtil {

    public static boolean isFingerprintAuthAvailable(Context context) {
        FingerprintManagerCompat mFingerprintManager;
        mFingerprintManager = FingerprintManagerCompat.from(context);
        if (mFingerprintManager.isHardwareDetected() && mFingerprintManager.hasEnrolledFingerprints()) {
            return true;
        } else {
            return false;
        }
    }

    public static FingerprintManagerCompat getFingerprintManagerCompat(Context context) {
        FingerprintManagerCompat mFingerprintManager;
        mFingerprintManager = FingerprintManagerCompat.from(context);
        return mFingerprintManager;
    }

}
