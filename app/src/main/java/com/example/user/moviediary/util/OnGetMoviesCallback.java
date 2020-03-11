package com.example.user.moviediary.util;

import com.example.user.moviediary.model.SearchResults;

import java.util.List;

public interface OnGetMoviesCallback {

    void onSuccess(List<SearchResults.ResultsBean> resultsBeanList);

    void onError();

}
