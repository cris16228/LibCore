package com.github.cris16228.libcore;

import android.text.TextUtils;
import android.util.Base64;

public class Base64Utils {

    static final String UNICODE_FORMAT = "UTF8";

    public static String getUnicodeFormat() {
        return UNICODE_FORMAT;
    }

    public static class Base64Encoder {

        public String encrypt(String unencryptedString, int flag, String unicode_format) {
            String encryptedString = null;
            try {
                byte[] plainText;
                if (TextUtils.isEmpty(unicode_format))
                    plainText = unencryptedString.getBytes();
                else
                    plainText = unencryptedString.getBytes(unicode_format);
                encryptedString = Base64.encodeToString(plainText, flag);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return encryptedString;
        }

        public byte[] encryptV2(String unencryptedString, String unicode_format) {
            byte[] encryptedString = null;
            try {
                byte[] plainText;
                if (TextUtils.isEmpty(unicode_format))
                    plainText = unencryptedString.getBytes();
                else
                    plainText = unencryptedString.getBytes(unicode_format);
                encryptedString = java.util.Base64.getEncoder().encode(plainText);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return encryptedString;
        }

        public byte[] encryptV2(String unencryptedString) {
            return java.util.Base64.getEncoder().encode(unencryptedString.getBytes());
        }

        public String encrypt(byte[] bytes, int flag) {
            String encryptedString = null;
            try {
                encryptedString = Base64.encodeToString(bytes, flag);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return encryptedString;
        }
    }

    public static class Base64Decoder {

        public String decrypt(String encryptedString, int flag) {
            String unencryptedString = null;
            try {
                unencryptedString = new String(Base64.decode(encryptedString, flag));
            } catch (Exception e) {
                e.printStackTrace();
            }
            return unencryptedString;
        }

        public byte[] decryptV2(String encryptedString, String unicode_format) {
            byte[] unencryptedString = null;
            try {
                byte[] plainText;
                if (TextUtils.isEmpty(unicode_format))
                    plainText = encryptedString.getBytes();
                else
                    plainText = encryptedString.getBytes(unicode_format);
                unencryptedString = java.util.Base64.getDecoder().decode(plainText);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return unencryptedString;
        }

        public byte[] decryptV2(String encryptedString) {
            return java.util.Base64.getDecoder().decode(encryptedString);
        }
    }
}
