package com.redhat.services.ninja.entity;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

public class Event {
    private LocalDateTime timestamp = LocalDateTime.now();
    private String type;
    private String user;
    private String description;

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public Optional<Type> getKnownType() {
        return Type.fromString(type);
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public enum Type {
        POINT_INCREMENT;

        private static final Function<String, String> NAME_NORMALIZER = name -> name.toLowerCase().replace('_', ' ');
        private static final Map<String, Type> TYPE_MAP = new HashMap<>();

        static {
            for (Type type : Type.values()) {
                TYPE_MAP.put(type.toString(), type);
            }
        }

        public static Optional<Type> fromString(String type) {
            String normalizedName = NAME_NORMALIZER.apply(type);

            return Optional.ofNullable(TYPE_MAP.get(normalizedName));
        }

        @Override
        public String toString() {
            return NAME_NORMALIZER.apply(this.name());
        }
    }
}