package com.android.campusquora;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.campusquora.model.Post;
import com.squareup.picasso.Picasso;

public class PostActivity extends AppCompatActivity {

    private static final String LOG_TAG = PostActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);
        Log.v(LOG_TAG, "onCreate Called");
        Post post = (Post) getIntent().getSerializableExtra("Post");
        TextView postTitleHeader = findViewById(R.id.post_title_header);
        TextView postContent = findViewById(R.id.post_content);
        postTitleHeader.setText(post.getHeading());
        postContent.setText(post.getText());
        final ImageView postImage = findViewById(R.id.post_image);
        Log.v(LOG_TAG, "Picasso Called for " + post.getHeading() + ": " + post.getImageUrl());
        Picasso.with(this).load(post.getImageUrl()).into(postImage);
    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Log.v(LOG_TAG, "onBackPressed Called");
        finish();
        startActivity(new Intent(getApplicationContext(), MainActivity.class));
    }
}
