package com.example.shopbaefood.adapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.StrikethroughSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.shopbaefood.R;
import com.example.shopbaefood.model.Product;
import com.example.shopbaefood.model.dto.AccountToken;
import com.example.shopbaefood.model.dto.ApiResponse;
import com.example.shopbaefood.service.ApiService;
import com.example.shopbaefood.util.Notification;
import com.example.shopbaefood.util.UtilApp;
import com.google.gson.Gson;

import java.util.List;

import cn.pedant.SweetAlert.SweetAlertDialog;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {
    private List<Product> productList;
    private Gson gson;
    private AccountToken accountToken;
    private Long userId;

    public ProductAdapter(List<Product> productList) {
        this.productList = productList;
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        gson = new Gson();
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_product, parent, false);
        View view2 = LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_mer_detail, parent, false);
        SharedPreferences info = view2.getContext().getSharedPreferences("info", Context.MODE_PRIVATE);
        if (!info.getString("info", "").isEmpty()) {
            accountToken = gson.fromJson(info.getString("info", ""), AccountToken.class);
            userId = accountToken.getUser().getId();
        }
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Product product = productList.get(position);
        if (product == null) {
            return;
        }
        holder.tvName.setText(product.getName());

        SpannableString spannableString = new SpannableString(String.valueOf(product.getOldPrice()));
        spannableString.setSpan(new StrikethroughSpan(), 0, String.valueOf(product.getOldPrice()).length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        // Tạo một chuỗi văn bản mới kết hợp với chuỗi gạch ngang
        SpannableStringBuilder builder = new SpannableStringBuilder();
        builder.append(spannableString); // Giá cũ có gạch ngang

        // Hiển thị chuỗi văn bản trong TextView
        holder.tvOldPrice.setText(builder);

        holder.tvNewPrice.setText(String.valueOf(product.getNewPrice()));
        UtilApp.getImagePicasso(product.getImage(), holder.imgProduct);
        holder.imgAddToCart.setOnClickListener(view -> {
            // TODO: them code add to card kèm alert
            Long proId = product.getId();
            Log.d("cart", "userId: " + userId + " productId" + proId);
            holder.imgAddToCart.setImageResource(R.drawable.add_to_cart_checked);
            new Handler().postDelayed(() -> {
                holder.imgAddToCart.setImageResource(R.drawable.add_to_cart);
            }, 1000);
            ApiService apiService = UtilApp.retrofitAuth(view.getContext()).create(ApiService.class);
            Call<ApiResponse> call = apiService.addToCart(proId, userId);
            call.enqueue(new Callback<ApiResponse>() {
                @Override
                public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                    if (response.isSuccessful()) {
                        //thêm thành công
                        Notification.sweetAlertNow(view.getContext(), SweetAlertDialog.SUCCESS_TYPE, "Success", "Thêm vào rỏ hàng thành công", 1000);
                    } else {
                        //thêm không thành công
                        Notification.sweetAlertNow(view.getContext(), SweetAlertDialog.ERROR_TYPE, "Error", "Thêm không thành công");
                    }
                }

                @Override
                public void onFailure(Call<ApiResponse> call, Throwable t) {
                    // Thông báo lỗi do hệ thống
                    Log.d("err", t.getMessage());
                    Notification.sweetAlertNow(view.getContext(), SweetAlertDialog.ERROR_TYPE, "Error server", "Lỗi hệ thống phía server");
                }
            });
        });
    }

    @Override
    public int getItemCount() {
        if (productList != null) {
            return productList.size();
        }
        return 0;
    }

    public class ProductViewHolder extends RecyclerView.ViewHolder {
        private ImageView imgProduct;
        private TextView tvName;
        private TextView tvNewPrice;
        private TextView tvOldPrice;
        private ImageView imgAddToCart;


        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            imgProduct = itemView.findViewById(R.id.image_product);
            tvName = itemView.findViewById(R.id.product_name);
            tvNewPrice = itemView.findViewById(R.id.product_price_new);
            tvOldPrice = itemView.findViewById(R.id.product_price_old);
            imgAddToCart = itemView.findViewById(R.id.img_add_to_cart);

        }
    }
}