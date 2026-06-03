package com.example.myapplication;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.myapplication.Entity.Articles;
import com.example.myapplication.network.OnArticleClickListener;
import java.util.ArrayList;
import java.util.List;

public class FavoritesAdapter extends RecyclerView.Adapter<FavoritesAdapter.ViewHolder> {

    private final Context context;
    private final List<Articles> items;
    private final OnArticleClickListener listener;

    public FavoritesAdapter(Context context, List<Articles> items, OnArticleClickListener listener) {
        this.context = context;
        this.items = new ArrayList<>(items != null ? items : new ArrayList<>());
        this.listener = listener;
    }

    public void setItems(List<Articles> newItems) {
        items.clear();
        if (newItems != null) {
            items.addAll(newItems);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_article_favorite, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Articles article = items.get(position);
        holder.title.setText(article.getTitle());
        holder.tag.setText(article.getCategory());
        holder.source.setText(article.getSource());
        holder.likes.setText("1.2k"); // Giá trị tương tác mẫu theo UI mục tiêu

        if (article.getImageUrl() != null && !article.getImageUrl().isEmpty()) {
            Glide.with(context).load(article.getImageUrl()).centerCrop().into(holder.image);
        } else {
            holder.image.setImageResource(R.drawable.bg_article_image_placeholder);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onArticleClick(article);
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView image;
        TextView title, tag, source, likes;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.img_favorite);
            title = itemView.findViewById(R.id.tv_title_favorite);
            tag = itemView.findViewById(R.id.tv_tag_favorite);
            source = itemView.findViewById(R.id.tv_source_favorite);
            likes = itemView.findViewById(R.id.tv_likes_favorite);
        }
    }
}