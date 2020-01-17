package com.android.campusquora;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Context;
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
import com.bumptech.glide.Glide;
import com.bumptech.glide.Registry;
import com.bumptech.glide.annotation.GlideModule;
import com.bumptech.glide.module.AppGlideModule;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.firebase.ui.storage.images.FirebaseImageLoader;
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
import com.google.firebase.storage.StorageReference;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class MainActivity extends AppCompatActivity implements PostAdapter.OnNoteListener  {

    private static final int DOWNLOAD_BATCH_SIZE = 10;
    private static final String LOG_TAG = MainActivity.class.getSimpleName();

    private ProgressDialog progressDialog;
    private ContentLoadingProgressBar postLoadingProgressBar;
    private PostAdapter adapter;
    private List<Post> itemList;
    private CollectionReference dataref= FirebaseFirestore.getInstance().collection("Posts");
    private CollectionReference userref = FirebaseFirestore.getInstance().collection("Users");
    private CollectionReference tagref = FirebaseFirestore.getInstance().collection("Tags");

    private DocumentSnapshot lastVisible = null;
    private static final int RC_SIGN_IN = 123;
    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    FirebaseUser current_user;
    private boolean isScrolling = false;
    private boolean allPostsLoaded = false;
    private RecyclerView recyclerView;
    private List<String> list;
    private Intent i;
    private QueryUtils queryUtils = new QueryUtils();
    LinearLayoutManager linearLayoutManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.v(LOG_TAG, "onCreate Called");
        setContentView(R.layout.activity_main);
        progressDialog=new ProgressDialog(this);

        postLoadingProgressBar = findViewById(R.id.post_loading_progress_bar);
        itemList=new ArrayList<>();
        list=new ArrayList<String>();
        FloatingActionButton newp = findViewById(R.id.fab);
//        newp.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                finish();
//                startActivity(new Intent(getApplicationContext(), NewPost.class));
//            }
//            });
        newp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tagref.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot documentSnapshot : task.getResult()) {
                                list.add((String)documentSnapshot.getId());
                            }
                            if(list.isEmpty())
                                Toast.makeText(MainActivity.this, "fOkay", Toast.LENGTH_SHORT).show();
                            i=new Intent(getApplicationContext(), NewPost.class);
                            i.putStringArrayListExtra("list",(ArrayList<String>)list);
                            finish();
                            startActivity(i);
                        }
                    }}
                );}});

                linearLayoutManager = new LinearLayoutManager(this);
//        setUpRecyclerView();

    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.v(LOG_TAG, "onStart Called");

        current_user = mAuth.getCurrentUser();
        if(current_user == null) {
            finish();
            startActivity(new Intent(this, RegisterActivity.class));
        }
        setUpRecyclerView();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.v(LOG_TAG, "onCreateOptionsMenu Called");
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        Log.v(LOG_TAG, "onOptionsItemSelected Called");
        if(item.getItemId() == R.id.sign_out) {
            signOut();
        } else if(item.getItemId() == R.id.settings) {
            startActivity(new Intent(this, ProfileActivity.class));
        }
        return super.onOptionsItemSelected(item);
    }

    private void setUpRecyclerView(){
        Log.v(LOG_TAG, "setUpRecyclerView Called");
        progressDialog.setMessage("Loading Posts ...");
        progressDialog.show();
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);
        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(linearLayoutManager);
        if(current_user == null) {
            finish();
            startActivity(new Intent(this, RegisterActivity.class));
        }

        adapter=new PostAdapter(itemList, current_user,this,this);
        recyclerView.setAdapter(adapter);
//        adapter.notifyDataSetChanged();
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
        Log.v(LOG_TAG, "getPostsFromFirestore Called");
        if(allPostsLoaded) return;
        postLoadingProgressBar.setVisibility(View.VISIBLE);
        Query query;
        if(lastVisible != null) {
            query = dataref.orderBy("postTime").startAfter(lastVisible).limit(DOWNLOAD_BATCH_SIZE);
        } else {
            query = dataref.orderBy("postTime").limit(DOWNLOAD_BATCH_SIZE);
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
                            itemList.add(new Post(documentSnapshot.getId(), postHeading, postText,
                                    documentSnapshot.getLong("Likes"),
                                    documentSnapshot.getLong("Dislikes"),
                                    documentSnapshot.getLong("NumberOfComments"),
                                    (ArrayList<String>) documentSnapshot.get("Tags"),
                                    documentSnapshot.getLong("postTime"),
                                    documentSnapshot.getString("imageURL")));
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
                    progressDialog.hide();

                }
                else{
                    String error= Objects.requireNonNull(task.getException()).getMessage();
                    progressDialog.hide();
                    Toast.makeText(MainActivity.this, error, Toast.LENGTH_SHORT).show();
                }
//                bar.dismiss();
            }
        });
        postLoadingProgressBar.setVisibility(View.INVISIBLE);
    }


    @Override
    protected void onPause() {
        super.onPause();
        Log.v(LOG_TAG, "onPause Called");
        progressDialog.dismiss();
    }

    public void signOut() {
        Log.v(LOG_TAG, "signOut Called");
        AuthUI.getInstance()
                .signOut(this)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    public void onComplete(@NonNull Task<Void> task) {
                        finish();
                        startActivity(new Intent(getApplicationContext(), RegisterActivity.class));
                    }
                });
    }

    @Override
    public void onNoteClick(Post it) {
        Log.v(LOG_TAG, "onNoteClick Called");
        Intent intent = new Intent(getApplicationContext(), PostActivity.class);
        intent.putExtra("Post", it);

//        Toast.makeText(MainActivity.this, "Okay", Toast.LENGTH_SHORT).show();
        startActivity(intent);
        finish();
    }

    @Override
    public void onUpvoteClick(final Post it) {
        queryUtils.onUpvoteClick(this, dataref, it, current_user, userref, adapter);
    }

    @Override
    public void onDownvoteClick(final Post it) {
        queryUtils.onDownvoteClick(this, dataref, it, current_user, userref, adapter);
    }
}

