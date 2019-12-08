package com.android.campusquora;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.Toast;

import androidx.core.widget.ContentLoadingProgressBar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.campusquora.model.Post;
import com.android.campusquora.model.User;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.firestore.Transaction;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class MainActivity extends AppCompatActivity implements PostAdapter.OnNoteListener {

    private static final int DOWNLOAD_BATCH_SIZE = 10;
    private static final String LOG_TAG = MainActivity.class.getSimpleName();

//    private ProgressDialog progressDialog;
    private ContentLoadingProgressBar postLoadingProgressBar;
    private PostAdapter adapter;
    private List<Post> itemList;
    private CollectionReference dataref= FirebaseFirestore.getInstance().collection("Posts");
    private CollectionReference userref = FirebaseFirestore.getInstance().collection("Users");
    private DocumentSnapshot lastVisible = null;
    private static final int RC_SIGN_IN = 123;
    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    FirebaseUser current_user;
    private boolean isScrolling = false;
    private boolean allPostsLoaded = false;
    LinearLayoutManager linearLayoutManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        progressDialog=new ProgressDialog(this);
        postLoadingProgressBar = findViewById(R.id.post_loading_progress_bar);
        itemList=new ArrayList<>();
        FloatingActionButton newp = findViewById(R.id.fab);
        newp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                startActivity(new Intent(getApplicationContext(), NewPost.class));
            }
            });
        linearLayoutManager = new LinearLayoutManager(this);
    }

    @Override
    protected void onStart() {
        super.onStart();

        current_user = mAuth.getCurrentUser();
        if(current_user == null) {
            createSignInIntent();
        }
        setUpRecyclerView();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == R.id.sign_out) {
            signOut();
        }
        return super.onOptionsItemSelected(item);
    }

    public void createSignInIntent() {
        // [START auth_fui_create_intent]
        // Choose authentication providers
        List<AuthUI.IdpConfig> providers = Arrays.asList(
                new AuthUI.IdpConfig.GoogleBuilder().build(),
                new AuthUI.IdpConfig.EmailBuilder().build());

        // Create and launch sign-in intent
        startActivityForResult(
                AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setAvailableProviders(providers)
                        .build(),
                RC_SIGN_IN);
        // [END auth_fui_create_intent]
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            IdpResponse response = IdpResponse.fromResultIntent(data);

            if (resultCode == RESULT_OK) {
                // Successfully signed in
                current_user = FirebaseAuth.getInstance().getCurrentUser();
                userref.document(current_user.getUid()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if(task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if(document == null || !document.exists()) {
                                HashMap<String, Object> userData = new HashMap<>();
                                userData.put(User.getFieldEmail(), current_user.getEmail());
                                userref.document(current_user.getUid()).set(userData, SetOptions.merge());
                            } else {
                                Toast.makeText(MainActivity.this, "Welcome back, " + current_user.getEmail(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });
                // ...
            } else {
                Toast.makeText(MainActivity.this, "Error", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(MainActivity.this, "Error", Toast.LENGTH_SHORT).show();
        }
    }
    private void setUpRecyclerView(){
//        progressDialog.setMessage("Loading Posts ...");
//        progressDialog.show();
//        progressDialog.setCancelable(false);
//        progressDialog.setCanceledOnTouchOutside(false);
        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(linearLayoutManager);
        if(current_user == null) {
            createSignInIntent();
        }

        adapter=new PostAdapter(itemList, current_user,this,this);
        recyclerView.setAdapter(adapter);
        itemList.clear();
        getPostsFromFirestore();

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if(newState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
                    isScrolling = true;
                }
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                int currentItems = linearLayoutManager.getChildCount();
                int totalItems = linearLayoutManager.getItemCount();
                int scrolledItems = linearLayoutManager.findFirstVisibleItemPosition();

                if(isScrolling && currentItems + scrolledItems == totalItems) {
                    isScrolling = false;
                    getPostsFromFirestore();
                }
            }
        });


    }

    private void getPostsFromFirestore() {
        if(allPostsLoaded) return;
        postLoadingProgressBar.setVisibility(View.VISIBLE);
        Query query;
        if(lastVisible != null) {
            query = dataref.orderBy("Heading").startAfter(lastVisible).limit(DOWNLOAD_BATCH_SIZE);
        } else {
            query = dataref.orderBy("Heading").limit(DOWNLOAD_BATCH_SIZE);
        }

        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()){
                    QuerySnapshot querySnapshot = task.getResult();
                    if(querySnapshot != null) {
                        for (QueryDocumentSnapshot documentSnapshot : querySnapshot) {
                            Object tempObject = documentSnapshot.get("Heading");
                            String postHeading = null;
                            if (tempObject != null) {
                                postHeading = tempObject.toString();
                            }
                            tempObject = documentSnapshot.get("Text");
                            String postText = null;
                            if (tempObject != null) {
                                postText = tempObject.toString();
                            }
                            itemList.add(new Post(documentSnapshot.getId(), postHeading, postText, documentSnapshot.getLong("Likes"), documentSnapshot.getLong("Dislikes"), documentSnapshot.getLong("NumberOfComments"), (ArrayList<String>) documentSnapshot.get("Tags")));
                        }
                        if (querySnapshot.getDocuments().size() > 0) {
                            lastVisible = querySnapshot.getDocuments().get(task.getResult().getDocuments().size() - 1);
                        } else {
                            lastVisible = null;
                            allPostsLoaded = true;
                        }
                    }
                    adapter.notifyDataSetChanged();
                    adapter.filterList(itemList);
//                    progressDialog.hide();

                }
                else{
                    String error= Objects.requireNonNull(task.getException()).getMessage();
//                    progressDialog.hide();
                    Toast.makeText(MainActivity.this, error, Toast.LENGTH_SHORT).show();
                }
//                bar.dismiss();
            }
        });
        postLoadingProgressBar.setVisibility(View.INVISIBLE);
    }


    public void signOut() {
        // [START auth_fui_signout]
        AuthUI.getInstance()
                .signOut(this)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    public void onComplete(@NonNull Task<Void> task) {
                        finish();
                        isDestroyed();
                    }
                });
        // [END auth_fui_signout]
    }

    @Override
    public void onNoteClick(Post it) {
        Toast.makeText(MainActivity.this, "Okay", Toast.LENGTH_SHORT).show();
    }

    private int hasVoted(String postID, Transaction transaction) {
        Log.v(LOG_TAG, "hasVoted");
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

    @Override
    public void onUpvoteClick(final Post it) {
        Log.v(LOG_TAG, "onUpvoteClick");
        final DocumentReference postRef = dataref.document(it.getPostID());
        final DocumentReference hasVotedRef = userref.document(current_user.getUid()).collection("hasVoted").document(it.getPostID());
        FirebaseFirestore.getInstance().runTransaction(new Transaction.Function<Void>() {
            @Nullable
            @Override
            public Void apply(@NonNull Transaction transaction) {
                int votedFlag = hasVoted(it.getPostID(), transaction);
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
                transaction.update(postRef, "Dilikes", it.getDownvotes(), "Likes", it.getUpvotes());
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

    @Override
    public void onDownvoteClick(final Post it) {
        Log.v(LOG_TAG, "onDownClick");
        Toast.makeText(this, "Down", Toast.LENGTH_SHORT).show();
        final DocumentReference postRef = dataref.document(it.getPostID());
        final DocumentReference hasVotedRef = userref.document(current_user.getUid()).collection("hasVoted").document(it.getPostID());
        FirebaseFirestore.getInstance().runTransaction(new Transaction.Function<Void>() {
            @Nullable
            @Override
            public Void apply(@NonNull Transaction transaction) {
                int votedFlag = hasVoted(it.getPostID(), transaction);
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
                transaction.update(postRef, "Dilikes", it.getDownvotes(), "Likes", it.getUpvotes());
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
