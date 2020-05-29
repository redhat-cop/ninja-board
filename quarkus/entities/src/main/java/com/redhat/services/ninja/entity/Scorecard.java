package com.redhat.services.ninja.entity;

import javax.json.bind.annotation.JsonbProperty;
import javax.json.bind.annotation.JsonbTransient;
import java.util.*;

public class Scorecard implements Identifiable<String> {
    private String username;
    private Map<String, Integer> pointMap;
    private String level = "ZERO";
    private int pointsToNextLevel = 0;
    private String nextLevel = "";

    @Override
    @JsonbTransient
    public String getIdentifier() {
        return getUsername();
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Map<String, Integer> getPointMap() {
        return Collections.unmodifiableMap(getNonNullMap());
    }

    public int getTotal() {
        return getNonNullMap().values().stream().mapToInt(i -> i).sum();
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public int getPointsToNextLevel() {
        return pointsToNextLevel;
    }

    public void setPointsToNextLevel(int pointsToNextLevel) {
        this.pointsToNextLevel = pointsToNextLevel;
    }

    public String getNextLevel() {
        return nextLevel;
    }

    public void setNextLevel(String nextLevel) {
        this.nextLevel = nextLevel;
    }

    public void computeLevel(SortedSet<Level> levels) {
        int total = getTotal();

        new TreeSet<>(levels).descendingSet().stream().filter(l -> l.getMinimumPoint() <= total).findFirst()
                .ifPresent(l -> level = l.getName());

        levels.stream().filter(l -> l.getMinimumPoint() > total).findFirst()
                .ifPresent(l -> {
                    nextLevel = l.getName();
                    pointsToNextLevel = l.getMinimumPoint() - total;
                });
    }

    public int increment(String category, int incrementBy) {
        return getNonNullMap().merge(category, incrementBy, Integer::sum);
    }

    public void setPointMap(Map<String, Integer> pointMap) {
        this.pointMap = pointMap;
    }

    private synchronized Map<String, Integer> getNonNullMap() {
        pointMap = Objects.requireNonNullElseGet(pointMap, HashMap::new);
        return pointMap;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Scorecard scorecard = (Scorecard) o;
        return username.equals(scorecard.username);
    }

    @Override
    public int hashCode() {
        return Objects.hash(username);
    }
}