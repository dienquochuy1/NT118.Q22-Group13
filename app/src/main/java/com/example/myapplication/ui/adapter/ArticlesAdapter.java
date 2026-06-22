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
import com.example.myapplication.Entity.Articles;
import com.example.myapplication.R;
import com.example.myapplication.network.OnArticleClickListener;
import com.example.myapplication.auth.SessionStore;
import com.example.myapplication.network.ApiClient;
import com.example.myapplication.data.ApiResponse;
import com.example.myapplication.data.article.ArticleActionState;
import com.example.myapplication.ui.home.MainActivity;
import android.widget.Toast;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import java.util.ArrayList;
import java.util.List;

public class ArticlesAdapter extends RecyclerView.Adapter<ArticlesAdapter.ViewHolder> {

    private final Context context;
    private final List<Articles> items;
    private final OnArticleClickListener listener;

    public ArticlesAdapter(Context context, List<Articles> items, OnArticleClickListener listener) {
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
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_article_vertical, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Articles article = items.get(position);
        holder.title.setText(article.getTitle());
        holder.tag.setText(article.getCategory());
        
        String src = article.getSource();
        if (src != null && (src.startsWith("http://") || src.startsWith("https://"))) {
            src = com.example.myapplication.util.FormatUtils.extractSourceName(src);
        }
        holder.source.setText(src);
        holder.time.setText(article.getPublishDate());
        
        if (holder.likes != null) {
            holder.likes.setText(com.example.myapplication.util.FormatUtils.formatLikesCount(article.getLikesCount()));
        }

        if (article.getImageUrl() != null && !article.getImageUrl().isEmpty()) {
            Glide.with(context).load(article.getImageUrl()).centerCrop().into(holder.image);
        } else {
            holder.image.setImageResource(R.drawable.bg_article_image_placeholder);
        }

        // Bookmark icon state binding
        if (holder.btnBookmark != null) {
            holder.btnBookmark.setVisibility(android.view.View.GONE);
            holder.btnBookmark.setImageResource(
                    article.isBookmarked() ? R.drawable.bookmark_filled : R.drawable.bookmark_outline
            );

            holder.btnBookmark.setOnClickListener(v -> {
                SessionStore sessionStore = new SessionStore(context);
                if (!sessionStore.isLoggedIn()) {
                    Toast.makeText(context, "Vui lòng đăng nhập để lưu bài viết.", Toast.LENGTH_SHORT).show();
                    return;
                }

                String authHeader = sessionStore.getTokenType() + " " + sessionStore.getAccessToken();
                int articleId;
                try {
                    articleId = Integer.parseInt(article.getId());
                } catch (NumberFormatException e) {
                    Toast.makeText(context, "Mã bài viết không hợp lệ.", Toast.LENGTH_SHORT).show();
                    return;
                }

                holder.btnBookmark.setEnabled(false);
                ApiClient.getArticleActionApi().toggleBookmark(authHeader, articleId)
                        .enqueue(new Callback<ApiResponse<ArticleActionState>>() {
                            @Override
                            public void onResponse(Call<ApiResponse<ArticleActionState>> call, Response<ApiResponse<ArticleActionState>> response) {
                                holder.btnBookmark.setEnabled(true);
                                ApiResponse<ArticleActionState> body = response.body();
                                if (response.isSuccessful() && body != null && body.isSuccess()) {
                                    ArticleActionState state = body.getData();
                                    boolean bookmarked = state != null && Boolean.TRUE.equals(state.isBookmarked());
                                    article.setBookmarked(bookmarked);
                                    holder.btnBookmark.setImageResource(
                                            bookmarked ? R.drawable.bookmark_filled : R.drawable.bookmark_outline
                                    );
                                    
                                    // Mark bookmark dirty to sync other lists
                                    MainActivity.isBookmarkDirty = true;
                                    
                                    Toast.makeText(context, bookmarked ? "Đã lưu bài viết." : "Đã bỏ lưu bài viết.", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(context, "Không thể cập nhật trạng thái lưu.", Toast.LENGTH_SHORT).show();
                                }
                            }

                            @Override
                            public void onFailure(Call<ApiResponse<ArticleActionState>> call, Throwable t) {
                                holder.btnBookmark.setEnabled(true);
                                Toast.makeText(context, "Không thể kết nối máy chủ.", Toast.LENGTH_SHORT).show();
                            }
                        });
            });
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onArticleClick(article);
        });
    }

    public List<Articles> getItems() {
        return items;
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView image, btnBookmark;
        TextView title, tag, time, source, likes;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.img_vertical);
            title = itemView.findViewById(R.id.tv_title_vertical);
            tag = itemView.findViewById(R.id.tv_tag_vertical);
            time = itemView.findViewById(R.id.tv_time_vertical);
            source = itemView.findViewById(R.id.tv_source_vertical);
            btnBookmark = itemView.findViewById(R.id.btn_bookmark_vertical);
//            likes = itemView.findViewById(R.id.tv_likes_vertical);
        }
    }
}