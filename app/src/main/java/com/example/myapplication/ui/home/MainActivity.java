package com.example.myapplication.ui.home;

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

import com.example.myapplication.ui.adapter.ArticlesAdapter;
import com.example.myapplication.Entity.Articles;
import com.example.myapplication.ui.adapter.FavoritesAdapter;
import com.example.myapplication.ui.adapter.FeaturedAdapter;
import com.example.myapplication.ui.profile.Home_user;
import com.example.myapplication.R;
import com.example.myapplication.ui.adapter.SavedArticlesFragment;
import com.example.myapplication.ui.adapter.SearchActivity;
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
                    // 1. Luôn hiện lại Header chứa hàng nút lọc
                    activityMainBinding.layoutHeader.getRoot().setVisibility(View.VISIBLE);

                    // 2. Kiểm tra nếu nút Tin Tức đang được chọn thì hiện Mâm cỗ tổng hợp
                    if (activityMainBinding.layoutHeader.btnCatNews.isSelected()) {
                        showHomeUI();
                    } else {
                        // 3. Nếu một danh mục khác đang được chọn, ẩn mâm cỗ tổng hợp đi
                        activityMainBinding.layoutHomeViews.getRoot().setVisibility(View.GONE);
                        activityMainBinding.fragmentContainer.setVisibility(View.VISIBLE);

                        // Tìm xem nút nào đang xanh để lấy lại tên danh mục và dựng lại Fragment
                        String currentCategory = getSelectedCategoryName();
                        replaceFragment(CategoryFragment.newInstance(currentCategory));
                    }
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

        setupCategoryFilterButtons();
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

    private void setupCategoryFilterButtons() {
        // 1. Khai báo danh sách gom nhóm quản lý các nút
        java.util.List<com.google.android.material.button.MaterialButton> catButtons = new java.util.ArrayList<>();

        // 2. Ánh xạ View thông qua lớp binding trung gian layoutHeader
        catButtons.add(activityMainBinding.layoutHeader.btnCatNews);
        catButtons.add(activityMainBinding.layoutHeader.btnCatAi);
        catButtons.add(activityMainBinding.layoutHeader.btnCatSecurity);
        catButtons.add(activityMainBinding.layoutHeader.btnCatReview);
        catButtons.add(activityMainBinding.layoutHeader.btnCatLife);
        catButtons.add(activityMainBinding.layoutHeader.btnCatOther);

        // Mặc định ban đầu khi mở app: Chọn sẵn nút Tin Tức đầu tiên
        activityMainBinding.layoutHeader.btnCatNews.setSelected(true);

        // 3. Chạy vòng lặp gán sự kiện Click tập trung cho toàn bộ hàng nút
        for (com.google.android.material.button.MaterialButton button : catButtons) {
            button.setOnClickListener(v -> {
                // Đưa tất cả các nút về trạng thái tắt lựa chọn
                for (com.google.android.material.button.MaterialButton btn : catButtons) {
                    btn.setSelected(false);
                }

                // Bật sáng màu xanh riêng cho nút vừa được click
                button.setSelected(true);

                // 4. Xử lý logic hoán đổi cấu trúc giao diện màn hình bên dưới
                if (button.getId() == R.id.btn_cat_news) {
                    // Nếu bấm về nút Tin tức gốc -> Hiện lại mâm cỗ tổng hợp cũ
                    showHomeUI();
                } else {
                    // Nếu bấm vào bất kỳ danh mục nào khác -> Ẩn mâm cỗ, hiện không gian trống kẹp giữa
                    activityMainBinding.layoutHomeViews.getRoot().setVisibility(View.GONE);
                    activityMainBinding.fragmentContainer.setVisibility(View.VISIBLE);

                    // Thực hiện thay thế mảnh Fragment trống dùng chung kèm chuỗi tên danh mục tương ứng
                    String selectedCategory = button.getText().toString();
                    replaceFragment(CategoryFragment.newInstance(selectedCategory));
                }
            });
        }
    }

    private String getSelectedCategoryName() {
        if (activityMainBinding.layoutHeader.btnCatAi.isSelected()) {
            return activityMainBinding.layoutHeader.btnCatAi.getText().toString();
        }
        if (activityMainBinding.layoutHeader.btnCatSecurity.isSelected()) {
            return activityMainBinding.layoutHeader.btnCatSecurity.getText().toString();
        }
        if (activityMainBinding.layoutHeader.btnCatReview.isSelected()) {
            return activityMainBinding.layoutHeader.btnCatReview.getText().toString();
        }
        if (activityMainBinding.layoutHeader.btnCatLife.isSelected()) {
            return activityMainBinding.layoutHeader.btnCatLife.getText().toString();
        }
        if (activityMainBinding.layoutHeader.btnCatOther.isSelected()) {
            return activityMainBinding.layoutHeader.btnCatOther.getText().toString();
        }
        return "Tin tức";
    }
}
