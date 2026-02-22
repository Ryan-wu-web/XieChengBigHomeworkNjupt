package com.yisu.app.adapter;

import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.yisu.app.R;
import com.yisu.app.model.Hotel;
import java.util.List;
import java.util.Random;

public class HotelAdapter extends RecyclerView.Adapter<HotelAdapter.ViewHolder> {

    private List<Hotel> list;
    private OnItemClickListener listener;
    private Random random = new Random();

    public interface OnItemClickListener {
        void onItemClick(Hotel hotel);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public HotelAdapter(List<Hotel> list) {
        this.list = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_hotel, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Hotel hotel = list.get(position);
        
        // 酒店名称
        holder.tvName.setText(hotel.name);
        
        // 生成随机评分（1.0-5.0，保留一位小数）
        float rating = 1.0f + random.nextFloat() * 4.0f;
        rating = Math.round(rating * 10) / 10.0f;
        
        // 根据评分生成评价文字
        String ratingText = getRatingText(rating);
        holder.tvRating.setText(String.format("%.1f %s", rating, ratingText));
        
        // 点评数和收藏数（如果Hotel模型中有数据则使用，否则随机生成）
        int reviewCount = hotel.reviewCount > 0 ? hotel.reviewCount : (10 + random.nextInt(9990));
        int favoriteCount = hotel.favoriteCount > 0 ? hotel.favoriteCount : (5 + random.nextInt(4996));
        
        holder.tvReviewCount.setText(reviewCount + "点评");
        holder.tvFavoriteCount.setText(favoriteCount + "收藏");
        
        // 描述信息（使用address或description）
        String description = hotel.description != null && !hotel.description.isEmpty() 
            ? hotel.description 
            : (hotel.address != null ? hotel.address : "环境舒适，服务热情");
        
        // 如果描述太长，截取前50个字符
        if (description.length() > 50) {
            description = description.substring(0, 50) + "...";
        }
        holder.tvDescription.setText(description);
        
        // 设施标签
        setupFacilities(holder.llFacilities, hotel);
        
        // 价格显示
        int price = (int) hotel.minPrice;
        holder.tvPrice.setText(String.valueOf(price));
        
        // 如果有原价（比现价高），显示划掉的原价
        if (price > 0 && random.nextFloat() > 0.5f) {
            int originalPrice = price + random.nextInt(50) + 10;
            holder.tvOriginalPrice.setText("¥" + originalPrice);
            holder.tvOriginalPrice.setPaintFlags(holder.tvOriginalPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            holder.tvOriginalPrice.setVisibility(View.VISIBLE);
        } else {
            holder.tvOriginalPrice.setVisibility(View.GONE);
        }
        
        // 会员优惠（随机显示）
        if (random.nextFloat() > 0.6f) {
            int discount = 5 + random.nextInt(20);
            holder.tvMemberDiscount.setText("会员出行 优惠" + discount + " >");
            holder.tvMemberDiscount.setVisibility(View.VISIBLE);
        } else {
            holder.tvMemberDiscount.setVisibility(View.GONE);
        }
        
        // 加载封面图片
        String imageUrl = hotel.coverImage != null && !hotel.coverImage.isEmpty() 
            ? hotel.coverImage 
            : (hotel.images != null && !hotel.images.isEmpty() ? hotel.images : null);
        
        if (imageUrl != null) {
            // 替换localhost为10.0.2.2（Android模拟器）
            if (imageUrl.contains("localhost")) {
                imageUrl = imageUrl.replace("localhost", "10.0.2.2");
            }
            
            Glide.with(holder.itemView.getContext())
                    .load(imageUrl)
                    .placeholder(R.color.primary_blue)
                    .error(R.color.primary_blue)
                    .centerCrop()
                    .into(holder.ivImage);
        } else {
            holder.ivImage.setBackgroundColor(holder.itemView.getContext().getResources().getColor(R.color.primary_blue));
        }
        
        holder.itemView.setOnClickListener(v -> {
            if(listener != null) listener.onItemClick(hotel);
        });
    }

    /**
     * 根据评分返回评价文字
     */
    private String getRatingText(float rating) {
        if (rating >= 4.5f) {
            return "很好";
        } else if (rating >= 4.0f) {
            return "好";
        } else if (rating >= 3.5f) {
            return "一般";
        } else if (rating >= 3.0f) {
            return "较差";
        } else {
            return "差";
        }
    }

    /**
     * 设置设施标签
     */
    private void setupFacilities(LinearLayout container, Hotel hotel) {
        container.removeAllViews();
        
        // 解析设施标签（从facilities或tags字段）
        String facilitiesStr = hotel.facilities != null ? hotel.facilities : "";
        String tagsStr = hotel.tags != null ? hotel.tags : "";
        
        // 合并facilities和tags
        String allTags = facilitiesStr;
        if (!tagsStr.isEmpty()) {
            if (!allTags.isEmpty()) {
                allTags += "," + tagsStr;
            } else {
                allTags = tagsStr;
            }
        }
        
        // 如果没有标签，使用默认标签
        if (allTags.isEmpty()) {
            allTags = "洗衣房,咖啡厅,自助入住,自助早餐";
        }
        
        // 解析标签（支持逗号、中文逗号、空格分隔）
        String[] tags = allTags.split("[,，\\s]+");
        
        // 最多显示4个标签
        int maxTags = Math.min(tags.length, 4);
        for (int i = 0; i < maxTags; i++) {
            String tag = tags[i].trim();
            if (!tag.isEmpty()) {
                TextView tagView = new TextView(container.getContext());
                tagView.setText(tag);
                tagView.setTextSize(11);
                tagView.setTextColor(0xFF666666);
                tagView.setBackgroundResource(R.drawable.bg_facility_tag);
                tagView.setPadding(
                    (int) (8 * container.getContext().getResources().getDisplayMetrics().density),
                    (int) (4 * container.getContext().getResources().getDisplayMetrics().density),
                    (int) (8 * container.getContext().getResources().getDisplayMetrics().density),
                    (int) (4 * container.getContext().getResources().getDisplayMetrics().density)
                );
                
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                );
                if (i > 0) {
                    params.setMargins(
                        (int) (6 * container.getContext().getResources().getDisplayMetrics().density),
                        0, 0, 0
                    );
                }
                tagView.setLayoutParams(params);
                
                container.addView(tagView);
            }
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivImage;
        TextView tvName, tvRating, tvReviewCount, tvFavoriteCount, tvDescription;
        TextView tvPrice, tvOriginalPrice, tvMemberDiscount;
        LinearLayout llFacilities;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivImage = itemView.findViewById(R.id.ivHotelImage);
            tvName = itemView.findViewById(R.id.tvHotelName);
            tvRating = itemView.findViewById(R.id.tvRating);
            tvReviewCount = itemView.findViewById(R.id.tvReviewCount);
            tvFavoriteCount = itemView.findViewById(R.id.tvFavoriteCount);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            tvOriginalPrice = itemView.findViewById(R.id.tvOriginalPrice);
            tvMemberDiscount = itemView.findViewById(R.id.tvMemberDiscount);
            llFacilities = itemView.findViewById(R.id.llFacilities);
        }
    }
}
