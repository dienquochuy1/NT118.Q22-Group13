package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.auth.SessionStore;
import com.example.myapplication.data.ApiResponse;
import com.example.myapplication.data.comment.CommentCreateRequest;
import com.example.myapplication.data.comment.CommentDto;
import com.example.myapplication.network.ApiClient;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * ArticleDetailActivity — Displays the full content of a news article.
 * Matches the Stitch "Article Reading (TechByte Redesign)" design.
 *
 * Receives article data via Intent extras and populates the layout.
 */
public class ArticleDetailActivity extends AppCompatActivity {
    private int articleId = -1;
    private CommentAdapter commentAdapter;
    private TextView tvCommentsStatus;
    private EditText etCommentContent;
    private Button btnSendComment;
    private SessionStore sessionStore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Apply theme before super.onCreate()
        android.content.SharedPreferences themePrefs = getSharedPreferences("ThemePrefs", MODE_PRIVATE);
        boolean isDarkMode = themePrefs.getBoolean("isDarkMode", false);
        if (isDarkMode) {
            androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(
                    androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(
                    androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO);
        }

        super.onCreate(savedInstanceState);
        androidx.activity.EdgeToEdge.enable(this);
        setContentView(R.layout.activity_article_detail);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.layout_article_detail), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Get references
        ImageView btnBack = findViewById(R.id.btn_back);
        ImageView btnShare = findViewById(R.id.btn_share);
        ImageView btnBookmark = findViewById(R.id.btn_bookmark_detail);
        ImageView imgHero = findViewById(R.id.img_article_hero);
        TextView tvCategory = findViewById(R.id.tv_detail_category);
        TextView tvTitle = findViewById(R.id.tv_detail_title);
        TextView tvAuthor = findViewById(R.id.tv_detail_author);
        TextView tvDate = findViewById(R.id.tv_detail_date);
        TextView tvContent = findViewById(R.id.tv_detail_content);
        RecyclerView recyclerComments = findViewById(R.id.recycler_comments);
        tvCommentsStatus = findViewById(R.id.tv_comments_status);
        etCommentContent = findViewById(R.id.et_comment_content);
        btnSendComment = findViewById(R.id.btn_send_comment);
        sessionStore = new SessionStore(this);

        commentAdapter = new CommentAdapter();
        recyclerComments.setLayoutManager(new LinearLayoutManager(this));
        recyclerComments.setNestedScrollingEnabled(false);
        recyclerComments.setAdapter(commentAdapter);

        // Get data from Intent
        Intent intent = getIntent();
        articleId = parseArticleId(intent.getStringExtra("article_id"));
        String title = intent.getStringExtra("article_title");
        String content = intent.getStringExtra("article_content");
        String summary = intent.getStringExtra("article_summary");
        String source = intent.getStringExtra("article_source");
        String author = intent.getStringExtra("article_author");
        String date = intent.getStringExtra("article_date");
        String category = intent.getStringExtra("article_category");
        String imageUrl = intent.getStringExtra("article_image");

        // Populate views
        if (title != null) tvTitle.setText(title);
        if (category != null) tvCategory.setText(category);
        if (author != null && !author.isEmpty()) {
            tvAuthor.setText(author);
        } else if (source != null) {
            tvAuthor.setText(source);
        }
        if (date != null) tvDate.setText(date);

        // Show content or fallback to summary
        if (content != null && !content.isEmpty()) {
            tvContent.setText(content);
        } else if (summary != null) {
            tvContent.setText(summary);
        }

        // Load hero image with Glide
        if (imageUrl != null && !imageUrl.isEmpty()) {
            com.bumptech.glide.Glide.with(this)
                    .load(imageUrl)
                    .placeholder(R.drawable.bg_article_image_placeholder)
                    .error(R.drawable.bg_article_image_placeholder)
                    .centerCrop()
                    .into(imgHero);
        } else {
            com.bumptech.glide.Glide.with(this).clear(imgHero);
            imgHero.setImageDrawable(null);
        }

        // Back button
        btnBack.setOnClickListener(v -> finish());

