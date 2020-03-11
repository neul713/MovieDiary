package com.example.user.moviediary.fragment;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.example.user.moviediary.R;
import com.example.user.moviediary.adapter.MovieHashtagAdapter;
import com.example.user.moviediary.adapter.MoviePostingSearchAdapter;
import com.example.user.moviediary.model.MovieDiary;
import com.example.user.moviediary.util.DbOpenHelper;

import java.util.ArrayList;

public class FrgPostingSearch extends Fragment implements TextView.OnEditorActionListener {
    private ArrayList<MovieDiary> list = new ArrayList<>();
    private Context mContext;
    private Activity mActivity;
    private View view;

    private MoviePostingSearchAdapter adapter;

    private DbOpenHelper dbOpenHelper;
    private RecyclerView rcvPostingSearch;
    private EditText edtPostingSearch;

    private String title;

    int tempMvId;
    String tempTitle;
    String tempPoster;
    String tempMovieDate;
    String tempPostingDate;
    float tempStar;
    String tempContent;

    public static FrgPostingSearch newInstance(String title) {
        FrgPostingSearch fragment = new FrgPostingSearch();
        Bundle args = new Bundle();

        args.putString("title", title);

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
        view = inflater.inflate(R.layout.fragment_posting_search, container, false);

        edtPostingSearch = view.findViewById(R.id.edtPostingSearch);
        rcvPostingSearch = view.findViewById(R.id.rcvPostingSearch);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(mContext, 3);
        rcvPostingSearch.setLayoutManager(gridLayoutManager);

        title = getArguments().getString("title");

        title = title.replace(" ", "");
        insertList(title);

        edtPostingSearch.setOnEditorActionListener(this);

        edtPostingSearch.setText(title);
        adapter = new MoviePostingSearchAdapter(R.layout.item_user, list);
        rcvPostingSearch.setAdapter(adapter);

        return view;
    }

    private void insertList(String title) {
        list.clear();
        dbOpenHelper = new DbOpenHelper(getContext());
        dbOpenHelper.openPosting();
        Cursor cursor = dbOpenHelper.searchPostingColumn("title", title);
        while (cursor.moveToNext()) {

            tempMvId = cursor.getInt(cursor.getColumnIndex("mv_id"));
            tempTitle = cursor.getString(cursor.getColumnIndex("title"));
            tempPoster = cursor.getString(cursor.getColumnIndex("mv_poster"));
            tempMovieDate = cursor.getString(cursor.getColumnIndex("mv_date"));
            tempPostingDate = cursor.getString(cursor.getColumnIndex("post_date"));
            tempStar = cursor.getFloat(cursor.getColumnIndex("star"));
            tempContent = cursor.getString(cursor.getColumnIndex("content"));

            list.add(new MovieDiary(tempMvId, tempPoster, tempTitle, tempStar, tempMovieDate, tempContent));
        }

        Log.d("SIZE_CHECK", String.valueOf(list.size()));

        dbOpenHelper.close();
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        String searchMovieTitle = edtPostingSearch.getText().toString().trim();
        searchMovieTitle = searchMovieTitle.replace(" ", "");
        insertList(searchMovieTitle);

        edtPostingSearch.setText(searchMovieTitle);
        adapter = new MoviePostingSearchAdapter(R.layout.item_user, list);
        rcvPostingSearch.setAdapter(adapter);
        return false;
    }
}