package com.example.user.moviediary.fragment;

import android.os.Bundle;
import android.widget.Toast;

import com.example.user.moviediary.util.DeveloperKey;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayer.Provider;
import com.google.android.youtube.player.YouTubePlayerSupportFragment;

public class FrgYoutubePlayer extends YouTubePlayerSupportFragment implements YouTubePlayer.OnInitializedListener{

    private static final String MOVIE_URL = "MOVIE_URL";

    public static FrgYoutubePlayer newInstance(String url) {
        FrgYoutubePlayer fragment = new FrgYoutubePlayer();

        Bundle args = new Bundle();
        args.putString(MOVIE_URL, url);
        fragment.setArguments(args);
        fragment.init();
        return fragment;
    }

    private void init() {

        this.initialize(DeveloperKey.YOUTUBE, this);

    }

    @Override
    public void onInitializationSuccess(Provider provider, YouTubePlayer youTubePlayer, boolean b) {
        if (!b)
            youTubePlayer.cueVideo(getArguments().getString(MOVIE_URL), 0);

    }

    @Override
    public void onInitializationFailure(Provider provider, YouTubeInitializationResult youTubeInitializationResult) {
        Toast.makeText(getContext(), "영상로드실패", Toast.LENGTH_SHORT).show();

    }
}