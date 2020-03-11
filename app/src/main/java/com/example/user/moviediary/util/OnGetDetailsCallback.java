package com.example.user.moviediary.util;

import com.example.user.moviediary.model.MovieDetails;

public interface OnGetDetailsCallback {

    void onSuccess(MovieDetails movieDetails);

    void onError();
}
