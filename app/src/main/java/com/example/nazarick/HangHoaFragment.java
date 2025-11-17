package com.example.nazarick;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;

public class HangHoaFragment extends Fragment {

    private TextView txt_tongton, txt_tongsanpham;
    private ListView listView;
    private ArrayList<HangHoa> danhSachHangHoa;
    private ArrayList<HangHoa> danhSachHienThi;
    private HangHoaAdapter adapter;
    EditText editText_nameproduct;
    EditText editText_price;
    EditText editText_quantity;
    EditText edit_search; // Ô tìm kiếm
    Button button_addproduct;
    static SQLiteDatabase mydatabase;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_hang_hoa, container, false);
        editText_nameproduct = view.findViewById(R.id.editText_nameproduct);
        editText_price = view.findViewById(R.id.editText_price);
        editText_quantity = view.findViewById(R.id.editText_quantity);
        edit_search = view.findViewById(R.id.edit_search); // Khởi tạo ô tìm kiếm
        txt_tongton = view.findViewById(R.id.txt_hanghoa);
        txt_tongsanpham = view.findViewById(R.id.txt_tongsanpham);
        listView = view.findViewById(R.id.lv_hanghoa);
        button_addproduct = view.findViewById(R.id.button_addproduct);

        DatabaseHelper dbHelper = new DatabaseHelper(requireContext());
        mydatabase = dbHelper.getWritableDatabase();

        danhSachHangHoa = new ArrayList<>();
        danhSachHienThi = new ArrayList<>();
        adapter = new HangHoaAdapter(requireContext(), danhSachHienThi);
        listView.setAdapter(adapter);

        // Load dữ liệu khi mở fragment
        loadData();

        // =================== TÌM KIẾM SẢN PHẨM ===================
        edit_search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                timKiemSanPham(s.toString().trim());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Thêm sản phẩm mẫu khi nhấn button
        button_addproduct.setOnClickListener(v -> {

            String ten = editText_nameproduct.getText().toString().trim().toLowerCase();
            String giaText = editText_price.getText().toString().trim();
            String soLuongText = editText_quantity.getText().toString().trim();

            if (ten.isEmpty() || giaText.isEmpty() || soLuongText.isEmpty()) {
                Toast.makeText(getContext(), "Vui lòng nhập đủ thông tin!", Toast.LENGTH_SHORT).show();
                return;
            }

            int gia = Integer.parseInt(giaText);
            int soLuong = Integer.parseInt(soLuongText);

            ContentValues values = new ContentValues();
            values.put("tenSanPham", ten);
            values.put("soLuongTon", soLuong);
            values.put("giaBan", gia);

            long result = mydatabase.insert("tbHangHoa", null, values);

            if (result == -1) {
                Toast.makeText(getContext(), "Tên sản phẩm đã tồn tại!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), "Thêm thành công!", Toast.LENGTH_SHORT).show();
                loadData(); // Tải lại dữ liệu → cập nhật cả tìm kiếm

                editText_nameproduct.setText("");
                editText_price.setText("");
                editText_quantity.setText("");
            }
        });

        return view;
    }

    // ======================
    // LOAD DATA TỪ SQLITE
    // ======================
    public void loadData() {
        danhSachHangHoa.clear();

        Cursor c = mydatabase.query("tbHangHoa",
                null, null, null, null, null, null);

        if (c.moveToFirst()) {
            while (!c.isAfterLast()) {
                String ten = c.getString(0);
                int soLuong = c.getInt(1);
                int giaBan = c.getInt(2);

                danhSachHangHoa.add(new HangHoa(ten, soLuong, giaBan));
                c.moveToNext();
            }
        }
        c.close();

        // Cập nhật danh sách hiển thị (lọc theo tìm kiếm hiện tại)
        timKiemSanPham(edit_search.getText().toString().trim());

        capNhatTongTon();
        capNhatTongSanPham();
    }

    // ======================
    // TÌM KIẾM SẢN PHẨM
    // ======================
    private void timKiemSanPham(String keyword) {
        danhSachHienThi.clear();

        if (keyword.isEmpty()) {
            danhSachHienThi.addAll(danhSachHangHoa);
        } else {
            String lowerKeyword = keyword.toLowerCase();
            for (HangHoa hh : danhSachHangHoa) {
                if (hh.getTenSanPham().toLowerCase().contains(lowerKeyword)) {
                    danhSachHienThi.add(hh);
                }
            }
        }

        adapter.notifyDataSetChanged();
        capNhatTongTon();
        capNhatTongSanPham();
    }

    // ======================
    // CẬP NHẬT TỔNG TỒN & SỐ LƯỢNG HIỆN THỊ
    // ======================
    private void capNhatTongTon() {
        int tongTon = 0;
        for (HangHoa hh : danhSachHienThi) { // Dùng danh sách hiển thị
            tongTon += hh.getSoLuongTon();
        }
        txt_tongton.setText("Tổng tồn: " + tongTon);
    }

    private void capNhatTongSanPham() {
        int tongSanPham = danhSachHienThi.size();
        txt_tongsanpham.setText("Tổng sản phẩm: " + tongSanPham);
    }

    // ================================
    // MODEL HÀNG HÓA
    // ================================
    public static class HangHoa {
        private String tenSanPham;
        private int soLuongTon;
        private int giaBan;

        public HangHoa(String tenSanPham, int soLuongTon, int giaBan) {
            this.tenSanPham = tenSanPham;
            this.soLuongTon = soLuongTon;
            this.giaBan = giaBan;
        }

        public String getTenSanPham() { return tenSanPham; }
        public int getSoLuongTon() { return soLuongTon; }
        public int getGiaBan() { return giaBan; }

        @Override
        public String toString() {
            return tenSanPham;
        }
    }

    // ================================
    // HIỆN POPUP CẬP NHẬT
    // ================================
    private void showUpdateDialog(HangHoa hangHoa) {
        android.app.Dialog dialog = new android.app.Dialog(getContext());
        dialog.setContentView(R.layout.popup_update_hanghoa);

        EditText edtName = dialog.findViewById(R.id.edit_update_name);
        EditText edtPrice = dialog.findViewById(R.id.edit_update_price);
        EditText edtQuantity = dialog.findViewById(R.id.edit_update_quantity);
        Button btnSave = dialog.findViewById(R.id.btn_update_save);
        Button btnExit = dialog.findViewById(R.id.btn_update_exit);

        edtName.setText(hangHoa.getTenSanPham());
        edtPrice.setText(String.valueOf(hangHoa.getGiaBan()));
        edtQuantity.setText(String.valueOf(hangHoa.getSoLuongTon()));

        btnSave.setOnClickListener(v -> {
            String tenMoi = edtName.getText().toString().trim();
            if (tenMoi.isEmpty()) {
                Toast.makeText(getContext(), "Tên không được để trống!", Toast.LENGTH_SHORT).show();
                return;
            }

            int gia = Integer.parseInt(edtPrice.getText().toString());
            int soLuong = Integer.parseInt(edtQuantity.getText().toString());

            ContentValues values = new ContentValues();
            values.put("tenSanPham", tenMoi);
            values.put("giaBan", gia);
            values.put("soLuongTon", soLuong);

            // Cập nhật với tên cũ
            int rows = mydatabase.update("tbHangHoa", values, "tenSanPham = ?", new String[]{hangHoa.getTenSanPham()});

            if (rows > 0) {
                Toast.makeText(getContext(), "Cập nhật thành công!", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
                loadData(); // Tải lại dữ liệu
            } else {
                Toast.makeText(getContext(), "Cập nhật thất bại!", Toast.LENGTH_SHORT).show();
            }
        });

        btnExit.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    // ================================
    // ADAPTER HIỂN THỊ LISTVIEW
    // ================================
    public class HangHoaAdapter extends ArrayAdapter<HangHoa> {

        public HangHoaAdapter(@NonNull android.content.Context context,
                              @NonNull ArrayList<HangHoa> hangHoas) {
            super(context, 0, hangHoas);
        }

        @NonNull
        @Override
        public View getView(int position, View convertView,
                            @NonNull ViewGroup parent) {

            HangHoa hangHoa = getItem(position);

            if (convertView == null) {
                convertView = LayoutInflater.from(getContext())
                        .inflate(R.layout.item_hang_hoa, parent, false);
            }

            TextView tvTenSP = convertView.findViewById(R.id.tv_ten_sp);
            TextView tvSoLuong = convertView.findViewById(R.id.tv_so_luong);
            TextView tvGiaBan = convertView.findViewById(R.id.tv_gia_ban);
            Button btnEdit = convertView.findViewById(R.id.btn_edit);
            Button btnDelete = convertView.findViewById(R.id.btn_delete);

            if (hangHoa != null) {
                tvTenSP.setText(hangHoa.getTenSanPham());
                tvSoLuong.setText("SL tồn: " + hangHoa.getSoLuongTon());
                tvGiaBan.setText(String.format("%,d VNĐ", hangHoa.getGiaBan()));
            }

            btnDelete.setOnClickListener(v -> {
                mydatabase.delete("tbHangHoa", "tenSanPham = ?",
                        new String[]{hangHoa.getTenSanPham()});

                Toast.makeText(getContext(), "Đã xóa!", Toast.LENGTH_SHORT).show();
                loadData(); // Tải lại dữ liệu → cập nhật tìm kiếm
            });

            btnEdit.setOnClickListener(v -> showUpdateDialog(hangHoa));

            return convertView;
        }
    }
}