package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.myapplication.auth.SessionStore;
import com.example.myapplication.data.ApiResponse;
import com.example.myapplication.data.ArticleDetailResponse;
import com.example.myapplication.data.ArticleDetailData;
import com.example.myapplication.data.comment.CommentDto;
import com.example.myapplication.network.ApiClient;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ArticleDetailActivity extends AppCompatActivity {
    private int articleId = -1;
    private CommentAdapter commentAdapter;
    private RelatedArticlesAdapter relatedAdapter;

    // Khai báo View theo file XML mới thiết kế
    private ImageView imgHero, btnBookmark, btnShare, btnBack;
    private TextView tvCategory, tvTitle, tvAuthor, tvDate, tvSummary, tvContent, tvCommentsStatus, tvCommentsTitle;
    private EditText etCommentContent;
    private Button btnSendComment;
    private RecyclerView recyclerComments, rvRelatedArticles;
    private SessionStore sessionStore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        android.content.SharedPreferences themePrefs = getSharedPreferences("ThemePrefs", MODE_PRIVATE);
        boolean isDarkMode = themePrefs.getBoolean("isDarkMode", false);
        if (isDarkMode) {
            androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO);
        }

        super.onCreate(savedInstanceState);
        androidx.activity.EdgeToEdge.enable(this);
        setContentView(R.layout.article_detail);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.layout_article_detail), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Ánh xạ toàn bộ hệ thống View cực kỳ chuẩn xác
        initViews();

        // Setup RecyclerView cho Bình luận
        commentAdapter = new CommentAdapter();
        recyclerComments.setLayoutManager(new LinearLayoutManager(this));
        recyclerComments.setNestedScrollingEnabled(false);
        recyclerComments.setAdapter(commentAdapter);

        // Setup RecyclerView cho Bài viết liên quan dưới đáy (Chuẩn UI mới)
        rvRelatedArticles.setLayoutManager(new LinearLayoutManager(this));
        rvRelatedArticles.setNestedScrollingEnabled(false);
        relatedAdapter = new RelatedArticlesAdapter(this, new ArrayList<>(), articleDto -> {
            // Khi bấm vào bài viết liên quan -> Mở tiếp chính màn hình này với ID mới
            Intent intent = new Intent(ArticleDetailActivity.this, ArticleDetailActivity.class);
            intent.putExtra("article_id", String.valueOf(articleDto.getId()));
            startActivity(intent);
            finish(); // Đóng màn hình cũ để không bị tràn stack chuyển vùng
        });
        rvRelatedArticles.setAdapter(relatedAdapter);

        // Hứng ID bài viết truyền sang từ trang chủ MainActivity
        Intent intent = getIntent();
        articleId = parseArticleId(intent.getStringExtra("article_id"));

        // Nút Đóng (X) finish màn hình quay lại trang chủ
        btnBack.setOnClickListener(v -> finish());

        // Kích hoạt luồng gọi REST API lấy thông tin chi tiết từ Server Backend
        loadArticleDetailFromServer();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btn_back);
        imgHero = findViewById(R.id.img_article_hero);
        tvCategory = findViewById(R.id.tv_detail_category);
        tvTitle = findViewById(R.id.tv_detail_title);
        tvAuthor = findViewById(R.id.tv_detail_author);
        tvDate = findViewById(R.id.tv_detail_date);
        btnBookmark = findViewById(R.id.btn_bookmark_detail);
        btnShare = findViewById(R.id.btn_share);
        tvSummary = findViewById(R.id.tv_detail_summary);
        tvContent = findViewById(R.id.tv_detail_content);

        rvRelatedArticles = findViewById(R.id.rv_related_articles);
        recyclerComments = findViewById(R.id.recycler_comments);
        tvCommentsStatus = findViewById(R.id.tv_comments_status);
        tvCommentsTitle = findViewById(R.id.tv_comments_title);
        etCommentContent = findViewById(R.id.et_comment_content);
        btnSendComment = findViewById(R.id.btn_send_comment);
        sessionStore = new SessionStore(this);
    }

    /**
     * Thực hiện bắn lệnh gọi REST API lên Backend lấy dữ liệu Aiven Cloud
     */
    private void loadArticleDetailFromServer() {
        if (articleId <= 0) {
            tvTitle.setText("Lỗi mã bài viết không hợp lệ.");
            return;
        }

        // Gọi API qua tầng Network vừa dựng ở Giai đoạn 2
        ApiClient.getArticleApi().getArticleDetail(articleId).enqueue(new Callback<ArticleDetailResponse>() {
            @Override
            public void onResponse(Call<ArticleDetailResponse> call, Response<ArticleDetailResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    // Đổ dữ liệu thành công ra màn hình đọc báo
                    bindArticleDataToUI(response.body().getData());
                } else {
//                    tvContent.setText("Không thể kết nối hoặc bài viết không tồn tại trên máy chủ.");
                    tvContent.setText("Lỗi từ Server: " + response.code() + " - " + response.message() + " (ID bài báo: " + articleId + ")");
                }

            }

            @Override
            public void onFailure(Call<ArticleDetailResponse> call, Throwable t) {
                tvContent.setText("Lỗi kết nối mạng: " + t.getMessage());
            }
        });
    }

    /**
     * Điền dữ liệu từ Backend vào các View giao diện
     */
    private void bindArticleDataToUI(ArticleDetailData data) {
        if (data == null) return;

        tvTitle.setText(data.getTitle());
        tvContent.setText(data.getContent());

        // Đổ dữ liệu tóm tắt AI (Gemma) vào Card viền xanh
        if (data.getSummaryText() != null && !data.getSummaryText().isEmpty()) {
            tvSummary.setText(data.getSummaryText());
        } else {
            tvSummary.setText("Bài viết ngắn, không cần tóm tắt.");
        }

        // Gán text nguồn và định dạng thời gian
        tvAuthor.setText("TechByte Premium");
        if (data.getPublishedAt() != null) {
            tvDate.setText("• " + data.getPublishedAt());
        }

        // Nạp ảnh Thumbnail lớn mượt mà bằng thư viện Glide
        if (data.getThumbnailUrl() != null && !data.getThumbnailUrl().isEmpty()) {
            Glide.with(this).load(data.getThumbnailUrl()).centerCrop().into(imgHero);
        }

        // Đẩy mảng bài viết liên quan vào Adapter
        if (data.getRelated() != null) {
            relatedAdapter.setItems(data.getRelated());
        }

        // Tích hợp hệ thống chia sẻ bài viết (Share Intent)
        btnShare.setOnClickListener(v -> {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, data.getTitle());
            shareIntent.putExtra(Intent.EXTRA_TEXT, data.getTitle() + "\n\nĐọc thêm tại TechByte.");
            startActivity(Intent.createChooser(shareIntent, "Chia sẻ bài báo"));
        });

        // Kích hoạt lấy luồng bình luận đi kèm bài viết
        loadComments();
    }

    private int parseArticleId(String value) {
        if (value == null || value.trim().isEmpty()) return -1;
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException ignored) {
            return -1;
        }
    }

    // Giữ nguyên hệ thống quản lý Comment cũ tương tác với database của em
    private void loadComments() {
        if (articleId <= 0) return;
        tvCommentsStatus.setVisibility(View.VISIBLE);
        tvCommentsStatus.setText("Đang tải bình luận...");

        ApiClient.getCommentApi().getComments(articleId, 1, 20).enqueue(new Callback<ApiResponse<List<CommentDto>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<CommentDto>>> call, Response<ApiResponse<List<CommentDto>>> response) {
                ApiResponse<List<CommentDto>> body = response.body();
                if (!response.isSuccessful() || body == null || !body.isSuccess()) {
                    tvCommentsStatus.setText("Không thể tải bình luận.");
                    return;
                }

                List<CommentDto> comments = body.getData();
                commentAdapter.submitList(comments);
                if (comments == null || comments.isEmpty()) {
                    tvCommentsStatus.setVisibility(View.VISIBLE);
                    tvCommentsStatus.setText("Chưa có bình luận nào. Hãy là người đầu tiên!");
                } else {
                    tvCommentsStatus.setVisibility(View.GONE);
                    tvCommentsTitle.setText("• BÌNH LUẬN ( " + comments.size() + " )");
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<CommentDto>>> call, Throwable t) {
                tvCommentsStatus.setText("Lỗi kết nối bình luận.");
            }
        });
    }
}