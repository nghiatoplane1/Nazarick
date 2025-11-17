package com.example.nazarick;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class TongQuanFragment extends Fragment {

    private TextView tvTongHoaDon;
    private TextView tvDoanhThu;
    private TextView tvGiaTriKho;
    private BarChart barChart;

    private DatabaseHelper dbHelper;
    private SQLiteDatabase db;
    private SimpleDateFormat sdfDate;

    public TongQuanFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tong_quan, container, false);

        // Ánh xạ các view
        tvTongHoaDon = view.findViewById(R.id.tvTongHoaDon);
        tvDoanhThu = view.findViewById(R.id.tvDoanhThu);
        tvGiaTriKho = view.findViewById(R.id.tvGiaTriKho);
        barChart = view.findViewById(R.id.chartContainer).findViewById(R.id.barChart);

        // Khởi tạo database
        dbHelper = new DatabaseHelper(requireContext());
        db = dbHelper.getReadableDatabase();
        sdfDate = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

        // Load dữ liệu thống kê
        loadThongKe();
        setupBarChart();
        loadDoanhThu7Ngay();
        loadTopHangBanChay();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadThongKe();
        loadDoanhThu7Ngay();
        loadTopHangBanChay();
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

    // ================= THỐNG KÊ TỔNG QUAN =================

    private void loadThongKe() {
        tinhTongHoaDon();
        tinhDoanhThu();
        tinhGiaTriKho();
    }

    private void tinhTongHoaDon() {
        int tongHoaDon = 0;
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM tbHoaDon", null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                tongHoaDon = cursor.getInt(0);
            }
            cursor.close();
        }
        tvTongHoaDon.setText(NumberFormat.getNumberInstance(Locale.getDefault()).format(tongHoaDon));
    }

    private void tinhDoanhThu() {
        long tongDoanhThu = 0;
        Cursor cursor = db.rawQuery("SELECT SUM(tongTien) FROM tbHoaDon", null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                tongDoanhThu = cursor.getLong(0);
            }
            cursor.close();
        }

        if (tongDoanhThu >= 1000000) {
            double doanhThuTrieu = tongDoanhThu / 1000000.0;
            tvDoanhThu.setText(String.format(Locale.getDefault(), "%.1fM", doanhThuTrieu));
        } else {
            tvDoanhThu.setText(formatCurrency(tongDoanhThu));
        }
    }

    private void tinhGiaTriKho() {
        long giaTriKho = 0;
        Cursor cursor = db.rawQuery("SELECT SUM(soLuongTon * giaBan) FROM tbHangHoa", null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                giaTriKho = cursor.getLong(0);
            }
            cursor.close();
        }

        if (giaTriKho >= 1000000) {
            double giaTriTrieu = giaTriKho / 1000000.0;
            tvGiaTriKho.setText(String.format(Locale.getDefault(), "%.1fM VNĐ", giaTriTrieu));
        } else {
            tvGiaTriKho.setText(formatCurrency(giaTriKho));
        }
    }

    // ================= BIỂU ĐỒ DOANH THU 7 NGÀY =================

    private void setupBarChart() {
        barChart.getDescription().setEnabled(false);
        barChart.setDrawGridBackground(false);
        barChart.setDrawBarShadow(false);
        barChart.setDrawValueAboveBar(true);
        barChart.setPinchZoom(false);
        barChart.setScaleEnabled(false);

        // Cấu hình trục X
        XAxis xAxis = barChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f);
        xAxis.setTextSize(10f);

        // Cấu hình trục Y bên trái
        YAxis leftAxis = barChart.getAxisLeft();
        leftAxis.setDrawGridLines(true);
        leftAxis.setAxisMinimum(0f);
        leftAxis.setTextSize(10f);

        // Ẩn trục Y bên phải
        barChart.getAxisRight().setEnabled(false);

        // Ẩn legend
        barChart.getLegend().setEnabled(false);
    }

    private void loadDoanhThu7Ngay() {
        ArrayList<BarEntry> entries = new ArrayList<>();
        ArrayList<String> labels = new ArrayList<>();

        // Lấy doanh thu 7 ngày gần nhất
        Map<String, Long> doanhThuTheoNgay = new LinkedHashMap<>();
        Calendar calendar = Calendar.getInstance();

        // Khởi tạo 7 ngày với giá trị 0
        for (int i = 6; i >= 0; i--) {
            calendar.setTimeInMillis(System.currentTimeMillis());
            calendar.add(Calendar.DAY_OF_YEAR, -i);
            String ngay = sdfDate.format(calendar.getTime());
            doanhThuTheoNgay.put(ngay, 0L);
        }

        // Truy vấn doanh thu từ database
        Cursor cursor = db.query("tbHoaDon",
                new String[]{"thoiGian", "tongTien"},
                null, null, null, null, "thoiGian DESC");

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    String thoiGian = cursor.getString(0);
                    long tongTien = cursor.getLong(1);

                    // Lấy ngày từ chuỗi thời gian (định dạng: dd/MM/yyyy HH:mm)
                    if (thoiGian != null && thoiGian.length() >= 10) {
                        String ngay = thoiGian.substring(0, 10);

                        if (doanhThuTheoNgay.containsKey(ngay)) {
                            long currentValue = doanhThuTheoNgay.get(ngay);
                            doanhThuTheoNgay.put(ngay, currentValue + tongTien);
                        }
                    }
                } while (cursor.moveToNext());
            }
            cursor.close();
        }

        // Tạo dữ liệu cho biểu đồ
        int index = 0;
        for (Map.Entry<String, Long> entry : doanhThuTheoNgay.entrySet()) {
            String ngay = entry.getKey();
            long doanhThu = entry.getValue();

            entries.add(new BarEntry(index, doanhThu / 1000f)); // Chia 1000 để hiển thị (ngàn VNĐ)

            // Format label: chỉ hiển thị ngày/tháng
            String[] parts = ngay.split("/");
            if (parts.length >= 2) {
                labels.add(parts[0] + "/" + parts[1]);
            } else {
                labels.add(ngay);
            }
            index++;
        }

        // Tạo dataset
        BarDataSet dataSet = new BarDataSet(entries, "Doanh thu");
        dataSet.setColor(ContextCompat.getColor(requireContext(), R.color.chart_blue));
        dataSet.setValueTextSize(9f);
        dataSet.setValueTextColor(Color.BLACK);

        // Tạo BarData và set cho chart
        BarData barData = new BarData(dataSet);
        barData.setBarWidth(0.8f);

        barChart.setData(barData);
        barChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(labels));
        barChart.animateY(1000);
        barChart.invalidate();
    }

    // ================= TOP HÀNG BÁN CHẠY =================

