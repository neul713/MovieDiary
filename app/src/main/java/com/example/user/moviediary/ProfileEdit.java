package com.example.user.moviediary;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialog;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.user.moviediary.model.UserData;
import com.example.user.moviediary.util.DbOpenHelper;
import com.example.user.moviediary.util.GlideApp;
import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;
import com.kakao.auth.ApiErrorCode;
import com.kakao.auth.ApiResponseCallback;
import com.kakao.auth.AuthService;
import com.kakao.auth.ISessionCallback;
import com.kakao.auth.Session;
import com.kakao.auth.network.response.AccessTokenInfoResponse;
import com.kakao.kakaotalk.KakaoTalkService;
import com.kakao.kakaotalk.api.KakaoTalkApi;
import com.kakao.kakaotalk.callback.TalkResponseCallback;
import com.kakao.kakaotalk.response.KakaoTalkProfile;
import com.kakao.network.ErrorResult;
import com.kakao.usermgmt.LoginButton;
import com.kakao.usermgmt.UserManagement;
import com.kakao.usermgmt.callback.MeV2ResponseCallback;
import com.kakao.usermgmt.response.MeV2Response;
import com.kakao.util.exception.KakaoException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileEdit extends AppCompatActivity implements View.OnClickListener {

    private CircleImageView profileImage;
    private ImageButton ibKakaoLogin;
    private Button btnEditProfileImage, btnEditCancel, btnEditSave;
    private EditText txtName, txtDescription;

    private File tempFile;
    private File copyFile;
    private String profileImgPath;

    private DbOpenHelper dbOpenHelper;

    // 리퀘스트코드
    private static final int PICK_FROM_CAMERA = 2;
    private static final int PICK_FROM_ALBUM = 1;

    // 이미지가 저장될 폴더 이름
    private File storageDir;

    private static final String IS_CHANGE_PROFILE = "isChangeProfile";
    // 뒤로가기 버튼 입력시간이 담길 long 객체
    private long backbtnTime = 0l;
    //카카오 로그인
    SessionCallback callback;
    LoginButton loginKakaoReal;
    FrameLayout flLoginMain;
    LinearLayout llLoginMain;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        profileImage = findViewById(R.id.profileImage);
        btnEditProfileImage = findViewById(R.id.btnEditProfileImage);
        btnEditCancel = findViewById(R.id.btnEditCancel);
        btnEditSave = findViewById(R.id.btnEditSave);
        txtName = findViewById(R.id.txtName);
        txtDescription = findViewById(R.id.txtDescription);
        ibKakaoLogin = findViewById(R.id.ibKakaoLogin);
        loginKakaoReal = findViewById(R.id.loginKakaoReal);

        // 초기 세팅
        setupProfile();
        // 이름은 수정하지 못함
        txtName.setEnabled(false);

        //액션바 숨기기
        getSupportActionBar().hide();

        TedPermission.with(getApplicationContext())
                .setPermissionListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted() {
                        Toast.makeText(ProfileEdit.this, "카메라 권한 요청 허용되었습니다.", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onPermissionDenied(ArrayList<String> deniedPermissions) {
                        Toast.makeText(ProfileEdit.this, "카메라 권한 요청 거절되었습니다.", Toast.LENGTH_SHORT).show();
                    }
                })
                .setRationaleMessage("카메라 권한 허용 요청")
                .setDeniedMessage("요청 거절시 카메라를 사용할 수 없습니다.")
                .setPermissions(Manifest.permission.CAMERA,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE)
                .check();


        btnEditProfileImage.setOnClickListener(this);
        btnEditCancel.setOnClickListener(this);
        btnEditSave.setOnClickListener(this);
        ibKakaoLogin.setOnClickListener(this);

    }

    //프로필 뷰 설정
    private void setupProfile() {

        txtName.setText(UserData.userName);
        txtDescription.setText(UserData.diaryDescription);
        if (UserData.profileImgPath != null) {
            if (UserData.kakaoLogin == 0) {//카톡로그인아니면서 이미지따로 설정한경우
                profileImage.setImageURI(Uri.parse(UserData.profileImgPath));
            } else {//카톡로그인이면서 이미지 따로 있는경우
                GlideApp.with(this).load(UserData.profileImgPath)
                        .fitCenter()
                        .into(profileImage);
            }
        }else {
            profileImage.setImageResource(R.drawable.user_default_image);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnEditProfileImage:
                final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(ProfileEdit.this);
                bottomSheetDialog.setContentView(R.layout.user_profile_image_dialog);
                Button btnPofileImageDelete = bottomSheetDialog.findViewById(R.id.btnPofileImageDelete);
                Button btnPofileImageCameraOn = bottomSheetDialog.findViewById(R.id.btnPofileImageCameraOn);
                Button btnPofileImageLibraryOn = bottomSheetDialog.findViewById(R.id.btnPofileImageLibraryOn);

                btnPofileImageDelete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        profileImage.setImageResource(R.drawable.user_default_image);
                        bottomSheetDialog.dismiss();
                    }
                });
                btnPofileImageCameraOn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

                        try {
                            tempFile = createImageFile();
                        } catch (IOException e) {
                            Toast.makeText(ProfileEdit.this, "이미지 처리 오류! 다시 시도해주세요.", Toast.LENGTH_SHORT).show();
                            bottomSheetDialog.dismiss();
                            e.printStackTrace();
                        }
                        if (tempFile != null) {

                            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {

                                Uri photoUri = FileProvider.getUriForFile(ProfileEdit.this,
                                        "com.example.user.moviediary.fragment.provider", tempFile);
                                intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                                startActivityForResult(intent, PICK_FROM_CAMERA);

                            } else {

                                Uri photoUri = Uri.fromFile(tempFile);
                                intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                                (ProfileEdit.this).startActivityForResult(intent, PICK_FROM_CAMERA);

                            }
                        }
                        bottomSheetDialog.dismiss();
                    }
                });
                btnPofileImageLibraryOn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(Intent.ACTION_PICK);
                        intent.setType(MediaStore.Images.Media.CONTENT_TYPE);
                        (ProfileEdit.this).startActivityForResult(intent, PICK_FROM_ALBUM);
                        bottomSheetDialog.dismiss();
                    }
                });
                bottomSheetDialog.show();
                break;

            case R.id.btnEditCancel:
                onBackPressed();
                break;
            case R.id.btnEditSave:
                String description = txtDescription.getText().toString().trim();
                String name = txtName.getText().toString().trim();

                // 프로필 이미지 바꿀때 안에 있는 파일 지워줌. 용량 차지하지 못하게 한다.
                if(storageDir == null) {
                    profileImgPath = UserData.profileImgPath;
                } else if (storageDir.exists()) {
                    File[] fileList = storageDir.listFiles();
                    fileList = sortFileList(fileList);
                    for (int i = 0; i < fileList.length - 1; i++) {
                        if (fileList.length == 0) {
                            break;
                        }
                        fileList[i].delete();
                    }
                    //storageDir.delete();
                } else if (!storageDir.exists()) {
                    storageDir.mkdirs();
                }

                if (!description.equals("") && !name.equals("")) {

                    Log.d("User_check", name + ", " + description + ", " + profileImgPath);

                    //디비에 저장
                    dbOpenHelper = new DbOpenHelper(this);
                    dbOpenHelper.openUser();
                    int kakaoLogin = UserData.kakaoLogin;
                    dbOpenHelper.updateUserColumn(name, profileImgPath, description, kakaoLogin);
                    dbOpenHelper.close();

                    Intent intent = new Intent(ProfileEdit.this, MainActivity.class);
                    intent.putExtra(IS_CHANGE_PROFILE, true);
                    startActivity(intent);
                    finish();

                } else {
                    Toast.makeText(this, "입력되지 않은 정보가 있습니다.", Toast.LENGTH_SHORT).show();
                }
                break;

            case R.id.ibKakaoLogin:
                if (UserData.kakaoLogin == 0)//일반 로그인유저면 카카오 연동실행
                    loginKakaoReal.performClick();
                else{
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("카카오톡 로그인")
                            .setMessage("이미 카카오톡과 연동된 사용자입니다");

                    builder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });

                    AlertDialog alertDialog = builder.create();
                    alertDialog.show();
                }

                break;
        }
    }

    public File[] sortFileList(File[] files) {
        Arrays.sort(files,
                new Comparator<Object>() {
                    @Override
                    public int compare(Object object1, Object object2) {
                        String s1 = "";
                        String s2 = "";
                        s1 = ((File) object1).getName();
                        s2 = ((File) object2).getName();

                        return s1.compareTo(s2);
                    }
                });

        return files;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        //카카오로그인일 경우
        if (Session.getCurrentSession().handleActivityResult(requestCode, resultCode, data)) {
            super.onActivityResult(requestCode, resultCode, data);
            return;
        }

        if (resultCode != RESULT_CANCELED) {
            if (requestCode == PICK_FROM_CAMERA) {
                BitmapFactory.Options options = new BitmapFactory.Options();
                Bitmap originalBm = BitmapFactory.decodeFile(tempFile.getAbsolutePath(), options);
                Log.d("태그", "originalBm="+originalBm);
                ExifInterface exifInterface = null;
                // 속성을 체크해야된다.
                try {
                    exifInterface = new ExifInterface(tempFile.getAbsolutePath());
                } catch (IOException e) {
                    e.printStackTrace();
                }
                int exifOrientation; // 방향 설정값 저장 변수
                int exifDegree; // Degree 설정값 저장 변수
                if (exifInterface != null) {
                    exifOrientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                            ExifInterface.ORIENTATION_NORMAL);
                    exifDegree = exifOrientationToDegree(exifOrientation);
                } else {
                    exifDegree = 0;
                }

                Bitmap bitmapTemp = rotate(originalBm, exifDegree);

                profileImage.setImageBitmap(bitmapTemp);

            } else if (requestCode == PICK_FROM_ALBUM) {

                Uri photoUri = data.getData();

                Cursor cursor = null;

                try {
                    // Uri 스키마를 content:/// 에서 file:/// 로  변경한다
                    String[] proj = {MediaStore.Images.Media.DATA};

                    assert photoUri != null;
                    cursor = getContentResolver().query(photoUri, proj, null, null, null);

                    assert cursor != null;
                    int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);

                    cursor.moveToFirst();

                    tempFile = new File(cursor.getString(column_index));
                    copyFile = createImageFile();

                    Thread copyImage = new Thread(new Runnable() {
                        @Override
                        public void run() {

                            FileInputStream fi = null;
                            FileOutputStream fo = null;
                            byte[] buf = null;

                            try {
                                fi = new FileInputStream(tempFile.getAbsolutePath());
                                fo = new FileOutputStream(copyFile.getAbsolutePath());

                                buf = new byte[1024];

                                int length;
                                while ((length = fi.read(buf)) > 0) {
                                    fo.write(buf, 0, length);
                                    //파일 전부를 옮길때까지 이걸 반복
                                }
                                fo.flush();

                            } catch (FileNotFoundException e) {
                                e.printStackTrace();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            BitmapFactory.Options options = new BitmapFactory.Options();
                            final Bitmap originalBm = BitmapFactory.decodeFile(copyFile.getAbsolutePath(), options);

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    profileImage.setImageBitmap(originalBm);
                                }
                            });
                        }
                    });

                    copyImage.start();

                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    if (cursor != null) {
                        cursor.close();
                    }
                }

            }
        }
    }

    // 각도를 조절해서 다시 만든 비트맵
    private Bitmap rotate(Bitmap bitmap, int exifDegree) {
        Matrix matrix = new Matrix();
        matrix.postRotate(exifDegree);
        Bitmap tempBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(),
                matrix, true);
        return tempBitmap;
    }

    private int exifOrientationToDegree(int exifOrientation) {

        switch (exifOrientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                return 90;
            case ExifInterface.ORIENTATION_ROTATE_180:
                return 180;
            case ExifInterface.ORIENTATION_ROTATE_270:
                return 270;
        }
        return 0;
    }

    private File createImageFile() throws IOException {

        // 이미지 파일 이름 ( profileImage_{시간}_ )
        String timeStamp = new SimpleDateFormat("HHmmss").format(new Date());
        String imageFileName = "profileImage_" + timeStamp + "_";

        // 이미지가 저장될 폴더 이름
        storageDir = new File(Environment.getExternalStorageDirectory() + "/ProfileImage/");

        // 프로필 이미지 바꿀때 안에 있는 파일 지워줌. 용량 차지하지 못하게 한다.
        if (storageDir.exists()) {
            File[] fileList = storageDir.listFiles();
            for (int i = 0; i < fileList.length; i++) {
                if (fileList.length == 0) {
                    break;
                }
                fileList[i].delete();
                Toast.makeText(ProfileEdit.this, "파일삭제해씀", Toast.LENGTH_SHORT).show();
            }
            //storageDir.delete();
        } else if (!storageDir.exists()) {
            storageDir.mkdirs();
            Toast.makeText(ProfileEdit.this, "디렉토리만들겨", Toast.LENGTH_SHORT).show();
        }

        // 빈 파일 생성
        File image = File.createTempFile(imageFileName, ".jpg", storageDir);
        //이미지경로저장
        profileImgPath = image.getAbsolutePath();

        return image;
    }

    private class SessionCallback implements ISessionCallback {
        @Override
        public void onSessionOpened() {
            UserManagement.getInstance().me(new MeV2ResponseCallback() {
                @Override
                public void onFailure(ErrorResult errorResult) {
                    Log.d("카카오", "onFailure");
                    int result = errorResult.getErrorCode();

                    if (result == ApiErrorCode.CLIENT_ERROR_CODE) {
                        Toast.makeText(getApplicationContext(), "네트워크 연결이 불안정합니다. 다시 시도해 주세요.", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(getApplicationContext(), "로그인 도중 오류가 발생했습니다: " + errorResult.getErrorMessage(), Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onSessionClosed(ErrorResult errorResult) {
                    Log.d("카카오", "onSessionClosed");
                    Toast.makeText(getApplicationContext(), "세션이 닫혔습니다. 다시 시도해 주세요: " + errorResult.getErrorMessage(), Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onSuccess(MeV2Response result) {
                    Log.d("카카오", "onSuccess");
                    String name = result.getNickname();
                    String profileImgPath = result.getProfileImagePath();
                    String description = "다이어리 설명을 입력해 보세요.";
                    int kakaoLogin = 0; //카카오톡 로그인이 아니면 0, 나이정보가 없거나 미성년자면 1, 성인이면2.
                    //성인여부 검사
                    String age = String.valueOf(result.getKakaoAccount().getAgeRange());
                    if (!age.equals("null") && !age.equals("15~19")) {
                        kakaoLogin = 2;
                    } else {
                        kakaoLogin = 1;
                    }

                    //디비에 저장
                    DbOpenHelper dbOpenHelper = new DbOpenHelper(ProfileEdit.this);
                    dbOpenHelper.openUser();
                    dbOpenHelper.upgradeUserHelper();
                    dbOpenHelper.createUserHelper();
                    dbOpenHelper.insertUserColumn(name, profileImgPath, description, kakaoLogin);
                    dbOpenHelper.close();

                    Intent intent = new Intent(ProfileEdit.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                }
            });
        }

        @Override
        public void onSessionOpenFailed(KakaoException e) {
            Log.d("카카오", "onSessionOpenFailed");
            Toast.makeText(ProfileEdit.this, "로그인 도중 오류가 발생했습니다. 인터넷 연결을 확인해주세요: " + e.toString(), Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    public void onBackPressed() {

        long currentTime = System.currentTimeMillis();
        long getTime = currentTime - backbtnTime;

        if (getTime >= 0 && getTime < 500) {
            finish();
        } else {
            super.onBackPressed();
            backbtnTime = currentTime;
        }
    }


}
