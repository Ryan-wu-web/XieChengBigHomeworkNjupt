package com.yisu.app.adapter;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation;
import com.bumptech.glide.request.RequestOptions;
import com.yisu.app.R;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;

public class HotelImageAdapter extends RecyclerView.Adapter<HotelImageAdapter.ViewHolder> {

    private List<String> imageUrls;

    public HotelImageAdapter(List<String> imageUrls) {
        this.imageUrls = imageUrls != null ? imageUrls : new ArrayList<>();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_hotel_image, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String imageUrl = imageUrls.get(position);
        if (imageUrl != null && !imageUrl.isEmpty()) {
            // Replace localhost with 10.0.2.2 for Android emulator if needed
            if (imageUrl.contains("localhost")) {
                imageUrl = imageUrl.replace("localhost", "10.0.2.2");
            }
            
            // 使用centerCrop显示图片，优先显示图片的上半部分
            // 如果水印在底部，centerCrop会自动裁剪底部区域
            Glide.with(holder.itemView.getContext())
                    .load(imageUrl)
                    .placeholder(R.color.primary_blue)
                    .error(R.color.primary_blue)
                    .centerCrop()
                    .into(holder.imageView);
        } else {
            holder.imageView.setBackgroundColor(holder.itemView.getContext().getResources().getColor(R.color.primary_blue));
        }
    }

    @Override
    public int getItemCount() {
        return imageUrls != null ? imageUrls.size() : 0;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.ivHotelImage);
        }
    }
}