        // Share button
        btnShare.setOnClickListener(v -> {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, title != null ? title : "TechByte Article");
            shareIntent.putExtra(Intent.EXTRA_TEXT,
                    (title != null ? title : "") + "\n\n" +
                    (summary != null ? summary : "") + "\n\n— Chia sẻ từ TechByte");
            startActivity(Intent.createChooser(shareIntent, "Chia sẻ bài viết"));
        });

        // Bookmark toggle
        final boolean[] isBookmarked = {false};
        btnBookmark.setOnClickListener(v -> {
            isBookmarked[0] = !isBookmarked[0];
            btnBookmark.setImageResource(
                    isBookmarked[0] ? R.drawable.bookmark_filled : R.drawable.bookmark_outline
            );
        });

        btnSendComment.setOnClickListener(v -> submitComment());
        loadComments();
    }

    private int parseArticleId(String value) {
        if (value == null || value.trim().isEmpty()) {
            return -1;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException ignored) {
            return -1;
        }
    }

    private void loadComments() {
        if (articleId <= 0) {
            tvCommentsStatus.setText("Khong tim thay ma bai viet de tai binh luan.");
            btnSendComment.setEnabled(false);
            return;
        }

        tvCommentsStatus.setVisibility(View.VISIBLE);
        tvCommentsStatus.setText("Dang tai binh luan...");

        ApiClient.getCommentApi().getComments(articleId, 1, 20).enqueue(new Callback<ApiResponse<List<CommentDto>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<CommentDto>>> call, Response<ApiResponse<List<CommentDto>>> response) {
                ApiResponse<List<CommentDto>> body = response.body();
                if (!response.isSuccessful() || body == null || !body.isSuccess()) {
                    tvCommentsStatus.setText("Khong the tai binh luan.");
                    return;
                }

                List<CommentDto> comments = body.getData();
                commentAdapter.submitList(comments);
                if (comments == null || comments.isEmpty()) {
                    tvCommentsStatus.setVisibility(View.VISIBLE);
                    tvCommentsStatus.setText("Chua co binh luan.");
                } else {
                    tvCommentsStatus.setVisibility(View.GONE);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<CommentDto>>> call, Throwable t) {
                tvCommentsStatus.setVisibility(View.VISIBLE);
                tvCommentsStatus.setText("Khong the ket noi de tai binh luan.");
            }
        });
    }

    private void submitComment() {
        if (articleId <= 0) {
            Toast.makeText(this, "Khong tim thay bai viet.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!sessionStore.isLoggedIn()) {
            Toast.makeText(this, "Vui long dang nhap de binh luan.", Toast.LENGTH_SHORT).show();
            return;
        }

        String content = etCommentContent.getText().toString().trim();
        if (content.length() < 2) {
            etCommentContent.setError("Binh luan qua ngan");
            return;
        }

        btnSendComment.setEnabled(false);
        String authorization = sessionStore.getTokenType() + " " + sessionStore.getAccessToken();
        CommentCreateRequest request = new CommentCreateRequest(articleId, content);

        ApiClient.getCommentApi().createComment(authorization, request).enqueue(new Callback<ApiResponse<CommentDto>>() {
            @Override
            public void onResponse(Call<ApiResponse<CommentDto>> call, Response<ApiResponse<CommentDto>> response) {
                btnSendComment.setEnabled(true);
                ApiResponse<CommentDto> body = response.body();
                if (!response.isSuccessful() || body == null || !body.isSuccess()) {
                    String message = response.code() == 404
                            ? "Bai viet chua dong bo voi he thong binh luan."
                            : "Khong gui duoc binh luan.";
                    Toast.makeText(ArticleDetailActivity.this, message, Toast.LENGTH_SHORT).show();
                    return;
                }

                etCommentContent.setText("");
                Toast.makeText(ArticleDetailActivity.this, "Da gui binh luan.", Toast.LENGTH_SHORT).show();
                loadComments();
            }

            @Override
            public void onFailure(Call<ApiResponse<CommentDto>> call, Throwable t) {
                btnSendComment.setEnabled(true);
                Toast.makeText(ArticleDetailActivity.this, "Khong the ket noi may chu.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
