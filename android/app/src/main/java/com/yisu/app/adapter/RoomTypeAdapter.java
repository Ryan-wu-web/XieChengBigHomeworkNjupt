package com.yisu.app.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.yisu.app.R;
import com.yisu.app.model.RoomType;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class RoomTypeAdapter extends RecyclerView.Adapter<RoomTypeAdapter.ViewHolder> {

    private List<RoomType> roomTypes;

    public RoomTypeAdapter(List<RoomType> roomTypes) {
        // Sort by price from low to high
        this.roomTypes = roomTypes;
        Collections.sort(this.roomTypes, new Comparator<RoomType>() {
            @Override
            public int compare(RoomType r1, RoomType r2) {
                return Double.compare(r1.price, r2.price);
            }
        });
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_room_type, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        RoomType room = roomTypes.get(position);
        holder.tvRoomName.setText(room.name);
        holder.tvRoomDesc.setText(room.description != null ? room.description : "");
        // 价格显示为整数
        holder.tvRoomPrice.setText(String.valueOf((int)room.price));
        holder.tvRoomStock.setText("库存: " + room.stock);
    }

    @Override
    public int getItemCount() {
        return roomTypes != null ? roomTypes.size() : 0;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvRoomName, tvRoomDesc, tvRoomPrice, tvRoomStock;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvRoomName = itemView.findViewById(R.id.tvRoomName);
            tvRoomDesc = itemView.findViewById(R.id.tvRoomDesc);
            tvRoomPrice = itemView.findViewById(R.id.tvRoomPrice);
            tvRoomStock = itemView.findViewById(R.id.tvRoomStock);
        }
    }
}
