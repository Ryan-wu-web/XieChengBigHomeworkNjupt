package com.yisu.app.ai.network;

import com.yisu.app.model.Result;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class AIChatService {
    private static final String BASE_URL = "http://192.168.3.10:9090";
    private static AIChatService instance;
    private ApiService apiService;

    private AIChatService() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        apiService = retrofit.create(ApiService.class);
    }

    public static AIChatService getInstance() {
        if (instance == null) {
            instance = new AIChatService();
        }
        return instance;
    }

    public Call<Result<String>> chat(AIChatRequest request) {
        return apiService.aiChat(request);
    }

    public interface ApiService {
        @retrofit2.http.POST("/api/ai/chat")
        Call<Result<String>> aiChat(@retrofit2.http.Body AIChatRequest request);
    }

    public static class AIChatRequest {
        private String message;
        private boolean hotelQuery = true;

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public boolean isHotelQuery() {
            return hotelQuery;
        }

        public void setHotelQuery(boolean hotelQuery) {
            this.hotelQuery = hotelQuery;
        }
    }
}