package com.example.user.moviediary.fragment;

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

import com.example.user.moviediary.R;
import com.example.user.moviediary.adapter.MovieLikeAdapter;
import com.example.user.moviediary.model.MovieDiary;
import com.example.user.moviediary.util.DbOpenHelper;

import java.util.ArrayList;

public class FrgUserLikeList extends Fragment {

    private View view;
    private Context mContext;
    private MovieLikeAdapter adapter;
    private ArrayList<MovieDiary> list = new ArrayList<>();
    private DbOpenHelper dbOpenHelper;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_user_likelist, container, false);

        RecyclerView recyclerView = view.findViewById(R.id.rcvLike);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(mContext,3);
        recyclerView.setLayoutManager(gridLayoutManager);

        insertList("like_tbl","no DESC");
        adapter = new MovieLikeAdapter(R.layout.item_user, list);

        recyclerView.setAdapter(adapter);
        Log.d("ViewPager", "likelist size = "+list.size());
        return view;

    }

    private void insertList(String tbl_name, String sort) {

        list.clear();
        dbOpenHelper = new DbOpenHelper(getContext());
        dbOpenHelper.openPosting();
        Cursor cursor = dbOpenHelper.sortColumn(tbl_name,sort);

        while (cursor.moveToNext()) {
            int tempMvId = cursor.getInt(cursor.getColumnIndex("mv_id"));
            String tempPoster = cursor.getString(cursor.getColumnIndex("mv_poster"));
            String tempTitle = cursor.getString(cursor.getColumnIndex("title"));

            list.add(new MovieDiary(tempMvId,tempPoster,tempTitle));
        }

        Log.d("TAG",String.valueOf(list.size()));

        dbOpenHelper.close();

    }
}
