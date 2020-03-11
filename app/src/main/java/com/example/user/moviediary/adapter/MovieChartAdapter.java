package com.example.user.moviediary.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.user.moviediary.MainActivity;
import com.example.user.moviediary.R;
import com.example.user.moviediary.fragment.FrgMovieDetails;
import com.example.user.moviediary.fragment.FrgMovieDiaryDetails;
import com.example.user.moviediary.model.MovieChart;
import com.example.user.moviediary.model.MovieDetails;
import com.example.user.moviediary.model.MoviePopular;
import com.example.user.moviediary.model.SearchResults;
import com.example.user.moviediary.util.GlideApp;
import com.example.user.moviediary.util.MoviesRepository;
import com.example.user.moviediary.util.OnGetDetailsCallback;
import com.example.user.moviediary.util.OnGetMoviesByTitleAndReleaseDate;
import com.example.user.moviediary.util.OnGetPopularMoviesCallback;

import java.util.List;

public class MovieChartAdapter extends RecyclerView.Adapter<MovieChartAdapter.CustomViewHolder> {

    private final String TAG = "Parsing";
    private int layout;
    private List<MovieChart> list;
    private Context context;
    private View view;

    public MovieChartAdapter(int layout, List<MovieChart> list) {
        this.layout = layout;
        this.list = list;
    }

    @Override
    public int getItemCount() {
        return list != null ? list.size() : 0;
    }

    @NonNull
    @Override
    public MovieChartAdapter.CustomViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {

        view = LayoutInflater.from(viewGroup.getContext()).inflate(layout, viewGroup, false);
        CustomViewHolder customViewHolder = new CustomViewHolder(view);
        context = viewGroup.getContext();
        return customViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull CustomViewHolder customViewHolder, int i) {

        MovieChart movieChart = list.get(i);
        Log.d(TAG, movieChart.toString());
        GlideApp.with(customViewHolder.itemView).load(movieChart.getPoster())
                .override(185,260)
                .into(customViewHolder.poster);

        customViewHolder.rank.setText(movieChart.getRank() + "");
        customViewHolder.title.setText(movieChart.getTitle());
        customViewHolder.age.setText(movieChart.getAge());
        customViewHolder.reservationRate.setText(movieChart.getRsrvRate());
        customViewHolder.great.setText(movieChart.getGreat());
        customViewHolder.releaseDate.setText(movieChart.getRlsDate());

        final String title = movieChart.getTitle();
        String releaseData = movieChart.getRlsDate();
        String releaseYear = releaseData.substring(0,4);
        Log.d("MovieChartAdapter :",releaseYear);
        final int integerReleaseYear = Integer.parseInt(releaseYear);

        customViewHolder.llMovieView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("MovieChartAdapter :", "onClick");
                searchReleaseMovieFromTMDB(title, integerReleaseYear);
            }
        });

    }
    private void searchReleaseMovieFromTMDB(String title, int releaseYear) {
        final MoviesRepository moviesRepository = MoviesRepository.getInstance();
        MoviesRepository.setQuery(title);
        moviesRepository.searchMoviesByTitleAndReleaseDate(context, releaseYear, new OnGetMoviesByTitleAndReleaseDate() {
            @Override
            public void onSuccess(SearchResults.ResultsBean movieDetails) {
                Log.d("MovieChartAdapter :", "onSuccess");
                int id = movieDetails.getId();
                ((MainActivity)context).setChangeFragment(FrgMovieDetails.newInstance(id));
            }

            @Override
            public void onError() {
                Toast.makeText(context, "영화정보를 가져올 수 없습니다" +"\n직접 검색을 해주세요", Toast.LENGTH_SHORT).show();
            }
        });

    }


    public class CustomViewHolder extends RecyclerView.ViewHolder {

        ImageView poster;
        TextView rank;
        TextView title;
        TextView reservationRate;
        TextView great;
        TextView releaseDate;
        TextView age;
        LinearLayout llMovieView;

        public CustomViewHolder(@NonNull View itemView) {
            super(itemView);
            llMovieView = itemView.findViewById(R.id.llMovieView);
            poster = itemView.findViewById(R.id.poster);
            rank = itemView.findViewById(R.id.rank);
            age = itemView.findViewById(R.id.age);
            title = itemView.findViewById(R.id.title);
            reservationRate = itemView.findViewById(R.id.reservationRate);
            great = itemView.findViewById(R.id.great);
            releaseDate = itemView.findViewById(R.id.releaseDate);
        }
    }



}
