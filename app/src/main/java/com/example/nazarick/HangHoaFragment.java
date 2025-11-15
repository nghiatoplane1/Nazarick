package com.example.nazarick;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
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

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Locale;

public class HangHoaFragment extends Fragment {

    private TextView txt_hanghoa, txt_tongsanpham;
    private ListView listView;
    private ArrayList<HangHoa> danhSachHangHoa;
    private HangHoaAdapter adapter;
    private EditText editText_nameproduct, editText_price, editText_quantity;
    private Button button_addproduct;

    // DB
    private DatabaseHelper dbHelper;
    private SQLiteDatabase db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_hang_hoa, container, false);

        // ÁNH XẠ
        editText_nameproduct = view.findViewById(R.id.editText_nameproduct);
        editText_price = view.findViewById(R.id.editText_price);
        editText_quantity = view.findViewById(R.id.editText_quantity);
        txt_hanghoa = view.findViewById(R.id.txt_hanghoa);
        txt_tongsanpham = view.findViewById(R.id.txt_tongsanpham);
        listView = view.findViewById(R.id.lv_hanghoa);
        button_addproduct = view.findViewById(R.id.button_addproduct);

        // MỞ DB
        dbHelper = new DatabaseHelper(requireContext());
        db = dbHelper.getWritableDatabase();
        danhSachHangHoa = new ArrayList<>();
        adapter = new HangHoaAdapter(requireContext(), danhSachHangHoa);
        listView.setAdapter(adapter);

        loadData();

        button_addproduct.setOnClickListener(v -> themSanPham());

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadData();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (db != null && db.isOpen()) {  // ĐÃ SỬA: null0 → null
            db.close();
        }
        if (dbHelper != null) {
            dbHelper.close();
        }
    }

    private void themSanPham() {
        String ten = editText_nameproduct.getText().toString().trim().toLowerCase();
        String giaText = editText_price.getText().toString().trim();
        String slText = editText_quantity.getText().toString().trim();

        if (ten.isEmpty() || giaText.isEmpty() || slText.isEmpty()) {
            Toast.makeText(requireContext(), "Vui lòng nhập đủ thông tin!", Toast.LENGTH_SHORT).show();
            return;
        }

        int gia, soLuong;
        try {
            gia = Integer.parseInt(giaText);
            soLuong = Integer.parseInt(slText);
        } catch (NumberFormatException e) {
            Toast.makeText(requireContext(), "Giá và số lượng phải là số!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (soLuong < 0 || gia < 0) {
            Toast.makeText(requireContext(), "Số lượng và giá phải ≥ 0!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (isTenSanPhamTonTai(ten)) {
            Toast.makeText(requireContext(), "Tên sản phẩm đã tồn tại!", Toast.LENGTH_SHORT).show();
            return;
        }

        ContentValues cv = new ContentValues();
        cv.put("tenSanPham", ten);
        cv.put("soLuongTon", soLuong);
        cv.put("giaBan", gia);

        long result = db.insert("tbHangHoa", null, cv);
        if (result != -1) {
            Toast.makeText(requireContext(), "Thêm thành công!", Toast.LENGTH_SHORT).show();
            clearInput();
            loadData();
        }
    }

    private boolean isTenSanPhamTonTai(String ten) {
        Cursor c = db.query("tbHangHoa", new String[]{"tenSanPham"}, "tenSanPham = ?", new String[]{ten}, null, null, null);
        boolean exists = c.getCount() > 0;
        c.close();
        return exists;
    }

    public void loadData() {
        danhSachHangHoa.clear();
        Cursor c = db.query("tbHangHoa", null, null, null, null, null, "tenSanPham ASC");
        if (c != null && c.moveToFirst()) {
            do {
                String ten = c.getString(0);
                int sl = c.getInt(1);
                int gia = c.getInt(2);
                danhSachHangHoa.add(new HangHoa(ten, sl, gia));
            } while (c.moveToNext());
            c.close();
        }
        capNhatThongKe();
        adapter.notifyDataSetChanged();
    }

    private void capNhatThongKe() {
        txt_tongsanpham.setText("Tổng sản phẩm: " + danhSachHangHoa.size());
        int tongTonKho = tinhTongSoLuongTonKho();
        txt_hanghoa.setText("Tổng hàng tồn kho: " + tongTonKho + " món");
    }
    private int tinhTongSoLuongTonKho() {
        int tong = 0;
        for (HangHoa hh : danhSachHangHoa) {
            tong += hh.getSoLuongTon();
        }
        return tong;
    }
    private void clearInput() {
        editText_nameproduct.setText("");
        editText_price.setText("");
        editText_quantity.setText("");
    }

    private void showUpdateDialog(HangHoa hangHoa) {
        android.app.Dialog dialog = new android.app.Dialog(requireContext());
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
                Toast.makeText(requireContext(), "Tên không được để trống!", Toast.LENGTH_SHORT).show();
                return;
            }

            String giaStr = edtPrice.getText().toString().trim();
            String slStr = edtQuantity.getText().toString().trim();
            if (giaStr.isEmpty() || slStr.isEmpty()) {
                Toast.makeText(requireContext(), "Vui lòng nhập đủ!", Toast.LENGTH_SHORT).show();
                return;
            }

            int gia, sl;
            try {
                gia = Integer.parseInt(giaStr);
                sl = Integer.parseInt(slStr);
            } catch (NumberFormatException e) {
                Toast.makeText(requireContext(), "Dữ liệu không hợp lệ!", Toast.LENGTH_SHORT).show();
                return;
            }

            if (gia < 0 || sl < 0) {
                Toast.makeText(requireContext(), "Giá và SL ≥ 0!", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!tenMoi.equals(hangHoa.getTenSanPham()) && isTenSanPhamTonTai(tenMoi)) {
                Toast.makeText(requireContext(), "Tên đã tồn tại!", Toast.LENGTH_SHORT).show();
                return;
            }

            ContentValues cv = new ContentValues();
            cv.put("tenSanPham", tenMoi);
            cv.put("giaBan", gia);
            cv.put("soLuongTon", sl);

            int rows = db.update("tbHangHoa", cv, "tenSanPham = ?", new String[]{hangHoa.getTenSanPham()});
            if (rows > 0) {
                Toast.makeText(requireContext(), "Cập nhật thành công!", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
                loadData();
            }
        });

        btnExit.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    // === ADAPTER + VIEWHOLDER (KHÔNG STATIC) ===
    public class HangHoaAdapter extends ArrayAdapter<HangHoa> {

        // ĐÃ SỬA: BỎ static → hợp lệ với Java 11
        private class ViewHolder {
            TextView tvTen, tvSL, tvGia;
            Button btnEdit, btnDelete;
        }

        public HangHoaAdapter(@NonNull android.content.Context context, @NonNull ArrayList<HangHoa> hangHoas) {
            super(context, 0, hangHoas);
        }

        @NonNull
        @Override
        public View getView(int position, View convertView, @NonNull ViewGroup parent) {
            ViewHolder holder;

            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_hang_hoa, parent, false);
                holder = new ViewHolder();
                holder.tvTen = convertView.findViewById(R.id.tv_ten_sp);
                holder.tvSL = convertView.findViewById(R.id.tv_so_luong);
                holder.tvGia = convertView.findViewById(R.id.tv_gia_ban);
                holder.btnEdit = convertView.findViewById(R.id.btn_edit);
                holder.btnDelete = convertView.findViewById(R.id.btn_delete);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            HangHoa hh = getItem(position);
            if (hh != null) {
                holder.tvTen.setText(hh.getTenSanPham());
                holder.tvSL.setText("SL tồn: " + hh.getSoLuongTon());
                holder.tvGia.setText(formatCurrency(hh.getGiaBan()));

                holder.btnDelete.setOnClickListener(v -> {
                    db.delete("tbHangHoa", "tenSanPham = ?", new String[]{hh.getTenSanPham()});
                    Toast.makeText(requireContext(), "Đã xóa!", Toast.LENGTH_SHORT).show();
                    loadData();
                });

                holder.btnEdit.setOnClickListener(v -> showUpdateDialog(hh));
            }
            return convertView;
        }
    }

    private String formatCurrency(int amount) {
        return NumberFormat.getNumberInstance(new Locale("vi", "VN")).format(amount) + " VNĐ";
    }

    // === MODEL ===
    public static class HangHoa {
        private final String tenSanPham;
        private final int soLuongTon;
        private final int giaBan;

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
            return tenSanPham != null ? tenSanPham : "Sản phẩm";
        }
    }
}