package com.samwoo.webview.FingerPrint;

import android.content.Context;

import androidx.core.hardware.fingerprint.FingerprintManagerCompat;

import android.widget.ImageView;
import android.widget.TextView;

import com.samwoo.webview.R;


/**
 * Small helper class to manage text/icon around fingerprint authentication UI.
 */
public class FingerPrintUiHelper extends FingerprintManagerCompat.AuthenticationCallback {

    private static final long ERROR_TIMEOUT_MILLIS = 1600;
    private static final long SUCCESS_DELAY_MILLIS = 1300;

    private ImageView mIcon;
    private TextView mErrorTextView;
    private Callback mCallback;
    private androidx.core.os.CancellationSignal mCancellationSignal;

    private boolean mSelfCancelled;
    private Context context;

    /**
     * Constructor for {@link FingerPrintUiHelper}.
     */
    FingerPrintUiHelper(Context context, ImageView icon, TextView errorTextView, Callback callback) {
        this.context = context;
        this.mIcon = icon;
        this.mErrorTextView = errorTextView;
        this.mCallback = callback;
    }

    public void startListening(FingerprintManagerCompat.CryptoObject cryptoObject) {
        if (!FingerPrintUtil.isFingerprintAuthAvailable(context)) {
            return;
        }
        mCancellationSignal = new androidx.core.os.CancellationSignal();
        mSelfCancelled = false;
        // The line below prevents the false positive inspection from Android Studio
        // noinspection ResourceType
        FingerPrintUtil.getFingerprintManagerCompat(context).authenticate(cryptoObject, 0 /* flags */, mCancellationSignal, this, null);
        mIcon.setImageResource(R.drawable.fingerprint);
    }

    public void stopListening() {
        if (mCancellationSignal != null) {
            mSelfCancelled = true;
            mCancellationSignal.cancel();
            mCancellationSignal = null;
        }
    }

    @Override
    public void onAuthenticationError(int errMsgId, CharSequence errString) {
        if (!mSelfCancelled) {
            showError(errString);
            mIcon.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mCallback.onError();
                }
            }, ERROR_TIMEOUT_MILLIS);
        }
    }

    @Override
    public void onAuthenticationHelp(int helpMsgId, CharSequence helpString) {
        showError(helpString);
    }

    @Override
    public void onAuthenticationFailed() {
        showError(mIcon.getResources().getString(
                R.string.fingerprint_not_recognized));
    }

    @Override
    public void onAuthenticationSucceeded(FingerprintManagerCompat.AuthenticationResult result) {
        mErrorTextView.removeCallbacks(mResetErrorTextRunnable);
        mIcon.setImageResource(R.drawable.ic_fingerprint_success);
        mErrorTextView.setTextColor(
                mErrorTextView.getResources().getColor(R.color.success_color));
        mErrorTextView.setText(
                mErrorTextView.getResources().getString(R.string.fingerprint_success));
        mIcon.postDelayed(new Runnable() {
            @Override
            public void run() {
                mCallback.onAuthenticated();
            }
        }, SUCCESS_DELAY_MILLIS);
    }

    private void showError(CharSequence error) {
        mIcon.setImageResource(R.drawable.ic_fingerprint_error);
        mErrorTextView.setText(error);
        mErrorTextView.setTextColor(
                mErrorTextView.getResources().getColor(R.color.warning_color));
        mErrorTextView.removeCallbacks(mResetErrorTextRunnable);
        mErrorTextView.postDelayed(mResetErrorTextRunnable, ERROR_TIMEOUT_MILLIS);
    }

    private Runnable mResetErrorTextRunnable = new Runnable() {
        @Override
        public void run() {
            mErrorTextView.setTextColor(
                    mErrorTextView.getResources().getColor(R.color.black_color));
            mErrorTextView.setText(
                    mErrorTextView.getResources().getString(R.string.fingerprint_hint));
            mIcon.setImageResource(R.drawable.fingerprint);
        }
    };

    public interface Callback {

        void onAuthenticated();

        void onError();
    }


}
