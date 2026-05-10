package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

/**
 * ArticleDetailActivity — Displays the full content of a news article.
 * Matches the Stitch "Article Reading (TechByte Redesign)" design.
 *
 * Receives article data via Intent extras and populates the layout.
 */
public class ArticleDetailActivity extends AppCompatActivity {

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

        // Get data from Intent
        Intent intent = getIntent();
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
                    .centerCrop()
                    .into(imgHero);
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
    }
}
