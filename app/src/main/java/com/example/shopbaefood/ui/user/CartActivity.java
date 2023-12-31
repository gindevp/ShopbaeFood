package com.example.shopbaefood.ui.user;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.example.shopbaefood.R;
import com.example.shopbaefood.adapter.CartAdapter;
import com.example.shopbaefood.adapter.OrderAdapter;
import com.example.shopbaefood.model.Cart;
import com.example.shopbaefood.model.Merchant;
import com.example.shopbaefood.model.Order;
import com.example.shopbaefood.model.dto.AccountToken;
import com.example.shopbaefood.model.dto.ApiResponse;
import com.example.shopbaefood.service.ApiService;
import com.example.shopbaefood.service.MyFirebaseService;
import com.example.shopbaefood.util.Notification;
import com.example.shopbaefood.util.UtilApp;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.gson.Gson;

import java.util.List;

import cn.pedant.SweetAlert.SweetAlertDialog;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CartActivity extends AppCompatActivity {
    Intent intent;
    BottomNavigationView bottomNavigationView;
    private ProgressBar progressBar, progressBar2;
    private ScrollView scrollView;
    private Button payUp, payDown, payBtn;
    private TextView merchantName, price, note, address;
    private FrameLayout layoutPay, layoutOrder;
    private MaterialButtonToggleGroup buttonToggleGroup;
    private MaterialButton status1Button, status2Button;
    private RecyclerView rcvEmtyCart, rcvOrder, rcvCart;
    Gson gson;
    Merchant merchant;
    AccountToken user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        status1Button = findViewById(R.id.toggle_status1);
        status2Button = findViewById(R.id.toggle_status2);
        buttonToggleGroup = findViewById(R.id.toggle_group);
        progressBar2 = findViewById(R.id.progressBar_order);
        rcvOrder = findViewById(R.id.recyclerView_order);
        address = findViewById(R.id.cart_address_val);
        note = findViewById(R.id.cart_note_val);
        payBtn = findViewById(R.id.paybtn);
        layoutPay = findViewById(R.id.layout_pay);
        layoutOrder = findViewById(R.id.layout_order);
        price = findViewById(R.id.cart_price_total_val);
        merchantName = findViewById(R.id.cart_mer_name_val);
        payUp = findViewById(R.id.payUp);
        payDown = findViewById(R.id.payDown);
        scrollView = findViewById(R.id.checkout);
        bottomNavigationView = findViewById(R.id.nav_cart_detail);
        progressBar = findViewById(R.id.progressBar_cart);
        rcvCart = findViewById(R.id.recyclerView_cart);
        rcvEmtyCart = findViewById(R.id.recyclerView_empty_cart);
        gson = new Gson();
        intent = getIntent();
        SharedPreferences info = getSharedPreferences("info", MODE_PRIVATE);
        user = gson.fromJson(info.getString("info", ""), AccountToken.class);
        if (intent.hasExtra("merchant")) {
            merchant = (Merchant) intent.getSerializableExtra("merchant");
            merchantName.setText(merchant.getName());
            getCart(user.getUser().getId(), merchant.getId());
            Log.d("logCart", "userid:" + user.getUser().getId() + "merId" + merchant.getId());
        }
        if(intent.hasExtra("pageOrder")){
            buttonToggleGroup.setVisibility(View.GONE);
            payUp.setVisibility(View.GONE);
            layoutPay.setVisibility(View.GONE);
            rcvCart.setVisibility(View.GONE);
            layoutOrder.setVisibility(View.VISIBLE);
            ApiService apiService = UtilApp.retrofitAuth(this).create((ApiService.class));
            Call<ApiResponse<List<Order>>> call = apiService.orderHistory(user.getUser().getId());
            call.enqueue(new Callback<ApiResponse<List<Order>>>() {
                @Override
                public void onResponse(Call<ApiResponse<List<Order>>> call, Response<ApiResponse<List<Order>>> response) {
                    if (response.isSuccessful()) {
                        progressBar2.setVisibility(View.GONE);
                        handlerOrderList(response.body().getData());
                    } else {
                        Notification.sweetAlert(CartActivity.this, SweetAlertDialog.ERROR_TYPE, "Error", "Lỗi rồi, thử lại đi");
                    }
                }

                @Override
                public void onFailure(Call<ApiResponse<List<Order>>> call, Throwable t) {
                    Notification.sweetAlert(CartActivity.this, SweetAlertDialog.ERROR_TYPE, "Error", "Lỗi hệ thống phía server");
                }
            });
        }
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                intent.setClass(CartActivity.this, HomeUserActivity.class);
                switch (item.getItemId()) {
                    case R.id.tab1_c:
                        intent.putExtra("pageToDisplay", 0); // 1 là trang bạn muốn hiển thị
                        startActivity(intent);
                        return true;
                    case R.id.tab2_c:
                        intent.putExtra("pageToDisplay", 1); // 1 là trang bạn muốn hiển thị
                        startActivity(intent);
                        return true;
                    case R.id.tab3_c:
                        intent.putExtra("pageToDisplay", 2); // 1 là trang bạn muốn hiển thị
                        startActivity(intent);
                        return true;
                    // Thêm các case cho các item khác nếu cần
                }
                return false;
            }
        });
        payUp.setOnClickListener(v -> {
            scrollView.setVisibility(View.VISIBLE);
            payUp.setVisibility(View.GONE);
            payDown.setVisibility(View.VISIBLE);
        });
        payDown.setOnClickListener(v -> {
            scrollView.setVisibility(View.GONE);
            payUp.setVisibility(View.VISIBLE);
            payDown.setVisibility(View.GONE);
        });
        isCheckedBtn();
        buttonToggleGroup.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                if (checkedId == R.id.toggle_status2) {
                    isCheckedBtn();
                    layoutPay.setVisibility(View.GONE);
                    layoutOrder.setVisibility(View.VISIBLE);
                    scrollView.setVisibility(View.GONE);
                    payUp.setVisibility(View.VISIBLE);
                    payDown.setVisibility(View.GONE);
                    ApiService apiService = UtilApp.retrofitAuth(this).create((ApiService.class));
                    Call<ApiResponse<List<Order>>> call = apiService.orderHistory(user.getUser().getId());
                    call.enqueue(new Callback<ApiResponse<List<Order>>>() {
                        @Override
                        public void onResponse(Call<ApiResponse<List<Order>>> call, Response<ApiResponse<List<Order>>> response) {
                            if (response.isSuccessful()) {
                                progressBar2.setVisibility(View.GONE);
                                handlerOrderList(response.body().getData());
                            } else {
                                Notification.sweetAlert(CartActivity.this, SweetAlertDialog.ERROR_TYPE, "Error", "Lỗi rồi, thử lại đi");
                            }
                        }

                        @Override
                        public void onFailure(Call<ApiResponse<List<Order>>> call, Throwable t) {
                            Notification.sweetAlert(CartActivity.this, SweetAlertDialog.ERROR_TYPE, "Error", "Lỗi hệ thống phía server");
                        }
                    });

                } else if (checkedId == R.id.toggle_status1) {
                    isCheckedBtn();
                    layoutPay.setVisibility(View.VISIBLE);
                    layoutOrder.setVisibility(View.GONE);
                }
            }
        });


        if (intent.hasExtra("merchant")) {
            payBtn.setOnClickListener(v -> {
                ApiService apiService = UtilApp.retrofitAuth(this).create(ApiService.class);
                double sum = Double.parseDouble(String.valueOf(price.getText()).substring(0, price.getText().length() - 2));
                Call<ApiResponse> call = apiService.odering(user.getUser().getId(), merchant.getId(), note.getText().toString(), address.getText().toString(), sum);
                call.enqueue(new Callback<ApiResponse>() {
                    @Override
                    public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                        if (response.isSuccessful()) {
                            CartAdapter adapter = (CartAdapter) rcvCart.getAdapter();
                            adapter.clearCart();
                            rcvCart.setVisibility(View.GONE);
                            scrollView.setVisibility(View.GONE);
                            payUp.setVisibility(View.VISIBLE);
                            payDown.setVisibility(View.GONE);
                            payUp.setEnabled(false);
                            rcvEmtyCart.setVisibility(View.VISIBLE);
                            payBtn.setEnabled(false);
                            address.setText("");
                            note.setText("");
                            MyFirebaseService.sendMessageToTopic("order_merchant_" + merchant.getId(), "Người mua mới", "Có người vừa đặt hàng tên: " + user.getUser().getName());
                            Notification.sweetAlertNow(CartActivity.this, SweetAlertDialog.SUCCESS_TYPE, "Success", "Đặt hàng thành công");
                        } else {
                            Notification.sweetAlertNow(CartActivity.this, SweetAlertDialog.ERROR_TYPE, "Error", "Đặt hàng không thành công");
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse> call, Throwable t) {
                        Notification.sweetAlertNow(CartActivity.this, SweetAlertDialog.WARNING_TYPE, "Error", "Lỗi hệ thông phía server");
                    }
                });
            });
        }
    }

    private void isCheckedBtn() {
        if (status1Button.isChecked()) {
            status1Button.setClickable(false);
            status2Button.setClickable(true);
        } else if (status2Button.isChecked()) {
            status2Button.setClickable(false);
            status1Button.setClickable(true);
        }
    }

    private void getCart(Long userId, Long merId) {
        ApiService apiService = UtilApp.retrofitAuth(this).create(ApiService.class);
        Call<ApiResponse<List<Cart>>> call = apiService.listCart(userId, merId);
        call.enqueue(new Callback<ApiResponse<List<Cart>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Cart>>> call, Response<ApiResponse<List<Cart>>> response) {
                if (response.isSuccessful()) {
                    progressBar.setVisibility(View.GONE);
                    handleCartList(response.body().getData());
                    if (response.body().getData().isEmpty()) {
                        payUp.setEnabled(false);
                        rcvCart.setVisibility(View.GONE);
                        rcvEmtyCart.setVisibility(View.VISIBLE);
                        payBtn.setEnabled(false);
                    } else {
                        payUp.setEnabled(true);
                        rcvCart.setVisibility(View.VISIBLE);
                        rcvEmtyCart.setVisibility(View.GONE);
                        payBtn.setEnabled(true);
                    }
                } else {
                    Notification.sweetAlertNow(CartActivity.this, SweetAlertDialog.ERROR_TYPE, "Không có dữ liệu", "");
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<Cart>>> call, Throwable t) {
                Notification.sweetAlert(CartActivity.this, SweetAlertDialog.ERROR_TYPE, "Lôi hệ thống phía server", "");
            }
        });

    }

    private void handleCartList(List<Cart> data) {
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 1);
        rcvCart.setLayoutManager(gridLayoutManager);
        CartAdapter adapter = new CartAdapter(data, price);
        rcvCart.setAdapter(adapter);
    }

    private void handlerOrderList(List<Order> data) {
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 1);
        rcvOrder.setLayoutManager(gridLayoutManager);
        OrderAdapter adapter = new OrderAdapter(data, user.getRoles()[0]);
        rcvOrder.setAdapter(adapter);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    protected void onStart() {
        super.onStart();
//        MyFirebaseService.subscribeToTopic("order_user_" + user.getId());
    }

    @Override
    protected void onStop() {
        super.onStop();
//        MyFirebaseService.unsubscribeFromTopic("order_user_" + user.getId());
    }
}