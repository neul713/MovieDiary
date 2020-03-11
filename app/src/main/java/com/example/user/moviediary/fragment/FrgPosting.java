package com.example.user.moviediary.fragment;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.user.moviediary.MainActivity;
import com.example.user.moviediary.R;
import com.example.user.moviediary.util.DbOpenHelper;
import com.example.user.moviediary.util.GlideApp;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class FrgPosting extends Fragment implements View.OnClickListener {

    private static final String MOVIE_ID = "MOVIE_ID";
    private static final String MOVIE_TITLE = "MOVIE_TITLE";
    private static final String POSTER_PATH = "POSTER_PATH";

    Button btnAddMovie, btnCancel, btnSave;
    ImageView postingImage;
    RatingBar postingRatingBar;
    TextView postingTitle, postingDate;
    EditText edtReview;

    private int movie_id;

    // 포스터 이미지 uri 저장하는 변수
    private String posterPath;

    private Context mContext;
    private Activity mActivity;
    private View view;

    private DbOpenHelper dbOpenHelper;
    CollapsingToolbarLayout collapseActionView;

    public static FrgPosting newInstance(int movie_id, String title, String poster_path) {
        FrgPosting fragment = new FrgPosting();
        Bundle args = new Bundle();
        args.putInt(MOVIE_ID, movie_id);
        args.putString(MOVIE_TITLE, title);
        args.putString(POSTER_PATH, poster_path);
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
        view = inflater.inflate(R.layout.fragment_posting, container, false);

        btnAddMovie = view.findViewById(R.id.btnAddMovie);
        btnCancel = view.findViewById(R.id.btnCancel);
        btnSave = view.findViewById(R.id.btnSave);
        postingImage = view.findViewById(R.id.postingImage);
        postingRatingBar = view.findViewById(R.id.postingRatingBar);
        postingTitle = view.findViewById(R.id.postingTitle);
        postingDate = view.findViewById(R.id.postingDate);
        edtReview = view.findViewById(R.id.edtReview);
        collapseActionView = view.findViewById(R.id.collapseActionView);

        //액션바 숨기기
        ((AppCompatActivity) getActivity()).getSupportActionBar().hide();

        //영화제목, 포스터 자동세팅
        autoSettingTitleAndPoster();

        dbOpenHelper = new DbOpenHelper(mContext);

        btnAddMovie.setOnClickListener(this);
        postingDate.setOnClickListener(this);
        btnCancel.setOnClickListener(this);
        btnSave.setOnClickListener(this);
        return view;
    }

    private void autoSettingTitleAndPoster() {
        //번들에서 무비아이디, 무비타이틀, 포스터패스 받아오기
        movie_id = getArguments().getInt(MOVIE_ID);
        String movie_title = getArguments().getString(MOVIE_TITLE);
        String poster_path = getArguments().getString(POSTER_PATH);

        //포스터설정
        if (poster_path != null) {
            posterPath = "https://image.tmdb.org/t/p/w500" + poster_path;
            GlideApp.with(view).load(posterPath)
                    .fitCenter()
                    .into(postingImage);
        } else {
            postingImage.setColorFilter(MainActivity.mainColor);
        }

        //제목설정
        postingTitle.setText(movie_title);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnAddMovie:
                // 버튼 누르면 검색화면 뜨게 함!
                ((MainActivity) mActivity).setChangeFragment(FrgMovieSearch.newInstance(true));
                break;
            case R.id.postingDate:
                // 현재 날짜 가져오기
                Calendar calendar = Calendar.getInstance();
                final int y = calendar.get(Calendar.YEAR);
                final int m = calendar.get(Calendar.MONTH);
                final int d = calendar.get(Calendar.DAY_OF_MONTH);
                // 날짜 선택 다이얼로그 뜨게 함!
                DatePickerDialog dpd = new DatePickerDialog(getContext(), new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {

                        postingDate.setText(year + "-" + (month + 1) + "-" + dayOfMonth);
                    }
                }, y, m, d); // 기본값 연월일
                // 날짜 입력 제한. 오늘 이후의 날짜는 선택할 수 없게한다.
                calendar.set(y, m, d);
                dpd.getDatePicker().setMaxDate(calendar.getTimeInMillis());
                dpd.show();
                break;
            case R.id.btnCancel:
                // 취소 재확인 다이얼로그 띄움
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setTitle("취소").setMessage("입력하신 내용이 지워집니다.");
                builder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        pageClear();
                    }
                });

                builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        // 취소를 취소
                    }
                });

                AlertDialog alertDialog = builder.create();
                alertDialog.show();
                break;
            case R.id.btnSave:
                if (postingTitle.getText().toString().equals("")) {
                    Toast.makeText(getContext(), "영화 정보를 추가해주세요.", Toast.LENGTH_SHORT).show();
                    break;
                }
                if (postingDate.getText().toString().equals("") || postingDate.getText().toString().equals("날짜를 선택하세요.") || edtReview.getText().toString().equals("")) {
                    Toast.makeText(getContext(), "입력되지않은 항목이 있습니다. 다시 확인해주세요.", Toast.LENGTH_SHORT).show();
                    break;
                }
                // 현재 날짜,시간 가져오기
                long now = System.currentTimeMillis();
                SimpleDateFormat curDateTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String dateTime = curDateTime.format(now);

                Log.d("curDateTime",dateTime);

                String mvTitleCheck = postingTitle.getText().toString();
                mvTitleCheck = mvTitleCheck.replace(" ","");

                String edtReviewCheck = edtReview.getText().toString();
                edtReviewCheck = edtReviewCheck.replace("\n"," \n");
                edtReviewCheck = edtReviewCheck.replace("#"," #");

                // DB에 넣어준다
                dbOpenHelper.openPosting();
                dbOpenHelper.createPostingHelper();
                dbOpenHelper.insertPostingColumn(movie_id, mvTitleCheck,
                        posterPath, postingDate.getText().toString().trim(),
                        dateTime, postingRatingBar.getRating(), edtReviewCheck+" ");
                // DB확인. 나중에 지워줭
                showDatabase("posting_tbl", "mv_id");

                // 포스팅 하면 찜목록에서 지워지기
                postingCheck(movie_id);

                dbOpenHelper.close();
                pageClear();
                Toast.makeText(getContext(), "저장되었습니다.", Toast.LENGTH_SHORT).show();
                ((MainActivity) mActivity).setChangeFragment(FrgUser.newInstance());
                break;
        }
    }

    private void postingCheck(int tempMvId) {
        dbOpenHelper = new DbOpenHelper(getContext());
        dbOpenHelper.openPosting();
        Cursor cursor = dbOpenHelper.selectPostingColumns();
        while (cursor.moveToNext()) {
            int checkMvId = cursor.getInt(cursor.getColumnIndex("mv_id"));
            if (tempMvId == checkMvId) {
                dbOpenHelper.openLike();
                dbOpenHelper.deleteLikeColumns(checkMvId);
            }
        }
        dbOpenHelper.close();
    }

    private void pageClear() {
        postingImage.setImageResource(R.drawable.movie_no_poster);
        postingRatingBar.setRating(3.0f);
        postingTitle.setText("");
        postingDate.setText("이 곳을 눌러 날짜를 선택하세요.");
        edtReview.setText("");
    }

    public void showDatabase(String tbl_name, String sort) {
        Cursor iCursor = dbOpenHelper.sortColumn(tbl_name, sort);
        Log.d("DbData", "DB Size: " + iCursor.getCount());

        while (iCursor.moveToNext()) {

            int tempMvId = iCursor.getInt(iCursor.getColumnIndex("mv_id"));
            String tempTitle = iCursor.getString(iCursor.getColumnIndex("title"));
            String tempPoster = iCursor.getString(iCursor.getColumnIndex("mv_poster"));
            String tempMovieDate = iCursor.getString(iCursor.getColumnIndex("mv_date"));
            String tempPostingDate = iCursor.getString(iCursor.getColumnIndex("post_date"));
            float tempStar = iCursor.getFloat(iCursor.getColumnIndex("star"));
            String tempContent = iCursor.getString(iCursor.getColumnIndex("content"));

            String Result = tempMvId + ", " + tempTitle + ", " + tempPoster +
                    ", " + tempMovieDate + ", " + tempPostingDate + ", " + tempStar + ", " + tempContent;

            Log.d("DbData", Result);
        }
    }

}
