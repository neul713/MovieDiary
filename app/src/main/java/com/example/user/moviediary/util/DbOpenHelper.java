package com.example.user.moviediary.util;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DbOpenHelper {

    private static final String DB_NAME = "Diary.db";
    private static final int VERSION = 1;
    public static SQLiteDatabase mDB;
    private UserTBLHelper userHelper;
    private PostingTBLHelper postingHelper;
    private LikeTBLHelper likeHelper;
    private Context mContext;

    public DbOpenHelper(Context context) {
        this.mContext = context;
    }

    public void close() {
        mDB.close();
    }

    public void createUserHelper() {
        userHelper.onCreate(mDB);
    }

    public void createPostingHelper() {
        postingHelper.onCreate(mDB);
    }

    public void createLikeHelper() {
        likeHelper.onCreate(mDB);
    }

    public void upgradeUserHelper() {
        userHelper.onUpgrade(mDB, VERSION, VERSION);
    }

    public void upgradePostingHelper() {
        postingHelper.onUpgrade(mDB, VERSION, VERSION);
    }

    public void upgradeLikeHelper() {
        likeHelper.onUpgrade(mDB, VERSION, VERSION);
    }

    public DbOpenHelper openUser() throws SQLException {
        userHelper = new UserTBLHelper(mContext, DB_NAME, null, VERSION);
        mDB = userHelper.getWritableDatabase();
        return this;
    }

    public DbOpenHelper openPosting() throws SQLException {
        postingHelper = new PostingTBLHelper(mContext, DB_NAME, null, VERSION);
        mDB = postingHelper.getWritableDatabase();
        return this;
    }

    public DbOpenHelper openLike() throws SQLException {
        likeHelper = new LikeTBLHelper(mContext, DB_NAME, null, VERSION);
        mDB = likeHelper.getWritableDatabase();
        return this;
    }

    public Cursor sortColumn(String tbl_name, String sort) {
        Cursor c = mDB.rawQuery("SELECT * FROM " + tbl_name + " ORDER BY " + sort + ";", null);
        return c;
    }

    //유저 테이블 CRUD 모음

    public long insertUserColumn(String name, String profile_img, String diary_desc, int kakao_login) {
        ContentValues values = new ContentValues();
        values.put(DiaryDB.CreateUser.NAME, name);
        values.put(DiaryDB.CreateUser.PROFILE_IMG, profile_img);
        values.put(DiaryDB.CreateUser.DIARY_DESC, diary_desc);
        values.put(DiaryDB.CreateUser.KAKAO_LOGIN, kakao_login);
        return mDB.insert(DiaryDB.CreateUser.USER_TBL, null, values);
    }

    public Cursor selectUserColumns() {
        return mDB.query(DiaryDB.CreateUser.USER_TBL, null, null, null, null, null, null);
    }

    public boolean updateUserColumn(String name, String profile_img, String diary_desc, int kakao_login) {
        ContentValues values = new ContentValues();
        values.put(DiaryDB.CreateUser.NAME, name);
        values.put(DiaryDB.CreateUser.PROFILE_IMG, profile_img);
        values.put(DiaryDB.CreateUser.KAKAO_LOGIN, kakao_login);
        values.put(DiaryDB.CreateUser.DIARY_DESC, diary_desc);
        return mDB.update(DiaryDB.CreateUser.USER_TBL, values, "name=" + "'" + name + "'", null) > 0;

    }

    public void deleteUserColumns(String name) {
        mDB.delete(DiaryDB.CreateUser.USER_TBL, "name=" + "'" + name + "'", null);

    }

    ///////////////////////////////////////////////////
    //포스팅 테이블 CRUD 모음

    public long insertPostingColumn(int mv_id, String title, String poster, String mv_date, String post_date
            , float star, String content) {
        ContentValues values = new ContentValues();
        values.put(DiaryDB.CreatePosting.STAR, star);
        values.put(DiaryDB.CreatePosting.MV_ID, mv_id);
        values.put(DiaryDB.CreatePosting.POST_DATE, post_date);
        values.put(DiaryDB.CreatePosting.MV_DATE, mv_date);
        values.put(DiaryDB.CreatePosting.TITLE, title);
        values.put(DiaryDB.CreatePosting.MV_POSTER, poster);
        values.put(DiaryDB.CreatePosting.CONTENT, content);
        return mDB.insert(DiaryDB.CreatePosting.POSTING_TBL, null, values);
    }

    public Cursor selectPostingColumns() {
        return mDB.query(DiaryDB.CreatePosting.POSTING_TBL, null, null, null, null, null, DiaryDB.CreatePosting.MV_DATE);
    }

    public boolean updatePostingColumn(int mv_id, String title, String poster, String mv_date, String post_date
            , float star, String content) {
        ContentValues values = new ContentValues();
        values.put(DiaryDB.CreatePosting.MV_ID, mv_id);
        values.put(DiaryDB.CreatePosting.TITLE, title);
        values.put(DiaryDB.CreatePosting.MV_POSTER, poster);
        values.put(DiaryDB.CreatePosting.MV_DATE, mv_date);
        values.put(DiaryDB.CreatePosting.POST_DATE, post_date);
        values.put(DiaryDB.CreatePosting.STAR, star);
        values.put(DiaryDB.CreatePosting.CONTENT, content);
        return mDB.update(DiaryDB.CreatePosting.POSTING_TBL, values, "mv_id=" + mv_id, null) > 0;

    }

    public void deletePostingColumns(int mv_id) {
        mDB.delete(DiaryDB.CreatePosting.POSTING_TBL, "mv_id=" + mv_id, null);

    }

    public Cursor searchPostingColumn(String columnName, String str) {
        Cursor c = mDB.rawQuery("SELECT * FROM posting_tbl WHERE " + columnName + " LIKE '%" + str + "%';", null);
        return c;
    }

    public boolean isExistPostingColumn(int mv_id) {
        Cursor c = mDB.rawQuery("SELECT * FROM posting_tbl WHERE mv_id = " + mv_id, null);
        return c.getCount() > 0;
    }


    ///////////////////////////////////////////////////
    //라이크 테이블 CRUD 모음

    public long insertLikeColumn(int mv_id, String title, String mv_poster) {
        ContentValues values = new ContentValues();
        values.put(DiaryDB.CreateLike.MV_ID, mv_id);
        values.put(DiaryDB.CreateLike.TITLE, title);
        values.put(DiaryDB.CreateLike.MV_POSTER, mv_poster);
        return mDB.insert(DiaryDB.CreateLike.LIKE_TBL, null, values);
    }

    public Cursor selectLikeColumns() {
        return mDB.query(DiaryDB.CreateLike.LIKE_TBL, null, null, null, null, null, null);
    }

    public boolean updateLikeColumn(int mv_id, String title, String mv_poster) {
        ContentValues values = new ContentValues();
        values.put(DiaryDB.CreateLike.MV_ID, mv_id);
        values.put(DiaryDB.CreateLike.TITLE, title);
        values.put(DiaryDB.CreateLike.MV_POSTER, mv_poster);
        return mDB.update(DiaryDB.CreateLike.LIKE_TBL, values, "mv_id=" + mv_id, null) > 0;

    }

    public boolean isExistLikeColumn(int mv_id) {
        Cursor c = mDB.rawQuery("SELECT * FROM like_tbl WHERE mv_id = " + mv_id, null);
        return c.getCount() > 0;
    }

    public void deleteLikeColumns(int mv_id) {
        mDB.delete(DiaryDB.CreateLike.LIKE_TBL, "mv_id=" + mv_id, null);

    }

    ///////////////////////////////////////////////////

    //유저테이블
    private class UserTBLHelper extends SQLiteOpenHelper {

        public UserTBLHelper(Context context, String dbName, Object o, int version) {
            super(context, DB_NAME, null, VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(DiaryDB.CreateUser.CREATE_USR);

        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS " + DiaryDB.CreateUser.USER_TBL);
            onCreate(db);
        }

    }

    //포스팅테이블
    private class PostingTBLHelper extends SQLiteOpenHelper {

        public PostingTBLHelper(Context context, String dbName, Object o, int version) {
            super(context, DB_NAME, null, VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(DiaryDB.CreatePosting.CREATE_POSTING);

        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS " + DiaryDB.CreatePosting.POSTING_TBL);
            onCreate(db);
        }

    }

    //라이크 테이블
    private class LikeTBLHelper extends SQLiteOpenHelper {

        public LikeTBLHelper(Context context, String dbName, Object o, int version) {
            super(context, DB_NAME, null, VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(DiaryDB.CreateLike.CREATE_LIKE);

        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS " + DiaryDB.CreateLike.LIKE_TBL);
            onCreate(db);
        }

    }

}
