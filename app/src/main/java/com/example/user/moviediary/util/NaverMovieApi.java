package com.example.user.moviediary.util;

import com.example.user.moviediary.model.NaverMovie;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Query;

public interface NaverMovieApi {

    @Headers({"X-Naver-Client-Id: aE3MMoNzPS9dca33vsdW", "X-Naver-Client-Secret: Zy2nackoVV"})
    @GET("search/movie.json")
    Call<NaverMovie> searchNaverMovie(
            @Query("query") String query,
            @Query("display") int display,
            @Query("yearfrom") String yearfrom,
            @Query("yearto") String yearto
//            @Query("country") String country
    );

}
