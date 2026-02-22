package com.yisu.app.activity;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.yisu.app.R;
import com.yisu.app.adapter.HotelImageAdapter;
import com.yisu.app.adapter.RoomTypeAdapter;
import com.yisu.app.model.Hotel;
import com.yisu.app.model.Result;
import com.yisu.app.model.RoomType;
import com.yisu.app.network.RetrofitClient;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HotelDetailActivity extends AppCompatActivity {

    private int hotelId;
    private TextView tvName, tvStar, tvAddress, tvDesc, tvDetailCheckIn, tvDetailCheckOut, tvDetailNights;
    private RatingBar ratingBar;
    private TextView tvRatingScore, tvReviewCount, tvFavoriteCount;
    private TextView tvMinPrice, tvMaxPrice, tvRoomTypeCount;
    private ViewPager2 viewPagerImages;
    private TabLayout tabLayoutImages;
    private RecyclerView rvRooms;
    
    private HotelImageAdapter imageAdapter;
    private RoomTypeAdapter roomAdapter;
    private List<String> imageUrls = new ArrayList<>();
    private List<RoomType> roomTypes = new ArrayList<>();
    
    private Calendar checkInCalendar, checkOutCalendar;
    private SimpleDateFormat dateFormat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hotel_detail);
        
        // 设置状态栏透明，让图片可以延伸到状态栏
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            getWindow().getDecorView().setSystemUiVisibility(
                android.view.View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | android.view.View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
            getWindow().setStatusBarColor(android.graphics.Color.TRANSPARENT);
        }
        
        hotelId = getIntent().getIntExtra("hotelId", 0);
        long checkInMillis = getIntent().getLongExtra("checkInDate", 0);
        long checkOutMillis = getIntent().getLongExtra("checkOutDate", 0);
        
        // Initialize calendars
        dateFormat = new SimpleDateFormat("MM月dd日", Locale.getDefault());
        checkInCalendar = Calendar.getInstance();
        checkOutCalendar = Calendar.getInstance();
        checkOutCalendar.add(Calendar.DAY_OF_MONTH, 1);
        
        if (checkInMillis > 0) {
            checkInCalendar.setTimeInMillis(checkInMillis);
        }
        if (checkOutMillis > 0) {
            checkOutCalendar.setTimeInMillis(checkOutMillis);
        }
        
        initViews();
        setupImagePager();
        setupRoomList();
        updateDateDisplay();
        setupRatingAndCounts(); // 设置评分、点评数、收藏数（随机数据）
        
        loadDetail();
    }

    private void initViews() {
        tvName = findViewById(R.id.tvDetailName);
        tvStar = findViewById(R.id.tvDetailStar);
        tvAddress = findViewById(R.id.tvDetailAddress);
        tvDesc = findViewById(R.id.tvDetailDesc);
        viewPagerImages = findViewById(R.id.viewPagerImages);
        tabLayoutImages = findViewById(R.id.tabLayoutImages);
        rvRooms = findViewById(R.id.rvRooms);
        tvDetailCheckIn = findViewById(R.id.tvDetailCheckIn);
        tvDetailCheckOut = findViewById(R.id.tvDetailCheckOut);
        tvDetailNights = findViewById(R.id.tvDetailNights);
        
        // 评分相关View
        ratingBar = findViewById(R.id.ratingBar);
        tvRatingScore = findViewById(R.id.tvRatingScore);
        tvReviewCount = findViewById(R.id.tvReviewCount);
        tvFavoriteCount = findViewById(R.id.tvFavoriteCount);
        
        // 价格概览View
        tvMinPrice = findViewById(R.id.tvMinPrice);
        tvMaxPrice = findViewById(R.id.tvMaxPrice);
        tvRoomTypeCount = findViewById(R.id.tvRoomTypeCount);
        
        // 设置日期选择点击事件（TextView和父LinearLayout都可以点击）
        android.view.View.OnClickListener checkInListener = v -> {
            Log.d("HotelDetail", "Check-in clicked");
            showDatePicker(true);
        };
        android.view.View.OnClickListener checkOutListener = v -> {
            Log.d("HotelDetail", "Check-out clicked");
            showDatePicker(false);
        };
        
        tvDetailCheckIn.setOnClickListener(checkInListener);
        tvDetailCheckOut.setOnClickListener(checkOutListener);
        
        // 也设置父LinearLayout的点击事件（如果存在）
        View checkInParent = (View) tvDetailCheckIn.getParent();
        if (checkInParent != null && checkInParent instanceof android.widget.LinearLayout) {
            checkInParent.setOnClickListener(checkInListener);
        }
        View checkOutParent = (View) tvDetailCheckOut.getParent();
        if (checkOutParent != null && checkOutParent instanceof android.widget.LinearLayout) {
            checkOutParent.setOnClickListener(checkOutListener);
        }
    }

    private void setupImagePager() {
        imageAdapter = new HotelImageAdapter(imageUrls);
        viewPagerImages.setAdapter(imageAdapter);
        
        if (imageUrls.size() > 1) {
            new TabLayoutMediator(tabLayoutImages, viewPagerImages, (tab, position) -> {
            }).attach();
        } else {
            tabLayoutImages.setVisibility(android.view.View.GONE);
        }
    }

    private void setupRoomList() {
        rvRooms.setLayoutManager(new LinearLayoutManager(this));
        roomAdapter = new RoomTypeAdapter(roomTypes);
        rvRooms.setAdapter(roomAdapter);
    }

    private void showDatePicker(boolean isCheckIn) {
        // 确保在主线程中执行
        if (Looper.myLooper() != Looper.getMainLooper()) {
            new Handler(Looper.getMainLooper()).post(() -> showDatePicker(isCheckIn));
            return;
        }
        
        Calendar calendar = isCheckIn ? checkInCalendar : checkOutCalendar;
        
        // 使用Material主题的DatePickerDialog
        DatePickerDialog datePickerDialog = new DatePickerDialog(
            this,
            android.R.style.Theme_Material_Light_Dialog,
            (view, year, month, dayOfMonth) -> {
                calendar.set(year, month, dayOfMonth);
                if (isCheckIn) {
                    if (checkOutCalendar.before(checkInCalendar) || 
                        checkOutCalendar.equals(checkInCalendar)) {
                        checkOutCalendar.setTimeInMillis(checkInCalendar.getTimeInMillis());
                        checkOutCalendar.add(Calendar.DAY_OF_MONTH, 1);
                    }
                } else {
                    if (checkOutCalendar.before(checkInCalendar) || 
                        checkOutCalendar.equals(checkInCalendar)) {
                        checkOutCalendar.setTimeInMillis(checkInCalendar.getTimeInMillis());
                        checkOutCalendar.add(Calendar.DAY_OF_MONTH, 1);
                    }
                }
                updateDateDisplay();
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        );
        
        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
        
        // 确保对话框可以显示
        try {
            // 检查Activity是否还在运行
            boolean canShow = !isFinishing();
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR1) {
                canShow = canShow && !isDestroyed();
            }
            
            if (canShow) {
                datePickerDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
                datePickerDialog.show();
                Log.d("HotelDetail", "DatePickerDialog shown for " + (isCheckIn ? "check-in" : "check-out"));
            } else {
                Log.w("HotelDetail", "Activity is finishing or destroyed, cannot show dialog");
            }
        } catch (Exception e) {
            Log.e("HotelDetail", "Error showing DatePickerDialog", e);
            // 如果Material主题失败，尝试使用默认主题
            try {
                DatePickerDialog fallbackDialog = new DatePickerDialog(
                    this,
                    (view, year, month, dayOfMonth) -> {
                        calendar.set(year, month, dayOfMonth);
                        if (isCheckIn) {
                            if (checkOutCalendar.before(checkInCalendar) || 
                                checkOutCalendar.equals(checkInCalendar)) {
                                checkOutCalendar.setTimeInMillis(checkInCalendar.getTimeInMillis());
                                checkOutCalendar.add(Calendar.DAY_OF_MONTH, 1);
                            }
                        } else {
                            if (checkOutCalendar.before(checkInCalendar) || 
                                checkOutCalendar.equals(checkInCalendar)) {
                                checkOutCalendar.setTimeInMillis(checkInCalendar.getTimeInMillis());
                                checkOutCalendar.add(Calendar.DAY_OF_MONTH, 1);
                            }
                        }
                        updateDateDisplay();
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
                );
                fallbackDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
                boolean canShowFallback = !isFinishing();
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR1) {
                    canShowFallback = canShowFallback && !isDestroyed();
                }
                if (canShowFallback) {
                    fallbackDialog.show();
                }
            } catch (Exception e2) {
                Log.e("HotelDetail", "Error showing fallback DatePickerDialog", e2);
                Toast.makeText(this, "无法打开日期选择器", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void updateDateDisplay() {
        tvDetailCheckIn.setText(dateFormat.format(checkInCalendar.getTime()));
        tvDetailCheckOut.setText(dateFormat.format(checkOutCalendar.getTime()));
        
        long diff = checkOutCalendar.getTimeInMillis() - checkInCalendar.getTimeInMillis();
        long nights = diff / (24 * 60 * 60 * 1000);
        tvDetailNights.setText(nights + "晚");
    }

    /**
     * 更新价格概览（最低价、最高价、房型数量）
     */
    private void updatePriceOverview() {
        if (roomTypes == null || roomTypes.isEmpty()) {
            tvMinPrice.setText("--");
            tvMaxPrice.setText("--");
            tvRoomTypeCount.setText("0种");
            return;
        }
        
        // 计算最低价和最高价
        double minPrice = Double.MAX_VALUE;
        double maxPrice = 0;
        
        for (RoomType room : roomTypes) {
            if (room.price < minPrice) {
                minPrice = room.price;
            }
            if (room.price > maxPrice) {
                maxPrice = room.price;
            }
        }
        
        // 如果所有价格都是0或无效，显示默认值
        if (minPrice == Double.MAX_VALUE || minPrice == 0) {
            tvMinPrice.setText("--");
            tvMaxPrice.setText("--");
        } else {
            tvMinPrice.setText(String.valueOf((int)minPrice));
            tvMaxPrice.setText(String.valueOf((int)maxPrice));
        }
        
        // 更新房型数量
        tvRoomTypeCount.setText(roomTypes.size() + "种");
    }

    /**
     * 设置评分、点评数、收藏数（使用随机数据）
     */
    private void setupRatingAndCounts() {
        Random random = new Random();
        
        // 生成1.0-5.0之间的随机评分（保留一位小数）
        float rating = 1.0f + random.nextFloat() * 4.0f;
        rating = Math.round(rating * 10) / 10.0f; // 保留一位小数
        
        // 生成10-9999之间的随机点评数
        int reviewCount = 10 + random.nextInt(9990);
        
        // 生成5-5000之间的随机收藏数
        int favoriteCount = 5 + random.nextInt(4996);
        
        // 设置评分
        ratingBar.setRating(rating);
        tvRatingScore.setText(String.format(Locale.getDefault(), "%.1f", rating));
        
        // 设置点评数
        tvReviewCount.setText(String.valueOf(reviewCount));
        
        // 设置收藏数
        tvFavoriteCount.setText(String.valueOf(favoriteCount));
    }

    private void loadDetail() {
        RetrofitClient.getApiService().getHotelDetail(hotelId).enqueue(new Callback<Result<Map<String, Object>>>() {
            @Override
            public void onResponse(Call<Result<Map<String, Object>>> call, Response<Result<Map<String, Object>>> response) {
                if(response.isSuccessful() && response.body() != null) {
                    Result<Map<String, Object>> result = response.body();
                    if("200".equals(result.code) && result.data != null) {
                        Map<String, Object> data = result.data;
                        
                        // Parse hotel
                        if(data.containsKey("hotel")) {
                            Object hotelObj = data.get("hotel");
                            if(hotelObj instanceof Map) {
                                Map hotel = (Map) hotelObj;
                                tvName.setText((String)hotel.get("name"));
                                tvStar.setText((String)hotel.get("starRating"));
                                tvAddress.setText((String)hotel.get("address"));
                                tvDesc.setText((String)hotel.get("description"));
                                
                                // Parse images
                                Object imagesObj = hotel.get("images");
                                if(imagesObj != null) {
                                    parseImages(imagesObj.toString());
                                }
                                
                                // Toolbar已隐藏，不再设置标题
                            }
                        }
                        
                        // Parse rooms
                        if(data.containsKey("rooms")) {
                            Object roomsObj = data.get("rooms");
                            if(roomsObj instanceof List) {
                                Gson gson = new Gson();
                                Type listType = new TypeToken<List<RoomType>>(){}.getType();
                                roomTypes = gson.fromJson(gson.toJson(roomsObj), listType);
                                roomAdapter = new RoomTypeAdapter(roomTypes);
                                rvRooms.setAdapter(roomAdapter);
                                
                                // 更新价格统计
                                updatePriceOverview();
                            }
                        }
                    }
                } else {
                    Toast.makeText(HotelDetailActivity.this, "加载失败", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Result<Map<String, Object>>> call, Throwable t) {
                Toast.makeText(HotelDetailActivity.this, "网络错误: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void parseImages(String imagesStr) {
        imageUrls.clear();
        try {
            if(imagesStr.startsWith("[")) {
                // JSON array
                Gson gson = new Gson();
                Type listType = new TypeToken<List<String>>(){}.getType();
                imageUrls = gson.fromJson(imagesStr, listType);
            } else if(imagesStr.contains(",")) {
                // Comma separated
                String[] urls = imagesStr.split(",");
                for(String url : urls) {
                    imageUrls.add(url.trim());
                }
            } else {
                // Single image
                imageUrls.add(imagesStr);
            }
        } catch (Exception e) {
            // Fallback: use cover image or default
            imageUrls.add("");
        }
        
        if(imageUrls.isEmpty()) {
            imageUrls.add("");
        }
        
        imageAdapter = new HotelImageAdapter(imageUrls);
        viewPagerImages.setAdapter(imageAdapter);
        
        if(imageUrls.size() > 1) {
            tabLayoutImages.setVisibility(android.view.View.VISIBLE);
            new TabLayoutMediator(tabLayoutImages, viewPagerImages, (tab, position) -> {
            }).attach();
        } else {
            tabLayoutImages.setVisibility(android.view.View.GONE);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
