package com.example.user.moviediary.fragment;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.database.Cursor;
import android.graphics.drawable.GradientDrawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.user.moviediary.MainActivity;
import com.example.user.moviediary.R;
import com.example.user.moviediary.adapter.MovieRcmAdapter;
import com.example.user.moviediary.model.MovieDetails;
import com.example.user.moviediary.model.MovieRecommendations;
import com.example.user.moviediary.model.MovieVideo;
import com.example.user.moviediary.model.NaverMovie;
import com.example.user.moviediary.util.DbOpenHelper;
import com.example.user.moviediary.util.GlideApp;
import com.example.user.moviediary.util.MoviesRepository;
import com.example.user.moviediary.util.NaverMovieRepository;
import com.example.user.moviediary.util.OnGetDetailsCallback;
import com.example.user.moviediary.util.OnGetNaverMovieCallback;
import com.example.user.moviediary.util.OnGetRecommendationsCallback;
import com.example.user.moviediary.util.OnGetVideoCallback;
import com.yarolegovich.discretescrollview.DSVOrientation;
import com.yarolegovich.discretescrollview.DiscreteScrollView;
import com.yarolegovich.discretescrollview.InfiniteScrollAdapter;
import com.yarolegovich.discretescrollview.transform.ScaleTransformer;

import java.util.ArrayList;
import java.util.List;


public class FrgMovieDetails extends Fragment implements DiscreteScrollView.OnItemChangedListener, View.OnClickListener {

    private final String TAG = "MovieDetails";
    private static final String MOVIE_ID = "MOVIE_ID";

    private ImageView ivBackdrop;
    private ImageView ivPoster;
    private TextView tvTitle;
    private TextView tvOverview;
    private TextView tvRuntime;
    private TextView tvRelease;
    private TextView tvVoteAvg;
    private RatingBar ratingBar;
    private TextView tvOverviewRcm;
    private ScrollView scrollView;
    private ImageButton ibMore;
    private ImageButton ibLess;
    private ImageButton ibLike;
    private ImageButton ibUnlike;

    private MovieDetails currentMovieDetails;

    private View view;
    private Context mContext;
    private Activity mActivity;
    private MoviesRepository moviesRepository;
    private LinearLayout layoutView;
    private FrameLayout flYoutube;
    private TextView tvNotFoundVideo;
    private int movie_id;
    private List<MovieRecommendations.ResultsBean> list;
    private DiscreteScrollView discreteRcm;
    private InfiniteScrollAdapter infiniteAdapter;

    private MyTask mTask;
    private boolean dataComplete;

    private DbOpenHelper dbOpenHelper;

