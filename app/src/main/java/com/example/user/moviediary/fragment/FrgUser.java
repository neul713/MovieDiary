package com.example.user.moviediary.fragment;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.Volley;
import com.example.user.moviediary.MainActivity;
import com.example.user.moviediary.R;
import com.example.user.moviediary.UserJoinActivity;
import com.example.user.moviediary.adapter.MovieDiaryAdapter;
import com.example.user.moviediary.model.MovieDiary;
import com.example.user.moviediary.model.UserData;
import com.example.user.moviediary.util.BackupVolley;
import com.example.user.moviediary.util.DbOpenHelper;
import com.example.user.moviediary.util.GlideApp;
import com.example.user.moviediary.util.WrapContentHeightViewPager;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.kakao.network.ErrorResult;
import com.kakao.usermgmt.UserManagement;
import com.kakao.usermgmt.callback.UnLinkResponseCallback;
import com.kakao.util.helper.log.Logger;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;

import com.example.user.moviediary.ProfileEdit;
import com.example.user.moviediary.adapter.MovieHashtagAdapter;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.content.Context.CLIPBOARD_SERVICE;

public class FrgUser extends Fragment implements View.OnClickListener, View.OnTouchListener, CompoundButton.OnCheckedChangeListener {

    //Shared Preference 키값
    private static final String USER = "User", INIT = "init";
    private static final String INCLUDE_ADULT = "Include_adult", ADULT = "adult";

    private DrawerLayout mainLayout;
    private LinearLayout drawerLayout;
    private Button btnTheme, btnMail, btnLogout, btnBackupSend, btnBackupGet, btnReset;
    private Switch switchAdult;

    private ImageButton ibSetting, ibSeach;
    private EditText edtSearch;
    private TextView txtTitle, diaryCount, wishCount, userName, diaryDesc;
    private Button btnEditProfile;
    private CircleImageView userImage;

    private MovieDiaryAdapter movieDiaryAdapter;
    private ArrayList<MovieDiary> diaryList = new ArrayList<>();
    private ArrayList<MovieDiary> wishList = new ArrayList<>();
    private ArrayList<MovieDiary> searchList = new ArrayList<>();

    private Context mContext;
    private Activity mActivity;
    private View view;

    private FrameLayout flTop;

    private WrapContentHeightViewPager viewPager;
    private Adapter adapter;
    private MovieHashtagAdapter searchAdapter;

    private DbOpenHelper dbOpenHelper;
    private SharedPreferences pref;

    private RequestQueue queue;

    public static FrgUser newInstance() {
        FrgUser fragment = new FrgUser();
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
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_user, container, false);
        new ThemeColors(mContext);

        view.setTag("FrgUser");

        mainLayout = view.findViewById(R.id.mainLayout);
        drawerLayout = view.findViewById(R.id.drawerLayout);
        ibSetting = view.findViewById(R.id.ibSetting);

        btnTheme = view.findViewById(R.id.btnTheme);
        btnMail = view.findViewById(R.id.btnMail);
        switchAdult = view.findViewById(R.id.switchAdult);
        btnLogout = view.findViewById(R.id.btnLogout);
        btnBackupSend = view.findViewById(R.id.btnBackupSend);
        btnBackupGet = view.findViewById(R.id.btnBackupGet);
        btnReset = view.findViewById(R.id.btnReset);

        diaryCount = view.findViewById(R.id.diaryCount);
        wishCount = view.findViewById(R.id.wishCount);
        userName = view.findViewById(R.id.userName);
        diaryDesc = view.findViewById(R.id.diaryDesc);
        btnEditProfile = view.findViewById(R.id.btnEditProfile);
        userImage = view.findViewById(R.id.userImage);
        ibSeach = view.findViewById(R.id.ibSeach);
        edtSearch = view.findViewById(R.id.edtSearch);
        txtTitle = view.findViewById(R.id.txtTitle);

        flTop = view.findViewById(R.id.flTop);
        viewPager = view.findViewById(R.id.viewPager);
        queue = BackupVolley.getInstance(getActivity()).getRequestQueue();

