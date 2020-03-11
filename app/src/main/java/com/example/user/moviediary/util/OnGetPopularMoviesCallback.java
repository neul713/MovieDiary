package com.example.user.moviediary.util;

import com.example.user.moviediary.model.MoviePopular;

import java.util.List;

public interface OnGetPopularMoviesCallback {

    void onSuccess(List<MoviePopular.ResultsBean> resultsBeanList);

    void onError();

}
