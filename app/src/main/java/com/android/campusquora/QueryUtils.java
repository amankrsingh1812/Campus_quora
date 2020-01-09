package com.android.campusquora;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Log;
import android.util.Patterns;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;
import java.net.URL;

class QueryUtils {

    public String name;

    String validateEmail(String emailInput) {
        emailInput = emailInput.trim();
        if (emailInput.isEmpty()) {
            return "Field can't be empty";
        } else if (!Patterns.EMAIL_ADDRESS.matcher(emailInput).matches()) {
            return "Please enter a valid email address";
        } else {
            return "";
        }
    }

    String validatePassword(String passwordInput) {
        passwordInput = passwordInput.trim();
        if (passwordInput.isEmpty()) {
            return "Field can't be empty";
        } else if (passwordInput.length() < 6) {
            return "Password too short";
        } else {
            return "";
        }
    }

    void setImage(final Context context, final ImageView imageView, String postId) {
        StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("images/"+postId+".jpg");
//        storageReference.getBytes(Long.MAX_VALUE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
//            @Override
//            public void onSuccess(byte[] bytes) {
//                Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
//                imageView.setImageBitmap(bitmap);
//            }
//        }).addOnFailureListener(new OnFailureListener() {
//            @Override
//            public void onFailure(@NonNull Exception e) {
//                Toast.makeText(context, "Could not get Image", Toast.LENGTH_SHORT).show();
//            }
//        });
        storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                String imageURL = uri.toString();
                Glide.with(context).load(imageURL).into(imageView);
            }
        });
        Glide.with(context).load(storageReference).into(imageView);
    }

}
