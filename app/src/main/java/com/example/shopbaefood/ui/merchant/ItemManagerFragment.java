package com.example.shopbaefood.ui.merchant;


import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.example.shopbaefood.R;
import com.example.shopbaefood.adapter.ProductAdapter;
import com.example.shopbaefood.databinding.FragmentItemManagerBinding;
import com.example.shopbaefood.model.Product;
import com.example.shopbaefood.model.dto.AccountToken;
import com.example.shopbaefood.model.dto.ApiResponse;
import com.example.shopbaefood.model.dto.ProductForm;
import com.example.shopbaefood.service.ApiService;
import com.example.shopbaefood.util.Notification;
import com.example.shopbaefood.util.UtilApp;
import com.google.gson.Gson;

import java.util.List;

import cn.pedant.SweetAlert.SweetAlertDialog;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class ItemManagerFragment extends Fragment {
    private FragmentItemManagerBinding binding;
    private Uri imageUri;

    private ProductForm productForm;
    private AccountToken accountToken;
    private Gson gson;
    Dialog dialog;

    public ItemManagerFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentItemManagerBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        dialog= UtilApp.showProgressBarDialog(view.getContext());
        gson = new Gson();
        SharedPreferences sharedPreferences = view.getContext().getSharedPreferences("info", Context.MODE_PRIVATE);
        accountToken = gson.fromJson(sharedPreferences.getString("info", ""), AccountToken.class);

        binding.uploadProductCloud.setOnClickListener(v -> {
            selectImage();
        });
        binding.btnAddProduct.setOnClickListener(v -> {
            if (binding.idPro.getText().toString().isEmpty()) {
                uploadImage(v);
            } else {
                saveProduct(v);
            }
        });
        binding.popupProUp.setOnClickListener(v -> {
            binding.addProductMer.setVisibility(View.VISIBLE);
            binding.recyclerViewProducts.setVisibility(View.GONE);
            binding.popupProUp.setVisibility(View.GONE);
            binding.popupProDown.setVisibility(View.VISIBLE);
        });
        binding.popupProDown.setOnClickListener(v -> {
            UtilApp.closeKeyBoard(v);
            binding.btnAddProduct.setVisibility(View.VISIBLE);
            binding.addProductMer.setVisibility(View.GONE);
            binding.recyclerViewProducts.setVisibility(View.VISIBLE);
            binding.popupProUp.setVisibility(View.VISIBLE);
            binding.popupProDown.setVisibility(View.GONE);
        });
        binding.btnEditSubmit.setOnClickListener(v -> {
            dialog.show();
            productForm = new ProductForm(
                    Long.parseLong(binding.idPro.getText().toString()),
                    binding.productName.getText().toString(),
                    binding.productDescription.getText().toString(),
                    Double.parseDouble(binding.productOldPrice.getText().toString()),
                    Double.parseDouble(binding.productNewPrice.getText().toString()),
                    binding.imageLinkHide.getText().toString(),
                    Integer.parseInt(binding.productQuantity.getText().toString())
            );
            ApiService apiService = UtilApp.retrofitAuth(v.getContext()).create(ApiService.class);
            Call<ApiResponse> call = apiService.editProduct(productForm, accountToken.getMerchant().getId());
            call.enqueue(new Callback<ApiResponse>() {
                @Override
                public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                    dialog.cancel();
                    if (response.isSuccessful()) {
                        binding.btnEditSubmit.setVisibility(View.INVISIBLE);
                        showListProduct(v);
                        Notification.sweetAlert(v.getContext(), SweetAlertDialog.SUCCESS_TYPE, "Success", "Bạn sửa thành công ròi ");
                        clearForm();
                    } else {
                        Notification.sweetAlert(v.getContext(), SweetAlertDialog.ERROR_TYPE, "Lỗi ròi", "");
                    }
                }

                @Override
                public void onFailure(Call<ApiResponse> call, Throwable t) {
                    Notification.sweetAlert(v.getContext(), SweetAlertDialog.ERROR_TYPE, "Lỗi server ròi", "");
                }
            });
        });
        binding.btnClearProduct.setOnClickListener(v -> {
            clearForm();
        });
        showListProduct(view);
        return view;
    }

    private void clearForm() {
        binding.productName.setText("");
        binding.imageLinkHide.setText("");
        binding.productDescription.setText("");
        binding.productQuantity.setText("");
        binding.productNewPrice.setText("");
        binding.productOldPrice.setText("");
        binding.idPro.setText("");
        binding.numberOrderPro.setText("");
        binding.showwImageProduct.setVisibility(View.GONE);
        binding.btnEditSubmit.setVisibility(View.INVISIBLE);
    }

    private void showListProduct(View view) {
        ApiService apiService = UtilApp.retrofitAuth(view.getContext()).create(ApiService.class);
        Call<ApiResponse<List<Product>>> call = apiService.listProduct(accountToken.getMerchant().getId());
        call.enqueue(new Callback<ApiResponse<List<Product>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Product>>> call, Response<ApiResponse<List<Product>>> response) {
                if (response.isSuccessful()) {
                    handlerProduct(view, response.body().getData());
                } else {
                    Notification.sweetAlert(view.getContext(), SweetAlertDialog.ERROR_TYPE, "Error", "Lỗi ròi");
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<Product>>> call, Throwable t) {
                Notification.sweetAlert(view.getContext(), SweetAlertDialog.ERROR_TYPE, "Error", "Lỗi server");
            }
        });
    }

    private void handlerProduct(View view, List<Product> productList) {
        GridLayoutManager gridLayoutManager = new GridLayoutManager(view.getContext(), 1);
        binding.recyclerViewProducts.setLayoutManager(gridLayoutManager);
        ProductAdapter adapter = new ProductAdapter(productList, true, binding,dialog);
        binding.recyclerViewProducts.setAdapter(adapter);
    }

    private void saveProduct(View view) {
        dialog.show();
        productForm = new ProductForm(binding.productName.getText().toString(),
                binding.productDescription.getText().toString(),
                Double.parseDouble(binding.productOldPrice.getText().toString()),
                Double.parseDouble(binding.productNewPrice.getText().toString()),
                binding.imageLinkHide.getText().toString(),
                Integer.parseInt(binding.productQuantity.getText().toString()));

        ApiService apiService = UtilApp.retrofitAuth(view.getContext()).create(ApiService.class);
        Call<ApiResponse> call = apiService.saveProduct(productForm, accountToken.getMerchant().getId());
        call.enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                dialog.cancel();
                if (response.isSuccessful()) {
                    clearForm();
                    showListProduct(view);
                    Notification.sweetAlertNow(view.getContext(), SweetAlertDialog.SUCCESS_TYPE, "Success", "Thêm sản phẩm thành công", 1000);
                } else {
                    Notification.sweetAlert(view.getContext(), SweetAlertDialog.ERROR_TYPE, "Error", "Lỗi ròi không thêm được");
                }
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                Notification.sweetAlert(view.getContext(), SweetAlertDialog.ERROR_TYPE, "Error", "Lỗi server không kết nối được");
            }
        });
    }

    private void uploadImage(View view) {
        if (imageUri == null) {
            Notification.sweetAlertNow(view.getContext(), SweetAlertDialog.WARNING_TYPE, "Phải có ảnh", "");
        } else {
            UtilApp.uploadImageToFirebaseStorage(imageUri, new UtilApp.OnImageUploadListener() {
                @Override
                public void onSuccess(String imageUrl) {
                    Toast.makeText(view.getContext(), "success", Toast.LENGTH_SHORT).show();
                    binding.imageLinkHide.setText(imageUrl);
                    saveProduct(view);
                }

                @Override
                public void onFailure(String error) {
                    Toast.makeText(view.getContext(), "faile upload image", Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    private void selectImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, 100);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100 && data != null && data.getData() != null) {
            binding.showwImageProduct.setVisibility(View.VISIBLE);
            imageUri = data.getData();
            binding.showwImageProduct.setImageURI(imageUri);
        }
    }
}