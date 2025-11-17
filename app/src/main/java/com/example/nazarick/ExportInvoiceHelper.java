package com.example.nazarick;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;
import android.widget.Toast;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ExportInvoiceHelper {

    private Context context;
    private DatabaseHelper dbHelper;
    private SQLiteDatabase db;

    public ExportInvoiceHelper(Context context) {
        this.context = context;
        this.dbHelper = new DatabaseHelper(context);
        this.db = dbHelper.getReadableDatabase();
    }


    public boolean exportToFile() {
        try {
            // Tạo tên file với timestamp
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault());
            String fileName = "HoaDon_" + sdf.format(new Date()) + ".csv";

            // Lấy thư mục Downloads
            File exportDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            if (!exportDir.exists()) {
                exportDir.mkdirs();
            }

            File file = new File(exportDir, fileName);
            FileWriter writer = new FileWriter(file);

            // Ghi header CSV
            writer.append("Mã Hóa Đơn");
            writer.append(",");
            writer.append("Thời Gian");
            writer.append(",");
            writer.append("Chi Tiết");
            writer.append(",");
            writer.append("Tổng Tiền (VNĐ)");
            writer.append("\n");

            // Lấy dữ liệu từ database
            Cursor cursor = db.query("tbHoaDon", null, null, null, null, null, "thoiGian DESC");

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    String maHD = cursor.getString(0);
                    String thoiGian = cursor.getString(1);
                    String chiTiet = cursor.getString(2);
                    int tongTien = cursor.getInt(3);

                    // Ghi dữ liệu, xử lý dấu phẩy trong nội dung
                    writer.append(escapeCSV(maHD));
                    writer.append(",");
                    writer.append(escapeCSV(thoiGian));
                    writer.append(",");
                    writer.append(escapeCSV(chiTiet));
                    writer.append(",");
                    writer.append(String.valueOf(tongTien));
                    writer.append("\n");

                } while (cursor.moveToNext());
                cursor.close();
            }

            writer.flush();
            writer.close();

            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } finally {
            if (db != null && db.isOpen()) {
                db.close();
            }
            if (dbHelper != null) {
                dbHelper.close();
            }
        }
    }

    /**
     * Escape các ký tự đặc biệt trong CSV
     */
    private String escapeCSV(String value) {
        if (value == null) {
            return "";
        }
        // Nếu có dấu phẩy, dấu ngoặc kép hoặc xuống dòng, bọc trong dấu ngoặc kép
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    /**
     * Lấy đường dẫn file đã xuất
     */
    public String getExportPath() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault());
        String fileName = "HoaDon_" + sdf.format(new Date()) + ".csv";
        File exportDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        File file = new File(exportDir, fileName);
        return file.getAbsolutePath();
    }
}

