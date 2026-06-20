package com.example.myapplication.ui.adapter;

import android.content.Intent;
import android.os.Bundle;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.Entity.Articles;
import com.example.myapplication.R;
import com.example.myapplication.data.home.HomeArticle;
import com.example.myapplication.data.home.HomeResponse;
import com.example.myapplication.network.ApiClient;
import com.example.myapplication.network.HomeApi;
import com.example.myapplication.ui.home.ArticleDetailActivity;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SearchActivity extends AppCompatActivity {
    private EditText etKeyword;
    private TextView tvSearchStatus;
    private ArticlesAdapter articlesAdapter;
    private final List<Articles> allArticles = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        android.content.SharedPreferences themePrefs = getSharedPreferences("ThemePrefs", android.content.Context.MODE_PRIVATE);
        boolean isDarkMode = themePrefs.getBoolean("isDarkMode", false);
        if (isDarkMode) {
            androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
        etKeyword = findViewById(R.id.et_keyword);
        tvSearchStatus = findViewById(R.id.tv_search_status);

        RecyclerView rvSearchResults = findViewById(R.id.rv_search_results);
        rvSearchResults.setLayoutManager(new LinearLayoutManager(this));
        articlesAdapter = new ArticlesAdapter(this, new ArrayList<>(), this::openArticleDetail);
        rvSearchResults.setAdapter(articlesAdapter);

        findViewById(R.id.btn_search_submit).setOnClickListener(v -> performSearch());
        etKeyword.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                performSearch();
                return true;
            }
            return false;
        });

        fetchSearchData();
    }

    private void fetchSearchData() {
        tvSearchStatus.setText("Đang tải dữ liệu bài viết...");
        HomeApi homeApi = ApiClient.getHomeApi();
        homeApi.getHome().enqueue(new Callback<HomeResponse>() {
            @Override
            public void onResponse(Call<HomeResponse> call, Response<HomeResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    allArticles.clear();
                    allArticles.addAll(mapUniqueHomeArticles(response.body()));
                    tvSearchStatus.setText("Nhập từ khóa để tìm theo tiêu đề và nội dung.");
                    performSearch();
                } else {
                    tvSearchStatus.setText("Không thể tải dữ liệu tìm kiếm.");
                    Toast.makeText(SearchActivity.this, "Backend phản hồi lỗi: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<HomeResponse> call, Throwable t) {
                tvSearchStatus.setText("Không thể kết nối Server.");
                Toast.makeText(SearchActivity.this, "Không thể kết nối Server: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private List<Articles> mapUniqueHomeArticles(HomeResponse data) {
        Map<String, Articles> uniqueArticles = new LinkedHashMap<>();
        addUniqueArticles(uniqueArticles, mapHomeArticles(data.getLatest(), "Mới nhất"));
        addUniqueArticles(uniqueArticles, mapHomeArticles(data.getTrending(), "Xu hướng"));
        addUniqueArticles(uniqueArticles, mapHomeArticles(data.getFeatures(), "Nổi bật"));
        return new ArrayList<>(uniqueArticles.values());
    }

    private List<Articles> mapHomeArticles(List<HomeArticle> items, String defaultCategory) {
        List<Articles> mapped = new ArrayList<>();
        if (items == null) return mapped;

        for (HomeArticle item : items) {
            if (item == null) continue;

            String title = item.getTitle() != null ? item.getTitle() : "(Không có tiêu đề)";
            String source = item.getSource() != null ? item.getSource() : "TechByte";
            String time = item.getTime() != null ? item.getTime() : "Vừa xong";
            String thumbnail = item.getThumbnail() != null ? item.getThumbnail() : "";
            String summary = item.getSummary() != null && !item.getSummary().trim().isEmpty()
                    ? item.getSummary()
                    : source + " - " + time;
            String content = item.getContent() != null ? item.getContent() : "";

            mapped.add(new Articles(
                    String.valueOf(item.getId()),
                    title,
                    summary,
                    content,
                    defaultCategory,
                    source,
                    "",
                    thumbnail,
                    time,
                    System.currentTimeMillis()
            ));
        }
        return mapped;
    }

    private void addUniqueArticles(Map<String, Articles> uniqueArticles, List<Articles> articles) {
        for (Articles article : articles) {
            if (article == null) continue;
            String key = article.getId() != null ? article.getId() : article.getTitle();
            if (key != null && !uniqueArticles.containsKey(key)) {
                uniqueArticles.put(key, article);
            }
        }
    }

    private void performSearch() {
        String keyword = etKeyword.getText() != null ? etKeyword.getText().toString() : "";
        String normalizedKeyword = normalizeSearchText(keyword);
        if (normalizedKeyword.isEmpty()) {
            articlesAdapter.setItems(new ArrayList<>());
            tvSearchStatus.setText("Nhập từ khóa để tìm theo tiêu đề và nội dung.");
            return;
        }

        List<Articles> results = new ArrayList<>();
        for (Articles article : allArticles) {
            if (matchesKeyword(article, normalizedKeyword)) {
                results.add(article);
            }
        }

        articlesAdapter.setItems(results);
        tvSearchStatus.setText(results.isEmpty()
                ? "Không tìm thấy bài viết phù hợp."
                : "Tìm thấy " + results.size() + " bài viết.");
    }

    private boolean matchesKeyword(Articles article, String normalizedKeyword) {
        if (article == null) return false;
        String searchableText = safeText(article.getTitle()) + " " + safeText(article.getContent());
        return normalizeSearchText(searchableText).contains(normalizedKeyword);
    }

    private String normalizeSearchText(String text) {
        if (text == null) return "";
        String normalized = Normalizer.normalize(text, Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "")
                .replace('\u0111', 'd')
                .replace('\u0110', 'D')
                .toLowerCase(Locale.ROOT)
                .trim();
        return normalized.replaceAll("\\s+", " ");
    }

    private String safeText(String text) {
        return text == null ? "" : text;
    }

    private void openArticleDetail(Articles article) {
        if (article == null) return;

        Intent intent = new Intent(SearchActivity.this, ArticleDetailActivity.class);
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