// Thay thế phương thức loadTopHangBanChay() trong TongQuanFragment.java

    private void loadTopHangBanChay() {
        // Tính toán sản phẩm bán chạy từ chi tiết hóa đơn
        Map<String, ProductSales> sanPhamBanChay = new HashMap<>();

        // Lấy tất cả hóa đơn
        Cursor cursor = db.query("tbHoaDon", null, null, null, null, null, null);

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    String chiTiet = cursor.getString(2); // Cột chiTiet

                    // Parse chi tiết hóa đơn (format: "tên1 xsl1, tên2 xsl2, ...")
                    if (chiTiet != null && !chiTiet.isEmpty()) {
                        // Tách theo dấu phẩy
                        String[] items = chiTiet.split(",");

                        for (String item : items) {
                            item = item.trim();

                            // Tìm vị trí của " x" (có khoảng trắng trước x)
                            int xIndex = item.lastIndexOf(" x");

                            if (xIndex != -1) {
                                // Tách tên sản phẩm và số lượng
                                String tenSP = item.substring(0, xIndex).trim();
                                String soLuongStr = item.substring(xIndex + 2).trim(); // +2 để bỏ qua " x"

                                try {
                                    int soLuong = Integer.parseInt(soLuongStr);

                                    if (!sanPhamBanChay.containsKey(tenSP)) {
                                        sanPhamBanChay.put(tenSP, new ProductSales(tenSP, 0, 0));
                                    }

                                    ProductSales ps = sanPhamBanChay.get(tenSP);
                                    ps.soLuongBan += soLuong;

                                    android.util.Log.d("TongQuanFragment",
                                            "Parsed: " + tenSP + " x " + soLuong);
                                } catch (NumberFormatException e) {
                                    android.util.Log.e("TongQuanFragment",
                                            "Lỗi parse số lượng: " + soLuongStr + " từ item: " + item);
                                    e.printStackTrace();
                                }
                            } else {
                                android.util.Log.w("TongQuanFragment",
                                        "Không tìm thấy ' x' trong item: " + item);
                            }
                        }
                    }
                } while (cursor.moveToNext());
            }
            cursor.close();
        }

        // Lấy giá bán từ bảng tbHangHoa
        for (ProductSales ps : sanPhamBanChay.values()) {
            Cursor c = db.query("tbHangHoa",
                    new String[]{"giaBan"},
                    "tenSanPham = ?",
                    new String[]{ps.tenSanPham},
                    null, null, null);

            if (c != null) {
                if (c.moveToFirst()) {
                    ps.giaBan = c.getInt(0);
                    ps.doanhThu = (long) ps.soLuongBan * ps.giaBan;
                } else {
                    android.util.Log.w("TongQuanFragment",
                            "Không tìm thấy giá cho sản phẩm: " + ps.tenSanPham);
                }
                c.close();
            }
        }

        // Sắp xếp theo doanh thu giảm dần và lấy top 3
        List<ProductSales> topProducts = new ArrayList<>(sanPhamBanChay.values());
        topProducts.sort((a, b) -> Long.compare(b.doanhThu, a.doanhThu));

        // Hiển thị top 3
        hienThiTopProducts(topProducts.size() >= 3 ? topProducts.subList(0, 3) : topProducts);
    }

    private void hienThiTopProducts(List<ProductSales> topProducts) {
        View view = getView();
        if (view == null) {
            return;
        }


        // Top 1
        if (topProducts.size() > 0) {
            TextView tvTop1Name = view.findViewById(R.id.tvTop1Name);
            TextView tvTop1Quantity = view.findViewById(R.id.tvTop1Quantity);
            TextView tvTop1Revenue = view.findViewById(R.id.tvTop1Revenue);

            if (tvTop1Name != null && tvTop1Quantity != null && tvTop1Revenue != null) {
                ProductSales ps = topProducts.get(0);
                tvTop1Name.setText(ps.tenSanPham);
                tvTop1Quantity.setText("Đã bán: " + ps.soLuongBan + " sản phẩm");
                tvTop1Revenue.setText(formatDoanhThu(ps.doanhThu));
            }
        }

        // Top 2
        if (topProducts.size() > 1) {
            TextView tvTop2Name = view.findViewById(R.id.tvTop2Name);
            TextView tvTop2Quantity = view.findViewById(R.id.tvTop2Quantity);
            TextView tvTop2Revenue = view.findViewById(R.id.tvTop2Revenue);

            if (tvTop2Name != null && tvTop2Quantity != null && tvTop2Revenue != null) {
                ProductSales ps = topProducts.get(1);
                tvTop2Name.setText(ps.tenSanPham);
                tvTop2Quantity.setText("Đã bán: " + ps.soLuongBan + " sản phẩm");
                tvTop2Revenue.setText(formatDoanhThu(ps.doanhThu));
            }
        }

        // Top 3
        if (topProducts.size() > 2) {
            TextView tvTop3Name = view.findViewById(R.id.tvTop3Name);
            TextView tvTop3Quantity = view.findViewById(R.id.tvTop3Quantity);
            TextView tvTop3Revenue = view.findViewById(R.id.tvTop3Revenue);

            if (tvTop3Name != null && tvTop3Quantity != null && tvTop3Revenue != null) {
                ProductSales ps = topProducts.get(2);
                tvTop3Name.setText(ps.tenSanPham);
                tvTop3Quantity.setText("Đã bán: " + ps.soLuongBan + " sản phẩm");
                tvTop3Revenue.setText(formatDoanhThu(ps.doanhThu));
            }
        }
    }

    // ================= HELPER CLASSES & METHODS =================

    private static class ProductSales {
        String tenSanPham;
        int soLuongBan;
        int giaBan;
        long doanhThu;

        ProductSales(String tenSanPham, int soLuongBan, int giaBan) {
            this.tenSanPham = tenSanPham;
            this.soLuongBan = soLuongBan;
            this.giaBan = giaBan;
            this.doanhThu = 0;
        }
    }

    private String formatCurrency(long amount) {
        return NumberFormat.getNumberInstance(new Locale("vi", "VN")).format(amount) + " VNĐ";
    }

    private String formatDoanhThu(long amount) {
        if (amount >= 1000000) {
            double doanhThuTrieu = amount / 1000000.0;
            return String.format(Locale.getDefault(), "%.1fM", doanhThuTrieu);
        } else if (amount >= 1000) {
            double doanhThuNgan = amount / 1000.0;
            return String.format(Locale.getDefault(), "%.1fK", doanhThuNgan);
        } else {
            return NumberFormat.getNumberInstance(Locale.getDefault()).format(amount);
        }
    }
}