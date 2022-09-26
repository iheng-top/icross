package com.example.icross;

public class RankListItem {
    private int num;
    private String name;
    private String location;
    private String time;
    private int score;

    public int getNum() {
        return num;
    }

    public void setNum(int num) {
        this.num = num;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    @Override
    public String toString() {
        return "RankListItem{" +
                "num=" + num +
                ", name='" + name + '\'' +
                ", location='" + location + '\'' +
                ", time=" + time +
                ", score=" + score +
                '}';
    }
}
