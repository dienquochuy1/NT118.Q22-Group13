package com.example.myapplication.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.data.comment.CommentDto;
import com.example.myapplication.data.comment.CommentUserDto;
import com.example.myapplication.util.DateUtils;

import java.util.ArrayList;
import java.util.List;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.CommentViewHolder> {
    private final List<CommentDto> comments = new ArrayList<>();

    public void submitList(List<CommentDto> items) {
        comments.clear();
        if (items != null) {
            comments.addAll(items);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_comment, parent, false);
        return new CommentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentViewHolder holder, int position) {
        CommentDto comment = comments.get(position);
        CommentUserDto user = comment.getUser();

        String name = user != null && user.getName() != null && !user.getName().isEmpty()
                ? user.getName()
                : "Người dùng";

        holder.tvUserName.setText(name);
        holder.tvContent.setText(comment.getContent() != null ? comment.getContent() : "");
        holder.tvCreatedAt.setText(DateUtils.formatCommentDate(comment.getCreatedAt()));
    }

    @Override
    public int getItemCount() {
        return comments.size();
    }

    static class CommentViewHolder extends RecyclerView.ViewHolder {
        TextView tvUserName;
        TextView tvCreatedAt;
        TextView tvContent;

        CommentViewHolder(@NonNull View itemView) {
            super(itemView);
            tvUserName = itemView.findViewById(R.id.tv_comment_user);
            tvCreatedAt = itemView.findViewById(R.id.tv_comment_date);
            tvContent = itemView.findViewById(R.id.tv_comment_content);
        }
    }
}
