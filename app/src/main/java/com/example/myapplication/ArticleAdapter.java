package com.example.myapplication;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.Entity.Articles;

import java.util.List;

/**
 * RecyclerView Adapter for displaying article cards on the Home screen.
 * Each card shows: thumbnail image, category chip, title, summary,
 * source name, publish date, and bookmark icon.
 */
public class ArticleAdapter extends RecyclerView.Adapter<ArticleAdapter.ArticleViewHolder> {

    private final Context context;
    private final List<Articles> articleList;

    public ArticleAdapter(Context context, List<Articles> articleList) {
        this.context = context;
        this.articleList = articleList;
    }

    @NonNull
    @Override
    public ArticleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_article, parent, false);
        return new ArticleViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ArticleViewHolder holder, int position) {
        Articles article = articleList.get(position);

        holder.tvTitle.setText(article.getTitle());
        holder.tvSummary.setText(article.getSummary());
        holder.tvSource.setText(article.getSource());
        holder.tvDate.setText(article.getPublishDate());
        holder.tvCategory.setText(article.getCategory());

        // Bookmark toggle icon
        holder.btnBookmark.setImageResource(
                article.isBookmarked() ? R.drawable.bookmark_filled : R.drawable.bookmark_outline
        );

        // Load thumbnail image with Glide if URL is available
        if (article.getImageUrl() != null && !article.getImageUrl().isEmpty()) {
            com.bumptech.glide.Glide.with(context)
                    .load(article.getImageUrl())
                    .placeholder(R.drawable.bg_article_image_placeholder)
                    .error(R.drawable.bg_article_image_placeholder)
                    .centerCrop()
                    .into(holder.imgArticle);
        } else {
            com.bumptech.glide.Glide.with(context).clear(holder.imgArticle);
            holder.imgArticle.setImageDrawable(null);
        }

        // Bookmark click
        holder.btnBookmark.setOnClickListener(v -> {
            article.setBookmarked(!article.isBookmarked());
            holder.btnBookmark.setImageResource(
                    article.isBookmarked() ? R.drawable.bookmark_filled : R.drawable.bookmark_outline
            );
        });

        // Card click → Open Article Detail
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, ArticleDetailActivity.class);
            intent.putExtra("article_id", article.getId());
            intent.putExtra("article_title", article.getTitle());
            intent.putExtra("article_content", article.getContent());
            intent.putExtra("article_summary", article.getSummary());
            intent.putExtra("article_source", article.getSource());
            intent.putExtra("article_author", article.getAuthor());
            intent.putExtra("article_date", article.getPublishDate());
            intent.putExtra("article_category", article.getCategory());
            intent.putExtra("article_image", article.getImageUrl());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return articleList.size();
    }

    static class ArticleViewHolder extends RecyclerView.ViewHolder {
        ImageView imgArticle, btnBookmark;
        TextView tvTitle, tvSummary, tvSource, tvDate, tvCategory;

        public ArticleViewHolder(@NonNull View itemView) {
            super(itemView);
            imgArticle = itemView.findViewById(R.id.img_article);
            btnBookmark = itemView.findViewById(R.id.btn_bookmark);
            tvTitle = itemView.findViewById(R.id.tv_title);
            tvSummary = itemView.findViewById(R.id.tv_summary);
            tvSource = itemView.findViewById(R.id.tv_source);
            tvDate = itemView.findViewById(R.id.tv_date);
            tvCategory = itemView.findViewById(R.id.tv_category);
        }
    }
}
