package com.example.nazarick;

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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.snackbar.Snackbar;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class BanHangFragment extends Fragment {
    private Spinner spinner_sp;
    private List<String> list;
    private ArrayList<String> arrayList;
    private ArrayAdapter<String> adapter;
    private ListView lv;
    private EditText edit_sp, edit_soluong, edit_tienthanhtoan;
    private Button btn_addsp, btn_tinhtien;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate layout cho fragment
        View view = inflater.inflate(R.layout.fragment_ban_hang, container, false);

        // Ánh xạ các view
        spinner_sp = view.findViewById(R.id.spinner_sp);
        lv = view.findViewById(R.id.lv);
        edit_sp = view.findViewById(R.id.edit_sp);
        edit_soluong = view.findViewById(R.id.edit_soluong);
        edit_tienthanhtoan = view.findViewById(R.id.edit_tienthanhtoan);
        btn_addsp = view.findViewById(R.id.btn_addsp);
        btn_tinhtien = view.findViewById(R.id.btn_tinhtien);

        // Danh sách sản phẩm
        list = new ArrayList<>();
        list.add("Trà sữa");
        list.add("Trà đào");
        list.add("Cafe đen");
        list.add("Cafe sữa");
        list.add("Cafe đá");

        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(requireContext(),
                androidx.appcompat.R.layout.support_simple_spinner_dropdown_item, list);
        spinner_sp.setAdapter(spinnerAdapter);

        // ListView
        arrayList = new ArrayList<>();
        adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, arrayList);
        lv.setAdapter(adapter);

        // Spinner chọn sản phẩm
        spinner_sp.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String msg = "Bạn chọn sản phẩm: " + list.get(position);
                Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
                edit_sp.setText(list.get(position));
                edit_soluong.setText("1");
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                Toast.makeText(requireContext(), "Bạn chưa chọn sản phẩm", Toast.LENGTH_SHORT).show();
            }
        });

        // Nút thêm sản phẩm vào ListView
        btn_addsp.setOnClickListener(v -> {
            if (!edit_sp.getText().toString().isEmpty() && !edit_soluong.getText().toString().isEmpty()) {
                arrayList.add(edit_sp.getText().toString() + " - SL: " + edit_soluong.getText().toString());
                adapter.notifyDataSetChanged();
                Snackbar.make(v, "Bạn đã thêm sản phẩm", Snackbar.LENGTH_SHORT).show();
            } else {
                Toast.makeText(requireContext(), "Vui lòng nhập sản phẩm và số lượng", Toast.LENGTH_SHORT).show();
            }
        });

        // Xóa sản phẩm khi long click
        lv.setOnItemLongClickListener((parent, listView, position, id) -> {
            arrayList.remove(position);
            adapter.notifyDataSetChanged();
            Toast.makeText(requireContext(), "Bạn đã xóa một sản phẩm", Toast.LENGTH_SHORT).show();
            return true;
        });

        // Tính tiền
        btn_tinhtien.setOnClickListener(v -> {
            int tongtien = 0;
            int traSua = 20000, traDao = 25000, cafeDen = 15000, cafeSua = 20000, cafeDa = 25000;

            for (String item : arrayList) {
                try {
                    String tenSP = item.substring(0, item.indexOf("-")).trim();
                    String slStr = item.substring(item.indexOf("SL:") + 3).trim();
                    int soLuong = Integer.parseInt(slStr);

                    int gia = 0;
                    switch (tenSP) {
                        case "Trà sữa": gia = traSua; break;
                        case "Trà đào": gia = traDao; break;
                        case "Cafe đen": gia = cafeDen; break;
                        case "Cafe sữa": gia = cafeSua; break;
                        case "Cafe đá": gia = cafeDa; break;
                    }

                    tongtien += gia * soLuong;
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(requireContext(), "Lỗi khi tính tiền!", Toast.LENGTH_SHORT).show();
                }
            }

            // Hiển thị định dạng VNĐ
            NumberFormat nf = NumberFormat.getNumberInstance(new Locale("vi", "VN"));
            edit_tienthanhtoan.setText(nf.format(tongtien) + " VNĐ");
        });

        // Padding Edge-to-Edge
        ViewCompat.setOnApplyWindowInsetsListener(view.findViewById(R.id.fragment_ban_hang_root), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        return view;
    }
}