package com.example.user.moviediary.util;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.util.Log;
import android.util.LruCache;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.user.moviediary.model.Like;
import com.example.user.moviediary.model.MovieDiary;
import com.google.gson.JsonObject;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class BackupVolley {

    public static final String URL_SEND_REQUEST = "http://mirae08.dothome.co.kr/Register.php";
    private static final String TAG = "BackUpTag";

    private final static String POSTING_LIST = "posting_list";
    private final static String LIKE_LIST = "like_list";
    private final static String USER_DATA = "user_data";

    private Context context;
    private  DbOpenHelper dbOpenHelper;

    private static BackupVolley backupVolley;
    private RequestQueue requestQueue;
    private ImageLoader imageLoader;
    private final LruCache<String, Bitmap> cache = new LruCache<String, Bitmap>(20);

    public static BackupVolley getInstance(Context context) {
        if (backupVolley == null) {
            backupVolley = new BackupVolley(context);
        }
        return backupVolley;
    }

    private BackupVolley(Context context) {
        this.context = context;

        requestQueue = Volley.newRequestQueue(context);
        imageLoader = new ImageLoader(requestQueue, new ImageLoader.ImageCache() {

            @Override
            public Bitmap getBitmap(String url) {
                return cache.get(url);
            }

            @Override
            public void putBitmap(String url, Bitmap bitmap) {
                cache.put(url, bitmap);
            }
        });

    }

    public RequestQueue getRequestQueue() {
        return requestQueue;
    }

    public ImageLoader getImageLoader() {
        return imageLoader;
    }

    //유저에게 제공할 백업 id로 쓰일 난수코드 생성
    public static String getRamdomPassword(int len) {

        char[] charSet = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9'
                , 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N'
                , 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z'};

        int idx = 0;

        StringBuffer sb = new StringBuffer();

        for (int i = 0; i < len; i++) {
            // 36 * 생성된 난수를 Int로 추출 (소숫점제거)
            idx = (int) (charSet.length * Math.random());
            sb.append(charSet[idx]);
        }
        return sb.toString();
    }

    //db의 posting 테이블 like 테이블의 데이터를 모두 가져와 json객체에 저장
    public JSONObject createJasonUserData() {

        JSONObject obj = new JSONObject();
        try {
            JSONArray posting = new JSONArray();//배열이 필요할때

           dbOpenHelper = new DbOpenHelper(context);
            //다이어리 포스팅 리스트 디비에서 얻기
            dbOpenHelper.openPosting();

            //다이어리 객체를 받을 리스트
            Cursor cursor = dbOpenHelper.selectPostingColumns();

            while (cursor.moveToNext()) {

                JSONObject sObject = new JSONObject();//배열 내에 들어갈 json
                int tempMvId = cursor.getInt(cursor.getColumnIndex("mv_id"));
                String tempTitle = cursor.getString(cursor.getColumnIndex("title"));
                String tempPoster = cursor.getString(cursor.getColumnIndex("mv_poster"));
                String tempMovieDate = cursor.getString(cursor.getColumnIndex("mv_date"));
                String tempPostingDate = cursor.getString(cursor.getColumnIndex("post_date"));
                float tempStar = cursor.getFloat(cursor.getColumnIndex("star"));
                String tempContent = cursor.getString(cursor.getColumnIndex("content"));
                //sObject 에 추가
                sObject.put("mv_id", tempMvId);
                sObject.put("title", tempTitle);
                sObject.put("poster", tempPoster);
                sObject.put("movie_date", tempMovieDate);
                sObject.put("posting_date", tempPostingDate);
                sObject.put("star", tempStar);
                sObject.put("content", tempContent);
                //배열에 추가
                posting.put(sObject);
            }


            JSONArray like = new JSONArray();//배열이 필요할때

            //다이어리 포스팅 리스트 디비에서 얻기
            dbOpenHelper.openLike();

            //다이어리 객체를 받을 리스트
            cursor = dbOpenHelper.selectLikeColumns();

            while (cursor.moveToNext()) {

                JSONObject sObject = new JSONObject();//배열 내에 들어갈 json

                int tempNo = cursor.getInt(cursor.getColumnIndex("no"));
                int tempMvId = cursor.getInt(cursor.getColumnIndex("mv_id"));
                String tempTitle = cursor.getString(cursor.getColumnIndex("title"));
                String tempPoster = cursor.getString(cursor.getColumnIndex("mv_poster"));

                //sObject 에 추가
                sObject.put("no", tempNo);
                sObject.put("mv_id", tempMvId);
                sObject.put("title", tempTitle);
                sObject.put("poster", tempPoster);

                //배열에 추가
                like.put(sObject);
            }

            obj.put("like_list", like);//배열을 넣음
            obj.put("posting_list", posting);//배열을 넣음

            //디비 닫기
            dbOpenHelper.close();


        } catch (JSONException e) {
            e.printStackTrace();
            //디비 닫기
            dbOpenHelper.close();
        }


        Log.d("백업", "생성한 json : " + obj.toString());

        return obj;

    }

    public static class RegisterRequest extends StringRequest {

        private Map<String, String> map;
        private String code;
        private JSONObject data;

        public RegisterRequest(String code, JSONObject data, Response.Listener<String> listener) {
            super(Method.POST, URL_SEND_REQUEST, listener, null);

            map = new HashMap<>();
            this.code = code;
            this.data = data;
        }

        @Override
        protected Map<String, String> getParams() throws AuthFailureError {

            map.put("CODE", this.code);
            map.put("DATA", data.toString());

            return map;
        }
    }

    public static class BackupRequest extends StringRequest {

        private static final String URL = "http://mirae08.dothome.co.kr/backup.php";
        private Map<String, String> map;
        private String code;


        public BackupRequest(String code, Response.Listener<String> listener) {
            super(Method.POST, URL, listener, null);

            map = new HashMap<>();
            this.code = code;

        }

        @Override
        protected Map<String, String> getParams() throws AuthFailureError {

            map.put("CODE", this.code);

            return map;
        }
    }

}


