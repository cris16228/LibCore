package com.github.cris16228.libcore.http.image_loader.interfaces;

import android.graphics.Bitmap;

public interface LoadImage {

    void onSuccess(Bitmap bitmap);

    void onFail();
}
