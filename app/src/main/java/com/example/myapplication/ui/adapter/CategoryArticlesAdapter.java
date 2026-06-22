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
import java.util.ArrayList;
import java.util.List;

public class CategoryArticlesAdapter extends RecyclerView.Adapter<CategoryArticlesAdapter.ViewHolder> {

    private final Context context;
    private final List<Articles> items;
    private final OnArticleClickListener listener;

    public CategoryArticlesAdapter(Context context, List<Articles> items, OnArticleClickListener listener) {
        this.context = context;
        this.items = new ArrayList<>(items != null ? items : new ArrayList<>());
        this.listener = listener;
    }

    /**
     * Dùng riêng cho việc nạp dữ liệu (Làm mới hoàn toàn danh sách)
     */
    public void setItems(List<Articles> newItems) {
        items.clear();
        if (newItems != null) {
            items.addAll(newItems);
        }
        notifyDataSetChanged();
    }

    /**
     * Cuộn vô hạn
     */
    public void addItems(List<Articles> additionalItems) {
        if (additionalItems == null || additionalItems.isEmpty()) return;

        int startPosition = items.size(); // Vị trí cuối cùng hiện tại của danh sách
        items.addAll(additionalItems); // Chèn toàn bộ trang mới vào đuôi

        // Thông báo cho hệ thống vẽ thêm phần tử từ vị trí cũ, tối ưu hóa hiệu năng RAM
        notifyItemRangeInserted(startPosition, additionalItems.size());
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_article_category_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Articles article = items.get(position);

        holder.title.setText(article.getTitle());
        holder.tag.setText(article.getCategory());
        holder.info.setText(article.getSummary()); // Hiển thị chuỗi gộp dạng "Nguồn • Thời gian" đã map từ DTO

        // Nạp ảnh nền cho thẻ bằng thư viện Glide
        if (article.getImageUrl() != null && !article.getImageUrl().isEmpty()) {
            Glide.with(context)
                    .load(article.getImageUrl())
                    .centerCrop()
                    .into(holder.image);
        } else {
            holder.image.setImageResource(R.drawable.bg_article_image_placeholder);
        }

        // Bắn sự kiện Click bài viết để mở màn hình chi tiết đọc báo (ArticleDetailActivity)
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onArticleClick(article);
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView image;
        TextView title, tag, info;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.img_category_card);
            title = itemView.findViewById(R.id.tv_title_category_card);
            tag = itemView.findViewById(R.id.tv_tag_category_card);
            info = itemView.findViewById(R.id.tv_info_category_card);
        }
    }
}