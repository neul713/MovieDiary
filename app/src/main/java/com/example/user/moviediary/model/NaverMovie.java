package com.example.user.moviediary.model;

import java.util.List;

public class NaverMovie {

    /**
     * lastBuildDate : Wed, 18 Dec 2019 20:14:39 +0900
     * total : 1
     * start : 1
     * display : 1
     * items : [{"title":"<b>겨울왕국<\/b>","link":"https://movie.naver.com/movie/bi/mi/basic.nhn?code=100931","image":"https://ssl.pstatic.net/imgmovie/mdi/mit110/1009/100931_P98_151621.jpg","subtitle":"Frozen","pubDate":"2013","director":"크리스 벅|제니퍼 리|","actor":"크리스틴 벨|이디나 멘젤|","userRating":"9.12"}]
     */

    private String lastBuildDate;
    private int total;
    private int start;
    private int display;
    private List<ItemsBean> items;

    public String getLastBuildDate() {
        return lastBuildDate;
    }

    public void setLastBuildDate(String lastBuildDate) {
        this.lastBuildDate = lastBuildDate;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public int getStart() {
        return start;
    }

    public void setStart(int start) {
        this.start = start;
    }

    public int getDisplay() {
        return display;
    }

    public void setDisplay(int display) {
        this.display = display;
    }

    public List<ItemsBean> getItems() {
        return items;
    }

    public void setItems(List<ItemsBean> items) {
        this.items = items;
    }

    public static class ItemsBean {
        /**
         * title : <b>겨울왕국</b>
         * link : https://movie.naver.com/movie/bi/mi/basic.nhn?code=100931
         * image : https://ssl.pstatic.net/imgmovie/mdi/mit110/1009/100931_P98_151621.jpg
         * subtitle : Frozen
         * pubDate : 2013
         * director : 크리스 벅|제니퍼 리|
         * actor : 크리스틴 벨|이디나 멘젤|
         * userRating : 9.12
         */

        private String title;
        private String link;
        private String image;
        private String subtitle;
        private String pubDate;
        private String director;
        private String actor;
        private String userRating;

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getLink() {
            return link;
        }

        public void setLink(String link) {
            this.link = link;
        }

        public String getImage() {
            return image;
        }

        public void setImage(String image) {
            this.image = image;
        }

        public String getSubtitle() {
            return subtitle;
        }

        public void setSubtitle(String subtitle) {
            this.subtitle = subtitle;
        }

        public String getPubDate() {
            return pubDate;
        }

        public void setPubDate(String pubDate) {
            this.pubDate = pubDate;
        }

        public String getDirector() {
            return director;
        }

        public void setDirector(String director) {
            this.director = director;
        }

        public String getActor() {
            return actor;
        }

        public void setActor(String actor) {
            this.actor = actor;
        }

        public String getUserRating() {
            return userRating;
        }

        public void setUserRating(String userRating) {
            this.userRating = userRating;
        }
    }
}
