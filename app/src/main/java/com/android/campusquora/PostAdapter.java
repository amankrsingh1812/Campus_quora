package com.android.campusquora;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.android.campusquora.model.Post;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.ViewHolder> {
    private List<Post> itemList;
    private Context context;
    private OnNoteListener mOnNoteListener;
    private int itemSelectedPosition;
    private FirebaseUser current_user;


    public PostAdapter(List<Post> itemList, FirebaseUser current_user, Context context, OnNoteListener onNoteListener) {
        this.itemList = itemList;
        this.current_user = current_user;
        this.context = context;
        this.mOnNoteListener=onNoteListener;

    }

    @NonNull
    @Override
    public PostAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v= LayoutInflater.from(parent.getContext()).inflate(R.layout.post_item,parent,false);
        return new ViewHolder(v,mOnNoteListener);
    }

    @Override
    public void onBindViewHolder(@NonNull PostAdapter.ViewHolder holder, int position) {
        Post ne=itemList.get(position);
        holder.bind(ne);
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    public void updatePost(Post it) {
        itemList.get(itemSelectedPosition).setUpvotes(it.getUpvotes());
        itemList.get(itemSelectedPosition).setDownvotes(it.getDownvotes());
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        private TextView textViewName;
        private TextView textViewtext;
        private TextView voteCount;
        private ImageView upvoteButton;
        private ImageView downvoteButton;
        private Post curentitem;
        OnNoteListener onNoteListener;

        public ViewHolder(@NonNull View itemView,OnNoteListener onNoteListener) {
            super(itemView);
            textViewName=itemView.findViewById(R.id.post_title);
            textViewtext=itemView.findViewById(R.id.post_author);
            voteCount = itemView.findViewById(R.id.vote_count);
            upvoteButton = itemView.findViewById(R.id.vote_up_button);
            downvoteButton = itemView.findViewById(R.id.vote_down_button);
            this.onNoteListener=onNoteListener;
            itemView.setOnClickListener(this);
        }

        void bind (Post item) {
            DocumentReference hasVotedRef = FirebaseFirestore.getInstance().collection("Users").document(current_user.getUid()).collection("hasVoted").document(item.getPostID());
            hasVotedRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    if(documentSnapshot != null) {
                        Boolean upvoted = documentSnapshot.getBoolean("upvoted");
                        if(upvoted != null) {
                            if(upvoted) {
                                upvoteButton.setColorFilter(R.color.design_default_color_on_primary);
                            } else {
                                downvoteButton.setColorFilter(R.color.colorAccent);
                            }
                        }
                    }
                }
            });
            textViewName.setText(item.getHeading());
            textViewtext.setText(item.getText());
            long upvotes = 0;
            long downvotes = 0;
            if(item.getUpvotes() != null) {
                upvotes = item.getUpvotes();
            }
            if(item.getDownvotes() != null) {
                downvotes = item.getDownvotes();
            }
            voteCount.setText(String.valueOf(upvotes - downvotes));
            curentitem=item;
            upvoteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onNoteListener.onUpvoteClick(curentitem);
                }
            });
            downvoteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onNoteListener.onDownvoteClick(curentitem);
                }
            });
        }
        @Override
        public void onClick(View v) {
            itemSelectedPosition = getAdapterPosition();
            onNoteListener.onNoteClick(curentitem);
        }
    }
    public interface OnNoteListener{
        void onNoteClick(Post it);
        void onUpvoteClick(Post it);
        void onDownvoteClick(Post it);
    }

    public void filterList(List<Post> filteredList) {
        itemList = filteredList;
        notifyDataSetChanged();
    }

}