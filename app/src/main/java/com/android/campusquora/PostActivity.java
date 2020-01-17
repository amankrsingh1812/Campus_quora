package com.android.campusquora;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.campusquora.model.Comment;
import com.android.campusquora.model.Post;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.firebase.ui.firestore.SnapshotParser;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.firestore.Transaction;
import com.squareup.picasso.Picasso;


import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class PostActivity extends AppCompatActivity {

    private static final String LOG_TAG = PostActivity.class.getSimpleName();

    private FirestoreRecyclerAdapter adapter;
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseUser current_user;
    private CollectionReference commentRef;
    private CollectionReference dataref= FirebaseFirestore.getInstance().collection("Posts");
    private CollectionReference userref = FirebaseFirestore.getInstance().collection("Users");

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);
        Log.v(LOG_TAG, "onCreate Called");
        current_user = mAuth.getCurrentUser();
        final Post post = (Post) getIntent().getSerializableExtra("Post");
        commentRef =  FirebaseFirestore.getInstance()
                .collection("Posts")
                .document(post.getPostID())
                .collection("comments");
        TextView postTitleHeader = findViewById(R.id.post_title_header);
        TextView postContent = findViewById(R.id.post_content);
        ImageView upvoteImageView = findViewById(R.id.upvote_image_button);
        ImageView dowvoteImageView = findViewById(R.id.downvote_image_button);
        RecyclerView recyclerView = findViewById(R.id.comments);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        postTitleHeader.setText(post.getHeading());
        postContent.setText(post.getText());
        final QueryUtils queryUtils = new QueryUtils();
        final ImageView postImage = findViewById(R.id.post_image);
        Log.v(LOG_TAG, "Picasso Called for " + post.getHeading() + ": " + post.getImageUrl());
        Picasso.with(this).load(post.getImageUrl()).into(postImage);

        dowvoteImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(PostActivity.this, "Down", Toast.LENGTH_SHORT).show();
                final DocumentReference postRef = dataref.document(post.getPostID());
                final DocumentReference hasVotedRef = userref.document(current_user.getUid()).collection("hasVoted").document(post.getPostID());
                FirebaseFirestore.getInstance().runTransaction(new Transaction.Function<Void>() {
                    @Nullable
                    @Override
                    public Void apply(@NonNull Transaction transaction) {
                        int votedFlag = queryUtils.hasVoted(post.getPostID(), transaction, current_user, userref);
                        Log.v(LOG_TAG, "Before: it.getUp, it.getDown: " + post.getUpvotes() + ", " + post.getDownvotes());
                        if(votedFlag == -1) {
                            transaction.delete(hasVotedRef);
                            post.setDownvotes(post.getDownvotes() - 1);
                        } else if(votedFlag == 0) {
                            HashMap<String, Object> hasVotedObject = new HashMap<>();
                            hasVotedObject.put("upvoted", false);
                            transaction.set(hasVotedRef, hasVotedObject, SetOptions.merge());
                            post.setDownvotes(post.getDownvotes() + 1);
                        } else {
                            transaction.update(hasVotedRef, "upvoted", false);
                            post.setDownvotes(post.getDownvotes() + 1);
                            post.setUpvotes(post.getUpvotes() - 1);
                        }
                        Log.v(LOG_TAG, "After: it.getUp, it.getDown: " + post.getUpvotes() + ", " + post.getDownvotes());
                        transaction.update(postRef, "Dislikes", post.getDownvotes(), "Likes", post.getUpvotes());
                        return null;
                    }
                }).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(PostActivity.this, "downvoted", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        upvoteImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Post it = post;
                final DocumentReference postRef = dataref.document(it.getPostID());
                final DocumentReference hasVotedRef = userref.document(current_user.getUid()).collection("hasVoted").document(it.getPostID());
                FirebaseFirestore.getInstance().runTransaction(new Transaction.Function<Void>() {
                    @Nullable
                    @Override
                    public Void apply(@NonNull Transaction transaction) {
                        int votedFlag = queryUtils.hasVoted(it.getPostID(), transaction, current_user, userref);
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
                        Toast.makeText(PostActivity.this, "upvoted", Toast.LENGTH_SHORT).show();
                    }
                });

            }
        });

        final EditText addCommentEditText = findViewById(R.id.add_comment);
        addCommentEditText.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                final int DRAWABLE_RIGHT = 2;

                if(event.getAction() == MotionEvent.ACTION_UP) {
                    if(event.getRawX() >= (addCommentEditText.getRight() - addCommentEditText.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())) {
                        String actual_comment = addCommentEditText.getText().toString();
                        String userID = current_user.getUid();
                        String user_name = current_user.getDisplayName();
                        Date date = new Date();
                        Long comment_time = date.getTime();
                        Map<String, Object> new_comment = new HashMap<>();
                        new_comment.put("author", user_name);
                        new_comment.put("commentTime", comment_time);
                        new_comment.put("upvotes", 0);
                        new_comment.put("downvotes", 0);
                        new_comment.put("text", actual_comment);
                        new_comment.put("author_id", userID);

                        commentRef.add(new_comment).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentReference> task) {
                                if(task.isSuccessful()) {
                                    Toast.makeText(PostActivity.this, "Comment Added Successfully.", Toast.LENGTH_SHORT).show();
                                    onRestart();
                                } else {
                                    Toast.makeText(PostActivity.this, "Add Comment Failed.", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });

                        return true;
                    }
                }
                return false;
            }
        });

        Query query = commentRef.orderBy("commentTime");

        Log.v(LOG_TAG, "collectionID: " + "Posts/" + post.getPostID() + "/comments");

        FirestoreRecyclerOptions<Comment> options = new FirestoreRecyclerOptions.Builder<Comment>()
                .setQuery(query, new SnapshotParser<Comment>() {
                    @NonNull
                    @Override
                    public Comment parseSnapshot(@NonNull DocumentSnapshot snapshot) {
                        Log.v(LOG_TAG, "parseSnapshot Called");
                        Comment comment = new Comment(
                                snapshot.getString("text"),
                                snapshot.getLong("commentTime"),
                                snapshot.getLong("upvotes"),
                                snapshot.getLong("downvotes"),
                                snapshot.getString("author")
                        );
                        return comment;
                    }
                })
                .build();

        adapter = new FirestoreRecyclerAdapter<Comment, CommentHolder>(options){

            @NonNull
            @Override
            public CommentHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                Log.v(LOG_TAG, "onCreateViewHolder called");
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.comment_item, parent, false);

                return new CommentHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull CommentHolder commentHolder, int i, @NonNull Comment comment) {
                Log.v(LOG_TAG, "onBindViewHolder called");
                commentHolder.bind(comment);
            }
        };

        recyclerView.setAdapter(adapter);
    }

    @Override
    protected void onStart() {
        super.onStart();
        adapter.startListening();
        adapter.notifyDataSetChanged();

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Log.v(LOG_TAG, "onBackPressed Called");
        finish();
        startActivity(new Intent(getApplicationContext(), MainActivity.class));
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.v(LOG_TAG, "onStop called");
        adapter.stopListening();
    }

    public class CommentHolder extends RecyclerView.ViewHolder {
        private TextView author_name;
        private TextView comment_time;
        private TextView comment_text;

        public CommentHolder(@NonNull View itemView) {
            super(itemView);
            Log.v(LOG_TAG, "CommentHolder called");
            author_name = itemView.findViewById(R.id.comment_author);
            comment_time = itemView.findViewById(R.id.comment_time);
            comment_text = itemView.findViewById(R.id.actual_comment);
        }

        public void bind(final Comment commentItem) {
            Log.v(LOG_TAG, "bind called");
            author_name.setText(commentItem.getAuthor());
            comment_time.setText(commentItem.getCommentTime());
            comment_text.setText(commentItem.getText());
        }
    }
}

