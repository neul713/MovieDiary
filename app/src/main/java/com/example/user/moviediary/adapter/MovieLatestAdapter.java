package com.example.user.moviediary.adapter;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.user.moviediary.MainActivity;
import com.example.user.moviediary.R;
import com.example.user.moviediary.fragment.FrgMovieComments;
import com.example.user.moviediary.fragment.FrgMovieDetails;
import com.example.user.moviediary.model.MovieLatest;
import com.example.user.moviediary.model.NaverMovie;
import com.example.user.moviediary.util.DbOpenHelper;
import com.example.user.moviediary.util.GlideApp;
import com.example.user.moviediary.util.NaverMovieRepository;
import com.example.user.moviediary.util.OnGetNaverMovieCallback;

import java.util.List;


public class MovieLatestAdapter extends RecyclerView.Adapter<MovieLatestAdapter.LatestViewHolder> {

    private List<MovieLatest.ResultsBean> list;
    private int layout;
    private View view;
    private Context context;
    private DbOpenHelper dbOpenHelper;

    public MovieLatestAdapter(int layout, List<MovieLatest.ResultsBean> list) {
        this.list = list;
        this.layout = layout;
    }

    @NonNull
    @Override
    public LatestViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        view = LayoutInflater.from(viewGroup.getContext()).inflate(layout, viewGroup, false);
        LatestViewHolder searchViewHolder = new LatestViewHolder(view);
        context = viewGroup.getContext();
        dbOpenHelper = new DbOpenHelper(context);

