package com.example.user.moviediary.util;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.example.user.moviediary.model.MovieDetails;
import com.example.user.moviediary.model.MovieLatest;
import com.example.user.moviediary.model.MoviePopular;
import com.example.user.moviediary.model.MovieRecommendations;
import com.example.user.moviediary.model.MovieVideo;
import com.example.user.moviediary.model.SearchResults;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MoviesRepository {

    private static final String INCLUDE_ADULT = "Include_adult", ADULT = "adult";

    private final String TAG = "MovieDetails";

    private static final String BASE_URL = "https://api.themoviedb.org/3/";
    private static final String LANGUAGE = "ko-KR";
    private static String query = "";
    private static String region = "KR";
    private static int page = 1;
    private boolean include_adult = false;

    private static MoviesRepository repository;

    private TMDBApi api;

    private MoviesRepository(TMDBApi api) {
        this.api = api;
    }

    //싱글톤으로 작성. 검색결과를 가져오는 도구같은거니 도구는 하나만있으면 됨. 재사용하면 되니까.
    public static MoviesRepository getInstance() {
        if (repository == null) {
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())//파싱등록
                    .build();

            repository = new MoviesRepository(retrofit.create(TMDBApi.class));
        }

        return repository;
    }

    // 영화 검색 메소드 시작
    public void getSearchedMovieResult(Context context, final OnGetMoviesCallback callback) {

        SharedPreferences pref = context.getSharedPreferences(INCLUDE_ADULT,Activity.MODE_PRIVATE);
        boolean check = pref.getBoolean(ADULT,false);
        include_adult = check;
        Log.d("태그", "성인영화포함여부="+include_adult);

        Call<SearchResults> call = api.searchMovies(DeveloperKey.TMDB, LANGUAGE, query, page, include_adult);
        call.enqueue(new Callback<SearchResults>() {
            @Override
            public void onResponse(Call<SearchResults> call, Response<SearchResults> response) {
                SearchResults results = response.body();

                if (results != null && results.getResults().size() != 0) {
                    callback.onSuccess(results.getResults());
                }
                else {
                    callback.onError();
                }
            }

            @Override
            public void onFailure(Call<SearchResults> call, Throwable t) {
                callback.onError();
            }
        });
    }

    public static void setQuery(String QUERY) {
        MoviesRepository.query = QUERY;
    }

    public static void switchToTheNextPage() {
        MoviesRepository.page = ++page;
    }

    public static void setPage(int PAGE) {
        MoviesRepository.page = PAGE;
    }

    public static int getPage() {
        return page;
    }

    // ~ 여기까지가 영화 검색관련 메소드

    // 영화 타이틀+개봉일자 조건으로 검색 메소드 시작
    public void searchMoviesByTitleAndReleaseDate(Context context, int releaseYear, final OnGetMoviesByTitleAndReleaseDate callback) {

        SharedPreferences pref = context.getSharedPreferences(INCLUDE_ADULT,Activity.MODE_PRIVATE);
        boolean check = pref.getBoolean(ADULT,false);
        include_adult = check;
        Log.d("태그", "성인영화포함여부="+include_adult);

        Call<SearchResults> call = api.searchMoviesByTitleAndReleaseDate(DeveloperKey.TMDB, LANGUAGE, query, page, include_adult, releaseYear);
        call.enqueue(new Callback<SearchResults>() {
            @Override
            public void onResponse(Call<SearchResults> call, Response<SearchResults> response) {
                SearchResults results = response.body();
                Log.d("MovieChartAdapter :", "onResponse");
                if (results != null && results.getResults().size() !=0) {
                    List<SearchResults.ResultsBean> list = results.getResults();
                    SearchResults.ResultsBean movieDetails = list.get(0);

                    callback.onSuccess(movieDetails);
                }else
                    callback.onError();

            }

            @Override
            public void onFailure(Call<SearchResults> call, Throwable t) {
                Log.d("MovieChartAdapter :", "onFailure");
                callback.onError();
            }
        });
    }


    //영화 디테일정보얻기 메소드 시작
    public void getMovieDetailsResult(int movie_id, final OnGetDetailsCallback callback) {
        Call<MovieDetails> call = api.getMovieDetails(movie_id, DeveloperKey.TMDB, LANGUAGE);
        call.enqueue(new Callback<MovieDetails>() {

            @Override
            public void onResponse(Call<MovieDetails> call, Response<MovieDetails> response) {
                MovieDetails results = response.body();
                Log.d(TAG, "영화받기 onResponse = "+results.getId()+","+results.getTitle());
                if (results != null) {
                    Log.d(TAG, "영화받기 not null = "+results.getId()+","+results.getTitle());
                    callback.onSuccess(results);
                }
                else
                    callback.onError();

            }

            @Override
            public void onFailure(Call<MovieDetails> call, Throwable t) {
                Log.d(TAG, "영화받기 onFailure = ");
                callback.onError();
            }
        });

    }

    /* ~ 여기까지가 영화 디테일정보얻기 메소드

      영화 유튜브영상 받기 메소드 시작 */
    public void getMovieVideoResult(int movie_id, final OnGetVideoCallback callback) {
        Call<MovieVideo> call = api.getMovieVideo(movie_id, DeveloperKey.TMDB, LANGUAGE);
        call.enqueue(new Callback<MovieVideo>() {

            @Override
            public void onResponse(Call<MovieVideo> call, Response<MovieVideo> response) {

                MovieVideo results = response.body();

                if(results!=null && results.getResults().size()!=0)
                    callback.onSuccess(results);
                else
                    callback.onError();

            }

            @Override
            public void onFailure(Call<MovieVideo> call, Throwable t) {
                callback.onError();
            }
        });

    }

    /* ~ 여기까지가 영화 유튜브영상 받기 메소드

      추천영화 받기 메소드 시작 */
    public void getMovieRecommendations(int movie_id, final OnGetRecommendationsCallback callback) {
        Call<MovieRecommendations> call = api.getMovieRecommendations(movie_id, DeveloperKey.TMDB, LANGUAGE, page);
        call.enqueue(new Callback<MovieRecommendations>() {

            @Override
            public void onResponse(Call<MovieRecommendations> call, Response<MovieRecommendations> response) {

                MovieRecommendations results = response.body();

                if (results.getResults().size() != 0 && results.getResults().size() !=0) {
                    callback.onSuccess(results.getResults());
                } else {
                    callback.onError();
                }
            }

            @Override
            public void onFailure(Call<MovieRecommendations> call, Throwable t) {
                Log.d(TAG, "데이터받기에러");
                callback.onError();
            }
        });

    }
    /* ~ 여기까지가 추천영화받기

      인기영화받기 메소드 시작 */
    public void getPopularMovieList(final OnGetPopularMoviesCallback callback) {
        Call<MoviePopular> call = api.getMoviePopularList(DeveloperKey.TMDB, LANGUAGE, page, region);
        call.enqueue(new Callback<MoviePopular>() {
            @Override
            public void onResponse(Call<MoviePopular> call, Response<MoviePopular> response) {

                MoviePopular results = response.body();

                if (results.getResults().size() != 0 && results.getResults().size() != 0) {
                    Log.d(TAG, results.getResults().toString());
                    callback.onSuccess(results.getResults());
                } else {
                    callback.onError();
                }
            }

            @Override
            public void onFailure(Call<MoviePopular> call, Throwable t) {
                callback.onError();
            }
        });

    }
    /* ~ 여기까지가 인기영화받기

  최신영화받기 메소드 시작 */
    public void getLatestMovieList(final OnGetLatestMoviesCallback callback) {
        Call<MovieLatest> call = api.getMovieLatestList(DeveloperKey.TMDB, LANGUAGE, page, region);
        call.enqueue(new Callback<MovieLatest>() {
            @Override
            public void onResponse(Call<MovieLatest> call, Response<MovieLatest> response) {

                MovieLatest results = response.body();

                if (results.getResults().size() != 0 && results.getResults().size() != 0) {
                    Log.d(TAG, results.getResults().toString());
                    Log.d(TAG, "토탈페이지 = "+results.getTotal_pages());
                    Log.d(TAG, "전체결과 = "+results.getTotal_results());

                    callback.onSuccess(results.getResults());
                } else {
                    callback.onError();
                }
            }

            @Override
            public void onFailure(Call<MovieLatest> call, Throwable t) {
                callback.onError();
            }
        });

    }


}