        // 게시물 수 세팅
        diaryCountSetting();

        // 찜 수 세팅
        wishCountSetting();

        //액션바 숨기기
        ((AppCompatActivity) getActivity()).getSupportActionBar().hide();

        //탭레이아웃 세팅
        setupTabLayout();

        //프로필세팅
        setupProfile();

        ibSetting.setOnClickListener(this);
        btnEditProfile.setOnClickListener(this);
        btnBackupSend.setOnClickListener(this);
        ibSeach.setOnClickListener(this);

        //drawer
        btnTheme.setOnClickListener(this);
        btnMail.setOnClickListener(this);
        btnLogout.setOnClickListener(this);
        drawerLayout.setOnTouchListener(this);
        btnBackupSend.setOnClickListener(this);
        btnBackupGet.setOnClickListener(this);
        btnReset.setOnClickListener(this);

        switchAdult.setOnCheckedChangeListener(this);
        mainLayout.setDrawerListener(listener);

        return view;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ibSeach:
                edtSearch.setVisibility(View.VISIBLE);
                ibSeach.setVisibility(View.INVISIBLE);
                txtTitle.setVisibility(View.INVISIBLE);
                ibSetting.setVisibility(View.INVISIBLE);

                edtSearch.setHintTextColor(Color.WHITE);

