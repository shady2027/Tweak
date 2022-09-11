package com.devhub.tweak;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

public class CategoryRVAdapter extends RecyclerView.Adapter<CategoryRVAdapter.ViewHolder> {

    private ArrayList<CategoryRVModal> categoryRVModalArrayList;
    private Context context;

    public CategoryRVAdapter(ArrayList<CategoryRVModal> categoryRVModalArrayList, Context context, CategoryClickInterface categoryClickInterface) {
        this.categoryRVModalArrayList = categoryRVModalArrayList;
        this.context = context;
        this.categoryClickInterface = categoryClickInterface;
    }

    private CategoryClickInterface categoryClickInterface;

    public CategoryRVAdapter(ArrayList<CategoryRVModal> categoryRVModalArrayList, Context context) {
        this.categoryRVModalArrayList = categoryRVModalArrayList;
        this.context = context;
    }

    @NonNull
    @Override
    public CategoryRVAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.category_rv_item,parent,false);
        return new CategoryRVAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryRVAdapter.ViewHolder holder, int position) {
        CategoryRVModal categoryRVModal = categoryRVModalArrayList.get(position);
        holder.categoryTV.setText(categoryRVModal.getCategory());
        Glide.with(context).load(categoryRVModal.getCategoryIVUrl()).into(holder.categoryIV);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                categoryClickInterface.onCategoryClick(holder.getAdapterPosition());
            }
        });
    }

    @Override
    public int getItemCount() {
        return categoryRVModalArrayList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        private ImageView categoryIV;
        private TextView categoryTV;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            categoryIV = itemView.findViewById(R.id.travel_btn);
            categoryTV = itemView.findViewById(R.id.travel_txt);
        }
    }

    public interface CategoryClickInterface{
        void onCategoryClick(int position);
    }
}
