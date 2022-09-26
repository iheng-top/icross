package com.example.icross;

import org.litepal.crud.LitePalSupport;

public class HistoryItem extends LitePalSupport {
    String time;
    int score;

    public HistoryItem(String time, int score) {
        this.time = time;
        this.score = score;
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
        return "HistoryItem{" +
                "time='" + time + '\'' +
                ", score=" + score +
                '}';
    }
}
