package com.yisu.app.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.yisu.app.R;
import com.yisu.app.model.Banner;
import java.util.List;

public class BannerAdapter extends RecyclerView.Adapter<BannerAdapter.BannerViewHolder> {

    private List<Banner> banners;
    private OnBannerClickListener listener;

    public interface OnBannerClickListener {
        void onBannerClick(Banner banner);
    }

    public BannerAdapter(List<Banner> banners, OnBannerClickListener listener) {
        this.banners = banners;
        this.listener = listener;
    }

    @NonNull
    @Override
    public BannerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_banner, parent, false);
        return new BannerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BannerViewHolder holder, int position) {
        Banner banner = banners.get(position);
        
        if (banner.imageUrl != null && !banner.imageUrl.isEmpty()) {
            // Replace localhost with 10.0.2.2 for Android emulator if needed
            String imageUrl = banner.imageUrl;
            if (imageUrl.contains("localhost")) {
                imageUrl = imageUrl.replace("localhost", "10.0.2.2");
            }
            
            Glide.with(holder.itemView.getContext())
                    .load(imageUrl)
                    .placeholder(R.color.primary_blue)
                    .error(R.color.primary_blue)
                    .centerCrop()
                    .into(holder.imageView);
        } else {
            holder.imageView.setImageResource(android.R.color.transparent);
            holder.imageView.setBackgroundColor(holder.itemView.getContext().getResources().getColor(R.color.primary_blue));
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null && banner.hotelId > 0) {
                    listener.onBannerClick(banner);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return banners != null ? banners.size() : 0;
    }

    static class BannerViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;

        BannerViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.ivBanner);
        }
    }
}
