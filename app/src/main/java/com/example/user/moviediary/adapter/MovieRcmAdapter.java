package com.example.user.moviediary.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.user.moviediary.MainActivity;
import com.example.user.moviediary.R;
import com.example.user.moviediary.model.MovieRecommendations;
import com.example.user.moviediary.fragment.FrgMovieDetails;
import com.example.user.moviediary.util.GlideApp;

import java.util.List;

public class MovieRcmAdapter extends RecyclerView.Adapter<MovieRcmAdapter.RcmViewHolder> {

    private int layout;
    private List<MovieRecommendations.ResultsBean> list;
    private View view;
    private Context mContext;

    public MovieRcmAdapter(int layout, List<MovieRecommendations.ResultsBean> list) {
        this.layout = layout;
        this.list = list;
    }

    @NonNull
    @Override
    public RcmViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        view = LayoutInflater.from(viewGroup.getContext()).inflate(layout, viewGroup, false);
        MovieRcmAdapter.RcmViewHolder rcmViewHolder = new MovieRcmAdapter.RcmViewHolder(view);
        mContext = viewGroup.getContext();
        return rcmViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull RcmViewHolder rcmViewHolder, int i) {

        final MovieRecommendations.ResultsBean resultsBean = list.get(i);

        rcmViewHolder.tvTitle.setText(resultsBean.getTitle());
        rcmViewHolder.tvVoteAvg.setText(resultsBean.getVote_average()+"");

        String url = "https://image.tmdb.org/t/p/w780" + resultsBean.getBackdrop_path();
        GlideApp.with(rcmViewHolder.itemView).load(url)
                .centerCrop()
                .into(rcmViewHolder.ivBackdrop);

        rcmViewHolder.ivBackdrop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity) mContext).setChangeFragment(FrgMovieDetails.newInstance(resultsBean.getId()));
            }
        });
    }

    @Override
    public int getItemCount() {
        return list != null ? list.size() : 0;
    }

    public class RcmViewHolder extends RecyclerView.ViewHolder {

        TextView tvTitle, tvVoteAvg;
        ImageView ivBackdrop;

        public RcmViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvVoteAvg = itemView.findViewById(R.id.tvVoteAvg);
            ivBackdrop = itemView.findViewById(R.id.ivBackdrop);
        }
    }
}
