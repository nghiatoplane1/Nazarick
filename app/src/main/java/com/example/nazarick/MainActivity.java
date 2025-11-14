package com.example.nazarick;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;

public class MainActivity extends AppCompatActivity {

    // Danh sách icon
    private int[] tabIcons = {
            R.drawable.ic_overview,
            R.drawable.ic_goods,
            R.drawable.ic_sale,
            R.drawable.ic_invoice,
            R.drawable.ic_more
    };

    // Danh sách tên tab
    private String[] tabTitles = {
            "Tổng quan",
            "Hàng hóa",
            "Bán hàng",
            "Hóa đơn",
            "Nhiều hơn"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TabLayout tabLayout = findViewById(R.id.tablayout);
        ViewPager viewPager = findViewById(R.id.viewpager);

        // Gán adapter cho ViewPager
        setupViewPager(viewPager);

        // Liên kết TabLayout với ViewPager
        tabLayout.setupWithViewPager(viewPager);

        // Tạo giao diện custom cho từng tab
        setupCustomTabs(tabLayout);
    }

    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new TongQuanFragment(), ""); // fragment mẫu
        adapter.addFragment(new HangHoaFragment(), "");
        adapter.addFragment(new BanHangFragment(), "");
        adapter.addFragment(new HoaDonFragment(), "");
        adapter.addFragment(new NhieuHonFragment(), "");
        viewPager.setAdapter(adapter);
    }

    private void setupCustomTabs(TabLayout tabLayout) {
        for (int i = 0; i < tabTitles.length; i++) {

            View tabView = LayoutInflater.from(this)
                    .inflate(R.layout.custom_bottom_tab, null);

            ImageView icon = tabView.findViewById(R.id.tab_icon);
            TextView text = tabView.findViewById(R.id.tab_text);

            icon.setImageResource(tabIcons[i]);
            text.setText(tabTitles[i]);

            // Tab đầu tiên được highlight
            if (i == 0) {
                icon.setColorFilter(getColor(R.color.blue));
                text.setTextColor(getColor(R.color.blue));
            }

            tabLayout.getTabAt(i).setCustomView(tabView);
        }

        // Sự kiện đổi màu khi chọn tab
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                View view = tab.getCustomView();
                ImageView icon = view.findViewById(R.id.tab_icon);
                TextView text = view.findViewById(R.id.tab_text);

                icon.setColorFilter(getColor(R.color.blue));
                text.setTextColor(getColor(R.color.blue));
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                View view = tab.getCustomView();
                ImageView icon = view.findViewById(R.id.tab_icon);
                TextView text = view.findViewById(R.id.tab_text);

                icon.setColorFilter(getColor(R.color.gray));
                text.setTextColor(getColor(R.color.gray));
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
    }
}
