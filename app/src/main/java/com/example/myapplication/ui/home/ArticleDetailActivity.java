package com.example.myapplication.ui.home;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.myapplication.ui.adapter.CommentAdapter;
import com.example.myapplication.R;
import com.example.myapplication.ui.adapter.RelatedArticlesAdapter;
import com.example.myapplication.auth.SessionStore;
import com.example.myapplication.data.ApiResponse;
import com.example.myapplication.data.ArticleDetailResponse;
import com.example.myapplication.data.ArticleDetailData;
import com.example.myapplication.data.article.ArticleActionState;
import com.example.myapplication.data.comment.CommentCreateRequest;
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
    private ImageView imgHero, btnLike, btnBookmark, btnShare, btnBack;
    private TextView tvCategory, tvTitle, tvAuthor, tvDate, tvSummary, tvContent, tvCommentsStatus, tvCommentsTitle;
    private EditText etCommentContent;
    private Button btnSendComment;
    private LinearLayout btnTts;
    private RecyclerView recyclerComments, rvRelatedArticles;
    private SessionStore sessionStore;
    private MediaPlayer summaryVoicePlayer;
    private boolean isPreparingSummaryVoice;
    private String summaryVoiceLink;

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
            Insets ime = insets.getInsets(WindowInsetsCompat.Type.ime());
            View commentInput = v.findViewById(R.id.layout_comment_input);
            int keyboardOffset = Math.max(0, ime.bottom - systemBars.bottom);
            int inputBottomPadding = (int) (10 * getResources().getDisplayMetrics().density + 0.5f);

            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);
            if (commentInput != null) {
                commentInput.setPadding(
                        commentInput.getPaddingLeft(),
                        commentInput.getPaddingTop(),
                        commentInput.getPaddingRight(),
                        inputBottomPadding + systemBars.bottom
                );
                commentInput.setTranslationY(-keyboardOffset);
            }
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
        btnLike.setOnClickListener(v -> toggleLike());
        btnBookmark.setOnClickListener(v -> toggleBookmark());
        btnSendComment.setOnClickListener(v -> submitComment());
        btnTts.setOnClickListener(v -> toggleSummaryVoice());

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
        btnLike = findViewById(R.id.btn_like_detail);
        btnBookmark = findViewById(R.id.btn_bookmark_detail);
        btnShare = findViewById(R.id.btn_share);
        btnTts = findViewById(R.id.btn_tts);
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

    private String getAuthorizationHeader() {
        return sessionStore.getTokenType() + " " + sessionStore.getAccessToken();
    }

    private boolean canSubmitArticleAction() {
        if (articleId <= 0) {
            Toast.makeText(this, "Không tìm thấy bài viết.", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!sessionStore.isLoggedIn()) {
            Toast.makeText(this, "Vui lòng đăng nhập để sử dụng chức năng này.", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private String getApiErrorMessage(Response<ApiResponse<ArticleActionState>> response, String fallback) {
        ApiResponse<ArticleActionState> body = response.body();
        if (body != null && body.getMessage() != null && !body.getMessage().trim().isEmpty()) {
            return body.getMessage();
        }

        return fallback + " Mã lỗi: " + response.code();
    }

    private void toggleLike() {
        if (!canSubmitArticleAction()) return;

        btnLike.setEnabled(false);
        ApiClient.getArticleActionApi().toggleLike(getAuthorizationHeader(), articleId)
                .enqueue(new Callback<ApiResponse<ArticleActionState>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<ArticleActionState>> call, Response<ApiResponse<ArticleActionState>> response) {
                        btnLike.setEnabled(true);
                        ApiResponse<ArticleActionState> body = response.body();
                        if (!response.isSuccessful() || body == null || !body.isSuccess()) {
                            Toast.makeText(ArticleDetailActivity.this, getApiErrorMessage(response, "Không cập nhật được trạng thái thích."), Toast.LENGTH_SHORT).show();
                            return;
                        }

                        ArticleActionState state = body.getData();
                        boolean liked = state != null && Boolean.TRUE.equals(state.isLiked());
                        btnLike.setImageResource(liked ? R.drawable.heart_filled : R.drawable.heart_outline);
                        Toast.makeText(ArticleDetailActivity.this, liked ? "Đã thích bài viết." : "Đã bỏ thích bài viết.", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<ArticleActionState>> call, Throwable t) {
                        btnLike.setEnabled(true);
                        Toast.makeText(ArticleDetailActivity.this, "Không thể kết nối máy chủ.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void toggleBookmark() {
        if (!canSubmitArticleAction()) return;

        btnBookmark.setEnabled(false);
        ApiClient.getArticleActionApi().toggleBookmark(getAuthorizationHeader(), articleId)
                .enqueue(new Callback<ApiResponse<ArticleActionState>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<ArticleActionState>> call, Response<ApiResponse<ArticleActionState>> response) {
                        btnBookmark.setEnabled(true);
                        ApiResponse<ArticleActionState> body = response.body();
                        if (!response.isSuccessful() || body == null || !body.isSuccess()) {
                            Toast.makeText(ArticleDetailActivity.this, getApiErrorMessage(response, "Không cập nhật được trạng thái lưu."), Toast.LENGTH_SHORT).show();
                            return;
                        }

                        ArticleActionState state = body.getData();
                        boolean bookmarked = state != null && Boolean.TRUE.equals(state.isBookmarked());
                        btnBookmark.setImageResource(bookmarked ? R.drawable.bookmark_filled : R.drawable.bookmark_outline);
                        Toast.makeText(ArticleDetailActivity.this, bookmarked ? "Đã lưu bài viết." : "Đã bỏ lưu bài viết.", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<ArticleActionState>> call, Throwable t) {
                        btnBookmark.setEnabled(true);
                        Toast.makeText(ArticleDetailActivity.this, "Không thể kết nối máy chủ.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

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
        summaryVoiceLink = data.getSumVoiceLink();

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

    private void toggleSummaryVoice() {
        if (summaryVoicePlayer != null || isPreparingSummaryVoice) {
            stopSummaryVoice();
            return;
        }

        if (summaryVoiceLink == null || summaryVoiceLink.trim().isEmpty()) {
            Toast.makeText(this, "Bai viet chua co audio.", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            isPreparingSummaryVoice = true;
            summaryVoicePlayer = new MediaPlayer();
            summaryVoicePlayer.setDataSource(summaryVoiceLink.trim());
            summaryVoicePlayer.setOnPreparedListener(player -> {
                isPreparingSummaryVoice = false;
                player.start();
            });
            summaryVoicePlayer.setOnCompletionListener(player -> stopSummaryVoice());
            summaryVoicePlayer.setOnErrorListener((player, what, extra) -> {
                stopSummaryVoice();
                Toast.makeText(this, "Khong phat duoc audio.", Toast.LENGTH_SHORT).show();
                return true;
            });
            summaryVoicePlayer.prepareAsync();
            Toast.makeText(this, "Dang tai audio...", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            stopSummaryVoice();
            Toast.makeText(this, "Khong phat duoc audio.", Toast.LENGTH_SHORT).show();
        }
    }

    private void stopSummaryVoice() {
        isPreparingSummaryVoice = false;
        if (summaryVoicePlayer == null) {
            return;
        }

        try {
            if (summaryVoicePlayer.isPlaying()) {
                summaryVoicePlayer.stop();
            }
        } catch (IllegalStateException ignored) {
            // MediaPlayer can be between async states; release below is still valid.
        }
        summaryVoicePlayer.release();
        summaryVoicePlayer = null;
    }

    private int parseArticleId(String value) {
        if (value == null || value.trim().isEmpty()) return -1;
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException ignored) {
            return -1;
        }
    }

    @Override
    protected void onDestroy() {
        stopSummaryVoice();
        super.onDestroy();
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

    private void submitComment() {
        if (articleId <= 0) {
            Toast.makeText(this, "Không tìm thấy bài viết.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!sessionStore.isLoggedIn()) {
            Toast.makeText(this, "Vui lòng đăng nhập để bình luận.", Toast.LENGTH_SHORT).show();
            return;
        }

        String content = etCommentContent.getText().toString().trim();
        if (content.length() < 2) {
            etCommentContent.setError("Bình luận quá ngắn");
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
                            ? "Bài viết chưa đồng bộ với hệ thống bình luận."
                            : "Không gửi được bình luận.";
                    Toast.makeText(ArticleDetailActivity.this, message, Toast.LENGTH_SHORT).show();
                    return;
                }

                etCommentContent.setText("");
                Toast.makeText(ArticleDetailActivity.this, "Đã gửi bình luận.", Toast.LENGTH_SHORT).show();
                loadComments();
            }

            @Override
            public void onFailure(Call<ApiResponse<CommentDto>> call, Throwable t) {
                btnSendComment.setEnabled(true);
                Toast.makeText(ArticleDetailActivity.this, "Không thể kết nối máy chủ.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
