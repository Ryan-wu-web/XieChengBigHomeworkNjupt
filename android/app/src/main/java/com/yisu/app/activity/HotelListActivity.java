package com.yisu.app.activity;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.yisu.app.R;
import com.yisu.app.adapter.HotelAdapter;
import com.yisu.app.model.Hotel;
import com.yisu.app.model.Page;
import com.yisu.app.model.Result;
import com.yisu.app.network.RetrofitClient;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HotelListActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private HotelAdapter adapter;
    private List<Hotel> hotelList = new ArrayList<>();
    private EditText etListSearch;
    private Button btnListSearch;
    private TextView tvCity, tvListCheckIn, tvListCheckOut, tvNights;
    private ProgressBar progressBar;
    
    private String keyword;
    private String city = "北京";
    private Calendar checkInCalendar, checkOutCalendar;
    private SimpleDateFormat dateFormat;
    
    private int currentPage = 1;
    private int pageSize = 20;
    private boolean isLoading = false;
    private boolean hasMore = true;
    private String starRating, priceRange, tag;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hotel_list);

        // Get intent data
        keyword = getIntent().getStringExtra("keyword");
        String intentCity = getIntent().getStringExtra("city");
        if (intentCity != null) {
            city = intentCity;
        }
        long checkInMillis = getIntent().getLongExtra("checkInDate", 0);
        long checkOutMillis = getIntent().getLongExtra("checkOutDate", 0);
        starRating = getIntent().getStringExtra("starRating");
        priceRange = getIntent().getStringExtra("priceRange");
        tag = getIntent().getStringExtra("tag");
        
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
        setupRecyclerView();
        updateDateDisplay();
        
        loadData(1, true);
    }

    private void initViews() {
        etListSearch = findViewById(R.id.etListSearch);
        btnListSearch = findViewById(R.id.btnListSearch);
        tvCity = findViewById(R.id.tvCity);
        tvListCheckIn = findViewById(R.id.tvListCheckIn);
        tvListCheckOut = findViewById(R.id.tvListCheckOut);
        tvNights = findViewById(R.id.tvNights);
        progressBar = findViewById(R.id.progressBar);
        recyclerView = findViewById(R.id.recyclerView);
        
        if (keyword != null) {
            etListSearch.setText(keyword);
        }
        
        tvCity.setText(city);
        tvCity.setOnClickListener(v -> {
            Intent intent = new Intent(HotelListActivity.this, LocationSearchActivity.class);
            startActivityForResult(intent, 100);
        });
        
        tvListCheckIn.setOnClickListener(v -> showDatePicker(true));
        tvListCheckOut.setOnClickListener(v -> showDatePicker(false));
        
        btnListSearch.setOnClickListener(v -> {
            keyword = etListSearch.getText().toString();
            currentPage = 1;
            hasMore = true;
            hotelList.clear();
            adapter.notifyDataSetChanged();
            loadData(1, true);
        });
    }

    private void setupRecyclerView() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        adapter = new HotelAdapter(hotelList);
        recyclerView.setAdapter(adapter);
        
        adapter.setOnItemClickListener(hotel -> {
            Intent intent = new Intent(HotelListActivity.this, HotelDetailActivity.class);
            intent.putExtra("hotelId", hotel.id);
            intent.putExtra("checkInDate", checkInCalendar.getTimeInMillis());
            intent.putExtra("checkOutDate", checkOutCalendar.getTimeInMillis());
            startActivity(intent);
        });
        
        // Scroll listener for pagination
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                
                int visibleItemCount = layoutManager.getChildCount();
                int totalItemCount = layoutManager.getItemCount();
                int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();
                
                if (!isLoading && hasMore) {
                    if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount
                            && firstVisibleItemPosition >= 0) {
                        loadData(currentPage + 1, false);
                    }
                }
            }
        });
    }

    private void showDatePicker(boolean isCheckIn) {
        Calendar calendar = isCheckIn ? checkInCalendar : checkOutCalendar;
        DatePickerDialog datePickerDialog = new DatePickerDialog(
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
                // Reload data with new dates
                currentPage = 1;
                hasMore = true;
                hotelList.clear();
                adapter.notifyDataSetChanged();
                loadData(1, true);
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
        datePickerDialog.show();
    }

    private void updateDateDisplay() {
        tvListCheckIn.setText(dateFormat.format(checkInCalendar.getTime()));
        tvListCheckOut.setText(dateFormat.format(checkOutCalendar.getTime()));
        
        long diff = checkOutCalendar.getTimeInMillis() - checkInCalendar.getTimeInMillis();
        long nights = diff / (24 * 60 * 60 * 1000);
        tvNights.setText(nights + "晚");
    }

    private void loadData(int page, boolean showProgress) {
        if (isLoading) return;
        
        isLoading = true;
        if (showProgress) {
            progressBar.setVisibility(View.VISIBLE);
        }
        
        currentPage = page;
        
        RetrofitClient.getApiService().getHotelList(page, pageSize, keyword, city).enqueue(new Callback<Result<Page<Hotel>>>() {
            @Override
            public void onResponse(Call<Result<Page<Hotel>>> call, Response<Result<Page<Hotel>>> response) {
                isLoading = false;
                progressBar.setVisibility(View.GONE);
                
                if (response.isSuccessful() && response.body() != null && response.body().data != null) {
                    Page<Hotel> pageData = response.body().data;
                    List<Hotel> newHotels = pageData.records;
                    
                    // Filter by star rating, price, tag if needed
                    if (starRating != null || priceRange != null || tag != null) {
                        newHotels = filterHotels(newHotels);
                    }
                    
                    if (page == 1) {
                        hotelList.clear();
                    }
                    hotelList.addAll(newHotels);
                    adapter.notifyDataSetChanged();
                    
                    hasMore = hotelList.size() < pageData.total;
                } else {
                    Toast.makeText(HotelListActivity.this, "加载失败", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Result<Page<Hotel>>> call, Throwable t) {
                isLoading = false;
                progressBar.setVisibility(View.GONE);
                Toast.makeText(HotelListActivity.this, "网络错误: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private List<Hotel> filterHotels(List<Hotel> hotels) {
        List<Hotel> filtered = new ArrayList<>();
        for (Hotel hotel : hotels) {
            boolean match = true;
            
            if (starRating != null && hotel.starRating != null && !hotel.starRating.contains(starRating)) {
                match = false;
            }
            
            if (priceRange != null) {
                if ("经济型".equals(priceRange) && hotel.minPrice > 200) {
                    match = false;
                } else if ("舒适型".equals(priceRange) && (hotel.minPrice < 200 || hotel.minPrice > 500)) {
                    match = false;
                }
            }
            
            if (tag != null && hotel.tags != null && !hotel.tags.contains(tag)) {
                match = false;
            }
            
            if (match) {
                filtered.add(hotel);
            }
        }
        return filtered;
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100 && resultCode == RESULT_OK && data != null) {
            String newCity = data.getStringExtra("city");
            if (newCity != null) {
                city = newCity;
                tvCity.setText(city);
                // 重新加载数据
                currentPage = 1;
                hasMore = true;
                hotelList.clear();
                adapter.notifyDataSetChanged();
                loadData(1, true);
            }
        }
    }
}
