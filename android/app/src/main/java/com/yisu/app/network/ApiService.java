package com.yisu.app.network;

import com.yisu.app.model.Banner;
import com.yisu.app.model.Hotel;
import com.yisu.app.model.LoginResult;
import com.yisu.app.model.Page;
import com.yisu.app.model.Result;
import com.yisu.app.model.RoomType;
import com.yisu.app.model.User;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiService {
    // User Auth
    @POST("user/login")
    Call<Result<LoginResult>> login(@Body User user);

    @POST("user/register")
    Call<Result<Object>> register(@Body User user);

    // Banner
    @GET("banner/list")
    Call<Result<List<Banner>>> getBannerList();

    // Hotel
    @GET("hotel/list")
    Call<Result<Page<Hotel>>> getHotelList(@Query("pageNum") int pageNum, @Query("pageSize") int pageSize, @Query("name") String name, @Query("city") String city);

    @GET("hotel/detail/{id}")
    Call<Result<Map<String, Object>>> getHotelDetail(@Path("id") int id);

    // Favorite
    @POST("favorite/toggle")
    Call<Result<Map<String, Object>>> toggleFavorite(@Query("hotelId") int hotelId, @Query("userId") int userId);

    @GET("favorite/check")
    Call<Result<Boolean>> checkFavorite(@Query("hotelId") int hotelId, @Query("userId") int userId);

    @GET("favorite/count")
    Call<Result<Integer>> getFavoriteCount(@Query("hotelId") int hotelId);

    @GET("favorite/list")
    Call<Result<List<Map<String, Object>>>> getFavorites(@Query("userId") int userId);

    // Review
    @GET("review/list")
    Call<Result<Page<Map<String, Object>>>> getReviews(@Query("hotelId") int hotelId, @Query("pageNum") int pageNum, @Query("pageSize") int pageSize);

    @GET("review/count")
    Call<Result<Integer>> getReviewCount(@Query("hotelId") int hotelId);

    @GET("review/rating")
    Call<Result<Double>> getAverageRating(@Query("hotelId") int hotelId);
}
