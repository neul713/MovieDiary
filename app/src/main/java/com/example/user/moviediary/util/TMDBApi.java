package com.example.user.moviediary.util;

import com.example.user.moviediary.model.MovieDetails;
import com.example.user.moviediary.model.MovieLatest;
import com.example.user.moviediary.model.MoviePopular;
import com.example.user.moviediary.model.MovieRecommendations;
import com.example.user.moviediary.model.MovieVideo;
import com.example.user.moviediary.model.SearchResults;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface TMDBApi {

    @GET("search/movie")
    Call<SearchResults> searchMovies(
            @Query("api_key") String apiKey,
            @Query("language") String language,
            @Query("query") String query,
            @Query("page") int page,
            @Query("include_adult") boolean include_adult
    );

    @GET("search/movie")
    Call<SearchResults> searchMoviesByTitleAndReleaseDate(
            @Query("api_key") String apiKey,
            @Query("language") String language,
            @Query("query") String query,
            @Query("page") int page,
            @Query("include_adult") boolean include_adult,
            @Query("primary_release_year") int primary_release_year
    );

    @GET("movie/{movie_id}")
    Call<MovieDetails> getMovieDetails(
            @Path("movie_id") int movie_id,
            @Query("api_key") String apiKey,
            @Query("language") String language
    );

    @GET("movie/{movie_id}/videos")
    Call<MovieVideo> getMovieVideo(
            @Path("movie_id") int movie_id,
            @Query("api_key") String apiKey,
            @Query("language") String language
    );

    @GET("movie/{movie_id}/similar")
    Call<MovieRecommendations> getMovieRecommendations(
            @Path("movie_id") int movie_id,
            @Query("api_key") String apiKey,
            @Query("language") String language,
            @Query("page") int page
    );

    @GET("movie/popular")
    Call<MoviePopular> getMoviePopularList(
            @Query("api_key") String apiKey,
            @Query("language") String language,
            @Query("page") int page,
            @Query("region") String region
    );

    @GET("movie/now_playing")
    Call<MovieLatest> getMovieLatestList(
            @Query("api_key") String apiKey,
            @Query("language") String language,
            @Query("page") int page,
            @Query("region") String region
    );

}
