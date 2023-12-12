package com.scanny.scanner.utils;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.scanny.scanner.R;

public class EnterCodeBottomSheetFragment extends BottomSheetDialogFragment {
    private EditText etEnterCode;
    private Button btnConfirm;

    // Listener để thông báo kết quả về Activity gọi
    public interface EnterCodeListener {
        void onCodeEntered(String code);
    }

    private EnterCodeListener listener;

    public void setEnterCodeListener(EnterCodeListener listener) {
        this.listener = listener;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bottom_sheet_enter_code, container, false);

        etEnterCode = view.findViewById(R.id.etEnterCode);
        btnConfirm = view.findViewById(R.id.btnConfirm);

        btnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String enteredCode = etEnterCode.getText().toString().trim();
                if (listener != null) {
                    listener.onCodeEntered(enteredCode);
                }
                dismiss();  // Đóng BottomSheet sau khi xử lý
            }
        });

        return view;
    }
}