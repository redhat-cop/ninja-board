package com.redhat.services.ninja.entity;

import javax.json.bind.annotation.JsonbTransient;
import java.time.LocalDateTime;
import java.util.*;

public class Period implements Comparable<Period>, Identifiable<String> {
    private String name;
    private LocalDateTime cumulatedOn = LocalDateTime.now();

    private Map<String, Record> records = new HashMap<>();

    public Period() {
    }

    public Period(String name, Map<String, Record> records) {
        this.name = name;
        setRecords(records);
    }

    private static Map<String, Record> sort(Map<String, Record> records) {
        Map<String, Record> newRecords = new HashMap<>();

        records.entrySet().stream()
                .sorted(Map.Entry.comparingByValue())
                .forEachOrdered(entry -> newRecords.put(entry.getKey(), entry.getValue()));
        return newRecords;
    }

    @Override
    @JsonbTransient
    public String getIdentifier() {
        return name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LocalDateTime getCumulatedOn() {
        return cumulatedOn;
    }

    public void setCumulatedOn(LocalDateTime cumulatedOn) {
        this.cumulatedOn = cumulatedOn;
    }

    public Map<String, Record> getRecords() {
        return Collections.unmodifiableMap(records);
    }

    public void setRecords(Map<String, Record> records) {
        this.records = sort(records);
    }

    public void record(Scorecard... scorecards) {
        Arrays.stream(scorecards).forEach(scorecard -> {
            Record record = new Record();
            record.setLevel(scorecard.getLevel());
            record.setScore(scorecard.getTotal());

            records.put(scorecard.getUsername(), record);
        });

        records = sort(records);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Period period = (Period) o;
        return name.equals(period.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    @Override
    public int compareTo(Period period) {
        Objects.requireNonNull(period, "Cannot compare with null value");

        return -this.name.compareTo(period.name);
    }
}
