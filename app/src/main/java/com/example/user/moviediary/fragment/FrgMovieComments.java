package com.example.user.moviediary.fragment;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.user.moviediary.MainActivity;
import com.example.user.moviediary.R;
import com.example.user.moviediary.adapter.MovieCommentsAdapter;
import com.example.user.moviediary.model.MovieComment;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class FrgMovieComments extends DialogFragment {

    private Context mContext;
    private List<MovieComment> list = new ArrayList<>();

    private RecyclerView recyclerView;
    private TextView totalReview;
    private LinearLayoutManager linearLayoutManager;
    private MovieCommentsAdapter adapter;
    private int page = 1;
    private int totalPage = 0;

    public static FrgMovieComments newInstance(String movieId, int page) {
        FrgMovieComments fragment = new FrgMovieComments();
        Bundle args = new Bundle();
        args.putInt("page", page);
        args.putString("movieId", movieId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = View.inflate(mContext, R.layout.fragment_movie_comments, null);
        recyclerView = view.findViewById(R.id.recyclerView);
        ImageButton ibClose = view.findViewById(R.id.ibClose);
        ImageButton ibBefore = view.findViewById(R.id.ibBefore);
        ImageButton ibNext = view.findViewById(R.id.ibNext);
        totalReview = view.findViewById(R.id.totalReview);

        //액션바 숨기기
        ((AppCompatActivity) getActivity()).getSupportActionBar().hide();

        page = getArguments().getInt("page");
        final String movieId = getArguments().getString("movieId");

        new CrawlingComments(movieId, page).execute();

        ibClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getDialog().dismiss();
            }
        });
        ibBefore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (page <= 1) {
                    Toast.makeText(mContext, "첫 페이지 입니다", Toast.LENGTH_SHORT).show();
                } else {
                    --page;
                    new CrawlingComments(movieId, page).execute();
                }
            }
        });
        ibNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(totalPage>page) {
                    ++page;
                    new CrawlingComments(movieId, page).execute();
                }else{
                    Toast.makeText(mContext, "마지막 페이지 입니다", Toast.LENGTH_SHORT).show();

                }
            }
        });


        return view;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);

        return dialog;
    }

    @Override
    public void onStart() {
        super.onStart();

        Dialog dialog = getDialog();
        if (dialog != null) {
            dialog.getWindow().setLayout((int) (MainActivity.deviceWidth * 0.95), ViewGroup.LayoutParams.MATCH_PARENT);
        }
    }

    private class CrawlingComments extends AsyncTask<Void, Void, Void> {

        //진행바표시
        private ProgressDialog progressDialog;
        private String movieId;
        private int page;

        public CrawlingComments(String movieId, int page) {
            this.movieId = movieId;
            this.page = page;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }

        @Override
        protected Void doInBackground(Void... params) {

            crawlingMovieComments();

            return null;

        }

        private String getNaverCommentsLink(String id, int page) {
            String link = "https://movie.naver.com/movie/bi/mi/pointWriteFormList.nhn?code=" + id
                    + "&type=after&isActualPointWriteExecute=false&isMileageSubscriptionAlready=false&isMileageSubscriptionReject=false&page=" + page;

            return link;
        }

        @Override
        protected void onPostExecute(Void result) {

            //레이아웃 매니저 설정
            if (linearLayoutManager == null)
                linearLayoutManager = new LinearLayoutManager(mContext);

            recyclerView.setLayoutManager(linearLayoutManager);

            //레이아웃 어댑터 설정
            adapter = new MovieCommentsAdapter(R.layout.item_movie_comment, list);
            recyclerView.setAdapter(adapter);

            super.onPostExecute(result);
        }

        private void crawlingMovieComments() {
            Document doc = null;
            try {
                Log.d("네이버", "movieId= " + movieId);
                //크롤링 할 인터넷 사이트 접속
                String link = getNaverCommentsLink(movieId, page);
                doc = Jsoup.connect(link).get();

                //페이지를 못찾으면 예외처리
                Elements notFound = doc.select("h1");
                if((notFound.text()).equals("Not Found")){
                    throw new IOException();
                }

                //전체 댓글 수를 페이지로 변환
                if (totalPage==0) {
                    Elements totalElms = doc.select("div.score_total em");
                    final String totalText = totalElms.text();

                    if(totalText.equals("")){
                        ((MainActivity)(mContext)).runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(mContext, "댓글이 없습니다", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }else {
                        Log.d("네이버", "totalText=" + totalText);
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                totalReview.setText("Review : " + totalText);
                            }
                        });

                        String removeComma = totalText.replace(",", "");
                        Log.d("네이버", "totalText removeComma=" + removeComma);
                        totalPage = (Integer.parseInt(removeComma) / 10) + 1;
                    }
                }

                //셀렉터를 통해 얻어올 자료 특정화
                Elements comment = doc.select("div.score_result");
                Elements commentElms = comment.select("li");

                //Iterator을 사용하여 하나씩 값 가져오기
                Iterator<Element> starElms = commentElms.select("div.star_score em").iterator();
                Iterator<Element> repleElms = commentElms.select("div.score_reple p").iterator();
                Elements idDateElms = commentElms.select("div.score_reple a");//아이디
                Iterator<Element> idElms = idDateElms.select("span").iterator();
                Iterator<Element> symElms = commentElms.select("div.btn_area a._sympathyButton").iterator();
                Iterator<Element> nsymElms = commentElms.select("div.btn_area a._notSympathyButton").iterator();

                list.clear();
                while (starElms.hasNext()) {
                    Element star = starElms.next();
                    Element reple = repleElms.next();
                    Element id = idElms.next();
                    Element sym = symElms.next();
                    Element nsym = nsymElms.next();
                    list.add(new MovieComment(id.text(), reple.text(), star.text(), sym.text(), nsym.text()));

                }

            } catch (IOException e) {
                ((MainActivity)(mContext)).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(mContext, "네이버 댓글 로드에 실패하였습니다", Toast.LENGTH_SHORT).show();
                    }
                });
            }

        }
    }
}
