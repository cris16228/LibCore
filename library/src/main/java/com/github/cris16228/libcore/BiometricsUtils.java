package com.github.cris16228.libcore;

import android.app.KeyguardManager;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.fragment.app.FragmentActivity;

public class BiometricsUtils {


    private final FragmentActivity context;
    private onBiometric onBiometric;
    private onBiometricPrompt onBiometricPrompt;


    public BiometricsUtils(FragmentActivity context) {
        this.context = context;
    }

    public BiometricsUtils.onBiometric getOnBiometric() {
        return onBiometric;
    }

    public void setOnBiometric(onBiometric _onBiometric) {
        onBiometric = _onBiometric;
    }

    public BiometricsUtils.onBiometricPrompt getOnBiometricPrompt() {
        return onBiometricPrompt;
    }

    public void setOnBiometricPrompt(onBiometricPrompt _onBiometricPrompt) {
        onBiometricPrompt = _onBiometricPrompt;
    }

    public void create(onBiometricPrompt onBiometricPrompt) {
        create(new onBiometric() {
            @Override
            public void onBiometricHWUnavailable() {

            }

            @Override
            public void onBiometricNoneEnrolled() {

            }

            @Override
            public void onBiometricNoHardware() {

            }

            @Override
            public void onBiometricSecurityUpdateAvailable() {

            }

            @Override
            public void onBiometricUnsupported() {

            }

            @Override
            public void onBiometricStatusUnknown() {

            }

            @Override
            public void onBiometricSuccess() {

            }
        }, onBiometricPrompt);
    }

    public void create(onBiometric onBiometric, onBiometricPrompt onBiometricPrompt) {
        if (!isSecure()) return;
        BiometricManager biometricManager = BiometricManager.from(context);
        switch (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_WEAK | BiometricManager.Authenticators.BIOMETRIC_STRONG)) {
            case BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE:
                onBiometric.onBiometricHWUnavailable();
                break;
            case BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED:
                onBiometric.onBiometricNoneEnrolled();
                break;
            case BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE:
                onBiometric.onBiometricNoHardware();
                break;
            case BiometricManager.BIOMETRIC_ERROR_SECURITY_UPDATE_REQUIRED:
                onBiometric.onBiometricSecurityUpdateAvailable();
                break;
            case BiometricManager.BIOMETRIC_ERROR_UNSUPPORTED:
                onBiometric.onBiometricUnsupported();
                break;
            case BiometricManager.BIOMETRIC_STATUS_UNKNOWN:
                onBiometric.onBiometricStatusUnknown();
                break;
            case BiometricManager.BIOMETRIC_SUCCESS:
                onBiometric.onBiometricSuccess();
                break;
        }
        BiometricPrompt biometricPrompt = new BiometricPrompt(context, context.getMainExecutor(), new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
                if (errorCode == BiometricPrompt.ERROR_NEGATIVE_BUTTON) {
                    onBiometricPrompt.NegativeButtonClick();
                }
            }

            @Override
            public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                onBiometricPrompt.onAuthenticationSuccess();
            }

            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
            }
        });
        BiometricPrompt.PromptInfo.Builder builder = new BiometricPrompt.PromptInfo.Builder();
        builder.setTitle(context.getResources().getString(R.string.use_fingerprint_title));
        builder.setNegativeButtonText(context.getResources().getString(R.string.fingerprint_use_pin));
        biometricPrompt.authenticate(builder.build());
    }

    public boolean isSecure() {
        KeyguardManager keyguardManager = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
        return keyguardManager.isKeyguardSecure();
    }

    public interface onBiometricPrompt {
        void NegativeButtonClick();

        void onAuthenticationSuccess();
    }

    public interface onBiometric {

        void onBiometricHWUnavailable();

        void onBiometricNoneEnrolled();

        void onBiometricNoHardware();

        void onBiometricSecurityUpdateAvailable();

        void onBiometricUnsupported();

        void onBiometricStatusUnknown();

        void onBiometricSuccess();
    }
}
