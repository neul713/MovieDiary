package com.example.user.moviediary.fragment;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.support.v7.widget.SearchView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.user.moviediary.MainActivity;
import com.example.user.moviediary.R;
import com.example.user.moviediary.adapter.MovieSearchAdapter;
import com.example.user.moviediary.model.SearchResults;
import com.example.user.moviediary.util.MoviesRepository;
import com.example.user.moviediary.util.OnGetMoviesCallback;

import java.util.ArrayList;
import java.util.List;

import static android.content.Context.SEARCH_SERVICE;

public class FrgMovieSearch extends Fragment {

    private final String TAG = "SearchingMovie";
    private static final String IS_POSTING_FRG = "IS_POSTING_FRG";

    private View view;
    private Context mContext;
    private Activity mActivity;

    private MoviesRepository moviesRepository;
    private MovieSearchAdapter adapter;
    private List<SearchResults.ResultsBean> list;
    private RecyclerView rcvSearch;
    private LinearLayout searchMain;
    private MenuItem movieSearch;

    private boolean isPostingFrg;

    public static FrgMovieSearch newInstance(boolean isPostingFrg) {
        FrgMovieSearch fragment = new FrgMovieSearch();
        Bundle args = new Bundle();
        args.putBoolean(IS_POSTING_FRG, isPostingFrg);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
        if (context instanceof Activity) {
            this.mActivity = (Activity) context;
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_movie_search, container, false);

        setHasOptionsMenu(true);
        ((AppCompatActivity) mActivity).getSupportActionBar().setDisplayShowTitleEnabled(false);
        ((AppCompatActivity) mActivity).getSupportActionBar().show();

        isPostingFrg = getArguments().getBoolean(IS_POSTING_FRG);

        rcvSearch = view.findViewById(R.id.rcvSearch);
        searchMain = view.findViewById(R.id.searchMain);
        searchMain.setAlpha((float) 0.5);

        rcvSearch.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                int lastVisibleItemPosition = ((LinearLayoutManager) recyclerView.getLayoutManager()).findLastCompletelyVisibleItemPosition();
                int itemTotalCount = recyclerView.getAdapter().getItemCount();
                if (lastVisibleItemPosition == (itemTotalCount - 1) && itemTotalCount % 20 == 0) {

                    MoviesRepository.switchToTheNextPage();
                    moviesRepository.getSearchedMovieResult(mContext,new OnGetMoviesCallback() {
                        @Override
                        public void onSuccess(List<SearchResults.ResultsBean> resultsBeanList) {
                            list.addAll(resultsBeanList);
                            adapter.notifyDataSetChanged();
                        }

                        @Override
                        public void onError() {
                            Toast.makeText(mContext, "더 이상 검색결과가 없습니다", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });

        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.movie_search, menu);

        movieSearch = menu.findItem(R.id.movieSearch);
        SearchView searchView = (SearchView) movieSearch.getActionView();
        SearchManager searchManager = (SearchManager) mContext.getSystemService(SEARCH_SERVICE);
        searchView.setSearchableInfo(searchManager.getSearchableInfo(mActivity.getComponentName()));
        searchView.setQueryHint("영화제목을 입력하세요");

        EditText searchEditText = (EditText) searchView.findViewById(android.support.v7.appcompat.R.id.search_src_text);
        searchEditText.setTextColor(Color.WHITE);
        searchEditText.setHintTextColor(Color.WHITE);


        searchView.setImeOptions(EditorInfo.IME_ACTION_DONE);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            //검색버튼을 눌렀을때
            @Override
            public boolean onQueryTextSubmit(String s) {
                startMovieSearching(s);
                return false;
            }

            //검색어 변화있을때
            @Override
            public boolean onQueryTextChange(String s) {
                if (s.length() >= 1 && s.matches("^[a-zA-Z가-힣]*$")) {
                    startMovieSearching(s);
                }
                return false;
            }
        });

    }

    private void startMovieSearching(final String word) {

        list = new ArrayList<>();

        String query = word;
        query = query.replace(" ", "+");
        MoviesRepository.setQuery(query);
        MoviesRepository.setPage(1);
        moviesRepository = MoviesRepository.getInstance();
        moviesRepository.getSearchedMovieResult(mContext, new OnGetMoviesCallback() {
            @Override
            public void onSuccess(List<SearchResults.ResultsBean> resultsBeanList) {
                list = resultsBeanList;
                adapter = new MovieSearchAdapter(R.layout.item_movie_search, list,
                        new MovieSearchAdapter.OnItemSelectedInterface() {
                            @Override
                            public void onItemSelected(View v, int movieID, String movieTitle, String moviePosterPath) {

                                if (isPostingFrg)
                                    ((MainActivity) mActivity).setChangeFragment(FrgPosting.newInstance(movieID, movieTitle, moviePosterPath));
                                else
                                    ((MainActivity) mActivity).setChangeFragment(FrgMovieDetails.newInstance(movieID));
                            }
                        });
                rcvSearch.setAdapter(adapter);
                LinearLayoutManager linearLayoutManager = new LinearLayoutManager(mContext);
                rcvSearch.setLayoutManager(linearLayoutManager);
                searchMain.setVisibility(View.GONE);
            }

            @Override
            public void onError() {

            }
        });

    }
}
