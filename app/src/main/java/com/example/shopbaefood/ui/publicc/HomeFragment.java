package com.example.shopbaefood.ui.publicc;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.example.shopbaefood.R;
import com.example.shopbaefood.adapter.MerchantAdapter;
import com.example.shopbaefood.model.Merchant;
import com.example.shopbaefood.model.dto.ApiResponse;
import com.example.shopbaefood.service.ApiService;
import com.example.shopbaefood.util.Notification;
import com.example.shopbaefood.util.UtilApp;

import java.util.List;

import cn.pedant.SweetAlert.SweetAlertDialog;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeFragment extends Fragment {
    private RecyclerView rcvMerchant;
    private ProgressBar progressBar;

    public HomeFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        rcvMerchant = view.findViewById(R.id.recycler_view_p);
        progressBar=view.findViewById(R.id.progressBar_p);
        progressBar.setVisibility(View.VISIBLE);
        getListMerchant(view);
        return view;
    }

    private void getListMerchant(View v) {
        ApiService apiService= UtilApp.retrofitCF().create(ApiService.class);
        Call<ApiResponse<List<Merchant>>> call= apiService.fetMerAll();
        call.enqueue(new Callback<ApiResponse<List<Merchant>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Merchant>>> call, Response<ApiResponse<List<Merchant>>> response) {
                if(response.isSuccessful()){
                    progressBar.setVisibility(View.GONE);
                    handleMerchantList(response.body().getData(),v);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<Merchant>>> call, Throwable t) {
                Notification.sweetAlert(v.getContext(), SweetAlertDialog.ERROR_TYPE,"lỗi hệ thống","");

            }
        });

        //Mock api
//        ApiService apiService= UtilApp.retrofitCFMock().create(ApiService.class);
//        Call<List<Merchant>> call= apiService.getMerAll();
//        call.enqueue(new Callback<List<Merchant>>() {
//            @Override
//            public void onResponse(Call<List<Merchant>> call, Response<List<Merchant>> response) {
//                progressBar.setVisibility(View.GONE);
//                handleMerchantList(response.body(),v);
//            }
//
//            @Override
//            public void onFailure(Call<List<Merchant>> call, Throwable t) {
//
//            }
//        });
    }

    private void handleMerchantList(List<Merchant> merchants, View v) {
        // Truy cập dữ liệu sau khi tải xong ở đây
        GridLayoutManager gridLayoutManager = new GridLayoutManager(v.getContext(), 2);
//        LinearLayoutManager gridLayoutManager= new LinearLayoutManager(view.getContext(),RecyclerView.HORIZONTAL,false);
        rcvMerchant.setLayoutManager(gridLayoutManager);
        MerchantAdapter merchantAdapter = new MerchantAdapter(merchants);
        rcvMerchant.setAdapter(merchantAdapter);
    }
}