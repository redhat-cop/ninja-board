package com.redhat.services.ninja.entity;

import java.util.Objects;

public class Record implements Comparable<Record> {
    private String level;
    private int score;

    public Record() {
    }

    public Record(String level, int score) {
        this.level = level;
        this.score = score;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    @Override
    public int compareTo(Record record) {
        Objects.requireNonNull(record, "Must not be null");

        return -Integer.compare(this.score, record.score);
    }
}
