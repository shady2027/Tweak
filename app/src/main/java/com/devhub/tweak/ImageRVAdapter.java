package com.devhub.tweak;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

public class ImageRVAdapter extends RecyclerView.Adapter<ImageRVAdapter.ViewHolder> {
    private ArrayList<String> imageRVArrayList;
    private Context context;

    public ImageRVAdapter(ArrayList<String> imageRVArrayList, Context context) {
        this.imageRVArrayList = imageRVArrayList;
        this.context = context;
    }

    @NonNull
    @Override
    public ImageRVAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.image_item,parent,false);
        return new ImageRVAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageRVAdapter.ViewHolder holder, int position) {
        Glide.with(context).load(imageRVArrayList.get(position)).into(holder.imageIV);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(context,WallpaperActivity.class);
                i.putExtra("imgUrl",imageRVArrayList.get(holder.getAdapterPosition()));
                context.startActivity(i);
            }
        });
    }

    @Override
    public int getItemCount() {
        return imageRVArrayList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        private ImageView imageIV;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageIV = itemView.findViewById(R.id.web_image_iv);
        }
    }
}

