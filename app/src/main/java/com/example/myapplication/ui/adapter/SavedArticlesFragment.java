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

public class SavedArticlesFragment extends Fragment {
    private TextView tvSavedStatus;
    private ArticlesAdapter articlesAdapter;
    private SessionStore sessionStore;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_saved_articles, container, false);

        sessionStore = new SessionStore(requireContext());
        tvSavedStatus = view.findViewById(R.id.tv_saved_status);

        RecyclerView rvSavedArticles = view.findViewById(R.id.rv_saved_articles);
        rvSavedArticles.setLayoutManager(new LinearLayoutManager(requireContext()));
        articlesAdapter = new ArticlesAdapter(requireContext(), new ArrayList<>(), this::openArticleDetail);
        rvSavedArticles.setAdapter(articlesAdapter);

        loadSavedArticles();
        return view;
    }

    private void loadSavedArticles() {
        if (!sessionStore.isLoggedIn()) {
            tvSavedStatus.setText("Vui lòng đăng nhập để xem bài viết đã lưu.");
            articlesAdapter.setItems(new ArrayList<>());
            return;
        }

        tvSavedStatus.setText("Đang tải bài viết đã lưu...");
        String authorization = sessionStore.getTokenType() + " " + sessionStore.getAccessToken();
        ApiClient.getArticleActionApi().getBookmarks(authorization)
                .enqueue(new Callback<ApiResponse<List<ArticleDto>>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<List<ArticleDto>>> call, Response<ApiResponse<List<ArticleDto>>> response) {
                        ApiResponse<List<ArticleDto>> body = response.body();
                        if (!response.isSuccessful() || body == null || !body.isSuccess()) {
                            tvSavedStatus.setText("Không thể tải bài viết đã lưu.");
                            Toast.makeText(requireContext(), "Không thể tải bài viết đã lưu. Mã lỗi: " + response.code(), Toast.LENGTH_SHORT).show();
                            return;
                        }

                        List<Articles> articles = mapArticles(body.getData());
                        articlesAdapter.setItems(articles);
                        tvSavedStatus.setText(articles.isEmpty()
                                ? "Bạn chưa lưu bài viết nào."
                                : "Có " + articles.size() + " bài viết đã lưu.");
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<List<ArticleDto>>> call, Throwable t) {
                        tvSavedStatus.setText("Không thể kết nối máy chủ.");
                        Toast.makeText(requireContext(), "Không thể kết nối máy chủ.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private List<Articles> mapArticles(List<ArticleDto> dtos) {
        List<Articles> articles = new ArrayList<>();
        if (dtos == null) return articles;

        for (ArticleDto dto : dtos) {
            if (dto == null) continue;

            Articles article = new Articles(
                    dto.getId(),
                    safeText(dto.getTitle(), "(Không có tiêu đề)"),
                    safeText(dto.getSummary(), ""),
                    safeText(dto.getContent(), ""),
                    "Đã lưu",
                    safeText(dto.getSource(), "TechByte"),
                    "",
                    safeText(dto.getThumbnailUrl(), ""),
                    safeText(dto.getTime(), ""),
                    System.currentTimeMillis()
            );
            article.setBookmarked(true);
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
