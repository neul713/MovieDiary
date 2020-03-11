package com.example.user.moviediary.util;

import android.content.Context;
import android.util.Log;

import com.example.user.moviediary.model.NaverMovie;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class NaverMovieRepository {

    private static final String BASE_URL = "https://openapi.naver.com/v1/";
    private static String query = "";
    private static String yearfrom = "";
    private static String yearto = "";

    private static NaverMovieRepository repository;
    private NaverMovieApi api;

    private NaverMovieRepository(NaverMovieApi api) {
        this.api = api;
    }

    public static NaverMovieRepository getInstance() {
        if (repository == null) {
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())//파싱등록
                    .build();

            repository = new NaverMovieRepository(retrofit.create(NaverMovieApi.class));
        }

        return repository;
    }

    // 영화 검색 메소드 시작
    public void getMovieResult(final Context context, final String query, String yearfrom, String yearto,
                               final OnGetNaverMovieCallback callback) {

        Call<NaverMovie> call = api.searchNaverMovie(query, 10, yearfrom, yearto);
        call.enqueue(new Callback<NaverMovie>() {

            @Override
            public void onResponse(Call<NaverMovie> call, Response<NaverMovie> response) {
                NaverMovie results = response.body();
                //callback 할 영화아이템 객체
                NaverMovie.ItemsBean item = null;

                //검색결과가 2개 이상인경우 > 찾고자 하는 정확한 영화를 다시 찾음
                if (results.getItems().size() > 1) {
                    item = findTheExactRequestMovie(results, query);
                    if(item==null){
                        callback.onError(false);
                    }
                    callback.onSuccess(item);
                }//검색결과가 딱 1개인 경우 > 해당영화를 콜백
                else if (results.getItems().size()==1) {
                    item = results.getItems().get(0);
                    callback.onSuccess(item);
                } else {//검색결과가 없는경우
                    //개봉년도 속성 포함하지 않고 재검색
                    callback.onError(true);
                }
            }

            @Override
            public void onFailure(Call<NaverMovie> call, Throwable t) {
                Log.d("네이버", "onFailure, t= " + t.toString() + "call =" + call.toString());
                callback.onError(false);
            }
        });
    }
    //개봉년도 포함하지 않고 재검색
    public void researchWithoutYearParams(Context context, final String query, final OnGetNaverMovieCallback callback) {
        Call<NaverMovie> call = api.searchNaverMovie(query, 10, null, null);
        call.enqueue(new Callback<NaverMovie>() {

            @Override
            public void onResponse(Call<NaverMovie> call, Response<NaverMovie> response) {
                NaverMovie results = response.body();
                //callback 할 영화아이템 객체
                NaverMovie.ItemsBean item = null;

                //검색결과가 2개 이상인경우 > 찾고자 하는 정확한 영화를 다시 찾음
                if (results.getItems().size() > 1) {
                    item = findTheExactRequestMovie(results, query);
                    if(item==null){
                        callback.onError(false);
                    }
                    callback.onSuccess(item);
                }//검색결과가 딱 1개인 경우 > 해당영화를 콜백
                else if (results.getItems().size()==1) {
                    item = results.getItems().get(0);
                    callback.onSuccess(item);
                }
                else {//검색결과가 없는경우
                    callback.onError(false);
                }
            }

            @Override
            public void onFailure(Call<NaverMovie> call, Throwable t) {
                Log.d("네이버", "onFailure, t= " + t.toString() + "call =" + call.toString());
                callback.onError(false);
            }
        });


    }

    private NaverMovie.ItemsBean findTheExactRequestMovie(NaverMovie results, String query) {

        //리턴할 영화객체
        NaverMovie.ItemsBean item = null;

        //검색쿼리의 특수문자 제거
        query = spCharRid(query);

        List<NaverMovie.ItemsBean> list = results.getItems();

        for (NaverMovie.ItemsBean movie : list) {
            String title = movie.getTitle();
            title = title.replaceAll("<b>", "");
            title = title.replaceAll("</b>", "");
            Log.d("네이버", "</br>제거된 제목 : "+title);
            //검색된 영화제목중에 특수문자 제거
            title = spCharRid(title);
            Log.d("네이버", "특문 제거된 제목 : "+title);
            if (title.equals(query)) {

                Log.d("네이버", "제목="+title);
                Log.d("네이버", "쿼리="+query);
                return movie;
            }

        }
        return item;
    }

    public String spCharRid(String strInput) {
        String strWork = strInput;

        String spChars[] = new String[]{ "-", "=", "'","~", "!",
                "#", "$", "%", "&",":", "<", ">",
                "\\.","\\*", "\\+","\\?","★","☆","♥","♡", ","};

        for (int i = 0; i < spChars.length; i++) {
            strWork = strWork.replaceAll(spChars[i], "");
            Log.d("네이버", "특문제거"+spChars[i]+"="+strWork);
        }

        return strWork;
    }
}
