package com.scanny.scanner.activity.uiv2fragment;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.scanny.scanner.R;
import com.scanny.scanner.databinding.Uiv2FragmentQrCodeBinding;


public class UIV2qrCodeFragment extends Fragment {

    private Uiv2FragmentQrCodeBinding binding;
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public UIV2qrCodeFragment() {
        // Required empty public constructor
    }

    public static UIV2qrCodeFragment newInstance(String param1, String param2) {
        UIV2qrCodeFragment fragment = new UIV2qrCodeFragment();
        Bundle args = new Bundle();
        args.putString( ARG_PARAM1, param1 );
        args.putString( ARG_PARAM2, param2 );
        fragment.setArguments( args );
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        if (getArguments() != null) {
            mParam1 = getArguments().getString( ARG_PARAM1 );
            mParam2 = getArguments().getString( ARG_PARAM2 );
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding=Uiv2FragmentQrCodeBinding.inflate(inflater,container,false );
        return binding.getRoot();
    }
}