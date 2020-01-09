package com.android.campusquora;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.hootsuite.nachos.NachoTextView;
import com.hootsuite.nachos.chip.Chip;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class NewPost extends AppCompatActivity {
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference postref = db.collection("Posts");
    private Button add;
    private EditText heading;
    private EditText text;
    private String uid;
    private FirebaseAuth mauth;
    private FirebaseUser user;
    private String TAG="abc";
    private ProgressDialog progressDialog;
    FirebaseStorage storage = FirebaseStorage.getInstance();
    private Button btnChoose, btnUpload;
    private ImageView imageView;
    private String postId;
    private Timestamp ptime;
    private ArrayList<String> tags;

    private Uri filePath;

    private Date date;

    private final int PICK_IMAGE_REQUEST = 71;
    private NachoTextView nachoTextView;
    private ArrayList<String> list =  new ArrayList<String>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_new_post);
//        String[] suggestions = new String[]{"Tortilla Chips", "Melted Cheese", "Salsa", "Guacamole", "Mexico", "Jalapeno"};
        list=getIntent().getStringArrayListExtra("list");
//        Toast.makeText(NewPost.this, list.get(0), Toast.LENGTH_SHORT).show();
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line, list);

        nachoTextView=findViewById(R.id.spinner1);
        nachoTextView.setAdapter(adapter);
        super.onCreate(savedInstanceState);
        heading=findViewById(R.id.heading);
        progressDialog = new ProgressDialog(this);
        text = findViewById(R.id.text);
//        user=mauth.getCurrentUser();
        uid = "fdgjjhh654876";
        date = new Date();
        ptime = new Timestamp(date);
        tags=new ArrayList<String>();

        add=findViewById(R.id.post_button);
        btnChoose = (Button) findViewById(R.id.btnChoose);
//        btnUpload = (Button) findViewById(R.id.btnUpload);
        imageView = (ImageView) findViewById(R.id.imgView);
        btnChoose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseImage();
            }
        });
        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressDialog.setMessage("Posting...");
                progressDialog.show();
                progressDialog.setCancelable(false);
                progressDialog.setCanceledOnTouchOutside(false);
                for (Chip chip : nachoTextView.getAllChips()) {
                    // Do something with the text of each chip
                    tags.add((String)chip.getText());
                }
                Map<String, Object> post=new HashMap<>();
                post.put("Tags",tags);
                post.put("Text",text.getText().toString());
                post.put("Heading",heading.getText().toString());
                post.put("Likes",0);
                post.put("Dislikes",0);
                post.put("UserID",uid);
                post.put("postTime",ptime);
               postref.add(post).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                            @Override
                            public void onSuccess(DocumentReference documentReference) {
                                postId = documentReference.getId();
                                Log.d(TAG, "DocumentSnapshot written with ID: " + postId);
                                progressDialog.hide();
                                try {
                                    Bitmap bitmap = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
                                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                                    byte[] data = baos.toByteArray();
                                    final StorageReference ref = storage.getReference().child("images/" + postId + ".jpg");
                                    UploadTask uploadTask = ref.putBytes(data);
                                    uploadTask.addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Toast.makeText(NewPost.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                } catch (Exception e) {
                                    Toast.makeText(NewPost.this, "No Image", Toast.LENGTH_SHORT).show();
                                }
                                finish();
                                startActivity(new Intent(getApplicationContext(), MainActivity.class));
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.w(TAG, "Error adding document", e);
                            }
                        });
            }
        });

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
        startActivity(new Intent(getApplicationContext(), MainActivity.class));
    }

    private void chooseImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null )
        {
            filePath = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);
                imageView.setImageBitmap(bitmap);
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }
}
