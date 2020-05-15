package com.redhat.services.ninja.entity;

import javax.json.bind.annotation.JsonbTransient;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class Scorecard implements Identifiable<String>{
    private String username;
    private Map<String, Integer> pointMap;

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

    public int increment(String category, int incrementBy) {
        return getNonNullMap().merge(category, incrementBy, Integer::sum);
    }

    public Map<String, Integer> getPointMap() {
        return Collections.unmodifiableMap(getNonNullMap());
    }

    public void setPointMap(Map<String, Integer> pointMap) {
        this.pointMap = pointMap;
    }

    public int getTotal(){
        return getNonNullMap().values().stream().mapToInt(i -> i).sum();
    }
    
    private synchronized Map<String,Integer> getNonNullMap(){
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