        return searchViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull final LatestViewHolder viewHolder, int i) {

        final MovieLatest.ResultsBean movie = list.get(i);

        //본문내용에 아이디+줄거리를 넣는데 아이디에만 색깔넣고 굵게하기
        String title = movie.getTitle();
        String overview = movie.getOverview();
        String titleOverview = title + "  " + overview;
        titleOverview = titleOverview.replace(" ", "\u00A0");
        int titleLength = title.length();
        SpannableStringBuilder customColor = new SpannableStringBuilder(titleOverview);
        customColor.setSpan(new ForegroundColorSpan(Color.parseColor("#0c085c")), 0, titleLength, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        customColor.setSpan(new StyleSpan(Typeface.BOLD), 0, titleLength, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        viewHolder.tvContent.setText(customColor);


        //본문제목설정
        viewHolder.tvLatestTitle.setText(title);

        //동그란 포스터이미지 설정
        if (movie.getPoster_path() != null) {
            String url = "https://image.tmdb.org/t/p/w92" + movie.getPoster_path();

            GlideApp.with(viewHolder.itemView).load(url)
                    .centerCrop()
                    .into(viewHolder.ivLatestPoster);
        }
        //동그란 포스터이미지 누르면 상세보기로 이동
        viewHolder.ivLatestPoster.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity) context).setChangeFragment(FrgMovieDetails.newInstance(movie.getId()));

            }
        });

        //상세보기버튼 클릭시 상세보기로 이동
        viewHolder.btnLatestMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity) context).setChangeFragment(FrgMovieDetails.newInstance(movie.getId()));

            }
        });

        //본문내용에 들어갈 영화 섬네일 설정
        if (movie.getPoster_path() != null) {
            String url = "https://image.tmdb.org/t/p/w1280" + movie.getBackdrop_path();

            GlideApp.with(context).load(url)
                    .centerCrop()
                    .into(viewHolder.ivBackdrop);
        }

        //해당영화가 찜하기 목록에 있는지 여부 검사해서 보여질 하트모양 설정

        viewHolder.likeButtonVisibleSetting(movie.getId());

        //빈 하트 누르면->찜목록추가
        viewHolder.ibUnlike.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dbOpenHelper.openLike();
                dbOpenHelper.createLikeHelper();

                //컬럼추가
                dbOpenHelper.insertLikeColumn(movie.getId(), movie.getTitle(), movie.getPoster_path());
                dbOpenHelper.close();

                viewHolder.ibUnlike.setVisibility(View.GONE);
                viewHolder.ibLike.setVisibility(View.VISIBLE);
            }
        });

        //찐 하트 누르면->찜목록삭제
        viewHolder.ibLike.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dbOpenHelper.openLike();
                dbOpenHelper.createLikeHelper();

                //컬럼삭제
                dbOpenHelper.deleteLikeColumns(movie.getId());
                dbOpenHelper.close();

                viewHolder.ibLike.setVisibility(View.GONE);
                viewHolder.ibUnlike.setVisibility(View.VISIBLE);
            }
        });

        //줄거리 더보기 설정 - 5줄 넘을때만 더보기 버튼 활성화
        viewHolder.tvContent.setMaxLines(Integer.MAX_VALUE);
        viewHolder.tvContent.post(new Runnable() {
            @Override
            public void run() {
                int lineCnt = viewHolder.tvContent.getLineCount();
                Log.d("cntTest", "movie = " + movie.getTitle() + ", 카운트 = " + lineCnt);
                if (lineCnt < 6) {
                    viewHolder.ibMore.setVisibility(View.GONE);
                } else {
                    viewHolder.tvContent.setMaxLines(5);
                    viewHolder.ibMore.setVisibility(View.VISIBLE);
                }
            }
        });

        viewHolder.ibMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewHolder.tvContent.setMaxLines(Integer.MAX_VALUE);
                viewHolder.ibLess.setVisibility(View.VISIBLE);
                viewHolder.ibMore.setVisibility(View.INVISIBLE);
            }
        });

        viewHolder.ibLess.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewHolder.tvContent.setMaxLines(5);
                viewHolder.ibMore.setVisibility(View.VISIBLE);
                viewHolder.ibLess.setVisibility(View.INVISIBLE);
            }
        });

        viewHolder.ibComments.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final NaverMovieRepository naverMovieRepository = NaverMovieRepository.getInstance();

                //개봉년도 구하기 (연도만 추출)
                String releaseDate = movie.getRelease_date();
                String releaseYear = releaseDate.substring(0, 4);

                //네이버 영화api 검색실행(한국,미국간 개봉일 차이때문에 시작하는 검색 시작하는 년도는 한국 개봉년도에-1을 해줌)
                naverMovieRepository.getMovieResult(context, movie.getTitle(), "" + (Integer.parseInt(releaseYear) - 1), releaseYear
                        , new OnGetNaverMovieCallback() {
                            @Override
                            public void onSuccess(NaverMovie.ItemsBean movieItem) {
                                Log.d("네이버", movieItem.getLink());
                                //영화 기본페이지 링크
                                String basicLink = movieItem.getLink();
                                //아이디만 추출
                                String movieId = basicLink.replaceAll("[^0-9]", "");
                                //댓글 프래그먼트 로드
                                FrgMovieComments dialog = (FrgMovieComments.newInstance(movieId, 1));
                                dialog.show(((MainActivity) context).getSupportFragmentManager(), null);

                            }

                            @Override
                            public void onError(Boolean research) {
                                //개봉년도 포함하지 않고 재검색
                                if (research) {
                                    naverMovieRepository.researchWithoutYearParams(context, movie.getTitle()
                                            , new OnGetNaverMovieCallback() {
                                                @Override
                                                public void onSuccess(NaverMovie.ItemsBean movieItem) {
                                                    Log.d("네이버", movieItem.getLink());
                                                    //영화 기본페이지 링크
                                                    String basicLink = movieItem.getLink();
                                                    //아이디만 추출
                                                    String movieId = basicLink.replaceAll("[^0-9]", "");
                                                    //댓글 프래그먼트 로드
                                                    FrgMovieComments dialog = (FrgMovieComments.newInstance(movieId, 1));
                                                    dialog.show(((MainActivity) context).getSupportFragmentManager(), null);
                                                }

                                                @Override
                                                public void onError(Boolean research) {
                                                    Toast.makeText(context, "네이버 댓글 로드에 실패하였습니다", Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                } else
                                    Toast.makeText(context, "네이버 댓글 로드에 실패하였습니다", Toast.LENGTH_SHORT).show();
                            }
                        });
            }

        });

    }


    @Override
    public int getItemCount() {
        return list != null ? list.size() : 0;
    }


    public class LatestViewHolder extends RecyclerView.ViewHolder {
        TextView tvContent, tvLatestTitle;
        ImageView ivLatestPoster, ivBackdrop;
        ImageButton ibMore, ibLess, ibUnlike, ibLike, ibComments;
        Button btnLatestMore;

        public LatestViewHolder(@NonNull View itemView) {
            super(itemView);
            this.tvContent = itemView.findViewById(R.id.tvContent);
            this.tvLatestTitle = itemView.findViewById(R.id.tvLatestTitle);
            this.ivLatestPoster = itemView.findViewById(R.id.ivLatestPoster);
            this.ivBackdrop = itemView.findViewById(R.id.ivBackdrop);
            this.ibMore = itemView.findViewById(R.id.ibMore);
            this.ibLess = itemView.findViewById(R.id.ibLess);
            this.btnLatestMore = itemView.findViewById(R.id.btnLatestMore);
            this.ibUnlike = itemView.findViewById(R.id.ibUnlike);
            this.ibLike = itemView.findViewById(R.id.ibLike);
            this.ibComments = itemView.findViewById(R.id.ibComments);

            ViewGroup.LayoutParams layoutParams = ivBackdrop.getLayoutParams();
            layoutParams.width = MainActivity.deviceWidth;
            layoutParams.height = MainActivity.deviceWidth;
            ivBackdrop.setLayoutParams(layoutParams);

        }

        private void likeButtonVisibleSetting(int movie_id) {
            dbOpenHelper.openLike();
            dbOpenHelper.createLikeHelper();
            //찜하기 목록에 있는지 여부 검사
            boolean isExistLike = dbOpenHelper.isExistLikeColumn(movie_id);
            dbOpenHelper.close();

            if (isExistLike) {
                ibLike.setVisibility(View.VISIBLE);
                ibUnlike.setVisibility(View.GONE);
            } else {
                ibLike.setVisibility(View.GONE);
                ibUnlike.setVisibility(View.VISIBLE);
            }
        }
    }


}
