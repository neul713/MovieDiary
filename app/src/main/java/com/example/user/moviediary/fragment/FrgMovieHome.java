package com.example.user.moviediary.fragment;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.user.moviediary.MainActivity;
import com.example.user.moviediary.R;
import com.example.user.moviediary.adapter.MovieChartAdapter;
import com.example.user.moviediary.adapter.MovieLatestAdapter;
import com.example.user.moviediary.model.MovieChart;
import com.example.user.moviediary.model.MovieLatest;
import com.example.user.moviediary.model.MoviePopular;
import com.example.user.moviediary.model.MovieVideo;
import com.example.user.moviediary.util.GlideApp;
import com.example.user.moviediary.util.MoviesRepository;
import com.example.user.moviediary.util.OnGetLatestMoviesCallback;
import com.example.user.moviediary.util.OnGetPopularMoviesCallback;
import com.example.user.moviediary.util.OnGetVideoCallback;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FrgMovieHome extends Fragment implements View.OnClickListener {

    private static final String TAG = "Parsing";

    private View view;
    private RecyclerView recyclerView;
    private RecyclerView rcvLatestMovie;
    private LinearLayout layoutView;
    private TextView tvPopularTitle, tvPopularOverview;
    private Button btnPopularMore;
    private ImageView ivPopularPoster;

    private ArrayList<MovieChart> list = new ArrayList<>();
    private List<MovieLatest.ResultsBean> latestList = new ArrayList<>();
    private int popularMovieId;

    private MovieChartAdapter adapter;
    private MovieLatestAdapter latestAdapter;
    private LinearLayoutManager linearLayoutManager;
    private LinearLayoutManager latestLayoutManager;
    private MoviesRepository moviesRepository;
    private MyTask mTask;
    private boolean dataComplete;

    private Context mContext;
    private Activity mActivity;

    public static FrgMovieHome newInstance() {
        FrgMovieHome fragment = new FrgMovieHome();
        Bundle args = new Bundle();
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
        view = inflater.inflate(R.layout.fragment_movie_home, container, false);

        recyclerView = view.findViewById(R.id.recyclerView);
        layoutView = view.findViewById(R.id.layoutView);
        ivPopularPoster = view.findViewById(R.id.ivPopularPoster);
        tvPopularTitle = view.findViewById(R.id.tvPopularTitle);
        tvPopularOverview = view.findViewById(R.id.tvPopularOverview);
        btnPopularMore = view.findViewById(R.id.btnPopularMore);
        rcvLatestMovie = view.findViewById(R.id.rcvLatestMovie);

        TextView tvPM = view.findViewById(R.id.tvPM);
        TextView tvLM = view.findViewById(R.id.tvLM);
        TextView tvMC = view.findViewById(R.id.tvMC);
        LinearLayout bar1 = view.findViewById(R.id.bar1);
        LinearLayout bar2 = view.findViewById(R.id.bar2);
        LinearLayout bar3 = view.findViewById(R.id.bar3);

        tvPM.setTextColor(MainActivity.mainColor);
        tvLM.setTextColor(MainActivity.mainColor);
        tvMC.setTextColor(MainActivity.mainColor);
        bar1.setBackgroundColor(MainActivity.mainColor);
        bar2.setBackgroundColor(MainActivity.mainColor);
        bar3.setBackgroundColor(MainActivity.mainColor);

        BottomNavigationView bottomMenu = getActivity().findViewById(R.id.bottomMenu);
        bottomMenu.setVisibility(View.VISIBLE);

        moviesRepository = MoviesRepository.getInstance();

        ((AppCompatActivity) getActivity()).getSupportActionBar().hide();

        btnPopularMore.setOnClickListener(this);
        ivPopularPoster.setOnClickListener(this);

        mTask = (MyTask) new MyTask().execute();

        return view;

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){

            case R.id.btnPopularMore:
                ((MainActivity) mActivity).setChangeFragment(FrgMovieDetails.newInstance(popularMovieId));
                break;

            case R.id.ivPopularPoster:
                ((MainActivity) mActivity).setChangeFragment(FrgMovieDetails.newInstance(popularMovieId));
                break;


        }
    }

    @Override
    public void onDestroy() {
        if (mTask != null) {
            mTask.cancel(true);
        }

        super.onDestroy();
    }

    private class MyTask extends AsyncTask<Void, Void, Void> {

        //진행바표시
        private ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            //진행다이어로그 시작
            layoutView.setVisibility(View.INVISIBLE);
            progressDialog = new ProgressDialog(mContext);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setMessage("로딩중입니다");
            progressDialog.show();

        }

        @Override
        protected Void doInBackground(Void... params) {

            getPopularMovieYoutubeFromTMDB();

            crawlingCGVMovieChart();

            getLatestMovieFromTMDB();

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

        private void  crawlingCGVMovieChart() {

            Document doc = null;
            try {
                //매니페스트에 인터넷허가, usesCleartextTraffic true추가해야함
                //implementation 'org.jsoup:jsoup:1.12.1'

                //크롤링 할 인터넷 사이트 접속
                doc = Jsoup.connect("http://www.cgv.co.kr/movies/").get();

                //셀렉터를 통해 얻어올 자료 특정화
                Elements titleElements = doc.select("div.box-contents strong.title");
                Elements posterElements = doc.select("div.box-image img[src]");
                Elements ageElements = doc.select("div.box-image span.thumb-image");
                Elements rsrvElements = doc.select("div.box-contents strong.percent");
                Elements greatElements = doc.select("div.box-contents span.percent");
                Elements releaseElements = doc.select("div.box-contents span.txt-info");

                list.clear();
                //1위~7위까지의 각 영화 정보 얻어오기
                for (int i = 0; i < 7; i++) {
                    Element titleElement = titleElements.get(i);
                    Element posterElement = posterElements.get(i);
                    Element ageElement = ageElements.get(i);
                    Element rsrvElement = rsrvElements.get(i);
                    Element greatElement = greatElements.get(i);
                    Element releaseElement = releaseElements.get(i);

                    int rank = (i + 1);
                    //텍스트 추출
                    String title = titleElement.text();
                    String poster = posterElement.attr("src");
                    String age = ageElement.text();
                    String rsrv = rsrvElement.text();
                    String great = greatElement.text();
                    String rlsDate = releaseElement.text();

                    MovieChart movieChart = new MovieChart(rank, title, poster, age, rsrv, great, rlsDate);
                    //리스트에 넣기
                    list.add(movieChart);

                }

            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        private void getPopularMovieYoutubeFromTMDB() {

            //최신영화정보받기
            moviesRepository.setPage(1);
            moviesRepository.getPopularMovieList(new OnGetPopularMoviesCallback() {
                @Override
                public void onSuccess(List<MoviePopular.ResultsBean> resultsBeanList) {
                    //총 20개의 인기영화를 받아오므로 20개중 하나를 랜덤으로 게시
                    int randomPick = (int) (Math.random() * 20);
                    Log.d("MovieDetails", "랜덤숫자=" + randomPick);
                    MoviePopular.ResultsBean popularMovie = resultsBeanList.get(randomPick);

                    getYoutubeMovieTrailer(popularMovie.getId(), popularMovie.getTitle()
                            , popularMovie.getPoster_path(), popularMovie.getOverview());
                }

                @Override
                public void onError() {

                }
            });
        }

        private void getYoutubeMovieTrailer(int movie_id, final String title
                , final String posterPath, final String overview) {
            moviesRepository.getMovieVideoResult(movie_id, new OnGetVideoCallback() {
                @Override
                public void onSuccess(final MovieVideo movieVideo) {

                    String site = movieVideo.getResults().get(0).getSite();
                    if (site.equals("YouTube")) {
                        final String videoUrl = movieVideo.getResults().get(0).getKey();

                        FrgYoutubePlayer youtubePlayer = FrgYoutubePlayer.newInstance(videoUrl);
                        FragmentTransaction fragmentTransaction = getChildFragmentManager().beginTransaction();
                        fragmentTransaction.add(R.id.flYoutube, youtubePlayer).commitAllowingStateLoss();

                        String url = "https://image.tmdb.org/t/p/w92" + posterPath;
                        GlideApp.with(view).load(url).centerCrop().into(ivPopularPoster);
                        String overviewReplace = overview.replace(" ", "\u00A0");
                        tvPopularOverview.setText(overviewReplace);
                        tvPopularTitle.setText(title);

                        popularMovieId = movieVideo.getId();
                        dataComplete = true;

                    } else {
                        //유튜브 영화 트레일러가 없으면 다른영화를 다시찾음
                        getPopularMovieYoutubeFromTMDB();
                    }
                }

                @Override
                public void onError() {
                    //유튜브 영화 트레일러가 없으면 다른영화를 다시찾음
                    getPopularMovieYoutubeFromTMDB();
                }
            });
        }

        private void getLatestMovieFromTMDB() {
            MoviesRepository.setPage(1);
            moviesRepository.getLatestMovieList(new OnGetLatestMoviesCallback() {
                @Override
                public void onSuccess(List<MovieLatest.ResultsBean> resultsBeanList) {
                    latestList = resultsBeanList;
                    latestAdapter = new MovieLatestAdapter(R.layout.item_movie_latest, latestList);
                    latestLayoutManager = new LinearLayoutManager(mContext);
                    rcvLatestMovie.setLayoutManager(latestLayoutManager);
                    rcvLatestMovie.setAdapter(latestAdapter);

                }

                @Override
                public void onError() {

                }

            });
        }


        @Override
        protected void onPostExecute(Void result) {

            //레이아웃 매니저 설정
            linearLayoutManager = new LinearLayoutManager(
                    mContext, LinearLayoutManager.HORIZONTAL, false);
            recyclerView.setLayoutManager(linearLayoutManager);

            //레이아웃 어댑터 설정
            adapter = new MovieChartAdapter(R.layout.item_movie_chart, list);
            recyclerView.setAdapter(adapter);
            ViewCompat.setNestedScrollingEnabled(recyclerView,false);

            //프로그레스바 제거
            progressDialog.dismiss();
            layoutView.setVisibility(View.VISIBLE);

            super.onPostExecute(result);
        }


    }


}
