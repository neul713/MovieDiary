package com.example.user.moviediary.model;

public class Like {
    private int no;
    private int mv_id;
    private String detailImage;
    private String detailTitle;

    public Like(int no, int mv_id, String detailImage, String detailTitle) {
        this.no = no;
        this.mv_id = mv_id;
        this.detailImage = detailImage;
        this.detailTitle = detailTitle;
    }

    @Override
    public String toString() {
        return "Like{" +
                "no=" + no +
                ", mv_id=" + mv_id +
                ", detailImage=\"" + detailImage + "\"" +
                ", detailTitle=\"" + detailTitle + "\"" +
                "}";
    }


}
