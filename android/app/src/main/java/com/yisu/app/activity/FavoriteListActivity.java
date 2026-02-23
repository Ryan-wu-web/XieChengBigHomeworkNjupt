package com.yisu.app.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.yisu.app.R;
import com.yisu.app.model.Result;
import com.yisu.app.network.RetrofitClient;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FavoriteListActivity extends AppCompatActivity {

    private RecyclerView rvFavorites;
    private FavoriteAdapter adapter;
    private List<Map<String, Object>> favoriteList = new ArrayList<>();
    private LinearLayout llEmpty;
    private int userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.activity_favorite_list);

            // 设置 Toolbar
            Toolbar toolbar = findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setTitle("我的收藏");
            }

            rvFavorites = findViewById(R.id.rvFavorites);
            llEmpty = findViewById(R.id.llEmpty);

            // 获取用户ID
            SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
            userId = prefs.getInt("userId", 0);

            // 设置 RecyclerView
            rvFavorites.setLayoutManager(new LinearLayoutManager(this));
            adapter = new FavoriteAdapter(favoriteList);
            rvFavorites.setAdapter(adapter);

            loadFavorites();
        } catch (Exception e) {
            Toast.makeText(this, "初始化失败: " + e.getMessage(), Toast.LENGTH_LONG).show();
            finish();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    private void loadFavorites() {
        if (userId == 0) {
            llEmpty.setVisibility(View.VISIBLE);
            rvFavorites.setVisibility(View.GONE);
            ((TextView) llEmpty.getChildAt(0)).setText("请先登录");
            return;
        }

        RetrofitClient.getApiService().getFavorites(userId).enqueue(new Callback<Result<List<Map<String, Object>>>>() {
            @Override
            public void onResponse(Call<Result<List<Map<String, Object>>>> call, Response<Result<List<Map<String, Object>>>> response) {
                try {
                    if (response.isSuccessful() && response.body() != null) {
                        Result<List<Map<String, Object>>> result = response.body();
                        if ("200".equals(result.code) && result.data != null) {
                            favoriteList.clear();
                            favoriteList.addAll(result.data);
                            adapter.notifyDataSetChanged();
                            
                            if (favoriteList.isEmpty()) {
                                llEmpty.setVisibility(View.VISIBLE);
                                rvFavorites.setVisibility(View.GONE);
                                ((TextView) llEmpty.getChildAt(0)).setText("暂无收藏");
                            } else {
                                llEmpty.setVisibility(View.GONE);
                                rvFavorites.setVisibility(View.VISIBLE);
                            }
                        }
                    }
                } catch (Exception e) {
                    Toast.makeText(FavoriteListActivity.this, "加载失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Result<List<Map<String, Object>>>> call, Throwable t) {
                Toast.makeText(FavoriteListActivity.this, "网络错误: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private class FavoriteAdapter extends RecyclerView.Adapter<FavoriteAdapter.ViewHolder> {
        private List<Map<String, Object>> list;

        FavoriteAdapter(List<Map<String, Object>> list) {
            this.list = list;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_hotel, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            try {
                Map<String, Object> hotel = list.get(position);
                
                Object nameObj = hotel.get("name");
                holder.tvName.setText(nameObj != null ? (String) nameObj : "");
                
                Object addressObj = hotel.get("address");
                if (addressObj == null) {
                    addressObj = hotel.get("description");
                }
                holder.tvAddress.setText(addressObj != null ? (String) addressObj : "");
                
                Object minPriceObj = hotel.get("minPrice");
                if (minPriceObj == null) {
                    minPriceObj = hotel.get("min_price");
                }
                if (minPriceObj != null) {
                    holder.tvPrice.setText("¥" + minPriceObj + " 起");
                } else {
                    holder.tvPrice.setText("--");
                }
                
                Object imageUrlObj = hotel.get("coverImage");
                if (imageUrlObj == null) {
                    imageUrlObj = hotel.get("cover_image");
                }
                if (imageUrlObj != null) {
                    String imageUrl = (String) imageUrlObj;
                    if (!imageUrl.startsWith("http")) {
                        imageUrl = "http://192.168.3.10:9090" + imageUrl;
                    }
                    try {
                        com.bumptech.glide.Glide.with(holder.itemView.getContext())
                                .load(imageUrl)
                                .placeholder(R.drawable.ic_launcher_background)
                                .into(holder.ivCover);
                    } catch (Exception e) {
                        // 忽略图片加载错误
                    }
                }

                holder.itemView.setOnClickListener(v -> {
                    try {
                        Object idObj = hotel.get("id");
                        if (idObj != null) {
                            int hotelId = ((Number) idObj).intValue();
                            Intent intent = new Intent(FavoriteListActivity.this, HotelDetailActivity.class);
                            intent.putExtra("hotelId", hotelId);
                            startActivity(intent);
                        }
                    } catch (Exception e) {
                        Toast.makeText(FavoriteListActivity.this, "跳转失败", Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (Exception e) {
                // 忽略绑定失败
            }
        }

        @Override
        public int getItemCount() {
            return list.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvName, tvAddress, tvPrice;
            ImageView ivCover;

            ViewHolder(View itemView) {
                super(itemView);
                tvName = itemView.findViewById(R.id.tvHotelName);
                tvAddress = itemView.findViewById(R.id.tvDescription);
                tvPrice = itemView.findViewById(R.id.tvPrice);
                ivCover = itemView.findViewById(R.id.ivHotelImage);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadFavorites();
    }
}
