package com.yisu.app;

import android.app.Application;
import com.amap.api.location.AMapLocationClient;

public class YisuApplication extends Application {
    
    @Override
    public void onCreate() {
        super.onCreate();
        
        // 高德地图 SDK 隐私合规设置
        // 必须在调用任何 SDK 接口前调用这两个接口
        AMapLocationClient.updatePrivacyShow(this, true, true);
        AMapLocationClient.updatePrivacyAgree(this, true);
        
        // 如果使用搜索 SDK，也需要设置
        // 注意：搜索 SDK 的隐私合规接口可能不同，需要查看具体文档
        // 通常 location SDK 的设置会影响到其他 SDK
    }
}
