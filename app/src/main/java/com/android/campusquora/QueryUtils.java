package com.android.campusquora;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.util.Patterns;
import android.widget.Adapter;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.android.campusquora.model.Post;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.firestore.Transaction;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Random;


class QueryUtils {

    public String name;
    private static final String LOG_TAG = MainActivity.class.getSimpleName();

    String validateEmail(String emailInput) {
        Log.v(LOG_TAG, "validateEmail Called");
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
        Log.v(LOG_TAG, "validatePassword Called");
        passwordInput = passwordInput.trim();
        if (passwordInput.isEmpty()) {
            return "Field can't be empty";
        } else if (passwordInput.length() < 6) {
            return "Password too short";
        } else {
            return "";
        }
    }

    String generateUniqueId() {
        Log.v(LOG_TAG, "generateUniqueId Called");
        Random random = new Random();
        final String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder unique_id = new StringBuilder();
        for(int i = 0; i < 20; ++i) {
            unique_id.append(chars.charAt(random.nextInt(chars.length())));
        }
        return unique_id.toString();
    }

    public int hasVoted(String postID, Transaction transaction, FirebaseUser current_user, CollectionReference userref) {
        Log.v(LOG_TAG, "hasVoted Called");
        int votedFlag;
        DocumentReference docRef = userref.document(current_user.getUid()).collection("hasVoted").document(postID);
        DocumentSnapshot hasVotedDoc;
        try {
            hasVotedDoc = transaction.get(docRef);
            Boolean upvoted = hasVotedDoc.getBoolean("upvoted");
            if(upvoted == null) {
                votedFlag = 0;
            }else if(upvoted) {
                votedFlag = 1;
            } else {
                votedFlag = -1;
            }
        } catch (FirebaseFirestoreException e) {
            votedFlag = 0;
        }
        Log.v(LOG_TAG, "hasVoted() votedFlag: " + votedFlag);
        return votedFlag;
    }

    public void onUpvoteClick(Context context, CollectionReference dataref, final Post it, final FirebaseUser current_user, final CollectionReference userref, final PostAdapter adapter) {
        Log.v(LOG_TAG, "onUpvoteClick Called");
        final DocumentReference postRef = dataref.document(it.getPostID());
        final DocumentReference hasVotedRef = userref.document(current_user.getUid()).collection("hasVoted").document(it.getPostID());
        FirebaseFirestore.getInstance().runTransaction(new Transaction.Function<Void>() {
            @Nullable
            @Override
            public Void apply(@NonNull Transaction transaction) {
                int votedFlag = hasVoted(it.getPostID(), transaction, current_user, userref);
                Log.v(LOG_TAG, "onUpvoteClick: Transaction: votedFlag: " + votedFlag);
                Log.v(LOG_TAG, "Before: it.getUp, it.getDown: " + it.getUpvotes() + ", " + it.getDownvotes());
                if(votedFlag == -1) {
                    it.setDownvotes(it.getDownvotes() - 1);
                    it.setUpvotes(it.getUpvotes() + 1);
                    transaction.update(hasVotedRef, "upvoted", true);
                } else if(votedFlag == 0) {
                    HashMap<String, Object> hasVotedObject = new HashMap<>();
                    hasVotedObject.put("upvoted", true);
                    it.setUpvotes(it.getUpvotes() + 1);
                    transaction.set(hasVotedRef, hasVotedObject, SetOptions.merge());
                } else {
                    it.setUpvotes(it.getUpvotes() - 1);
                    transaction.delete(hasVotedRef);
                }
                Log.v(LOG_TAG, "After: it.getUp, it.getDown: " + it.getUpvotes() + ", " + it.getDownvotes());
                transaction.update(postRef, "Dislikes", it.getDownvotes(), "Likes", it.getUpvotes());
                return null;
            }
        }).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.v(LOG_TAG, "Transaction Complete");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.v(LOG_TAG, "Transaction Failed: " + e.getMessage());
            }
        }).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                adapter.updatePost(it);
            }
        });
    }

    public void onDownvoteClick(Context context, CollectionReference dataref, final Post it, final FirebaseUser current_user, final CollectionReference userref, final PostAdapter adapter) {
        Log.v(LOG_TAG, "onDownClick Called");
        Toast.makeText(context, "Down", Toast.LENGTH_SHORT).show();
        final DocumentReference postRef = dataref.document(it.getPostID());
        final DocumentReference hasVotedRef = userref.document(current_user.getUid()).collection("hasVoted").document(it.getPostID());
        FirebaseFirestore.getInstance().runTransaction(new Transaction.Function<Void>() {
            @Nullable
            @Override
            public Void apply(@NonNull Transaction transaction) {
                int votedFlag = hasVoted(it.getPostID(), transaction, current_user, userref);
                Log.v(LOG_TAG, "Before: it.getUp, it.getDown: " + it.getUpvotes() + ", " + it.getDownvotes());
                if(votedFlag == -1) {
                    transaction.delete(hasVotedRef);
                    it.setDownvotes(it.getDownvotes() - 1);
                } else if(votedFlag == 0) {
                    HashMap<String, Object> hasVotedObject = new HashMap<>();
                    hasVotedObject.put("upvoted", false);
                    transaction.set(hasVotedRef, hasVotedObject, SetOptions.merge());
                    it.setDownvotes(it.getDownvotes() + 1);
                } else {
                    transaction.update(hasVotedRef, "upvoted", false);
                    it.setDownvotes(it.getDownvotes() + 1);
                    it.setUpvotes(it.getUpvotes() - 1);
                }
                Log.v(LOG_TAG, "After: it.getUp, it.getDown: " + it.getUpvotes() + ", " + it.getDownvotes());
                transaction.update(postRef, "Dislikes", it.getDownvotes(), "Likes", it.getUpvotes());
                return null;
            }
        }).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                adapter.updatePost(it);
            }
        });
    }

}
