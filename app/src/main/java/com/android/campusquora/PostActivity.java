package com.android.campusquora;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.campusquora.model.Post;

public class PostActivity extends AppCompatActivity {

    QueryUtils queryUtils = new QueryUtils();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);
        Post post = (Post) getIntent().getSerializableExtra("Post");
        TextView postTitleHeader = findViewById(R.id.post_title_header);
        TextView postContent = findViewById(R.id.post_content);
        postTitleHeader.setText(post.getHeading());
        postContent.setText(post.getText());
        final ImageView postImage = findViewById(R.id.post_image);
        queryUtils.setImage(this, postImage, post.getPostID());
    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
        startActivity(new Intent(getApplicationContext(), MainActivity.class));
    }
}
