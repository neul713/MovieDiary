package com.example.user.moviediary.util;

import com.example.user.moviediary.model.MovieVideo;

public interface OnGetVideoCallback {

    void onSuccess(MovieVideo movieVideo);

    void onError();

}
