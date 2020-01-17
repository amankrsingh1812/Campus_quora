package com.android.campusquora;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Registry;
import com.bumptech.glide.annotation.GlideModule;
import com.bumptech.glide.module.AppGlideModule;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.firebase.storage.StorageReference;

import java.io.InputStream;

@GlideModule
public class MyAppGlideModule extends AppGlideModule {

    private static  final String LOG_TAG = MyAppGlideModule.class.getSimpleName();
    @Override
    public void registerComponents(@NonNull Context context, @NonNull Glide glide, @NonNull Registry registry) {
        Log.v(LOG_TAG, "registerComponents Called");
        registry.append(StorageReference.class, InputStream.class, new FirebaseImageLoader.Factory());
    }
}