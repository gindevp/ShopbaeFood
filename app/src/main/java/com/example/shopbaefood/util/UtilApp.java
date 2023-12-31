package com.example.shopbaefood.util;


import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;


import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.shopbaefood.R;
import com.example.shopbaefood.model.dto.AccountToken;
import com.example.shopbaefood.model.intercepter.RetrofitClient;
import com.example.shopbaefood.model.intercepter.RetryInterceptor;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.gson.Gson;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import cn.pedant.SweetAlert.SweetAlertDialog;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class UtilApp {
    private static final int MAX_RETRY_COUNT = 3;
    private static final long RETRY_DELAY = 3000; // Thời gian chờ trước khi thử lại (3 giây trong ví dụ này)
    public static final String REALURL = "http://192.168.52.218:8080/ShopbaeFoodApi/";
    public static final String URLMOCK = "https://651e990a44a3a8aa4768a52a.mockapi.io/";

    private static OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS) // Thời gian chờ kết nối
            .readTimeout(30, TimeUnit.SECONDS) // Thời gian chờ để đọc dữ liệu
            .writeTimeout(30, TimeUnit.SECONDS) // Thời gian chờ để ghi dữ liệu
            .addInterceptor(new RetryInterceptor(MAX_RETRY_COUNT, RETRY_DELAY))
            .build();

    public static Retrofit retrofitCF() {
        return new Retrofit.Builder().baseUrl(REALURL).client(client).addConverterFactory(GsonConverterFactory.create()).build();
    }

    public static Retrofit retrofitCFMock() {
        return new Retrofit.Builder().baseUrl(URLMOCK).client(client).addConverterFactory(GsonConverterFactory.create()).build();
    }

    public static Retrofit retrofitAuth(Context context) {
        return RetrofitClient.getClient(REALURL, getAuthToken(context));
    }

    private static String getAuthToken(Context context) {
        Gson gson = new Gson();
        SharedPreferences info = context.getSharedPreferences("info", Context.MODE_PRIVATE);
        String json = info.getString("info", "");

        if (!json.isEmpty()) {
            AccountToken accountToken = gson.fromJson(json, AccountToken.class);
            Log.d("token", accountToken.getToken());
            return accountToken.getToken();
        }
        return null;
    }

    public static void getImagePicasso(String imageUrl, ImageView imageView) {
        Picasso.get().load(imageUrl).into(imageView);
    }

    public static void setImagePicasso(String imageUrl, ImageView imageView, int onLoad, int err) {
        // Tải hình ảnh từ URL và đặt vào ImageView
        Picasso.get()
                .load(imageUrl) // merchant.getImageUrl() là URL hình ảnh
                .placeholder(onLoad) // Ảnh mặc định trong thời gian tải
                .error(err) // Ảnh khi tải lỗi
                .into(imageView);
    }

    public static boolean isTime(String opentime, String closetime) {
        try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                DateTimeFormatter fomat = DateTimeFormatter.ofPattern("HH:mm");

                LocalTime currentTime = LocalTime.now();
                if (opentime != null && closetime != null) {
                    LocalTime startTimeDate = LocalTime.parse(opentime, fomat);
                    LocalTime endTimeDate = LocalTime.parse(closetime, fomat);

                    return currentTime.isAfter(startTimeDate) && currentTime.isBefore(endTimeDate);
                } else {
                    return false;
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return false;
    }


    public static void uploadImageToFirebaseStorage(Uri imageUri, OnImageUploadListener listener) {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss", Locale.CHINESE);
        Date now = new Date();
        String filename = formatter.format(now);

        StorageReference storageRef = storage.getReference().child("images/" + filename);

        storageRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> {
                    storageRef.getDownloadUrl()
                            .addOnSuccessListener(downloadUri -> {
                                String imageUrl = downloadUri.toString();
                                listener.onSuccess(imageUrl);
                            })
                            .addOnFailureListener(e -> {
                                listener.onFailure("Failed to get image URL.");
                            });
                })
                .addOnFailureListener(e -> {
                    listener.onFailure("Failed to upload image.");
                });
    }

    public interface OnImageUploadListener {
        void onSuccess(String imageUrl);

        void onFailure(String error);
    }

    public static void closeKeyBoard(View v) {
        try {
            InputMethodManager imm = (InputMethodManager) v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
        } catch (Exception e) {
        }
    }

    public static void setImageFromBitmapByteBlob(byte[] byteArray, ImageView imageView) {
// Chuyển mảng byte thành bitmap
        Bitmap bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
// Đặt bitmap vào ImageView
        imageView.setImageBitmap(bitmap);
    }

        public static void loadImageAndConvertToByteArray(Context context, String imageUrl, final OnImageByteArrayListener listener) {
            Picasso.get()
                    .load(imageUrl)
                    .into(new Target() {
                        @Override
                        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                            ByteArrayOutputStream stream = new ByteArrayOutputStream();
                            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                            byte[] byteArray = stream.toByteArray();
                            listener.onByteArrayGenerated(byteArray);
                        }

                        @Override
                        public void onBitmapFailed(Exception e, Drawable errorDrawable) {
                            // Xử lý lỗi khi tải ảnh
                            listener.onByteArrayGenerated(null);
                            Notification.sweetAlert(context, SweetAlertDialog.ERROR_TYPE,"Err","");
                            Log.d("exception",e.getMessage());
                        }

                        @Override
                        public void onPrepareLoad(Drawable placeHolderDrawable) {
                            // Được gọi khi Picasso bắt đầu tải ảnh
                        }
                    });
        }

        public interface OnImageByteArrayListener {
            void onByteArrayGenerated(byte[] byteArray);
        }
    public static Dialog showProgressBarDialog(Context context) {
        Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.progressbar_layout);

        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
        layoutParams.copyFrom(dialog.getWindow().getAttributes());
        layoutParams.width = 500; // Chiều rộng tùy chỉnh
        layoutParams.height = 500; // Chiều cao tùy chỉnh
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().setAttributes(layoutParams);
        return dialog;
    }
}
