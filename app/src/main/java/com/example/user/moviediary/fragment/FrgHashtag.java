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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.example.user.moviediary.R;
import com.example.user.moviediary.adapter.MovieHashtagAdapter;
import com.example.user.moviediary.model.MovieDiary;
import com.example.user.moviediary.util.DbOpenHelper;

import java.util.ArrayList;

public class FrgHashtag extends Fragment {
    private ArrayList<MovieDiary> list = new ArrayList<>();
    private Context mContext;
    private Activity mActivity;
    private View view;

    private MovieHashtagAdapter adapter;

    private DbOpenHelper dbOpenHelper;
    private RecyclerView rcvHashtag;
    private EditText edtHashTag;

    private String tag;

    int tempMvId;
    String tempTitle;
    String tempPoster;
    String tempMovieDate;
    String tempPostingDate;
    float tempStar;
    String tempContent;

    public static FrgHashtag newInstance(String tag) {
        FrgHashtag fragment = new FrgHashtag();
        Bundle args = new Bundle();

        args.putString("tag", tag);

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
        view = inflater.inflate(R.layout.fragment_hashtag, container, false);

        edtHashTag = view.findViewById(R.id.edtHashTag);
        rcvHashtag = view.findViewById(R.id.rcvHashtag);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(mContext, 3);
        rcvHashtag.setLayoutManager(gridLayoutManager);

        tag = getArguments().getString("tag");

        insertList();

        edtHashTag.setText(tag);
        adapter = new MovieHashtagAdapter(R.layout.item_user, list);
        rcvHashtag.setAdapter(adapter);

        return view;
    }

    private void insertList() {
        list.clear();
        dbOpenHelper = new DbOpenHelper(getContext());
        dbOpenHelper.openPosting();
        dbOpenHelper.createPostingHelper();

        Cursor cursor = dbOpenHelper.searchPostingColumn("content",tag+" ");

        Log.d("TAG", tag);

        while (cursor.moveToNext()) {
            tempMvId = cursor.getInt(cursor.getColumnIndex("mv_id"));
            tempTitle = cursor.getString(cursor.getColumnIndex("title"));
            tempPoster = cursor.getString(cursor.getColumnIndex("mv_poster"));
            tempMovieDate = cursor.getString(cursor.getColumnIndex("mv_date"));
            tempPostingDate = cursor.getString(cursor.getColumnIndex("post_date"));
            tempStar = cursor.getFloat(cursor.getColumnIndex("star"));
            tempContent = cursor.getString(cursor.getColumnIndex("content"));

            list.add(new MovieDiary(tempMvId,tempPoster,tempTitle,tempStar,tempMovieDate,tempContent));
        }

        Log.d("TAG", String.valueOf(list.size()));

        dbOpenHelper.close();
    }
}