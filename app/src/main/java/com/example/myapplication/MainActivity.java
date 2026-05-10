package com.example.myapplication;

import android.os.Bundle;
import android.view.View;

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

import com.example.myapplication.Entity.Articles;
import com.example.myapplication.databinding.ActivityMainBinding;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    ActivityMainBinding activityMainBinding;
    private RecyclerView recyclerView;
    private ArticleAdapter articleAdapter;
    private List<Articles> articleList;

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

        // Update the date in the header
        updateHeaderDate();

        // Setup RecyclerView for articles
        setupRecyclerView();

        // Xử lý sự kiện click trên Bottom Navigation
        com.google.android.material.bottomnavigation.BottomNavigationView navView = findViewById(R.id.home_bottom_navigation);
        if(navView != null){
            activityMainBinding.layoutBottomNav.homeBottomNavigation.setOnItemSelectedListener(item -> {
                int id = item.getItemId();

                if (id == R.id.bottom_nav_home) {
                    // Hiện lại giao diện Trang chủ
                    showHomeUI();
                    return true;
                } else if (id == R.id.bottom_nav_user) {
                    // Chuyển sang giao diện Cá nhân (Đăng nhập)
                    showUserUI();
                    return true;
                }
                return false;
            });
        }
        // Khôi phục trạng thái tab khi Activity bị recreate (do đổi theme)
        if (savedInstanceState == null) {
            // Mặc định luôn ở Trang chủ khi khởi động lần đầu
            activityMainBinding.layoutBottomNav.homeBottomNavigation.setSelectedItemId(R.id.bottom_nav_home);
            showHomeUI();
        } else {
            // Đọc lại ID của tab đang chọn từ savedInstanceState thay vì view chưa được restore
            int selectedId = savedInstanceState.getInt("selected_tab", R.id.bottom_nav_home);
            if (selectedId == R.id.bottom_nav_user) {
                // Fragment đã được tự động restore bởi FragmentManager,
                // ta chỉ cần ẩn/hiện Layout và Header tương ứng.
                activityMainBinding.layoutHeader.getRoot().setVisibility(View.GONE);
                activityMainBinding.layoutHomeViews.getRoot().setVisibility(View.GONE);
                activityMainBinding.fragmentContainer.setVisibility(View.VISIBLE);
            } else {
                showHomeUI();
            }
        }
    }

    /**
     * Cập nhật ngày tháng hiện tại trên header theo định dạng tiếng Việt.
     */
    private void updateHeaderDate() {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("EEEE, dd 'Tháng' M, yyyy", new Locale("vi"));
            String currentDate = sdf.format(new Date());
            // Viết hoa chữ cái đầu
            currentDate = currentDate.substring(0, 1).toUpperCase() + currentDate.substring(1);
            android.widget.TextView tvDate = findViewById(R.id.tv_date);
            if (tvDate != null) {
                tvDate.setText(currentDate);
            }
        } catch (Exception e) {
            // Ignore date formatting errors
        }
    }

    /**
     * Thiết lập RecyclerView với dữ liệu mẫu bài viết tin tức công nghệ.
     */
    private void setupRecyclerView() {
        recyclerView = findViewById(R.id.recycler_view);
        if (recyclerView == null) return;

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        articleList = getSampleArticles();
        articleAdapter = new ArticleAdapter(this, articleList);
        recyclerView.setAdapter(articleAdapter);
    }

    /**
     * Tạo danh sách bài viết mẫu — thay bằng dữ liệu từ Firebase/API sau.
     */
    private List<Articles> getSampleArticles() {
        List<Articles> articles = new ArrayList<>();

        articles.add(new Articles(
                "1",
                "GPT-5: Cái nhìn đầu tiên về Tác nhân Tự trị và Suy luận",
                "Mô hình mới của OpenAI hứa hẹn khả năng suy luận vượt trội, mở ra kỷ nguyên mới cho AI tự động hoạt động và ra quyết định.",
                "Bối cảnh năng suất hiện đại đã trải qua sự biến đổi lớn trong mười hai tháng qua. Khi các mô hình ngôn ngữ lớn chuyển từ những thử nghiệm mới lạ sang cơ sở hạ tầng cốt lõi, cách chúng ta nhìn nhận \"công việc\" đang thay đổi căn bản.\n\nKhông chỉ còn là về đầu ra, mà là sự phối hợp giữa trực giác con người và độ chính xác của thuật toán. Trong phân tích sâu này, chúng ta khám phá các ngành cụ thể nơi những thay đổi này rõ rệt nhất.\n\nTừ kỹ thuật phần mềm đến nghiên cứu pháp lý, việc tích hợp AI tạo sinh không chỉ tăng tốc các nhiệm vụ mà còn cho phép khám phá các không gian vấn đề phức tạp trước đây được coi là quá tốn tài nguyên để giải quyết.\n\nMột trong những thay đổi sâu sắc nhất được thấy trong lĩnh vực truyền thông trực quan. Các nhà thiết kế đang tận dụng mô hình khuếch tán để lặp qua hàng ngàn khái niệm trong thời gian trước đây cần để phác thảo một wireframe.",
                "AI",
                "TechByte",
                "Nguyễn Minh Trí",
                "",
                "10 Th5, 2026",
                System.currentTimeMillis()
        ));

        articles.add(new Articles(
                "2",
                "Máy tính lượng tử đe dọa các giao thức mã hóa tiêu chuẩn vào năm 2026",
                "Các chuyên gia cảnh báo rằng sự tiến bộ nhanh chóng trong điện toán lượng tử có thể phá vỡ các hệ thống bảo mật hiện tại sớm hơn dự kiến.",
                "Điện toán lượng tử đang tiến bước mạnh mẽ, và các chuyên gia an ninh mạng đang lo ngại. Khả năng của máy tính lượng tử trong việc phá vỡ các thuật toán mã hóa hiện tại đang trở thành mối đe dọa thực sự.\n\nRSA và ECC - hai trụ cột của bảo mật internet hiện đại - có thể bị phá vỡ bởi thuật toán Shor chạy trên máy tính lượng tử đủ mạnh. Điều này có nghĩa là mọi giao dịch ngân hàng, email bảo mật, và thông tin cá nhân đều có nguy cơ.\n\nNIST đã công bố các tiêu chuẩn mã hóa hậu lượng tử mới, nhưng việc triển khai trên quy mô lớn vẫn còn chậm. Các tổ chức được khuyến cáo bắt đầu lên kế hoạch chuyển đổi ngay từ bây giờ.",
                "Bảo mật",
                "CyberNews",
                "Trần Anh Khoa",
                "",
                "09 Th5, 2026",
                System.currentTimeMillis() - 86400000
        ));

        articles.add(new Articles(
                "3",
                "Cuộc đua 2nm: Tại sao Đài Loan vẫn là trung tâm của thế giới phần cứng",
                "TSMC tiếp tục dẫn đầu cuộc đua sản xuất chip tiên tiến, khẳng định vị thế không thể thay thế trong chuỗi cung ứng bán dẫn toàn cầu.",
                "TSMC vừa công bố tiến độ sản xuất chip 2nm đúng kế hoạch, với các lô sản phẩm đầu tiên dự kiến vào Q3 2026. Đây là bước nhảy vọt đáng kể so với quy trình 3nm hiện tại.\n\nChip 2nm hứa hẹn tăng 15% hiệu năng và giảm 30% tiêu thụ điện so với thế hệ trước. Apple, Qualcomm và NVIDIA đều đã đặt hàng cho các sản phẩm sử dụng quy trình mới.\n\nDù Intel và Samsung đang nỗ lực bắt kịp, khoảng cách công nghệ vẫn còn lớn. Địa chính trị và rủi ro chuỗi cung ứng tiếp tục là yếu tố quan trọng.",
                "Phần cứng",
                "ChipWorld",
                "Lê Hoàng Nam",
                "",
                "08 Th5, 2026",
                System.currentTimeMillis() - 172800000
        ));

        articles.add(new Articles(
                "4",
                "Rust chính thức tiếp quản nhân Linux. Đây là lý do tại sao.",
                "Ngôn ngữ lập trình Rust đang dần thay thế C trong các module mới của nhân Linux, mang lại sự an toàn bộ nhớ mà không hy sinh hiệu năng.",
                "Linus Torvalds vừa phê duyệt đợt merge lớn nhất của mã Rust vào nhân Linux, đánh dấu bước ngoặt lịch sử cho dự án. Hơn 20 module mới đã được viết hoàn toàn bằng Rust.\n\nRust mang lại an toàn bộ nhớ tại thời điểm biên dịch mà không cần garbage collector, giải quyết hàng loạt lỗ hổng bảo mật đã tồn tại hàng thập kỷ trong mã C truyền thống.\n\nCộng đồng phát triển đang phản ứng tích cực. Nhiều maintainer đã bắt đầu chuyển đổi các driver quan trọng sang Rust, và các khóa học Rust cho kernel dev đang được mở rộng.",
                "Lập trình",
                "DevBlog",
                "Phạm Quốc Huy",
                "",
                "07 Th5, 2026",
                System.currentTimeMillis() - 259200000
        ));

        articles.add(new Articles(
                "5",
                "Silicon Valley chuẩn bị cho kỷ nguyên hậu SaaS như thế nào",
                "Một phân tích sâu về sự chuyển đổi kinh tế đơn vị của các startup AI và sự quay trở lại với tích hợp dọc.",
                "Mô hình SaaS truyền thống đang bị thách thức khi AI có thể tự động hóa toàn bộ quy trình mà trước đây cần phần mềm chuyên dụng. Các startup đang chuyển sang mô hình 'AI-as-a-Service' với cấu trúc giá hoàn toàn khác.\n\nThay vì tính phí theo người dùng, các công ty mới tính phí theo kết quả đầu ra. Điều này thay đổi cơ bản kinh tế đơn vị và buộc các công ty SaaS lớn phải tái cấu trúc.\n\nXu hướng tích hợp dọc cũng đang quay trở lại mạnh mẽ. Các công ty như Tesla, Apple đã chứng minh giá trị của việc kiểm soát toàn bộ stack.",
                "Startup",
                "VentureBeat",
                "Hoàng Thị Mai",
                "",
                "06 Th5, 2026",
                System.currentTimeMillis() - 345600000
        ));

        return articles;
    }

    private void showHomeUI() {
        // Hiện Header và Views của Trang chủ
        activityMainBinding.layoutHeader.getRoot().setVisibility(View.VISIBLE);
        activityMainBinding.layoutHomeViews.getRoot().setVisibility(View.VISIBLE);
        // Ẩn vùng chứa Fragment cá nhân
        activityMainBinding.fragmentContainer.setVisibility(View.GONE);

    }

    private void showUserUI() {
        // Ẩn Header và Views của Trang chủ
        activityMainBinding.layoutHeader.getRoot().setVisibility(View.GONE);
        activityMainBinding.layoutHomeViews.getRoot().setVisibility(View.GONE);
        // Hiện vùng chứa Fragment cá nhân và thay thế bằng Home_UserFragment
        activityMainBinding.fragmentContainer.setVisibility(View.VISIBLE);
        replaceFragment(new Home_user());
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
        // Lưu lại tab hiện tại trước khi Activity bị huỷ (ví dụ vì đổi theme)
        if (activityMainBinding != null && activityMainBinding.layoutBottomNav != null) {
            outState.putInt("selected_tab", activityMainBinding.layoutBottomNav.homeBottomNavigation.getSelectedItemId());
        }
    }
}
