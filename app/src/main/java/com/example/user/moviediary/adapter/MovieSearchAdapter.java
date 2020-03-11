package com.example.user.moviediary.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.user.moviediary.R;
import com.example.user.moviediary.model.SearchResults;
import com.example.user.moviediary.util.GlideApp;

import java.util.List;

public class MovieSearchAdapter extends RecyclerView.Adapter<MovieSearchAdapter.SearchViewHolder>{

    private List<SearchResults.ResultsBean> list;
    private int layout;
    private View view;
    private Context context;

    private OnItemSelectedInterface mListener;

    public MovieSearchAdapter(int layout, List<SearchResults.ResultsBean> list, OnItemSelectedInterface mListener) {
        this.list = list;
        this.layout = layout;
        this.mListener = mListener;
    }

    @NonNull
    @Override
    public SearchViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        view = LayoutInflater.from(viewGroup.getContext()).inflate(layout, viewGroup, false);
        SearchViewHolder searchViewHolder = new SearchViewHolder(view);
        context = viewGroup.getContext();
        return searchViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull SearchViewHolder searchViewHolder, int i) {

        final SearchResults.ResultsBean searchResultBean = list.get(i);

        searchViewHolder.title.setText(searchResultBean.getTitle());
        String overview = searchResultBean.getOverview();
        overview = overview.replace(" ", "\u00A0");
        searchViewHolder.overview.setText(overview);


        if (searchResultBean.getPoster_path()!=null) {
            String url = "https://image.tmdb.org/t/p/w92" + searchResultBean.getPoster_path();

            GlideApp.with(searchViewHolder.itemView).load(url)
                    .centerCrop()
                    .into(searchViewHolder.poster);
        }

        //클릭이벤트
        searchViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int movieID = searchResultBean.getId();
                String movieTitle = searchResultBean.getTitle();
                String moviePosterPath = searchResultBean.getPoster_path();
                mListener.onItemSelected(v, movieID, movieTitle, moviePosterPath);

            }
        });

        searchViewHolder.overview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int movieID = searchResultBean.getId();
                String movieTitle = searchResultBean.getTitle();
                String moviePosterPath = searchResultBean.getPoster_path();
                mListener.onItemSelected(v, movieID, movieTitle, moviePosterPath);

            }
        });
    }

    @Override
    public int getItemCount() {
        return list != null ? list.size() : 0;
    }

    public class SearchViewHolder extends RecyclerView.ViewHolder {
        TextView title;
        TextView overview;
        ImageView poster;

        public SearchViewHolder(@NonNull View itemView) {
            super(itemView);
            this.title = itemView.findViewById(R.id.title);
            this.overview = itemView.findViewById(R.id.overview);
            this.poster = itemView.findViewById(R.id.poster);
        }
    }

    public interface OnItemSelectedInterface {
        void onItemSelected(View v, int movieID, String movieTitle, String moviePosterPath);
    }

}
