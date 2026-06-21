package com.example.myapplication.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.myapplication.R;

public class CategoryFragment extends Fragment {

    private static final String ARG_CATEGORY_NAME = "category_name";
    private String categoryName;

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
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // SỬA TẠI ĐÂY: Đổi từ R.layout.fragment_category thành R.layout.home_category
        View view = inflater.inflate(R.layout.home_category, container, false);

        TextView tvPlaceholder = view.findViewById(R.id.tv_category_placeholder);

        if (tvPlaceholder != null && categoryName != null) {
            tvPlaceholder.setText("Màn hình dữ liệu danh mục: " + categoryName);
        }
        return view;
    }
}