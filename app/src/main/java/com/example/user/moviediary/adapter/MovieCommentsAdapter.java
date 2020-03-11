package com.example.user.moviediary.adapter;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;

import com.example.user.moviediary.R;
import com.example.user.moviediary.model.MovieComment;

import java.util.List;

public class MovieCommentsAdapter extends RecyclerView.Adapter<MovieCommentsAdapter.CommentsViewHolder> {

    private List<MovieComment> list;
    private int layout;
    private Context context;

    public MovieCommentsAdapter(int layout, List<MovieComment> list) {
        this.list = list;
        this.layout = layout;
    }

    @NonNull
    @Override
    public CommentsViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(layout, viewGroup, false);
        CommentsViewHolder viewHolder = new CommentsViewHolder(view);
        context = viewGroup.getContext();

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull CommentsViewHolder viewHolder, int i) {
        final MovieComment mc = list.get(i);

        String star = mc.getStar();
        viewHolder.star.setText(star);
        viewHolder.ratingBar.setRating((Float.parseFloat(star)) / 2);
        viewHolder.userId.setText(mc.getId());

        String reple = mc.getComent();
        if (reple.length()>2 && reple.substring(0, 3).equals("관람객")) {
            SpannableStringBuilder customColor = new SpannableStringBuilder(reple);
            customColor.setSpan(new ForegroundColorSpan(Color.parseColor("#eb9000")), 0, 3, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            customColor.setSpan(new StyleSpan(Typeface.BOLD), 0, 3, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            viewHolder.comment.setText(customColor);
        } else {
            viewHolder.comment.setText(mc.getComent());
        }
        String symUP = mc.getSymUp();
        symUP = symUP.replaceAll("[^0-9]", "");
        String symDown = mc.getSymDown();
        symDown = symDown.replaceAll("[^0-9]", "");
        viewHolder.symUp.setText(symUP);
        viewHolder.symDown.setText(symDown);

    }

    @Override
    public int getItemCount() {
        return list != null ? list.size() : 0;
    }

    public class CommentsViewHolder extends RecyclerView.ViewHolder {
        private RatingBar ratingBar;
        private TextView symUp, symDown, userId, comment, star;

        public CommentsViewHolder(@NonNull View itemView) {
            super(itemView);
            ratingBar = itemView.findViewById(R.id.ratingBar);
            symUp = itemView.findViewById(R.id.symUp);
            symDown = itemView.findViewById(R.id.symDown);
            userId = itemView.findViewById(R.id.userId);
            comment = itemView.findViewById(R.id.comment);
            star = itemView.findViewById(R.id.star);
        }
    }
}
