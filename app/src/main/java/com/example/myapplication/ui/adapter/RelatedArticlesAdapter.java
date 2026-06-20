package com.example.myapplication.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.myapplication.R;
import com.example.myapplication.data.RelatedArticleDto;
import java.util.ArrayList;
import java.util.List;

public class RelatedArticlesAdapter extends RecyclerView.Adapter<RelatedArticlesAdapter.ViewHolder> {

    private final Context context;
    private final List<RelatedArticleDto> items;
    private final OnRelatedArticleClickListener listener;

    public interface OnRelatedArticleClickListener {
        void onRelatedArticleClick(RelatedArticleDto article);
    }

    public RelatedArticlesAdapter(Context context, List<RelatedArticleDto> items, OnRelatedArticleClickListener listener) {
        this.context = context;
        this.items = new ArrayList<>(items != null ? items : new ArrayList<>());
        this.listener = listener;
    }

    public void setItems(List<RelatedArticleDto> newItems) {
        items.clear();
        if (newItems != null) {
            items.addAll(newItems);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_related_article, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        RelatedArticleDto article = items.get(position);
        holder.title.setText(article.getTitle());
        holder.source.setText(article.getSource() != null && !article.getSource().isEmpty() ? "TechByte" : "SciTech");
        holder.time.setText(article.getPublishedAt() != null ? "1 giờ trước" : "Gợi ý");

        if (article.getThumbnailUrl() != null && !article.getThumbnailUrl().isEmpty()) {
            Glide.with(context).load(article.getThumbnailUrl()).centerCrop().into(holder.image);
        } else {
            holder.image.setImageResource(R.drawable.bg_article_image_placeholder);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onRelatedArticleClick(article);
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView image;
        TextView title, source, time;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.img_related_thumb);
            title = itemView.findViewById(R.id.tv_related_title);
            source = itemView.findViewById(R.id.tv_related_source);
            time = itemView.findViewById(R.id.tv_related_time);
        }
    }
}