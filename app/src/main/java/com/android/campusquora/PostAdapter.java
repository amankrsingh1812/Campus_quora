package com.android.campusquora;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.android.campusquora.model.Post;

import java.util.List;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.ViewHolder> {
    private List<Post> itemList;
    private Context context;
    private   OnNoteListener mOnNoteListener;


    public PostAdapter(List<Post> itemList, Context context,OnNoteListener onNoteListener) {
        this.itemList = itemList;
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

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        TextView textViewName;
        TextView textViewtext;
        private Post curentitem;
        OnNoteListener onNoteListener;

        public ViewHolder(@NonNull View itemView,OnNoteListener onNoteListener) {
            super(itemView);
            textViewName=itemView.findViewById(R.id.post_title);
            textViewtext=itemView.findViewById(R.id.post_author);
            this.onNoteListener=onNoteListener;
            itemView.setOnClickListener(this);

        }

        void bind (Post item) {
            textViewName.setText(item.getHeading());
            textViewtext.setText(item.getText());
            curentitem=item;
        }
        @Override
        public void onClick(View v) {
            onNoteListener.onNoteClick(curentitem);
        }
    }
    public interface OnNoteListener{
        void onNoteClick(Post it);
    }

    public void filterList(List<Post> filteredList) {
        itemList = filteredList;
        notifyDataSetChanged();
    }

}