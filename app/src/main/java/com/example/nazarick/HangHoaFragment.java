package com.example.nazarick;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;

public class HangHoaFragment extends Fragment {

    private TextView txt_hanghoa, txt_tongton;
    private ListView listView;
    private ArrayList<HangHoa> danhSachHangHoa;
    private HangHoaAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_hang_hoa, container, false);

        txt_hanghoa = view.findViewById(R.id.txt_hanghoa);
        txt_tongton = view.findViewById(R.id.txt_hanghoa);
        listView = view.findViewById(R.id.lv_hanghoa);

        khoiTaoDuLieu();

        capNhatTongTon();

        adapter = new HangHoaAdapter(requireContext(), danhSachHangHoa);
        listView.setAdapter(adapter);

        return view;
    }

    private void khoiTaoDuLieu() {
        danhSachHangHoa = new ArrayList<>();

        // Thêm dữ liệu mẫu
        danhSachHangHoa.add(new HangHoa("Trà sữa", 50, 20000));
        danhSachHangHoa.add(new HangHoa("Trà đào", 30, 25000));
        danhSachHangHoa.add(new HangHoa("Cafe đen", 40, 15000));
        danhSachHangHoa.add(new HangHoa("Cafe sữa", 35, 20000));
        danhSachHangHoa.add(new HangHoa("Cafe đá", 25, 25000));
    }

    private void capNhatTongTon() {
        int tongTon = 0;
        for (HangHoa hh : danhSachHangHoa) {
            tongTon += hh.getSoLuongTon();
        }
        txt_tongton.setText(String.valueOf(tongTon));
    }

    // Class model cho Hàng Hóa
    public static class HangHoa {
        private String tenSanPham;
        private int soLuongTon;
        private int giaBan;

        public HangHoa(String tenSanPham, int soLuongTon, int giaBan) {
            this.tenSanPham = tenSanPham;
            this.soLuongTon = soLuongTon;
            this.giaBan = giaBan;
        }

        public String getTenSanPham() {
            return tenSanPham;
        }

        public void setTenSanPham(String tenSanPham) {
            this.tenSanPham = tenSanPham;
        }

        public int getSoLuongTon() {
            return soLuongTon;
        }

        public void setSoLuongTon(int soLuongTon) {
            this.soLuongTon = soLuongTon;
        }

        public int getGiaBan() {
            return giaBan;
        }

        public void setGiaBan(int giaBan) {
            this.giaBan = giaBan;
        }
    }

    // Custom Adapter cho ListView
    private static class HangHoaAdapter extends ArrayAdapter<HangHoa> {

        public HangHoaAdapter(android.content.Context context, ArrayList<HangHoa> hangHoas) {
            super(context, 0, hangHoas);
        }

        @NonNull
        @Override
        public View getView(int position, View convertView, @NonNull ViewGroup parent) {
            HangHoa hangHoa = getItem(position);

            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_hang_hoa, parent, false);
            }

            TextView tvTenSP = convertView.findViewById(R.id.tv_ten_sp);
            TextView tvSoLuong = convertView.findViewById(R.id.tv_so_luong);
            TextView tvGiaBan = convertView.findViewById(R.id.tv_gia_ban);

            if (hangHoa != null) {
                tvTenSP.setText(hangHoa.getTenSanPham());
                tvSoLuong.setText("SL tồn: " + hangHoa.getSoLuongTon());
                tvGiaBan.setText(String.format("%,d VNĐ", hangHoa.getGiaBan()));
            }

            return convertView;
        }
    }
}