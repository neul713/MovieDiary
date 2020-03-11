package com.example.user.moviediary.util;

import com.example.user.moviediary.model.NaverMovie;

public interface OnGetNaverMovieCallback {
    void onSuccess(NaverMovie.ItemsBean movieItem);

    void onError(Boolean research);

}
