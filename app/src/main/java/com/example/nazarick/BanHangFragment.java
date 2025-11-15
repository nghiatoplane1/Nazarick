package com.example.nazarick;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
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

import com.google.android.material.snackbar.Snackbar;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class BanHangFragment extends Fragment {

    private Spinner spinner_sp;
    private ListView lv;
    private EditText edit_sp, edit_soluong, edit_tienthanhtoan;
    private Button btn_addsp, btn_tinhtien;

    // DB
    private DatabaseHelper dbHelper;
    private SQLiteDatabase db;

    // Spinner: danh sách sản phẩm từ DB
    private ArrayList<HangHoaFragment.HangHoa> danhSachHangHoa;
    private ArrayAdapter<HangHoaFragment.HangHoa> spinnerAdapter;

    // Giỏ hàng
    private ArrayList<SanPhamMua> danhSachMua;
    private ArrayAdapter<SanPhamMua> gioHangAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_ban_hang, container, false);

        // === ÁNH XẠ ===
        spinner_sp = view.findViewById(R.id.spinner_sp);
        lv = view.findViewById(R.id.lv);
        edit_sp = view.findViewById(R.id.edit_sp);
        edit_soluong = view.findViewById(R.id.edit_soluong);
        edit_tienthanhtoan = view.findViewById(R.id.edit_tienthanhtoan);
        btn_addsp = view.findViewById(R.id.btn_addsp);
        btn_tinhtien = view.findViewById(R.id.btn_tinhtien);

        // === MỞ DB ===
        dbHelper = new DatabaseHelper(requireContext());
        db = dbHelper.getWritableDatabase();

        // === SPINNER ===
        danhSachHangHoa = new ArrayList<>();
        spinnerAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                danhSachHangHoa);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_sp.setAdapter(spinnerAdapter);

        // GỌI SAU KHI ĐÃ GẮN ADAPTER
        loadSanPhamTuDB();

        spinner_sp.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View v, int pos, long id) {
                HangHoaFragment.HangHoa sp = danhSachHangHoa.get(pos);
                edit_sp.setText(sp.getTenSanPham());
                edit_soluong.setText("1");
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                edit_sp.setText("");
                edit_soluong.setText("");
            }
        });

        // === GIỎ HÀNG ===
        danhSachMua = new ArrayList<>();
        gioHangAdapter = new ArrayAdapter<SanPhamMua>(requireContext(),
                android.R.layout.simple_list_item_1, danhSachMua) {
            @NonNull
            @Override
            public View getView(int position, View convertView, @NonNull ViewGroup parent) {
                if (convertView == null) {
                    convertView = LayoutInflater.from(getContext())
                            .inflate(android.R.layout.simple_list_item_1, parent, false);
                }
                TextView tv = (TextView) convertView;
                tv.setText(danhSachMua.get(position).toString());
                return convertView;
            }
        };
        lv.setAdapter(gioHangAdapter);

        // === THÊM SẢN PHẨM ===
        btn_addsp.setOnClickListener(v -> {
            if (spinner_sp.getSelectedItem() == null) {
                Toast.makeText(requireContext(), "Chưa chọn sản phẩm!", Toast.LENGTH_SHORT).show();
                return;
            }

            HangHoaFragment.HangHoa spDB = (HangHoaFragment.HangHoa) spinner_sp.getSelectedItem();
            String slText = edit_soluong.getText().toString().trim();

            if (slText.isEmpty()) {
                Toast.makeText(requireContext(), "Nhập số lượng!", Toast.LENGTH_SHORT).show();
                return;
            }

            int soLuong;
            try {
                soLuong = Integer.parseInt(slText);
            } catch (NumberFormatException e) {
                Toast.makeText(requireContext(), "Số lượng không hợp lệ!", Toast.LENGTH_SHORT).show();
                return;
            }

            if (soLuong <= 0) {
                Toast.makeText(requireContext(), "Số lượng phải > 0!", Toast.LENGTH_SHORT).show();
                return;
            }

            if (soLuong > spDB.getSoLuongTon()) {
                Toast.makeText(requireContext(),
                        "Chỉ còn " + spDB.getSoLuongTon() + " sản phẩm!", Toast.LENGTH_LONG).show();
                return;
            }

            // Cập nhật hoặc thêm mới
            for (SanPhamMua item : danhSachMua) {
                if (item.tenSanPham.equals(spDB.getTenSanPham())) {
                    int tongMoi = item.soLuong + soLuong;
                    if (tongMoi > spDB.getSoLuongTon()) {
                        Toast.makeText(requireContext(), "Tổng vượt tồn kho!", Toast.LENGTH_LONG).show();
                        return;
                    }
                    item.soLuong = tongMoi;
                    gioHangAdapter.notifyDataSetChanged();
                    Snackbar.make(v, "Cập nhật số lượng!", Snackbar.LENGTH_SHORT).show();
                    edit_soluong.setText("1");
                    capNhatTongTien();
                    return;
                }
            }

            danhSachMua.add(new SanPhamMua(spDB.getTenSanPham(), soLuong, spDB.getGiaBan()));
            gioHangAdapter.notifyDataSetChanged();
            Snackbar.make(v, "Đã thêm vào giỏ!", Snackbar.LENGTH_SHORT).show();
            edit_soluong.setText("1");
            capNhatTongTien();
        });

        // === XÓA KHI LONG CLICK ===
        lv.setOnItemLongClickListener((parent, view1, position, id) -> {
            danhSachMua.remove(position);
            gioHangAdapter.notifyDataSetChanged();
            capNhatTongTien();
            Toast.makeText(requireContext(), "Đã xóa!", Toast.LENGTH_SHORT).show();
            return true;
        });

        // === TÍNH TIỀN ===
        btn_tinhtien.setOnClickListener(v -> {
            if (danhSachMua.isEmpty()) {
                Toast.makeText(requireContext(), "Giỏ hàng trống!", Toast.LENGTH_SHORT).show();
                return;
            }

            int tongTien = 0;
            StringBuilder chiTiet = new StringBuilder();

            for (SanPhamMua sp : danhSachMua) {
                tongTien += sp.giaBan * sp.soLuong;
                chiTiet.append(sp.tenSanPham).append(" x").append(sp.soLuong).append(", ");
            }
            if (chiTiet.length() > 0) chiTiet.setLength(chiTiet.length() - 2);

            // CẬP NHẬT TỒN KHO
            for (SanPhamMua sp : danhSachMua) {
                db.execSQL(
                        "UPDATE tbHangHoa SET soLuongTon = soLuongTon - ? WHERE tenSanPham = ?",
                        new Object[]{sp.soLuong, sp.tenSanPham});
            }

            // LƯU HÓA ĐƠN
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault());
            String maHD = "HD" + sdf.format(new Date());
            String thoiGian = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(new Date());

            db.execSQL(
                    "INSERT INTO tbHoaDon VALUES (?, ?, ?, ?)",
                    new Object[]{maHD, thoiGian, chiTiet.toString(), tongTien});

            // HIỂN THỊ
            edit_tienthanhtoan.setText(formatCurrency(tongTien));
            Toast.makeText(requireContext(),
                    "Thanh toán thành công!\nMã HĐ: " + maHD + "\nTổng: " + formatCurrency(tongTien),
                    Toast.LENGTH_LONG).show();

            // XÓA GIỎ
            danhSachMua.clear();
            gioHangAdapter.notifyDataSetChanged();
            capNhatTongTien();
            loadSanPhamTuDB();
        });

        // === CẬP NHẬT TỔNG TIỀN ===
        lv.setOnHierarchyChangeListener(new ViewGroup.OnHierarchyChangeListener() {
            @Override public void onChildViewAdded(View parent, View child) { capNhatTongTien(); }
            @Override public void onChildViewRemoved(View parent, View child) { capNhatTongTien(); }
        });

        // === EDGE-TO-EDGE ===
        ViewCompat.setOnApplyWindowInsetsListener(view.findViewById(R.id.fragment_ban_hang_root), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        return view;
    }

    // THÊM HÀM onResume()
    @Override
    public void onResume() {
        super.onResume();
        loadSanPhamTuDB(); // TỰ ĐỘNG LOAD KHI BẤM SANG TAB
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

    private void loadSanPhamTuDB() {
        danhSachHangHoa.clear();
        Cursor c = db.query("tbHangHoa", null, null, null, null, null, "tenSanPham ASC");
        if (c != null) {
            if (c.moveToFirst()) {
                do {
                    String ten = c.getString(0);
                    int ton = c.getInt(1);
                    int gia = c.getInt(2);
                    if (ton > 0) {
                        danhSachHangHoa.add(new HangHoaFragment.HangHoa(ten, ton, gia));
                    }
                } while (c.moveToNext());
            }
            c.close();
        }
        spinnerAdapter.notifyDataSetChanged();
    }

    private void capNhatTongTien() {
        int tong = 0;
        for (SanPhamMua sp : danhSachMua) {
            tong += sp.giaBan * sp.soLuong;
        }
        edit_tienthanhtoan.setText(formatCurrency(tong));
    }

    private String formatCurrency(int amount) {
        return NumberFormat.getNumberInstance(new Locale("vi", "VN")).format(amount) + " VNĐ";
    }

    // === MODEL GIỎ HÀNG ===
    public static class SanPhamMua {
        String tenSanPham;
        int soLuong;
        int giaBan;

        public SanPhamMua(String tenSanPham, int soLuong, int giaBan) {
            this.tenSanPham = tenSanPham;
            this.soLuong = soLuong;
            this.giaBan = giaBan;
        }

        @Override
        public String toString() {
            return tenSanPham + " x" + soLuong + " - " +
                    NumberFormat.getNumberInstance(new Locale("vi", "VN"))
                            .format((long) giaBan * soLuong) + " VNĐ";
        }
    }
}