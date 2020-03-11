package com.example.user.moviediary;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.user.moviediary.fragment.FrgUserJoinDefault;
import com.example.user.moviediary.util.DbOpenHelper;
import com.kakao.auth.ApiErrorCode;
import com.kakao.auth.ISessionCallback;
import com.kakao.auth.Session;
import com.kakao.network.ErrorResult;
import com.kakao.usermgmt.LoginButton;
import com.kakao.usermgmt.UserManagement;
import com.kakao.usermgmt.callback.MeV2ResponseCallback;
import com.kakao.usermgmt.response.MeV2Response;
import com.kakao.util.exception.KakaoException;

import java.security.MessageDigest;


public class UserJoinActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String USER = "User", INIT = "init";

    SessionCallback callback;
    LoginButton loginKakaoReal;
    FrameLayout flLoginMain;
    LinearLayout llLoginMain;
    // 뒤로가기 버튼 입력시간이 담길 long 객체
    private long backbtnTime = 0l;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getSupportActionBar().hide();

        setContentView(R.layout.fragment_user_join);


        ImageButton ibLoginDefault = findViewById(R.id.ibLoginDefault);
        ImageButton ibLoginKakao = findViewById(R.id.ibLoginKakao);
        loginKakaoReal = findViewById(R.id.loginKakaoReal);
        flLoginMain = findViewById(R.id.flLoginMain);
        llLoginMain = findViewById(R.id.llLoginMain);


        callback = new SessionCallback();
        Session.getCurrentSession().addCallback(callback);
        Session.getCurrentSession().checkAndImplicitOpen();

        ibLoginDefault.setOnClickListener(this);
        ibLoginKakao.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){

            case R.id.ibLoginDefault:
                FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                fragmentTransaction.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_right, R.anim.enter_from_right, R.anim.exit_to_right);
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.replace(R.id.flLoginMain, FrgUserJoinDefault.newInstance());
                fragmentTransaction.commit();
                break;

            case R.id.ibLoginKakao:
                loginKakaoReal.performClick();
                break;
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(Session.getCurrentSession().handleActivityResult(requestCode, resultCode, data)) {
            super.onActivityResult(requestCode, resultCode, data);
            return;
        }
        super.onActivityResult(requestCode,resultCode,data);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Session.getCurrentSession().removeCallback(callback);
    }

    private class SessionCallback implements ISessionCallback {
        @Override
        public void onSessionOpened() {
            UserManagement.getInstance().me(new MeV2ResponseCallback() {
                @Override
                public void onFailure(ErrorResult errorResult) {
                    int result = errorResult.getErrorCode();

                    if(result == ApiErrorCode.CLIENT_ERROR_CODE) {
                        Toast.makeText(getApplicationContext(), "네트워크 연결이 불안정합니다. 다시 시도해 주세요.", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(getApplicationContext(),"로그인 도중 오류가 발생했습니다: "+errorResult.getErrorMessage(),Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onSessionClosed(ErrorResult errorResult) {
                    Toast.makeText(getApplicationContext(),"세션이 닫혔습니다. 다시 시도해 주세요: "+errorResult.getErrorMessage(),Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onSuccess(MeV2Response result) {

                    String name = result.getNickname();
                    String profileImgPath = result.getProfileImagePath();
                    String description = "다이어리 설명을 입력해 보세요.";
                    int kakaoLogin =0; //카카오톡 로그인이 아니면 0, 나이정보가 없거나 미성년자면 1, 성인이면2.
                    //성인여부 검사
                    String age = String.valueOf(result.getKakaoAccount().getAgeRange());
                    Log.d("태그", "age = "+age);
                    if(!age.equals("null") && !age.equals("15~19")){
                        kakaoLogin = 2;
                    }else{
                        kakaoLogin = 1;
                    }

                    //디비에 저장
                    DbOpenHelper dbOpenHelper = new DbOpenHelper(UserJoinActivity.this);
                    dbOpenHelper.openUser();
                    dbOpenHelper.upgradeUserHelper();
                    dbOpenHelper.createUserHelper();
                    dbOpenHelper.insertUserColumn(name, profileImgPath, description, kakaoLogin);
                    dbOpenHelper.close();

                    //프로필 설정 완료했음을 저장
                    SharedPreferences.Editor editor = getSharedPreferences(USER, Context.MODE_PRIVATE).edit();
                    editor.putBoolean(INIT, true);
                    editor.apply();

                    Intent intent = new Intent(UserJoinActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                }
            });
        }

        @Override
        public void onSessionOpenFailed(KakaoException e) {
            Toast.makeText(getApplicationContext(), "로그인 도중 오류가 발생했습니다. 인터넷 연결을 확인해주세요: "+e.toString(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onBackPressed() {
        finish();
    }



    //카카오 디벨로퍼에서 사용할 키값 <-한번만 쓰고 마는건데 그냥 남겨둠
    private void getAppKeyHash() {
        try {
            PackageInfo info = getPackageManager().getPackageInfo(getPackageName(), PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md;
                md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                String something = new String(Base64.encode(md.digest(), 0));
                Log.d("해시키", something);
            }
        } catch (Exception e) {
            Log.d("해시키", e.toString());
        }
    }



}
