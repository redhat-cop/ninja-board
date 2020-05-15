package com.redhat.services.ninja.data;

import com.redhat.services.ninja.entity.Event;
import com.redhat.services.ninja.entity.Scorecard;
import com.redhat.services.ninja.entity.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

public class Database {
    private LocalDateTime createdOn = LocalDateTime.now();
    private Set<User> users = Set.of();
    private Set<Scorecard> scorecards = Set.of();
    private List<Event> events = List.of();

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

    public List<Event> getEvents() {
        return events;
    }

    public void setEvents(List<Event> events) {
        this.events = events;
    }

    public LocalDateTime getCreatedOn() {
        return createdOn;
    }
}
