package com.android.campusquora;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity implements PostAdapter.OnNoteListener {
    private static final String LOG_TAG = MainActivity.class.getSimpleName();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference postref = db.collection("Posts");
    private List<posts> itemList;
    private PostAdapter adapter;
    private FirebaseAuth mauth;
    private FirebaseUser user;
//    private ProgressDialog progressDialog;
    private static final int RC_SIGN_IN = 123;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.v(LOG_TAG, "onCreate");
        itemList=new ArrayList<>();
//        progressDialog=new ProgressDialog(this);
        mauth=FirebaseAuth.getInstance();
        setContentView(R.layout.activity_main);
//        Toolbar toolbar = findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                startActivity(new Intent(getApplicationContext(), NewPost.class));

            }
        });
        //createSignInIntent();
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.v(LOG_TAG, "onStart");
        user=mauth.getCurrentUser();
        if(user!=null)
        {
            Log.v(LOG_TAG, "User Logged In");
            setUpRecyclerView();
        } else {
            startActivity(new Intent(getApplicationContext(), RegisterActivity.class));
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

    public void signOut() {
        // [START auth_fui_signout]
        Log.v(LOG_TAG, "signOut");
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
    private void setUpRecyclerView(){

        Log.v(LOG_TAG, "setUpRecyclyerView");

//        progressDialog.setMessage("Loading Posts...");
//        progressDialog.show();
//        progressDialog.setCancelable(false);
//        progressDialog.setCanceledOnTouchOutside(false);

//        Query query = booksref.orderBy("ISBN",Query.Direction.DESCENDING);
//
//        FirestoreRecyclerOptions<Book_details> options=new FirestoreRecyclerOptions.Builder<Book_details>().setQuery(query,Book_details.class).build();
//
//        adapter =new BookAdapter(options);
//
        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter=new PostAdapter(itemList,this,this);
        recyclerView.setAdapter(adapter);
        itemList.clear();

        postref.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()){
                    for(QueryDocumentSnapshot documentSnapshot: task.getResult()){
                        itemList.add(new posts(documentSnapshot.get("Heading").toString(),documentSnapshot.get("Text").toString(),(long)documentSnapshot.get("Likes"),(long)documentSnapshot.get("Dislikes"),(ArrayList<String>) documentSnapshot.get("Tags")));
                    }
                    adapter.notifyDataSetChanged();
                    adapter.filterList(itemList);
                    Log.v(LOG_TAG, "Data Loaded");
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
    }
    private void filter(String text) {
        List<posts> filteredList = new ArrayList<>();

        for (posts item : itemList) {
            if (item.getHeading().toLowerCase().contains(text.toLowerCase())||item.getText().toLowerCase().contains(text.toLowerCase())) {
                filteredList.add(item);
            }
        }

        adapter.filterList(filteredList);
    }
    @Override
    public void onNoteClick(posts ne) {
        Toast.makeText(MainActivity.this, "abc", Toast.LENGTH_SHORT).show();
    }
}

