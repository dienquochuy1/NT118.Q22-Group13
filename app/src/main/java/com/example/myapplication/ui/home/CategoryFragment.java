package com.example.myapplication.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.Entity.Articles;
import com.example.myapplication.R;
import com.example.myapplication.auth.SessionStore;
import com.example.myapplication.data.ArticlePagedResponse;
import com.example.myapplication.ui.adapter.CategoryArticlesAdapter;
import com.example.myapplication.network.ApiClient;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CategoryFragment extends Fragment {

    private static final String ARG_CATEGORY_NAME = "category_name";
    private String categoryName;

    // Các biến quản lý trạng thái phân trang (Pagination State)
    private int currentPage = 1;
    private final int perPage = 15; // Mỗi trang bốc đúng 15 bài theo thiết kế Backend
    private boolean isLoading = false;
    private boolean hasNextPage = true;

    // Hạ tầng giao diện và mạng
    private RecyclerView rvArticles;
    private ProgressBar progressBar;
    private CategoryArticlesAdapter adapter;
    private SessionStore sessionStore;

    public static CategoryFragment newInstance(String categoryName) {
        CategoryFragment fragment = new CategoryFragment();
        Bundle args = new Bundle();
        args.putString(ARG_CATEGORY_NAME, categoryName);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            categoryName = getArguments().getString(ARG_CATEGORY_NAME);
        }
        sessionStore = new SessionStore(requireContext());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.home_category, container, false);

        // 1. Ánh xạ các View từ file home_category.xml mới nâng cấp
        rvArticles = view.findViewById(R.id.rv_category_articles);
        progressBar = view.findViewById(R.id.progress_bar_category);

        // 2. Thiết lập cấu hình RecyclerView chuyên biệt cho cuộn dọc
        setupRecyclerView();

        // 3. Kích hoạt gọi mạng nạp dữ liệu TRANG 1 ngay khi dựng xong UI
        loadCategoryArticles(currentPage);

        return view;
    }

    private void setupRecyclerView() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false);
        rvArticles.setLayoutManager(layoutManager);

        // Khởi tạo adapter nối tiếp sự kiện click mở màn hình chi tiết đọc báo
        adapter = new CategoryArticlesAdapter(requireContext(), new ArrayList<>(), this::navigateToDetail);
        rvArticles.setAdapter(adapter);

        // Kỹ thuật Endless Scroll Listener: Bắt sự kiện người dùng lướt chạm đáy màn hình
        rvArticles.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                // Chỉ tính toán khi người dùng có hành vi cuộn xuống (dy > 0)
                if (dy > 0) {
                    int visibleItemCount = layoutManager.getChildCount();
                    int totalItemCount = layoutManager.getItemCount();
                    int pastVisibleItems = layoutManager.findFirstVisibleItemPosition();

                    // Điều kiện kích hoạt: Máy đang rảnh mạng + Server báo vẫn còn trang tiếp theo
                    if (!isLoading && hasNextPage) {
                        if ((visibleItemCount + pastVisibleItems) >= totalItemCount - 2) {
                            // Tải trước khi cách đáy 2 thẻ bài viết để tạo cảm giác cuộn mượt không ngắt quãng
                            currentPage++;
                            loadCategoryArticles(currentPage);
                        }
                    }
                }
            }
        });
    }

    /**
     * Thực hiện kết nối mạng an toàn kéo dữ liệu phân trang từ Server Cloud
     */
    private void loadCategoryArticles(int page) {
        isLoading = true;
        if (page == 1) {
            progressBar.setVisibility(View.VISIBLE); // Chỉ hiện vòng xoay lớn ở giữa màn hình tại Trang 1
        }

        String authHeader = null;
        if (sessionStore != null && sessionStore.isLoggedIn()) {
            authHeader = sessionStore.getTokenType() + " " + sessionStore.getAccessToken();
        }

        ApiClient.getArticleApi()
                .getArticlesByCategory(authHeader, categoryName, page, perPage)
                .enqueue(new Callback<ArticlePagedResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<ArticlePagedResponse> call, @NonNull Response<ArticlePagedResponse> response) {
                        isLoading = false;
                        progressBar.setVisibility(View.GONE);

                        if (response.isSuccessful() && response.body() != null) {
                            ArticlePagedResponse pagedData = response.body();

                            // Cập nhật lại bản đồ phân trang từ Backend gửi về
                            if (pagedData.getPagination() != null) {
                                hasNextPage = pagedData.getPagination().isHasNextPage();
                            } else {
                                hasNextPage = false;
                            }

                            // Ánh xạ danh sách DTO Mạng sang Thực thể lớp UI Articles
                            List<Articles> mappedList = mapNetworkDataToUiEntities(pagedData.getItems());

                            // Phân phối luồng nạp dữ liệu
                            if (page == 1) {
                                adapter.setItems(mappedList); // Trang 1 -> Làm mới hoàn toàn
                            } else {
                                adapter.addItems(mappedList); // Trang 2 trở đi -> Gọi hàm nối đuôi mượt mà
                            }
                        } else {
                            Toast.makeText(getContext(), "Lỗi tải danh mục: " + response.code(), Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<ArticlePagedResponse> call, @NonNull Throwable t) {
                        isLoading = false;
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(getContext(), "Không thể kết nối máy chủ danh mục.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /**
     * Hàm phụ trách map chuẩn xác cấu trúc từ DTO mạng sang thực thể Articles giao diện của bạn
     */
    private List<Articles> mapNetworkDataToUiEntities(List<ArticlePagedResponse.ArticleItemDto> networkItems) {
        List<Articles> uiList = new ArrayList<>();
        if (networkItems == null) return uiList;

        for (ArticlePagedResponse.ArticleItemDto item : networkItems) {
            if (item == null) continue;

            String title = item.getTitle() != null ? item.getTitle() : "(Không có tiêu đề)";
            String source = item.getSource() != null ? item.getSource() : "TechByte";
            String time = item.getPublishedAt() != null ? item.getPublishedAt() : "Vừa xong";
            String thumbnail = item.getThumbnailUrl() != null ? item.getThumbnailUrl() : "";

            // Định dạng chuỗi gộp hiển thị trên TextView tv_info_category_card
            String cleanSource = com.example.myapplication.util.FormatUtils.getCleanSourceName(source);
            String summaryText = cleanSource + " • " + time;

            uiList.add(new Articles(
                    String.valueOf(item.getId()),
                    title,
                    summaryText,
                    "", // Content để rỗng vì sẽ được load chi tiết khi click vào đọc báo
                    item.getCategory() != null ? item.getCategory() : categoryName,
                    source,
                    "TechByte Premium",
                    thumbnail,
                    time,
                    System.currentTimeMillis()
            ));
        }
        return uiList;
    }

    /**
     * Đóng gói ID thật phóng thẳng sang màn hình chi tiết bài báo (ArticleDetailActivity)
     */
    private void navigateToDetail(Articles article) {
        if (article == null) return;

        Intent intent = new Intent(getActivity(), ArticleDetailActivity.class);
        intent.putExtra("article_id", article.getId());
        intent.putExtra("article_title", article.getTitle());
        intent.putExtra("article_category", article.getCategory());
        startActivity(intent);
    }
}