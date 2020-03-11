package com.example.user.moviediary.util;

import com.example.user.moviediary.model.MovieLatest;

import java.util.List;

public interface OnGetLatestMoviesCallback {

    void onSuccess(List<MovieLatest.ResultsBean> resultsBeanList);

    void onError();

}
