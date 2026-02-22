package com.yisu.app.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import com.yisu.app.R;
import com.yisu.app.activity.LocationSearchActivity;
import java.util.List;

public class LocationAdapter extends BaseAdapter {
    
    private Context context;
    private List<LocationSearchActivity.LocationItem> items;
    private LayoutInflater inflater;
    
    public LocationAdapter(Context context, List<LocationSearchActivity.LocationItem> items) {
        this.context = context;
        this.items = items;
        this.inflater = LayoutInflater.from(context);
    }
    
    @Override
    public int getCount() {
        return items.size();
    }
    
    @Override
    public LocationSearchActivity.LocationItem getItem(int position) {
        return items.get(position);
    }
    
    @Override
    public long getItemId(int position) {
        return position;
    }
    
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.item_location, parent, false);
            holder = new ViewHolder();
            holder.tvName = convertView.findViewById(R.id.tvLocationName);
            holder.tvAddress = convertView.findViewById(R.id.tvLocationAddress);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        
        LocationSearchActivity.LocationItem item = items.get(position);
        holder.tvName.setText(item.name);
        holder.tvAddress.setText(item.address != null ? item.address : "");
        
        return convertView;
    }
    
    static class ViewHolder {
        TextView tvName;
        TextView tvAddress;
    }
}
