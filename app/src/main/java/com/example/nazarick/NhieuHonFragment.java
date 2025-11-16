package com.example.nazarick;

import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.switchmaterial.SwitchMaterial;

/**
 * A simple {@link Fragment} subclass.
 * Fragment cài đặt với các chức năng: Dark Mode, Export dữ liệu, Reset, v.v.
 */
public class NhieuHonFragment extends Fragment {

    private static final String PREFS_NAME = "NazarickSettings";
    private static final String KEY_DARK_MODE = "dark_mode_enabled";
    
    private SwitchMaterial switchDarkMode;
    private TextView textViewVersion;
    private SharedPreferences sharedPreferences;
    private View layoutExportData;

    public NhieuHonFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Khởi tạo SharedPreferences
        sharedPreferences = requireContext().getSharedPreferences(PREFS_NAME, 0);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_nhieu_hon, container, false);
        
        // Khởi tạo các view
        initViews(view);
        
        // Thiết lập sự kiện
        setupEvents();
        
        // Khôi phục trạng thái dark mode
        restoreDarkModeState();
        
        return view;
    }

    /**
     * Khởi tạo các view
     */
    private void initViews(View view) {
        switchDarkMode = view.findViewById(R.id.sw_dark_mode);
        textViewVersion = view.findViewById(R.id.txt_version);
        layoutExportData = view.findViewById(R.id.layout_export_data);
        
        // Hiển thị phiên bản (có thể lấy từ BuildConfig)
        if (textViewVersion != null) {
            try {
                String versionName = requireContext().getPackageManager()
                        .getPackageInfo(requireContext().getPackageName(), 0).versionName;
                textViewVersion.setText(versionName);
            } catch (Exception e) {
                textViewVersion.setText("1.0.0");
            }
        }
    }

    /**
     * Thiết lập các sự kiện
     */
    private void setupEvents() {
        // Sự kiện khi thay đổi Dark Mode switch
        if (switchDarkMode != null) {
            switchDarkMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
                // Lưu trạng thái vào SharedPreferences
                saveDarkModeState(isChecked);
                
                // Áp dụng dark mode
                applyDarkMode(isChecked);
                
                // Hiển thị thông báo
                String message = isChecked ? "Đã bật chế độ tối" : "Đã tắt chế độ tối";
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
            });
        }
        
        // Sự kiện khi click vào "Xuất hóa đơn"
        if (layoutExportData != null) {
            layoutExportData.setOnClickListener(v -> showExportDialog());
        }
    }
    
    /**
     * Xuất hóa đơn ra file
     */
    private void showExportDialog() {
        // Xác nhận trước khi xuất
        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Xuất hóa đơn")
                .setMessage("Bạn có muốn xuất tất cả hóa đơn ra file không?")
                .setPositiveButton("Xuất", (dialog, which) -> exportInvoices())
                .setNegativeButton("Hủy", null)
                .show();
    }
    
    /**
     * Xuất hóa đơn ra file CSV
     */
    private void exportInvoices() {
        try {
            ExportInvoiceHelper exportHelper = new ExportInvoiceHelper(requireContext());
            boolean success = exportHelper.exportToFile();
            
            if (success) {
                String path = exportHelper.getExportPath();
                Toast.makeText(requireContext(), 
                        "Đã xuất hóa đơn thành công!\nFile: " + path, 
                        Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(requireContext(), 
                        "Xuất hóa đơn thất bại. Vui lòng thử lại.", 
                        Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(requireContext(), 
                    "Lỗi: " + e.getMessage(), 
                    Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Lưu trạng thái dark mode vào SharedPreferences
     */
    private void saveDarkModeState(boolean isEnabled) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(KEY_DARK_MODE, isEnabled);
        editor.apply();
    }

    /**
     * Khôi phục trạng thái dark mode từ SharedPreferences
     */
    private void restoreDarkModeState() {
        boolean isDarkModeEnabled = sharedPreferences.getBoolean(KEY_DARK_MODE, false);
        
        // Thiết lập switch (tạm thời tắt listener để tránh trigger sự kiện)
        if (switchDarkMode != null) {
            switchDarkMode.setOnCheckedChangeListener(null);
            switchDarkMode.setChecked(isDarkModeEnabled);
            switchDarkMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
                saveDarkModeState(isChecked);
                applyDarkMode(isChecked);
                String message = isChecked ? "Đã bật chế độ tối" : "Đã tắt chế độ tối";
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
            });
        }
        
        // Áp dụng dark mode
        applyDarkMode(isDarkModeEnabled);
    }

    /**
     * Áp dụng dark mode cho toàn bộ ứng dụng
     */
    private void applyDarkMode(boolean isEnabled) {
        if (isEnabled) {
            // Bật chế độ tối
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            // Tắt chế độ tối (sử dụng chế độ sáng)
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }
}