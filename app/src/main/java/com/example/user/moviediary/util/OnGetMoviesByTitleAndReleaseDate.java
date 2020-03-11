package com.example.user.moviediary.util;


import com.example.user.moviediary.model.SearchResults;

public interface OnGetMoviesByTitleAndReleaseDate {

    void onSuccess(SearchResults.ResultsBean movieDetails);

    void onError();

}
