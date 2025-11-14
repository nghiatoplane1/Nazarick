package com.example.nazarick;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HoaDonFragment extends Fragment {

    private Spinner spinner_time;
    private TextView txt_tongtien_hoadon;
    private ListView lv_hoadon;
    private ArrayList<HoaDon> danhSachHoaDon;
    private HoaDonAdapter adapter;
    private List<String> timePeriods;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_hoa_don, container, false);

        // Ánh xạ view
        spinner_time = view.findViewById(R.id.spinner_time);
        txt_tongtien_hoadon = view.findViewById(R.id.txt_tongtien_hoadon);
        lv_hoadon = view.findViewById(R.id.lv_hoadon);

        // Khởi tạo dữ liệu
        khoiTaoTimePeriods();
        khoiTaoDuLieuHoaDon();

        // Setup Spinner thời gian
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_dropdown_item, timePeriods);
        spinner_time.setAdapter(spinnerAdapter);

        // Setup ListView
        adapter = new HoaDonAdapter(requireContext(), danhSachHoaDon);
        lv_hoadon.setAdapter(adapter);

        // Tính tổng tiền ban đầu
        capNhatTongTien();

        // Xử lý sự kiện chọn thời gian
        spinner_time.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String period = timePeriods.get(position);
                Toast.makeText(requireContext(), "Lọc theo: " + period, Toast.LENGTH_SHORT).show();
                // Có thể thêm logic lọc hóa đơn theo thời gian ở đây
                capNhatTongTien();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        // Click vào hóa đơn để xem chi tiết
        lv_hoadon.setOnItemClickListener((parent, itemView, position, id) -> {
            HoaDon hoaDon = danhSachHoaDon.get(position);
            Toast.makeText(requireContext(),
                    "Mã HĐ: " + hoaDon.getMaHoaDon() + "\n" +
                            "Tổng tiền: " + formatCurrency(hoaDon.getTongTien()),
                    Toast.LENGTH_LONG).show();
        });

        // Xóa hóa đơn khi long click
        lv_hoadon.setOnItemLongClickListener((parent, itemView, position, id) -> {
            danhSachHoaDon.remove(position);
            adapter.notifyDataSetChanged();
            capNhatTongTien();
            Toast.makeText(requireContext(), "Đã xóa hóa đơn", Toast.LENGTH_SHORT).show();
            return true;
        });

        // Padding Edge-to-Edge
        ViewCompat.setOnApplyWindowInsetsListener(view.findViewById(R.id.fragment_hoa_don_root), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        return view;
    }

    private void khoiTaoTimePeriods() {
        timePeriods = new ArrayList<>();
        timePeriods.add("Hôm nay");
        timePeriods.add("Tuần này");
        timePeriods.add("Tháng này");
        timePeriods.add("Tất cả");
    }

    private void khoiTaoDuLieuHoaDon() {
        danhSachHoaDon = new ArrayList<>();

        // Thêm dữ liệu mẫu
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        String currentTime = sdf.format(new Date());

        danhSachHoaDon.add(new HoaDon("HD001", currentTime, "Trà sữa x2, Cafe đen x1", 55000));
        danhSachHoaDon.add(new HoaDon("HD002", currentTime, "Trà đào x1, Cafe sữa x2", 65000));
        danhSachHoaDon.add(new HoaDon("HD003", currentTime, "Cafe đá x3", 75000));
        danhSachHoaDon.add(new HoaDon("HD004", currentTime, "Trà sữa x1, Trà đào x1", 45000));
        danhSachHoaDon.add(new HoaDon("HD005", currentTime, "Cafe sữa x2, Cafe đen x1", 55000));
    }

    private void capNhatTongTien() {
        int tongTien = 0;
        for (HoaDon hd : danhSachHoaDon) {
            tongTien += hd.getTongTien();
        }
        txt_tongtien_hoadon.setText(formatCurrency(tongTien));
    }

    private String formatCurrency(int amount) {
        NumberFormat nf = NumberFormat.getNumberInstance(new Locale("vi", "VN"));
        return nf.format(amount) + " VNĐ";
    }

    // Class model cho Hóa Đơn
    public static class HoaDon {
        private String maHoaDon;
        private String thoiGian;
        private String chiTiet;
        private int tongTien;

        public HoaDon(String maHoaDon, String thoiGian, String chiTiet, int tongTien) {
            this.maHoaDon = maHoaDon;
            this.thoiGian = thoiGian;
            this.chiTiet = chiTiet;
            this.tongTien = tongTien;
        }

        public String getMaHoaDon() {
            return maHoaDon;
        }

        public String getThoiGian() {
            return thoiGian;
        }

        public String getChiTiet() {
            return chiTiet;
        }

        public int getTongTien() {
            return tongTien;
        }
    }

    // Custom Adapter cho ListView
    private class HoaDonAdapter extends ArrayAdapter<HoaDon> {

        public HoaDonAdapter(android.content.Context context, ArrayList<HoaDon> hoaDons) {
            super(context, 0, hoaDons);
        }

        @NonNull
        @Override
        public View getView(int position, View convertView, @NonNull ViewGroup parent) {
            HoaDon hoaDon = getItem(position);

            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_hoa_don, parent, false);
            }

            TextView tvMaHD = convertView.findViewById(R.id.tv_ma_hd);
            TextView tvThoiGian = convertView.findViewById(R.id.tv_thoi_gian);
            TextView tvChiTiet = convertView.findViewById(R.id.tv_chi_tiet);
            TextView tvTongTien = convertView.findViewById(R.id.tv_tong_tien);

            if (hoaDon != null) {
                tvMaHD.setText(hoaDon.getMaHoaDon());
                tvThoiGian.setText(hoaDon.getThoiGian());
                tvChiTiet.setText(hoaDon.getChiTiet());
                tvTongTien.setText(formatCurrency(hoaDon.getTongTien()));
            }

            return convertView;
        }
    }
}