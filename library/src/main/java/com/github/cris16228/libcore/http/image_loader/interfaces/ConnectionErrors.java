package com.github.cris16228.libcore.http.image_loader.interfaces;

import com.github.cris16228.library.http.image_loader.MemoryCache;

public interface ConnectionErrors {

    void FileNotFound(String url);

    default void OutOfMemory(MemoryCache memoryCache) {
        memoryCache.clear();
    }

    void NormalError();
}
