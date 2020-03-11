package com.example.user.moviediary.util;

import com.example.user.moviediary.model.MovieRecommendations;

import java.util.List;

public interface OnGetRecommendationsCallback {
    void onSuccess(List<MovieRecommendations.ResultsBean> resultsBeanList);
    void onError();
}
