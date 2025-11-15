package com.example.nazarick;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
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
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class HoaDonFragment extends Fragment {

    private Spinner spinner_time;
    private TextView txt_tongtien_hoadon;
    private ListView lv_hoadon;
    private ArrayList<HoaDon> danhSachHoaDon;
    private HoaDonAdapter adapter;
    private List<String> timePeriods;
    private SimpleDateFormat sdfDateTime, sdfDate;

    // DB
    private DatabaseHelper dbHelper;
    private SQLiteDatabase db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_hoa_don, container, false);

        // === ÁNH XẠ ===
        spinner_time = view.findViewById(R.id.spinner_time);
        txt_tongtien_hoadon = view.findViewById(R.id.txt_tongtien_hoadon);
        lv_hoadon = view.findViewById(R.id.lv_hoadon);

        // === DB ===
        dbHelper = new DatabaseHelper(requireContext());
        db = dbHelper.getWritableDatabase();

        // === FORMAT NGÀY ===
        sdfDateTime = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        sdfDate = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

        // === SPINNER ===
        khoiTaoTimePeriods();
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(
                requireContext(), android.R.layout.simple_spinner_item, timePeriods);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_time.setAdapter(spinnerAdapter);

        // === LISTVIEW ===
        danhSachHoaDon = new ArrayList<>();
        adapter = new HoaDonAdapter(requireContext(), danhSachHoaDon);
        lv_hoadon.setAdapter(adapter);

        // === LOAD DỮ LIỆU ===
        loadHoaDonTuDB();

        // === LỌC THEO THỜI GIAN ===
        spinner_time.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                locHoaDonTheoThoiGian(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                loadHoaDonTuDB();
            }
        });

        // === CHI TIẾT KHI CLICK ===
        lv_hoadon.setOnItemClickListener((parent, itemView, position, id) -> {
            HoaDon hd = danhSachHoaDon.get(position);
            Toast.makeText(requireContext(),
                    "Mã HĐ: " + hd.getMaHoaDon() + "\n" +
                            "Thời gian: " + hd.getThoiGian() + "\n" +
                            "Chi tiết: " + hd.getChiTiet() + "\n" +
                            "Tổng: " + formatCurrency(hd.getTongTien()),
                    Toast.LENGTH_LONG).show();
        });

        // === XÓA KHI LONG CLICK ===
        lv_hoadon.setOnItemLongClickListener((parent, itemView, position, id) -> {
            xoaHoaDon(position);
            return true;
        });

        // === EDGE-TO-EDGE ===
        ViewCompat.setOnApplyWindowInsetsListener(view.findViewById(R.id.fragment_hoa_don_root), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadHoaDonTuDB();
        spinner_time.setSelection(0); // Reset về "Hôm nay"
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (db != null && db.isOpen()) {
            db.close();
        }
        if (dbHelper != null) {
            dbHelper.close();
        }
    }

    private void khoiTaoTimePeriods() {
        timePeriods = new ArrayList<>();
        timePeriods.add("Hôm nay");
        timePeriods.add("Tuần này");
        timePeriods.add("Tháng này");
        timePeriods.add("Tất cả");
    }

    // === LOAD TẤT CẢ HÓA ĐƠN ===
    private void loadHoaDonTuDB() {
        danhSachHoaDon.clear();
        Cursor c = db.query("tbHoaDon", null, null, null, null, null, "thoiGian DESC");
        if (c != null) {
            if (c.moveToFirst()) {
                do {
                    danhSachHoaDon.add(new HoaDon(
                            c.getString(0), c.getString(1), c.getString(2), c.getInt(3)
                    ));
                } while (c.moveToNext());
            }
            c.close();
        }
        adapter.notifyDataSetChanged();
        capNhatTongTien();
    }

    // === LỌC THEO THỜI GIAN ===
    private void locHoaDonTheoThoiGian(int viTri) {
        String luaChon = timePeriods.get(viTri);
        danhSachHoaDon.clear();

        String query = "SELECT * FROM tbHoaDon";
        String[] args = null;

        if (!luaChon.equals("Tất cả")) {
            Calendar cal = Calendar.getInstance();
            String ngayHienTai = sdfDate.format(cal.getTime());

            switch (luaChon) {
                case "Hôm nay":
                    query += " WHERE thoiGian LIKE ?";
                    args = new String[]{ngayHienTai + "%"};
                    break;

                case "Tuần này":
                    cal.set(Calendar.DAY_OF_WEEK, cal.getFirstDayOfWeek());
                    String tuanBatDau = sdfDate.format(cal.getTime());
                    query += " WHERE thoiGian >= ?";
                    args = new String[]{tuanBatDau + " 00:00"};
                    break;

                case "Tháng này":
                    cal.set(Calendar.DAY_OF_MONTH, 1);
                    String thangBatDau = sdfDate.format(cal.getTime());
                    query += " WHERE thoiGian >= ?";
                    args = new String[]{thangBatDau + " 00:00"};
                    break;
            }
        }

        query += " ORDER BY thoiGian DESC";

        Cursor c = db.rawQuery(query, args);
        if (c != null) {
            if (c.moveToFirst()) {
                do {
                    danhSachHoaDon.add(new HoaDon(
                            c.getString(0), c.getString(1), c.getString(2), c.getInt(3)
                    ));
                } while (c.moveToNext());
            }
            c.close();
        }
        adapter.notifyDataSetChanged();
        capNhatTongTien();
    }

    // === XÓA HÓA ĐƠN ===
    private void xoaHoaDon(int position) {
        HoaDon hd = danhSachHoaDon.get(position);
        int rows = db.delete("tbHoaDon", "maHoaDon = ?", new String[]{hd.getMaHoaDon()});
        if (rows > 0) {
            Toast.makeText(requireContext(), "Đã xóa HĐ: " + hd.getMaHoaDon(), Toast.LENGTH_SHORT).show();
            loadHoaDonTuDB();
            spinner_time.setSelection(0);
        }
    }

    // === CẬP NHẬT TỔNG TIỀN ===
    private void capNhatTongTien() {
        long tong = 0;
        for (HoaDon hd : danhSachHoaDon) {
            tong += hd.getTongTien();
        }
        txt_tongtien_hoadon.setText(formatCurrency(tong));
    }

    // === FORMAT TIỀN ===
    private String formatCurrency(long amount) {
        return NumberFormat.getNumberInstance(new Locale("vi", "VN")).format(amount) + " VNĐ";
    }

    // === MODEL HÓA ĐƠN ===
    public static class HoaDon {
        private final String maHoaDon, thoiGian, chiTiet;
        private final int tongTien;

        public HoaDon(String maHoaDon, String thoiGian, String chiTiet, int tongTien) {
            this.maHoaDon = maHoaDon;
            this.thoiGian = thoiGian;
            this.chiTiet = chiTiet;
            this.tongTien = tongTien;
        }

        public String getMaHoaDon() { return maHoaDon; }
        public String getThoiGian() { return thoiGian; }
        public String getChiTiet() { return chiTiet; }
        public int getTongTien() { return tongTien; }
    }

    // === ADAPTER + VIEWHOLDER ===
    private class HoaDonAdapter extends ArrayAdapter<HoaDon> {

        private class ViewHolder {
            TextView tvMaHD, tvThoiGian, tvChiTiet, tvTongTien;
        }

        public HoaDonAdapter(android.content.Context context, ArrayList<HoaDon> hoaDons) {
            super(context, 0, hoaDons);
        }

        @NonNull
        @Override
        public View getView(int position, View convertView, @NonNull ViewGroup parent) {
            ViewHolder holder;

            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_hoa_don, parent, false);
                holder = new ViewHolder();
                holder.tvMaHD = convertView.findViewById(R.id.tv_ma_hd);
                holder.tvThoiGian = convertView.findViewById(R.id.tv_thoi_gian);
                holder.tvChiTiet = convertView.findViewById(R.id.tv_chi_tiet);
                holder.tvTongTien = convertView.findViewById(R.id.tv_tong_tien);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            HoaDon hd = getItem(position);
            if (hd != null) {
                holder.tvMaHD.setText(hd.getMaHoaDon());
                holder.tvThoiGian.setText(hd.getThoiGian());
                holder.tvChiTiet.setText(hd.getChiTiet());
                holder.tvTongTien.setText(formatCurrency(hd.getTongTien()));
            }
            return convertView;
        }
    }
}