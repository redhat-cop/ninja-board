package com.redhat.services.ninja.entity;

import javax.json.bind.annotation.JsonbTransient;
import java.util.Objects;

public class Level implements Identifiable<String>, Comparable<Level> {

    private String name;
    private int minimumPoint;

    @Override
    @JsonbTransient
    public String getIdentifier() {
        return getName();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name.trim().toUpperCase();
    }

    public int getMinimumPoint() {
        return minimumPoint;
    }

    public void setMinimumPoint(int minimumPoint) {
        this.minimumPoint = minimumPoint;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Level level = (Level) o;
        return name.equals(level.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    @Override
    public int compareTo(Level level) {
        return Integer.compare(this.minimumPoint, level.minimumPoint);
    }

    public enum KNOWN_LEVEL {
        RED(40), ZERO(0), GREY(20), BLUE(5), BLACK(75);

        private final Level level;

        KNOWN_LEVEL(int minimumPoint) {
            this.level = new Level();
            this.level.name = this.name();
            this.level.minimumPoint = minimumPoint;
        }

        public Level getLevel() {
            return level;
        }
    }
}