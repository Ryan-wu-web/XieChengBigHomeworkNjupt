package com.yisu.app.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.services.core.AMapException;
import com.amap.api.services.core.PoiItem;
import com.amap.api.services.poisearch.PoiResult;
import com.amap.api.services.poisearch.PoiSearch;
import com.yisu.app.R;
import com.yisu.app.adapter.LocationAdapter;
import android.util.Log;
import java.util.ArrayList;
import java.util.List;

public class LocationSearchActivity extends AppCompatActivity implements PoiSearch.OnPoiSearchListener {

    private EditText etSearch;
    private ListView listView;
    private LocationAdapter adapter;
    private List<LocationItem> locationList = new ArrayList<>();
    
    private PoiSearch.Query query;
    private PoiSearch poiSearch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location_search);
        
        // 确保高德地图 SDK 隐私合规设置（双重保险）
        AMapLocationClient.updatePrivacyShow(this, true, true);
        AMapLocationClient.updatePrivacyAgree(this, true);
        
        etSearch = findViewById(R.id.etLocationSearch);
        listView = findViewById(R.id.listViewLocations);
        
        adapter = new LocationAdapter(this, locationList);
        listView.setAdapter(adapter);
        
        // 点击列表项返回结果
        listView.setOnItemClickListener((parent, view, position, id) -> {
            Log.d("LocationSearch", "点击事件触发 - position: " + position + ", 列表大小: " + locationList.size());
            if (position >= 0 && position < locationList.size()) {
                LocationItem item = locationList.get(position);
                Log.d("LocationSearch", "获取到的item: " + (item != null ? item.name : "null"));
                if (item != null) {
                    // 创建新的Intent返回结果
                    Intent resultIntent = new Intent();
                    resultIntent.putExtra("city", item.city);
                    resultIntent.putExtra("location", item.name);
                    setResult(RESULT_OK, resultIntent);
                    Log.d("LocationSearch", "返回城市: " + item.city + ", 地点: " + item.name);
                    Toast.makeText(this, "已选择: " + item.city, Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Log.e("LocationSearch", "item 为 null");
                    Toast.makeText(this, "选择失败，请重试", Toast.LENGTH_SHORT).show();
                }
            } else {
                Log.e("LocationSearch", "position 超出范围: " + position + ", 列表大小: " + locationList.size());
                Toast.makeText(this, "选择失败，请重试", Toast.LENGTH_SHORT).show();
            }
        });
        
        // 搜索框文本变化监听
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0) {
                    // 用户输入城市名称，搜索城市相关的POI
                    searchCity(s.toString());
                } else {
                    locationList.clear();
                    adapter.notifyDataSetChanged();
                }
            }
            
            @Override
            public void afterTextChanged(Editable s) {}
        });
        
        // 设置搜索框提示文字
        etSearch.setHint("请输入城市名称，如：北京、上海");
    }
    
    /**
     * 搜索城市相关的POI
     * @param keyword 城市名称，如：北京、上海、广州
     */
    private void searchCity(String keyword) {
        try {
            Log.d("LocationSearch", "开始搜索城市: " + keyword);
            // 搜索城市相关的POI，主要搜索城市名称
            // 第二个参数是POI类型，空字符串表示搜索所有类型
            // 第三个参数是城市代码，空字符串表示在全国范围内搜索
            query = new PoiSearch.Query(keyword, "", "");
            query.setPageSize(20);
            query.setPageNum(1);
            
            poiSearch = new PoiSearch(this, query);
            poiSearch.setOnPoiSearchListener(this);
            poiSearch.searchPOIAsyn();
        } catch (AMapException e) {
            Log.e("LocationSearch", "城市搜索异常", e);
            e.printStackTrace();
            Toast.makeText(this, "搜索失败: " + e.getMessage() + " (错误码: " + e.getErrorCode() + ")", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Log.e("LocationSearch", "城市搜索未知异常", e);
            e.printStackTrace();
            Toast.makeText(this, "搜索失败: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
    
    @Override
    public void onPoiSearched(PoiResult result, int code) {
        Log.d("LocationSearch", "POI 搜索回调 - Code: " + code);
        
        if (code == AMapException.CODE_AMAP_SUCCESS) {
            if (result != null && result.getQuery() != null) {
                List<PoiItem> pois = result.getPois();
                Log.d("LocationSearch", "搜索到 " + (pois != null ? pois.size() : 0) + " 个结果");
                
                if (pois != null && pois.size() > 0) {
                    locationList.clear();
                    
                    // 过滤并添加城市相关的POI结果
                    for (PoiItem poi : pois) {
                        LocationItem item = new LocationItem();
                        item.name = poi.getTitle();
                        item.address = poi.getSnippet();
                        // 提取城市名称（去掉"市"字）
                        String cityName = poi.getCityName();
                        if (cityName != null) {
                            cityName = cityName.replace("市", "").replace("省", "");
                        } else {
                            // 如果城市名称为空，尝试从地址中提取
                            String adcode = poi.getAdCode();
                            if (adcode != null && adcode.length() >= 4) {
                                // 根据adcode判断城市（这里简化处理）
                                cityName = poi.getTitle(); // 使用标题作为城市名
                            } else {
                                cityName = poi.getTitle(); // 使用标题作为城市名
                            }
                        }
                        item.city = cityName;
                        item.latitude = poi.getLatLonPoint().getLatitude();
                        item.longitude = poi.getLatLonPoint().getLongitude();
                        locationList.add(item);
                        Log.d("LocationSearch", "添加结果: " + item.name + ", 城市: " + item.city);
                    }
                    Log.d("LocationSearch", "总共添加了 " + locationList.size() + " 个结果到列表");
                    adapter.notifyDataSetChanged();
                    Log.d("LocationSearch", "列表已更新，当前列表大小: " + locationList.size());
                } else {
                    Log.d("LocationSearch", "搜索成功但没有结果");
                    Toast.makeText(this, "未找到相关城市，请尝试其他关键词", Toast.LENGTH_SHORT).show();
                }
            } else {
                Log.w("LocationSearch", "搜索结果为空或查询对象为空");
            }
        } else {
            String errorMsg = "搜索失败，错误码: " + code;
            // 根据错误码提供更详细的错误信息
            // 高德地图错误码参考：https://lbs.amap.com/api/android-sdk/guide/map-tool/errorcode
            if (code == 1000) {
                errorMsg += " (KEY错误，请检查高德平台配置)";
            } else if (code == 1001) {
                errorMsg += " (KEY无效)";
            } else if (code == 1002) {
                errorMsg += " (KEY过期)";
            } else if (code == 1003) {
                errorMsg += " (服务不可用)";
            } else if (code == 1004) {
                errorMsg += " (用户KEY未激活)";
            } else if (code == 1005) {
                errorMsg += " (请求KEY与绑定平台不符)";
            } else if (code == 1006) {
                errorMsg += " (请求KEY与服务不匹配)";
            } else if (code == 1007) {
                errorMsg += " (请求KEY无权限)";
            } else if (code == 1008) {
                errorMsg += " (安全密钥错误)";
            } else if (code == 1009) {
                errorMsg += " (平台类型不匹配，请检查是否为Android平台)";
            }
            Log.e("LocationSearch", errorMsg);
            Toast.makeText(this, errorMsg, Toast.LENGTH_LONG).show();
        }
    }
    
    @Override
    public void onPoiItemSearched(PoiItem poiItem, int code) {
        // 单个POI搜索回调，这里不需要实现
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 不再需要清理定位客户端
    }
    
    public static class LocationItem {
        public String name;
        public String address;
        public String city;
        public double latitude;
        public double longitude;
    }
}
