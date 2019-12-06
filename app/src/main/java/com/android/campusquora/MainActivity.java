package com.android.campusquora;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.campusquora.model.Post;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity implements PostAdapter.OnNoteListener {

    private static final int DOWNLOAD_BATCH_SIZE = 10;

//    private ProgressDialog progressDialog;
    private ProgressBar postLoadingProgressBar;
    private PostAdapter adapter;
    private List<Post> itemList;
    private CollectionReference dataref= FirebaseFirestore.getInstance().collection("Posts");
    private DocumentSnapshot lastVisible = null;
    private static final int RC_SIGN_IN = 123;
    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FloatingActionButton newp;
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
        newp=findViewById(R.id.fab);
        newp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                startActivity(new Intent(getApplicationContext(), NewPost.class));
            }
            });
        linearLayoutManager = new LinearLayoutManager(this);
        setUpRecyclerView();
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser current_user = mAuth.getCurrentUser();
        if(current_user == null) {
            createSignInIntent();
        }
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
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                // ...
            } else {
                // Sign in failed. If response is null the user canceled the
                // sign-in flow using the back button. Otherwise check
                // response.getError().getErrorCode() and handle the error.
                // ...
            }
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

        adapter=new PostAdapter(itemList,this,this);
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
                    for(QueryDocumentSnapshot documentSnapshot: querySnapshot){
                        itemList.add(new Post(documentSnapshot.get("Heading").toString(),documentSnapshot.get("Text").toString(),documentSnapshot.getLong("Likes"),documentSnapshot.getLong("DisLikes"),(ArrayList<String>)documentSnapshot.get("Tags")));
                    }
                    if(querySnapshot.getDocuments().size() > 0) {
                        lastVisible = querySnapshot.getDocuments().get(task.getResult().getDocuments().size() - 1);
                    } else {
                        lastVisible = null;
                        allPostsLoaded = true;
                    }

                    adapter.notifyDataSetChanged();
                    adapter.filterList(itemList);
//                    progressDialog.hide();

                }
                else{
                    String error=task.getException().getMessage();
//                    progressDialog.hide();
                    Toast.makeText(MainActivity.this, error, Toast.LENGTH_SHORT).show();
                }
//                bar.dismiss();
            }
        });
        postLoadingProgressBar.setVisibility(View.GONE);
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
}
