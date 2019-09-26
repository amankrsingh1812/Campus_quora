package com.android.campusquora;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.common.api.AvailabilityException;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.List;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.ViewHolder> {
    private List<posts> itemList;
    private Context context;
    private   OnNoteListener mOnNoteListener;


    public PostAdapter(List<posts> itemList, Context context,OnNoteListener onNoteListener) {
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
        posts ne=itemList.get(position);
        holder.bind(ne);
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        TextView textViewName;
        TextView textViewtext;
        private posts curentitem;
        OnNoteListener onNoteListener;

        public ViewHolder(@NonNull View itemView,OnNoteListener onNoteListener) {
            super(itemView);
            textViewName=itemView.findViewById(R.id.book_name);
            textViewtext=itemView.findViewById(R.id.book_author);
            this.onNoteListener=onNoteListener;
            itemView.setOnClickListener(this);

        }

        void bind (posts item) {
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
        void onNoteClick(posts it);
    }

    public void filterList(List<posts> filteredList) {
        itemList = filteredList;
        notifyDataSetChanged();
    }

}