    public static FrgMovieDetails newInstance(int movie_id) {
        FrgMovieDetails fragment = new FrgMovieDetails();

        Bundle args = new Bundle();
        args.putInt(MOVIE_ID, movie_id);
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
        view = inflater.inflate(R.layout.fragment_movie_details, container, false);
        ((AppCompatActivity) getActivity()).getSupportActionBar().hide();

        scrollView = view.findViewById(R.id.scrollView);

        layoutView = view.findViewById(R.id.layoutView);
        ivBackdrop = view.findViewById(R.id.ivBackdrop);
        ivPoster = view.findViewById(R.id.ivPoster);
        tvTitle = view.findViewById(R.id.tvTitle);
        tvOverview = view.findViewById(R.id.tvOverview);
        tvRuntime = view.findViewById(R.id.tvRuntime);
        tvRelease = view.findViewById(R.id.tvRelease);
        tvVoteAvg = view.findViewById(R.id.tvVoteAvg);
        ratingBar = view.findViewById(R.id.ratingBar);
        tvOverviewRcm = view.findViewById(R.id.tvOverviewRcm);
        Button btnPosting = view.findViewById(R.id.btnPosting);
        ibLike = view.findViewById(R.id.ibLike);
        ibUnlike = view.findViewById(R.id.ibUnlike);
        ibMore = view.findViewById(R.id.ibMore);
        ibLess = view.findViewById(R.id.ibLess);
        ImageView ivComments = view.findViewById(R.id.ivComments);

        dbOpenHelper = new DbOpenHelper(mContext);

        ibMore.setOnClickListener(this);
        ibLess.setOnClickListener(this);
        ibLike.setOnClickListener(this);
        ibUnlike.setOnClickListener(this);
        ivComments.setOnClickListener(this);
        btnPosting.setOnClickListener(this);

        moviesRepository = MoviesRepository.getInstance();

        mTask = (MyTask) new MyTask().execute();
        return view;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.ibMore:
                tvOverview.setMaxLines(Integer.MAX_VALUE);
                ibLess.setVisibility(View.VISIBLE);
                ibMore.setVisibility(View.INVISIBLE);
                break;

            case R.id.ibLess:
                tvOverview.setMaxLines(5);
                ibMore.setVisibility(View.VISIBLE);
                ibLess.setVisibility(View.INVISIBLE);
                break;

            case R.id.ibUnlike:
                Log.d("DbData", "unlike클릭 -> 찜하기추가됨");
                dbOpenHelper.openLike();
                dbOpenHelper.createLikeHelper();
                Log.d("DbData", "db열기완료");
                //컬럼추가
                dbOpenHelper.insertLikeColumn(movie_id, currentMovieDetails.getTitle()
                        , currentMovieDetails.getPoster_path());
                showDatabase("like_tbl", "mv_id");
                dbOpenHelper.close();

                ibUnlike.setVisibility(View.GONE);
                ibLike.setVisibility(View.VISIBLE);
                break;

            case R.id.ibLike:
                Log.d("DbData", "like클릭 -> 찜하기삭제됨");
                dbOpenHelper.openLike();
                dbOpenHelper.createLikeHelper();
                Log.d("DbData", "db열기완료");
                //컬럼삭제
                dbOpenHelper.deleteLikeColumns(movie_id);
                showDatabase("like_tbl", "mv_id");
                dbOpenHelper.close();

                ibLike.setVisibility(View.GONE);
                ibUnlike.setVisibility(View.VISIBLE);
                break;

            case R.id.btnPosting:
                ((MainActivity) mActivity).setChangeFragment(FrgPosting.newInstance(movie_id
                        , currentMovieDetails.getTitle(), currentMovieDetails.getPoster_path()));
                break;

            case R.id.ivComments:
                onClickComments();
                break;

        }
    }

    private void onClickComments() {
        final NaverMovieRepository naverMovieRepository = NaverMovieRepository.getInstance();

        //개봉년도 구하기 (연도만 추출)
        String releaseDate = currentMovieDetails.getRelease_date();
        String releaseYear = releaseDate.substring(0, 4);
        //네이버 영화api 검색실행(한국,미국간 개봉일 차이때문에 시작하는 검색 시작하는 년도는 한국 개봉년도에-1을 해줌)
        naverMovieRepository.getMovieResult(mContext, currentMovieDetails.getTitle(), "" + (Integer.parseInt(releaseYear) - 1), releaseYear
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
                        dialog.show(((MainActivity) mContext).getSupportFragmentManager(), null);

                    }

                    @Override
                    public void onError(Boolean research) {
                        //개봉년도 포함하지 않고 재검색
                        if (research) {
                            naverMovieRepository.researchWithoutYearParams(mContext, currentMovieDetails.getTitle()
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
                                            dialog.show(((MainActivity) mContext).getSupportFragmentManager(), null);
                                        }

                                        @Override
                                        public void onError(Boolean research) {
                                            Toast.makeText(mContext, "네이버 댓글 로드에 실패하였습니다", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        } else
                            Toast.makeText(mContext, "네이버 댓글 로드에 실패하였습니다", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public void showDatabase(String tbl_name, String sort) {
        Cursor iCursor = dbOpenHelper.sortColumn(tbl_name, sort);
        Log.d("DbData", "DB Size: " + iCursor.getCount());

        while (iCursor.moveToNext()) {

            int tempMvId = iCursor.getInt(iCursor.getColumnIndex("mv_id"));
            String tempTitle = iCursor.getString(iCursor.getColumnIndex("title"));
            String tempPoster = iCursor.getString(iCursor.getColumnIndex("mv_poster"));
            String Result = tempMvId + ", " + tempTitle + ", " + tempPoster;

            Log.d("DbData", Result);
        }

    }


    @Override
    public void onDestroy() {
        if (mTask != null) {
            mTask.cancel(true);
        }

        super.onDestroy();
    }

    private void onMovieRcmChanged(MovieRecommendations.ResultsBean result) {
        tvOverviewRcm.setText(result.getOverview());
        //줄거리 데이터가 없을때
        if (tvOverviewRcm.getText().toString().equals("")) {
            tvOverviewRcm.setText("줄거리가 없습니다.");
        }
    }

    @Override
    public void onCurrentItemChanged(@Nullable RecyclerView.ViewHolder viewHolder,
                                     int adapterPosition) {
        int positionInDataSet = infiniteAdapter.getRealPosition(adapterPosition);
        onMovieRcmChanged(list.get(positionInDataSet));
    }


    private class MyTask extends AsyncTask<Void, Void, Void> {

        //진행바표시
        private ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            //진행다이어로그 시작
            layoutView.setVisibility(View.INVISIBLE);
            progressDialog = new ProgressDialog(mContext);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setMessage("로딩중입니다");
            progressDialog.show();
            super.onPreExecute();
        }

        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        @Override
        protected Void doInBackground(Void... params) {

            tvNotFoundVideo = view.findViewById(R.id.tvNotFoundVideo);
            flYoutube = view.findViewById(R.id.flYoutube);

            //포스터 이미지뷰 모서리 둥글게
            GradientDrawable drawable =
                    (GradientDrawable) mContext.getDrawable(R.drawable.background_round);
            ivPoster.setBackground(drawable);
            ivPoster.setClipToOutline(true);

            movie_id = getArguments().getInt(MOVIE_ID);

            //영화상세정보 가져와서 뷰에 세팅
            getMovieDetailsFromTMDB();

            //영화정보를 모두 가져올때까지 기다림
            while (true) {
                try {
                    Thread.sleep(100);
                    if (dataComplete == true)
                        break;
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            return null;

        }

        private void getMovieDetailsFromTMDB() {
            moviesRepository.getMovieDetailsResult(movie_id, new OnGetDetailsCallback() {
                @Override
                public void onSuccess(MovieDetails movieDetails) {
                    currentMovieDetails = movieDetails;

                    //db에서 해당영화가 찜목록에 있는지 확인(like버튼 visible setting)
                    likeButtonVisibleSetting();

                    //영화 상세정보 가져와서 뷰에 셋팅
                    setMovieDetailsOnTheView();

                    //비디오 가져오기
                    getMovieUrlFromTMDB();

                    //추천영화 가져오기
                    getMovieRecommendationsFromTMDB();

                }

                @Override
                public void onError() {

                }
            });


        }

        private void likeButtonVisibleSetting() {
            Log.d("DbData", "like클릭 -> 찜하기삭제됨");
            dbOpenHelper.openLike();
            dbOpenHelper.createLikeHelper();
            Log.d("DbData", "db열기완료");
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

        private void setMovieDetailsOnTheView() {

            Log.d(TAG, "영상여부" + currentMovieDetails.isVideo());

            tvTitle.setText(currentMovieDetails.getTitle());
            tvRuntime.setText(currentMovieDetails.getRuntime() + "min");
            tvRelease.setText(currentMovieDetails.getRelease_date());
            tvVoteAvg.setText(String.valueOf(currentMovieDetails.getVote_average()));
            ratingBar.setRating((float) currentMovieDetails.getVote_average() / 2);
            if (currentMovieDetails.getOverview().equals("")) {
                tvOverview.setText("줄거리가 없습니다.");

            } else {
                String overview = currentMovieDetails.getOverview();
                overview = overview.replace(" ", "\u00A0");
                tvOverview.setText(overview);
            }

            //줄거리 더보기 설정 - 5줄 넘을때만 더보기 버튼 활성화
            tvOverview.setMaxLines(Integer.MAX_VALUE);
            tvOverview.post(new Runnable() {
                @Override
                public void run() {
                    int lineCnt = tvOverview.getLineCount();
                    if (lineCnt < 6) {
                        ibMore.setVisibility(View.GONE);
                    } else {
                        tvOverview.setMaxLines(5);
                        ibMore.setVisibility(View.VISIBLE);
                    }
                }
            });

            //포스터설정
            if (currentMovieDetails.getPoster_path() != null) {
                String posterPath = "https://image.tmdb.org/t/p/w500" + currentMovieDetails.getPoster_path();
                GlideApp.with(view).load(posterPath)
                        .override(185, 260)
                        .into(ivPoster);
            }

            if (currentMovieDetails.getBackdrop_path() != null) {
                String backdropPath = "https://image.tmdb.org/t/p/w1280" + currentMovieDetails.getBackdrop_path();
                GlideApp.with(view).load(backdropPath)
                        .centerCrop()
                        .into(ivBackdrop);
            }

        }

        private void getMovieUrlFromTMDB() {
            moviesRepository.getMovieVideoResult(movie_id, new OnGetVideoCallback() {
                @Override
                public void onSuccess(final MovieVideo movieVideo) {

                    String site = movieVideo.getResults().get(0).getSite();
                    if (site.equals("YouTube")) {
                        final String videoUrl = movieVideo.getResults().get(0).getKey();

                        FrgYoutubePlayer youtubePlayer = FrgYoutubePlayer.newInstance(videoUrl);
                        FragmentTransaction fragmentTransaction = getChildFragmentManager().beginTransaction();
                        fragmentTransaction.add(R.id.flYoutube, youtubePlayer).commit();
                        tvNotFoundVideo.setVisibility(View.GONE);
                    } else {
                        onError();
                    }
                }

                @Override
                public void onError() {
                    flYoutube.setVisibility(View.GONE);
                    tvNotFoundVideo.setVisibility(View.VISIBLE);
                }
            });
        }

        private void getMovieRecommendationsFromTMDB() {
            MoviesRepository.setPage(1);
            moviesRepository.getMovieRecommendations(movie_id, new OnGetRecommendationsCallback() {
                @Override
                public void onSuccess(List<MovieRecommendations.ResultsBean> resultsBeanList) {
                    list = resultsBeanList;

                    discreteRcm = view.findViewById(R.id.discreteRcm);
                    discreteRcm.setAdapter(new MovieRcmAdapter(R.layout.item_movie_recommendations, list));

                    discreteRcm.setOrientation(DSVOrientation.HORIZONTAL);
                    discreteRcm.addOnItemChangedListener(FrgMovieDetails.this);
                    infiniteAdapter = InfiniteScrollAdapter.wrap(new MovieRcmAdapter(R.layout.item_movie_recommendations, list));
                    discreteRcm.setAdapter(infiniteAdapter);
                    discreteRcm.setItemTransformer(new ScaleTransformer.Builder()
                            .setMinScale(0.8f)
                            .build());

                    onMovieRcmChanged(resultsBeanList.get(0));
                    dataComplete = true;

                }

                @Override
                public void onError() {
                    tvOverviewRcm.setText("관련된 추천영화가 없습니다.");
                    dataComplete = true;

                }

            });
        }


        @Override
        protected void onPostExecute(Void result) {

            layoutView.setVisibility(View.VISIBLE);
            progressDialog.dismiss();

            dataComplete = false;
            scrollView.post(new Runnable() {
                @Override
                public void run() {
                    scrollView.scrollTo(0, 0);
                }
            });
            super.onPostExecute(result);
        }


    }


}
