package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.example.myapplication.Entity.Articles;
import com.example.myapplication.data.home.HomeArticle;
import com.example.myapplication.data.home.HomeResponse;
import com.example.myapplication.databinding.ActivityMainBinding;
import com.example.myapplication.network.ApiClient;
import com.example.myapplication.network.HomeApi;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    ActivityMainBinding activityMainBinding;

    private ViewPager2 viewPagerFeatured;
    private RecyclerView rvFavorites;
    private RecyclerView rvGeneral;

    private FeaturedAdapter featuredAdapter;
    private FavoritesAdapter favoritesAdapter;
    private ArticlesAdapter articlesAdapter;

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
        EdgeToEdge.enable(this);

        activityMainBinding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(activityMainBinding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        updateHeaderDate();
        setupHomeLists();
        setupSearch();

        com.google.android.material.bottomnavigation.BottomNavigationView navView = findViewById(R.id.home_bottom_navigation);
        if(navView != null){
            activityMainBinding.layoutBottomNav.homeBottomNavigation.setOnItemSelectedListener(item -> {
                int id = item.getItemId();
                if (id == R.id.bottom_nav_home) {
                    showHomeUI();
                    return true;
                } else if (id == R.id.bottom_nav_bookmark) {
                    showSavedUI();
                    return true;
                } else if (id == R.id.bottom_nav_user) {
                    showUserUI();
                    return true;
                }
                return false;
            });
        }

        if (savedInstanceState == null) {
            activityMainBinding.layoutBottomNav.homeBottomNavigation.setSelectedItemId(R.id.bottom_nav_home);
            showHomeUI();
        } else {
            int selectedId = savedInstanceState.getInt("selected_tab", R.id.bottom_nav_home);
            if (selectedId == R.id.bottom_nav_user) {
                activityMainBinding.layoutHeader.getRoot().setVisibility(View.GONE);
                activityMainBinding.layoutHomeViews.getRoot().setVisibility(View.GONE);
                activityMainBinding.fragmentContainer.setVisibility(View.VISIBLE);
            } else if (selectedId == R.id.bottom_nav_bookmark) {
                activityMainBinding.layoutHeader.getRoot().setVisibility(View.GONE);
                activityMainBinding.layoutHomeViews.getRoot().setVisibility(View.GONE);
                activityMainBinding.fragmentContainer.setVisibility(View.VISIBLE);
            } else {
                showHomeUI();
            }
        }
    }

    private void updateHeaderDate() {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("EEEE, dd 'Tháng' M, yyyy", new Locale("vi"));
            String currentDate = sdf.format(new Date());
            currentDate = currentDate.substring(0, 1).toUpperCase() + currentDate.substring(1);
            android.widget.TextView tvDate = findViewById(R.id.tv_date);
            if (tvDate != null) {
                tvDate.setText(currentDate);
            }
        } catch (Exception ignored) {}
    }

    private void setupHomeLists() {
        viewPagerFeatured = findViewById(R.id.vp_featured);
        rvFavorites = findViewById(R.id.rv_favorites);
        rvGeneral = findViewById(R.id.rv_general);
        featuredAdapter = new FeaturedAdapter(this, new ArrayList<>(), this::onArticleSelected);
        favoritesAdapter = new FavoritesAdapter(this, new ArrayList<>(), this::onArticleSelected);
        articlesAdapter = new ArticlesAdapter(this, new ArrayList<>(), this::onArticleSelected);

        if (viewPagerFeatured != null) {
            viewPagerFeatured.setAdapter(featuredAdapter);
            viewPagerFeatured.setOffscreenPageLimit(1);
        }

        if (rvFavorites != null) {
            rvFavorites.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
            rvFavorites.setAdapter(favoritesAdapter);
        }

        if (rvGeneral != null) {
            rvGeneral.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
            rvGeneral.setAdapter(articlesAdapter);
        }

        // Bắt đầu kích hoạt gọi API mạng lấy dữ liệu thật
        fetchHomeData();
    }

    /**
     * Thực hiện kết nối API lấy gói dữ liệu Trang chủ từ Backend (Aiven Cloud)
     */
    private void fetchHomeData() {
        HomeApi homeApi = ApiClient.getHomeApi();
        homeApi.getHome().enqueue(new Callback<HomeResponse>() {
            @Override
            public void onResponse(Call<HomeResponse> call, Response<HomeResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    applyHomeData(response.body());
                } else {
                    Toast.makeText(MainActivity.this, "Backend phản hồi lỗi: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<HomeResponse> call, Throwable t) {
                Toast.makeText(MainActivity.this, "Không thể kết nối Server: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    /**
     * Đổ trực tiếp dữ liệu Online từ Backend vào bộ 3 vùng UI
     */
    private void applyHomeData(HomeResponse data) {
        List<Articles> latestItems = mapHomeArticles(data.getLatest(), "LATEST");
        List<Articles> trendingItems = mapHomeArticles(data.getTrending(), "TRENDING");
        List<Articles> generalItems = mapHomeArticles(data.getFeatures(), "FEATURED");

        featuredAdapter.setItems(latestItems);
        favoritesAdapter.setItems(trendingItems);
        articlesAdapter.setItems(generalItems);
    }

    /**
     * Ánh xạ các trường từ DTO mạng sang thực thể UI và lấy ID số nguyên thật gửi từ Backend
     */
    private List<Articles> mapHomeArticles(List<HomeArticle> items, String defaultCategory) {
        List<Articles> mapped = new ArrayList<>();
        if (items == null) return mapped;

        for (HomeArticle item : items) {
            if (item == null) continue;

            String title = item.getTitle() != null ? item.getTitle() : "(Không có tiêu đề)";
            String source = item.getSource() != null ? item.getSource() : "TechByte";
            String time = item.getTime() != null ? item.getTime() : "Vừa xong";
            String thumbnail = item.getThumbnail() != null ? item.getThumbnail() : "";
            String content = item.getContent() != null ? item.getContent() : "";
            String summary = source + " • " + time;

            // 🔥 TRÍCH XUẤT ID THẬT: Lấy ID dạng int từ Backend chuyển sang String cấp cho lớp UI
            String realId = String.valueOf(item.getId());

            mapped.add(new Articles(
                    realId,
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

    /**
     * Xử lý Click bài viết: Đóng gói ID thật gửi thẳng sang màn hình chi tiết bài báo
     */
    private void setupSearch() {
        View btnSearch = findViewById(R.id.btn_search);
        if (btnSearch == null) return;

        btnSearch.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, SearchActivity.class)));
    }

    /**
     * Xu ly click bai viet va mo man hinh chi tiet.
     */
    private void onArticleSelected(Articles article) {
        if (article == null) return;

        Intent intent = new Intent(MainActivity.this, ArticleDetailActivity.class);
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

    private void showHomeUI() {
        activityMainBinding.layoutHeader.getRoot().setVisibility(View.VISIBLE);
        activityMainBinding.layoutHomeViews.getRoot().setVisibility(View.VISIBLE);
        activityMainBinding.fragmentContainer.setVisibility(View.GONE);
    }

    private void showUserUI() {
        activityMainBinding.layoutHeader.getRoot().setVisibility(View.GONE);
        activityMainBinding.layoutHomeViews.getRoot().setVisibility(View.GONE);
        activityMainBinding.fragmentContainer.setVisibility(View.VISIBLE);
        replaceFragment(new Home_user());
    }

    private void showSavedUI() {
        activityMainBinding.layoutHeader.getRoot().setVisibility(View.GONE);
        activityMainBinding.layoutHomeViews.getRoot().setVisibility(View.GONE);
        activityMainBinding.fragmentContainer.setVisibility(View.VISIBLE);
        replaceFragment(new SavedArticlesFragment());
    }

    private void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, fragment);
        fragmentTransaction.commit();
    }

    @Override
    protected void onSaveInstanceState(android.os.Bundle outState) {
        super.onSaveInstanceState(outState);
        if (activityMainBinding != null && activityMainBinding.layoutBottomNav != null) {
            outState.putInt("selected_tab", activityMainBinding.layoutBottomNav.homeBottomNavigation.getSelectedItemId());
        }
    }
}
