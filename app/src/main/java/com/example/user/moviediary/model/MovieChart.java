package com.example.user.moviediary.model;

public class MovieChart {

    private int rank;
    private String title;
    private String poster;
    private String age;
    private String rsrvRate;
    private String great;
    private String rlsDate;

    public MovieChart(int rank, String title, String poster, String age, String rsrvRate, String great, String rlsDate) {
        this.rank = rank;
        this.title = title;
        this.poster = poster;
        this.age = age;
        this.rsrvRate = rsrvRate;
        this.great = great;
        this.rlsDate = rlsDate;
    }

    public int getRank() {
        return rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getPoster() {
        return poster;
    }

    public void setPoster(String poster) {
        this.poster = poster;
    }

    public String getAge() {
        return age;
    }

    public void setAge(String age) {
        this.age = age;
    }

    public String getRsrvRate() {
        return rsrvRate;
    }

    public void setRsrvRate(String rsrvRate) {
        this.rsrvRate = rsrvRate;
    }

    public String getGreat() {
        return great;
    }

    public void setGreat(String great) {
        this.great = great;
    }

    public String getRlsDate() {
        return rlsDate;
    }

    public void setRlsDate(String rlsDate) {
        this.rlsDate = rlsDate;
    }

    @Override
    public String toString() {
        return "MovieChart{" +
                "rank=" + rank +
                ", title='" + title + '\'' +
                ", poster='" + poster + '\'' +
                ", age='" + age + '\'' +
                ", rsrvRate='" + rsrvRate + '\'' +
                ", great='" + great + '\'' +
                ", rlsDate='" + rlsDate + '\'' +
                '}';
    }
}
