package com.redhat.services.ninja.entity;

import java.time.LocalDateTime;
import java.util.*;

public class Database {
    private LocalDateTime createdOn = LocalDateTime.now();
    private Set<User> users = Set.of();
    private Set<Scorecard> scorecards = Set.of();
    private SortedSet<Level> levels;
    private List<Event> events = List.of();
    private SortedSet<Period> history = new TreeSet<>();

    public Database() {
        levels = new TreeSet<>();
        Arrays.stream(Level.KNOWN_LEVEL.values()).map(Level.KNOWN_LEVEL::getLevel).forEach(levels::add);
    }

    public Set<User> getUsers() {
        return users;
    }

    public void setUsers(Set<User> users) {
        this.users = users;
    }

    public Set<Scorecard> getScorecards() {
        return scorecards;
    }

    public void setScorecards(Set<Scorecard> scorecards) {
        this.scorecards = scorecards;
    }

    public SortedSet<Level> getLevels() {
        return levels;
    }

    public void setLevels(SortedSet<Level> levels) {
        this.levels = levels;
    }

    public List<Event> getEvents() {
        return events;
    }

    public void setEvents(List<Event> events) {
        this.events = events;
    }

    public LocalDateTime getCreatedOn() {
        return createdOn;
    }

    public SortedSet<Period> getHistory() {
        return history;
    }

    public void setHistory(SortedSet<Period> history) {
        this.history = history;
    }

    public void setCreatedOn(LocalDateTime createdOn) {
        this.createdOn = createdOn;
    }
}
