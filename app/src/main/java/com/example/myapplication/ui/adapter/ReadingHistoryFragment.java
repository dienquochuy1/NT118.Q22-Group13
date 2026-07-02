package com.example.myapplication.ui.adapter;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.Entity.Articles;
import com.example.myapplication.R;
import com.example.myapplication.auth.SessionStore;
import com.example.myapplication.data.ApiResponse;
import com.example.myapplication.data.article.ArticleDto;
import com.example.myapplication.network.ApiClient;
import com.example.myapplication.ui.home.ArticleDetailActivity;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ReadingHistoryFragment extends Fragment {
    private TextView tvHistoryStatus;
    private ArticlesAdapter articlesAdapter;
    private SessionStore sessionStore;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_reading_history, container, false);

        sessionStore = new SessionStore(requireContext());
        tvHistoryStatus = view.findViewById(R.id.tv_history_status);

        RecyclerView rvHistoryArticles = view.findViewById(R.id.rv_history_articles);
        rvHistoryArticles.setLayoutManager(new LinearLayoutManager(requireContext()));
        articlesAdapter = new ArticlesAdapter(requireContext(), new ArrayList<>(), this::openArticleDetail);
        rvHistoryArticles.setAdapter(articlesAdapter);

        loadReadingHistory();
        return view;
    }

    private void loadReadingHistory() {
        if (!sessionStore.isLoggedIn()) {
            tvHistoryStatus.setText("Vui lòng đăng nhập để xem lịch sử đọc.");
            articlesAdapter.setItems(new ArrayList<>());
            return;
        }

        tvHistoryStatus.setText("Đang tải lịch sử đọc...");
        String authorization = sessionStore.getTokenType() + " " + sessionStore.getAccessToken();

        ApiClient.getArticleApi().getReadingHistory(authorization)
                .enqueue(new Callback<ApiResponse<List<ArticleDto>>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<List<ArticleDto>>> call, Response<ApiResponse<List<ArticleDto>>> response) {
                        ApiResponse<List<ArticleDto>> body = response.body();
                        if (!response.isSuccessful() || body == null || !body.isSuccess()) {
                            tvHistoryStatus.setText("Không thể tải lịch sử đọc.");
                            Toast.makeText(requireContext(), "Lỗi tải lịch sử. Mã lỗi: " + response.code(), Toast.LENGTH_SHORT).show();
                            return;
                        }

                        List<Articles> articles = mapArticles(body.getData());
                        articlesAdapter.setItems(articles);
                        tvHistoryStatus.setText(articles.isEmpty()
                                ? "Lịch sử đọc của bạn đang trống."
                                : "Bạn đã xem " + articles.size() + " bài viết gần đây.");
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<List<ArticleDto>>> call, Throwable t) {
                        tvHistoryStatus.setText("Không thể kết nối máy chủ.");
                        Toast.makeText(requireContext(), "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private List<Articles> mapArticles(List<ArticleDto> dtos) {
        List<Articles> articles = new ArrayList<>();
        if (dtos == null) return articles;

        for (ArticleDto dto : dtos) {
            if (dto == null) continue;

            Articles article = new Articles(
                    String.valueOf(dto.getId()),
                    safeText(dto.getTitle(), "(Không có tiêu đề)"),
                    safeText(dto.getSummary(), ""),
                    safeText(dto.getContent(), ""),
                    "Lịch sử",
                    safeText(dto.getSource(), "TechByte"),
                    "",
                    safeText(dto.getThumbnailUrl(), ""),
                    safeText(dto.getTime(), ""),
                    System.currentTimeMillis()
            );

            if (dto.getStats() != null) {
                article.setLikesCount(dto.getStats().getLikes());
            } else {
                article.setLikesCount(0);
            }
            articles.add(article);
        }
        return articles;
    }

    private String safeText(String value, String fallback) {
        return value == null || value.trim().isEmpty() ? fallback : value;
    }

    private void openArticleDetail(Articles article) {
        if (article == null) return;

        Intent intent = new Intent(requireContext(), ArticleDetailActivity.class);
        intent.putExtra("article_id", article.getId());
        intent.putExtra("article_title", article.getTitle());
        intent.putExtra("article_content", article.getContent());
        intent.putExtra("article_summary", article.getSummary());
        intent.putExtra("article_source", article.getSource());
        intent.putExtra("article_author", article.getAuthor());
        intent.putExtra("article_date", article.getPublishDate());
        intent.putExtra("article_category", article.getCategory());
        intent.putExtra("article_image", article.getImageUrl());
        startActivity(intent);
    }
}