                edtSearch.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                    @Override
                    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                        Toast.makeText(getContext(), "키워드를 포함한 모든 검색 결과가 나타납니다.", Toast.LENGTH_SHORT).show();
                        String movieTitle = edtSearch.getText().toString().trim();
                        Log.d("title", movieTitle);
                        ((MainActivity) mActivity).setChangeFragment(FrgPostingSearch.newInstance(movieTitle));
                        return false;
                    }
                });
                break;
            case R.id.ibSetting:
                mainLayout.openDrawer(drawerLayout);
                break;

            case R.id.btnEditProfile:
                // 프로필 수정 창 뜨게 하기
                Intent intent = new Intent(getContext(), ProfileEdit.class);
                startActivity(intent);
                ((MainActivity) (mContext)).finish();
                // ((MainActivity) mContext).setChangeFragment(FrgUserProfileEdit.newInstance());
                break;

            //Drawer menu
            case R.id.btnTheme:
                MainActivity.isPressedTheme = true;
                int red = new Random().nextInt(255);
                int green = new Random().nextInt(255);
                int blue = new Random().nextInt(255);
                ThemeColors.setNewThemeColor((MainActivity) mContext, red, green, blue);
                break;

            case R.id.btnMail:
                onClickBtnMail();
                break;

            case R.id.btnBackupSend:
                onClickBtnBackupSend();
                break;

            case R.id.btnBackupGet:
                onClickBtnBackupGet();
                break;

            case R.id.btnLogout:
                onClickBtnLogout();
                break;
            case R.id.btnReset:
                // 재확인 다이얼로그 띄워주긔~~~~~~~~~~
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setTitle("데이터 초기화").setMessage("모든 데이터가 초기화 됩니다.\n초기화 하시겠습니까?");
                builder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        postingListReset();
                        likeListReset();
                        Toast.makeText(mContext, "초기화 되었습니다.", Toast.LENGTH_SHORT).show();
                        ((MainActivity) mActivity).setChangeFragment(FrgUser.newInstance());
                    }
                });
                builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        Toast.makeText(mContext, "취소 되었습니다.", Toast.LENGTH_SHORT).show();
                    }
                });
                AlertDialog alertDialog = builder.create();
                alertDialog.show();
                mainLayout.closeDrawer(drawerLayout);
                break;

        }
    }

    // postingList, likeList 지움
    private void postingListReset() {
        dbOpenHelper = new DbOpenHelper(mContext);
        dbOpenHelper.openPosting();
        Cursor cursor = dbOpenHelper.selectPostingColumns();
        while (cursor.moveToNext()) {
            int tempMvId = cursor.getInt(cursor.getColumnIndex("mv_id"));
            dbOpenHelper.deletePostingColumns(tempMvId);
        }
        dbOpenHelper.close();
    }

    private void likeListReset() {
        dbOpenHelper = new DbOpenHelper(mContext);
        dbOpenHelper.openLike();
        Cursor cursor = dbOpenHelper.selectLikeColumns();
        while (cursor.moveToNext()) {
            int tempMvId = cursor.getInt(cursor.getColumnIndex("mv_id"));
            dbOpenHelper.deleteLikeColumns(tempMvId);
        }
        dbOpenHelper.close();
    }


    private void onClickBtnBackupSend() {
        // 백업 재확인 다이얼로그
        View view = View.inflate(getContext(), R.layout.backup_send1, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setView(view);
        builder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                //백업코드 생성
                final String backupCode = BackupVolley.getRamdomPassword(10);
                Log.d("백업", "백업코드=" + backupCode);
                View view2 = View.inflate(getContext(), R.layout.backcup_send2, null);
                TextView txtBackcupCode = view2.findViewById(R.id.txtBackcupCode);
                txtBackcupCode.setText(backupCode);
                AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                builder.setView(view2);
                builder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //클립보드에 복사
                        ClipboardManager clipboardManager = (ClipboardManager) (mContext).getSystemService(CLIPBOARD_SERVICE);
                        ClipData clipData = ClipData.newPlainText("label", backupCode);
                        clipboardManager.setPrimaryClip(clipData);
                        //Toast.makeText(mContext, "복사되었습니다.",Toast.LENGTH_SHORT).show();
                        //서버로 데이터보내기
                        sendRegisterRequestToServer(backupCode);
                    }
                });
                builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
                builder.show();
            }
        });

        builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });
        builder.show();
    }

    private void onClickBtnBackupGet() {
        View view = View.inflate(getContext(), R.layout.backup_down1, null);
        final EditText edtBackupCode = view.findViewById(R.id.edtBackupCode);
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setView(view);

        builder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                getUserDataRequestToServer(edtBackupCode.getText().toString().trim());
                dialog.cancel();
            }
        });
        builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });
        builder.show();
    }

    private void sendRegisterRequestToServer(String backupCode) {
        //서버에 요청하고나서 받는 response 리스너설정
        Response.Listener<String> responseListener = new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    boolean success = jsonObject.getBoolean("success");

                    if (success) {
                        View view = View.inflate(getContext(), R.layout.backup_send3, null);
                        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                        builder.setView(view);

                        builder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });
                        builder.show();

                    } else {
                        View view = View.inflate(getContext(), R.layout.backup_send_reject, null);
                        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                        builder.setView(view);
                        builder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });
                        builder.show();
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.d("백업", "백업 에러" + e);
                }
            }
        };

        JSONObject userData = BackupVolley.getInstance(mContext).createJasonUserData();
        BackupVolley.RegisterRequest registerRequest
                = new BackupVolley.RegisterRequest(backupCode, userData, responseListener);

        RequestQueue queue = Volley.newRequestQueue(mContext);

        queue.add(registerRequest);

    }

    private void getUserDataRequestToServer(String code) {
        Response.Listener<String> responseListener = new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    boolean success = jsonObject.getBoolean("success");
                    String data = jsonObject.getString("DATA");

                    if (success) {
                        //string 으로 받은 json를 찐 json데이터로 변환
                        JsonParser Parser = new JsonParser();
                        JsonObject jsonObj = (JsonObject) Parser.parse(data);

                        //라이크 리스트를 제이슨 배열에 담음
                        JsonArray likeArray = (JsonArray) jsonObj.get("like_list");
                        //라이크 디비 오픈
                        DbOpenHelper dbOpenHelper = new DbOpenHelper(mContext);
                        dbOpenHelper.openLike();

                        for (int i = 0; i < likeArray.size(); i++) {
                            JsonObject object = (JsonObject) likeArray.get(i);

                            JsonElement no = object.get("no");
                            JsonElement id = object.get("mv_id");
                            JsonElement title = object.get("title");
                            JsonElement poster = object.get("poster");

                            if (!dbOpenHelper.isExistLikeColumn(id.getAsInt())) {
                                dbOpenHelper.insertLikeColumn(id.getAsInt(), title.getAsString(), poster.getAsString());

                            }
                        }

                        //포스팅 디비 오픈
                        dbOpenHelper.openPosting();
                        //라이크 리스트를 제이슨 배열에 담음
                        JsonArray postingArray = (JsonArray) jsonObj.get("posting_list");
                        for (int i = 0; i < postingArray.size(); i++) {
                            JsonObject object = (JsonObject) postingArray.get(i);

                            JsonElement star = object.get("star");
                            JsonElement id = object.get("mv_id");
                            JsonElement title = object.get("title");
                            JsonElement poster = object.get("poster");
                            JsonElement content = object.get("content");
                            JsonElement movie_date = object.get("movie_date");
                            JsonElement posting_date = object.get("posting_date");

                            if (!dbOpenHelper.isExistPostingColumn(id.getAsInt())) {
                                dbOpenHelper.insertPostingColumn(id.getAsInt(), title.getAsString()
                                        , poster.getAsString(), movie_date.getAsString()
                                        , posting_date.getAsString(), star.getAsFloat(), content.getAsString());
                            }
                        }

                        //디비 닫기
                        dbOpenHelper.close();

                        //완료 다이얼로그
                        View view = View.inflate(getContext(), R.layout.backup_down2, null);
                        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                        builder.setView(view);

                        builder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ((MainActivity) mContext).setChangeFragment(FrgUser.newInstance());
                                dialog.cancel();
                            }
                        });
                        builder.show();
                    } else {
                        View view = View.inflate(getContext(), R.layout.backup_down_reject, null);
                        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                        builder.setView(view);
                        builder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });
                        builder.show();
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.d("백업", "백업 받기 에러" + e);
                }
            }
        };

        BackupVolley.BackupRequest registerRequest
                = new BackupVolley.BackupRequest(code, responseListener);

        RequestQueue queue = Volley.newRequestQueue(mContext);

        queue.add(registerRequest);
    }


    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        return true;
    }

    //프로필 뷰 설정
    private void setupProfile() {

        userName.setText(UserData.userName);
        diaryDesc.setText(UserData.diaryDescription);
        if (UserData.profileImgPath != null)
            if (UserData.kakaoLogin == 0) {//카톡로그인아니면서 이미지따로 설정한경우
                userImage.setImageURI(Uri.parse(UserData.profileImgPath));
            } else {//카톡로그인이면서 이미지 따로 있는경우
                GlideApp.with(view).load(UserData.profileImgPath)
                        .fitCenter()
                        .into(userImage);
            }
        else {
            userImage.setImageResource(R.drawable.user_default_image);
            userImage.setColorFilter(MainActivity.mainColor);
        }
        if (UserData.kakaoLogin == 0) {
            switchAdult.setEnabled(false);
        } else {
            //성인영화포함여부 스위치 세팅
            pref = mContext.getSharedPreferences(INCLUDE_ADULT, Activity.MODE_PRIVATE);  // UI 상태를 저장합니다.
            boolean isIncludedAD = pref.getBoolean(ADULT, false);
            switchAdult.setChecked(isIncludedAD);
        }

    }

    //영화 다이어리 posting, like 리스트가 들어갈 탭 레이아웃 & 뷰페이저 설정
    private void setupTabLayout() {
        if (viewPager != null) {
            Log.d("ViewPager", "setupViewPager");
            setupViewPager(viewPager);
        }
        TabLayout tabLayout = view.findViewById(R.id.tabLayout);
        tabLayout.setupWithViewPager(viewPager);
        tabLayout.getTabAt(0).setIcon(R.drawable.user_ic_posting);
        tabLayout.getTabAt(1).setIcon(R.drawable.user_ic_like);

        viewPager.measure(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            flTop.setBackgroundColor(MainActivity.mainColor);
        }
    }

    private void setupViewPager(ViewPager viewPager) {

        adapter = new Adapter(getChildFragmentManager());
        adapter.addFragment(new FrgUserPostingList(), null);
        adapter.addFragment(new FrgUserLikeList(), null);
        viewPager.setAdapter(adapter);

    }

    //posting, like 리스트 정렬
    private void diaryCountSetting() {
        diaryList.clear();
        dbOpenHelper = new DbOpenHelper(getContext());
        dbOpenHelper.openPosting();
        dbOpenHelper.createPostingHelper();
        Cursor cursor = dbOpenHelper.selectPostingColumns();

        while (cursor.moveToNext()) {
            int tempMvId = cursor.getInt(cursor.getColumnIndex("mv_id"));
            String tempTitle = cursor.getString(cursor.getColumnIndex("title"));
            String tempPoster = cursor.getString(cursor.getColumnIndex("mv_poster"));
            String tempMovieDate = cursor.getString(cursor.getColumnIndex("mv_date"));
            String tempPostingDate = cursor.getString(cursor.getColumnIndex("post_date"));
            float tempStar = cursor.getFloat(cursor.getColumnIndex("star"));
            String tempContent = cursor.getString(cursor.getColumnIndex("content"));

            diaryList.add(new MovieDiary(tempMvId, tempPoster, tempTitle, tempStar, tempMovieDate, tempContent));
        }
        diaryCount.setText(String.valueOf(diaryList.size()));
        dbOpenHelper.close();
    }

    private void wishCountSetting() {
        wishList.clear();
        dbOpenHelper = new DbOpenHelper(getContext());
        dbOpenHelper.openLike();
        dbOpenHelper.createLikeHelper();
        Cursor cursor = dbOpenHelper.selectLikeColumns();

        while (cursor.moveToNext()) {
            int tempMvId = cursor.getInt(cursor.getColumnIndex("mv_id"));
            String tempPoster = cursor.getString(cursor.getColumnIndex("mv_poster"));
            String tempTitle = cursor.getString(cursor.getColumnIndex("title"));

            wishList.add(new MovieDiary(tempMvId, tempPoster, tempTitle));
        }
        wishCount.setText(String.valueOf(wishList.size()));

        dbOpenHelper.close();
    }

    //메일보내기 버튼 클릭시
    private void onClickBtnMail() {
        mainLayout.closeDrawer(drawerLayout);

        Intent email = new Intent(Intent.ACTION_SEND);
        email.setType("plain/text");
        // email setting 배열로 해놔서 복수 발송 가능
        String[] address = {"space@kokoboa.com"};
        email.putExtra(Intent.EXTRA_EMAIL, address);
        email.putExtra(Intent.EXTRA_SUBJECT, "문의 사항");
        email.putExtra(Intent.EXTRA_TEXT, "개발자님께 문의 및 의견 사항이 있어 메일을 보냅니다.\n");
        startActivity(email);
    }

    //로그아웃 버튼 클릭시
    private void onClickBtnLogout() {
        // 로그아웃 재확인 다이얼로그
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle("Logout").setMessage("로그아웃 하시겠습니까?\n모든 다이어리 정보가 삭제됩니다.");
        builder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {

                if (UserData.kakaoLogin != 0) {
                    userDatabaseRecreate();
                    onClickLogout();

                } else {
                    userDatabaseRecreate();

                    //액티비티 recreate
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                        mActivity.recreate();
                        ((MainActivity)mContext).finish();
                    }

                    else {
                        Intent i = mActivity.getPackageManager().getLaunchIntentForPackage(mActivity.getPackageName());
                        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        mActivity.startActivity(i);
                        ((MainActivity)mContext).finish();
                    }
                }
            }
        });

        builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    //카톡 로그아웃일 경우
    private void onClickLogout() {

        final String appendMessage = getString(R.string.com_kakao_confirm_unlink);
        new AlertDialog.Builder(mContext)
                .setMessage(appendMessage)
                .setPositiveButton(getString(R.string.com_kakao_ok_button),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                UserManagement.getInstance().requestUnlink(new UnLinkResponseCallback() {
                                    @Override
                                    public void onFailure(ErrorResult errorResult) {
                                        Logger.e(errorResult.toString());
                                    }

                                    @Override
                                    public void onSessionClosed(ErrorResult errorResult) {
                                        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                                        builder.setTitle("로그아웃 오류").setMessage("로그아웃 세션이 닫혔습니다. 어플리케이션이 종료됩니다.");
                                        builder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int id) {
                                                ((MainActivity)mContext).finish();
                                            }
                                        });


                                        AlertDialog alertDialog = builder.create();
                                        alertDialog.show();
                                    }

                                    @Override
                                    public void onNotSignedUp() {
                                        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                                        builder.setTitle("로그아웃 오류").setMessage("가입하지 않거나 이미 탈퇴한 회원입니다. 어플리케이션이 종료됩니다.");
                                        builder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int id) {
                                                ((MainActivity)mContext).finish();
                                            }
                                        });


                                        AlertDialog alertDialog = builder.create();
                                        alertDialog.show();
                                    }

                                    @Override
                                    public void onSuccess(Long userId) {
                                        //startActivity(new Intent(mContext, UserJoinActivity.class));
                                        mActivity.recreate();
                                    }
                                });

                            }
                        })
                .setNegativeButton(getString(R.string.com_kakao_cancel_button),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        }).show();
    }

    //유저테이블 드롭후 재생성
    private void userDatabaseRecreate() {

        //db테이블 모두 드롭후 재생성
        DbOpenHelper dbOpenHelper = new DbOpenHelper(mContext);
        dbOpenHelper.openUser();
        dbOpenHelper.upgradeUserHelper();
        dbOpenHelper.openLike();
        dbOpenHelper.upgradeLikeHelper();
        dbOpenHelper.openPosting();
        dbOpenHelper.upgradePostingHelper();
        dbOpenHelper.close();

        //프로필이 설정되지 않음을 저장
        SharedPreferences.Editor editor = mContext.getSharedPreferences(USER, Context.MODE_PRIVATE).edit();
        editor.putBoolean(INIT, false);
        editor.apply();

        //성인영화검색설정 false 로 저장
        editor = mContext.getSharedPreferences(INCLUDE_ADULT, Context.MODE_PRIVATE).edit();
        editor.putBoolean(ADULT, false);
        editor.apply();

    }

    //Drawer setting
    DrawerLayout.DrawerListener listener = new DrawerLayout.DrawerListener() {

        @Override
        public void onDrawerSlide(@NonNull View drawerView, float slideOffset) {

        }

        @Override
        public void onDrawerOpened(@NonNull View drawerView) {

        }

        @Override
        public void onDrawerClosed(@NonNull View drawerView) {

        }

        @Override
        public void onDrawerStateChanged(int newState) {

        }

    };

    //성인영화 검색 스위치 설정
    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

        if (UserData.kakaoLogin != 2) {
            switchAdult.setChecked(false);
            Toast.makeText(mContext, "카카오톡에 연령대 정보를 입력하지 않아 해당 기능을 사용할 수 없습니다.", Toast.LENGTH_SHORT).show();
        } else {
            SharedPreferences.Editor editor = pref.edit();
            editor.putBoolean(ADULT, isChecked);
            editor.commit();
        }
    }

    //뷰페이지 어댑터
    static class Adapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragments = new ArrayList<>();
        private final List<String> mFragmentTitles = new ArrayList<>();

        public Adapter(FragmentManager fm) {
            super(fm);
        }

        public void addFragment(Fragment fragment, String title) {
            mFragments.add(fragment);
            mFragmentTitles.add(title);
        }

        @Override
        public int getItemPosition(Object object) {
            return POSITION_NONE; // To make notifyDataSetChanged() do something
        }

        @Override
        public Fragment getItem(int position) {
            return mFragments.get(position);
        }

        @Override
        public int getCount() {
            return mFragments.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitles.get(position);
        }
    }

}
