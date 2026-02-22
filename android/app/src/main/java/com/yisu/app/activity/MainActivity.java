package com.yisu.app.activity;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.yisu.app.R;
import com.yisu.app.adapter.BannerAdapter;
import com.yisu.app.model.Banner;
import com.yisu.app.model.Result;
import com.yisu.app.network.ApiService;
import com.yisu.app.network.RetrofitClient;
import com.yisu.app.model.Hotel;
import com.yisu.app.model.Page;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private EditText etSearchKeyword;
    private Button btnSearch, btnLogout;
    private ViewPager2 viewPagerBanner;
    private TabLayout tabLayoutBanner;
    private BannerAdapter bannerAdapter;
    private List<Banner> bannerList = new ArrayList<>();
    private ApiService apiService;
    
    private TextView tvCheckInDate, tvCheckOutDate, tvLocation, tvUsername;
    private ChipGroup chipGroupTags;
    private Calendar checkInCalendar, checkOutCalendar;
    private SimpleDateFormat dateFormat;
    private String selectedStarRating = null;
    private String selectedPriceRange = null;
    private String selectedTag = null;
    private String selectedCity = "北京"; // 默认城市

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        // 隐藏ActionBar标题栏
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        // Check login status
        SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        String token = prefs.getString("token", null);
        if (token == null) {
            // Not logged in, go to login
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        apiService = RetrofitClient.getApiService();

        etSearchKeyword = findViewById(R.id.etSearchKeyword);
        btnSearch = findViewById(R.id.btnSearch);
        btnLogout = findViewById(R.id.btnLogout);
        viewPagerBanner = findViewById(R.id.viewPagerBanner);
        tabLayoutBanner = findViewById(R.id.tabLayoutBanner);
        tvCheckInDate = findViewById(R.id.tvCheckInDate);
        tvCheckOutDate = findViewById(R.id.tvCheckOutDate);
        tvLocation = findViewById(R.id.tvLocation);
        tvUsername = findViewById(R.id.tvUsername);
        chipGroupTags = findViewById(R.id.chipGroupTags);
        
        // 显示用户名
        String username = prefs.getString("username", "用户");
        tvUsername.setText(username);
        
        // 设置退出登录按钮点击事件
        btnLogout.setOnClickListener(v -> logout());
        
        // 设置地点选择点击事件
        tvLocation.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, LocationSearchActivity.class);
            startActivityForResult(intent, 100);
        });

        // Initialize date format and calendars
        dateFormat = new SimpleDateFormat("MM月dd日", Locale.getDefault());
        checkInCalendar = Calendar.getInstance();
        checkOutCalendar = Calendar.getInstance();
        checkOutCalendar.add(Calendar.DAY_OF_MONTH, 1);
        
        // Set initial dates
        updateDateDisplay();

        // Setup banner
        setupBanner();

        // Date picker listeners
        tvCheckInDate.setOnClickListener(v -> showDatePicker(true));
        tvCheckOutDate.setOnClickListener(v -> showDatePicker(false));

        // Chip group listener
        chipGroupTags.setOnCheckedStateChangeListener((group, checkedIds) -> {
            selectedStarRating = null;
            selectedPriceRange = null;
            selectedTag = null;
            
            for (int id : checkedIds) {
                Chip chip = findViewById(id);
                if (chip != null) {
                    String text = chip.getText().toString();
                    if (text.contains("星")) {
                        selectedStarRating = text;
                    } else if (text.equals("经济型") || text.equals("舒适型") || text.equals("豪华型")) {
                        selectedPriceRange = text;
                    } else {
                        selectedTag = text;
                    }
                }
            }
        });

        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String keyword = etSearchKeyword.getText().toString();
                Intent intent = new Intent(MainActivity.this, HotelListActivity.class);
                intent.putExtra("keyword", keyword);
                intent.putExtra("city", selectedCity);
                intent.putExtra("checkInDate", checkInCalendar.getTimeInMillis());
                intent.putExtra("checkOutDate", checkOutCalendar.getTimeInMillis());
                intent.putExtra("starRating", selectedStarRating);
                intent.putExtra("priceRange", selectedPriceRange);
                intent.putExtra("tag", selectedTag);
                startActivity(intent);
            }
        });

        // Load banners
        loadBanners();
        
        // Load tags from backend
        loadTagsFromBackend();
    }

    private void showDatePicker(boolean isCheckIn) {
        Calendar calendar = isCheckIn ? checkInCalendar : checkOutCalendar;
        DatePickerDialog datePickerDialog = new DatePickerDialog(
            this,
            (view, year, month, dayOfMonth) -> {
                calendar.set(year, month, dayOfMonth);
                if (isCheckIn) {
                    // Ensure check-out is after check-in
                    if (checkOutCalendar.before(checkInCalendar) || 
                        checkOutCalendar.equals(checkInCalendar)) {
                        checkOutCalendar.setTimeInMillis(checkInCalendar.getTimeInMillis());
                        checkOutCalendar.add(Calendar.DAY_OF_MONTH, 1);
                    }
                } else {
                    // Ensure check-out is after check-in
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
        // Set minimum date to today
        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
        datePickerDialog.show();
    }

    private void updateDateDisplay() {
        tvCheckInDate.setText(dateFormat.format(checkInCalendar.getTime()));
        tvCheckOutDate.setText(dateFormat.format(checkOutCalendar.getTime()));
        
        // Calculate nights
        long diff = checkOutCalendar.getTimeInMillis() - checkInCalendar.getTimeInMillis();
        long nights = diff / (24 * 60 * 60 * 1000);
        // Update nights display if needed
    }

    private void setupBanner() {
        // Make sure ViewPager2 is visible
        viewPagerBanner.setVisibility(View.VISIBLE);
        
        bannerAdapter = new BannerAdapter(bannerList, new BannerAdapter.OnBannerClickListener() {
            @Override
            public void onBannerClick(Banner banner) {
                // Jump to hotel detail
                Intent intent = new Intent(MainActivity.this, HotelDetailActivity.class);
                intent.putExtra("hotelId", banner.hotelId);
                startActivity(intent);
            }
        });
        viewPagerBanner.setAdapter(bannerAdapter);

        // Auto scroll
        viewPagerBanner.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
            }
        });

        // Connect TabLayout with ViewPager2
        if (bannerList.size() > 1) {
            new TabLayoutMediator(tabLayoutBanner, viewPagerBanner, (tab, position) -> {
                // Tab indicator dots
            }).attach();
        }
    }

    private void loadBanners() {
        Call<Result<List<Banner>>> call = apiService.getBannerList();
        call.enqueue(new Callback<Result<List<Banner>>>() {
            @Override
            public void onResponse(Call<Result<List<Banner>>> call, Response<Result<List<Banner>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Result<List<Banner>> result = response.body();
                    if ("200".equals(result.code) && result.data != null && !result.data.isEmpty()) {
                        bannerList.clear();
                        bannerList.addAll(result.data);
                        bannerAdapter.notifyDataSetChanged();
                        // Update tab indicators
                        if (bannerList.size() > 1) {
                            tabLayoutBanner.setVisibility(View.VISIBLE);
                        } else {
                            tabLayoutBanner.setVisibility(View.GONE);
                        }
                        // Make sure ViewPager2 is visible
                        viewPagerBanner.setVisibility(View.VISIBLE);
                    } else {
                        // No banners, show default
                        showDefaultBanner();
                    }
                } else {
                    Toast.makeText(MainActivity.this, "加载Banner失败: " + (response.code() != 0 ? response.code() : "网络错误"), Toast.LENGTH_SHORT).show();
                    showDefaultBanner();
                }
            }

            @Override
            public void onFailure(Call<Result<List<Banner>>> call, Throwable t) {
                Toast.makeText(MainActivity.this, "Banner加载失败: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                t.printStackTrace();
                showDefaultBanner();
            }
        });
    }

    private void showDefaultBanner() {
        // Show default banner if no banners available
        bannerList.clear();
        Banner defaultBanner = new Banner();
        defaultBanner.imageUrl = "";
        defaultBanner.hotelId = 0;
        bannerList.add(defaultBanner);
        bannerAdapter.notifyDataSetChanged();
        tabLayoutBanner.setVisibility(View.GONE);
    }

    /**
     * 从后台加载所有酒店的标签，动态生成快捷标签
     */
    private void loadTagsFromBackend() {
        // 获取酒店列表以提取所有标签（获取足够多的数据）
        Call<Result<Page<Hotel>>> call = apiService.getHotelList(1, 100, null, null);
        call.enqueue(new Callback<Result<Page<Hotel>>>() {
            @Override
            public void onResponse(Call<Result<Page<Hotel>>> call, Response<Result<Page<Hotel>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Result<Page<Hotel>> result = response.body();
                    if ("200".equals(result.code) && result.data != null && result.data.records != null) {
                        // 提取所有唯一的标签
                        Set<String> uniqueTags = new HashSet<>();
                        for (Hotel hotel : result.data.records) {
                            if (hotel.tags != null && !hotel.tags.trim().isEmpty()) {
                                // 解析标签（支持逗号、中文逗号、空格分隔）
                                String[] tags = hotel.tags.split("[,，\\s]+");
                                for (String tag : tags) {
                                    String trimmedTag = tag.trim();
                                    if (!trimmedTag.isEmpty()) {
                                        uniqueTags.add(trimmedTag);
                                    }
                                }
                            }
                        }
                        
                        // 动态生成标签Chip
                        addDynamicTags(uniqueTags);
                    }
                }
            }

            @Override
            public void onFailure(Call<Result<Page<Hotel>>> call, Throwable t) {
                // 标签加载失败不影响主功能，静默处理
                android.util.Log.w("MainActivity", "加载标签失败: " + t.getMessage());
            }
        });
    }

    /**
     * 动态添加标签Chip到ChipGroup
     */
    private void addDynamicTags(Set<String> tags) {
        if (tags == null || tags.isEmpty()) {
            return;
        }
        
        // 移除已存在的动态标签（保留固定的星级和价格范围标签）
        List<Chip> chipsToRemove = new ArrayList<>();
        for (int i = 0; i < chipGroupTags.getChildCount(); i++) {
            View child = chipGroupTags.getChildAt(i);
            if (child instanceof Chip) {
                Chip chip = (Chip) child;
                String text = chip.getText().toString();
                // 只保留星级和价格范围标签
                if (!text.contains("星") && 
                    !text.equals("经济型") && 
                    !text.equals("舒适型") && 
                    !text.equals("豪华型")) {
                    chipsToRemove.add(chip);
                }
            }
        }
        for (Chip chip : chipsToRemove) {
            chipGroupTags.removeView(chip);
        }
        
        // 添加新的动态标签
        for (String tag : tags) {
            Chip chip = new Chip(this, null, com.google.android.material.R.style.Widget_MaterialComponents_Chip_Choice);
            chip.setText(tag);
            chip.setCheckable(true);
            chip.setClickable(true);
            chipGroupTags.addView(chip);
        }
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        android.util.Log.d("MainActivity", "onActivityResult - requestCode: " + requestCode + ", resultCode: " + resultCode);
        if (requestCode == 100 && resultCode == RESULT_OK) {
            if (data != null) {
                String city = data.getStringExtra("city");
                String location = data.getStringExtra("location");
                android.util.Log.d("MainActivity", "收到城市: " + city + ", 地点: " + location);
                if (city != null && !city.isEmpty()) {
                    selectedCity = city;
                    tvLocation.setText(city);
                    android.util.Log.d("MainActivity", "城市已更新为: " + selectedCity);
                } else {
                    android.util.Log.w("MainActivity", "城市为空");
                }
            } else {
                android.util.Log.w("MainActivity", "data 为 null");
            }
        } else {
            android.util.Log.w("MainActivity", "结果码不匹配或失败");
        }
    }

    /**
     * 退出登录
     */
    private void logout() {
        // 清除登录信息
        SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.clear();
        editor.apply();
        
        // 跳转到登录页面
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
        
        Toast.makeText(this, "已退出登录", Toast.LENGTH_SHORT).show();
    }
}
