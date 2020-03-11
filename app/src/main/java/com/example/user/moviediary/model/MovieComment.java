package com.example.user.moviediary.model;

public class MovieComment {

    private String id;
    private String coment;
    private String star;
    private String symUp;
    private String symDown;

    public MovieComment(String id, String coment, String star, String symUp, String symDown) {
        this.id = id;
        this.coment = coment;
        this.star = star;
        this.symUp = symUp;
        this.symDown = symDown;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getComent() {
        return coment;
    }

    public void setComent(String coment) {
        this.coment = coment;
    }

    public String getStar() {
        return star;
    }

    public void setStar(String star) {
        this.star = star;
    }

    public String getSymUp() {
        return symUp;
    }

    public void setSymUp(String symUp) {
        this.symUp = symUp;
    }

    public String getSymDown() {
        return symDown;
    }

    public void setSymDown(String symDown) {
        this.symDown = symDown;
    }
}
