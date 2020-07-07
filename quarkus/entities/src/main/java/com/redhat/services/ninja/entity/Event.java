package com.redhat.services.ninja.entity;

import javax.json.bind.annotation.JsonbPropertyOrder;
import javax.json.bind.annotation.JsonbTransient;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

@JsonbPropertyOrder({"timestamp", "user", "type", "description"})
public class Event {
    private LocalDateTime timestamp = LocalDateTime.now();
    private String user;
    private String type;
    private String description;

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @JsonbTransient
    public Optional<Type> getKnownType() {
        return Type.fromString(type);
    }

    @JsonbTransient
    public void setKnownType(Type type) {
        this.type = type.toString();
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public enum Type {
        SUCCESSFUL_REGISTRATION("User %s was registered successfully."),
        FAILED_LDAP_REGISTRATION("Failed to register User %s."),
        POINT_INCREMENT("User %s score changed by %d from %d to %d in category %s for %s.");

        private static final Function<String, String> NAME_NORMALIZER = name -> name.toLowerCase().replace('_', ' ');
        private static final Map<String, Type> TYPE_MAP = new HashMap<>();

        static {
            for (Type type : Type.values()) {
                TYPE_MAP.put(type.toString(), type);
            }
        }

        private final String format;

        Type(String format) {
            this.format = format;
        }

        public static Optional<Type> fromString(String type) {
            var normalizedName = NAME_NORMALIZER.apply(type);

            return Optional.ofNullable(TYPE_MAP.get(normalizedName));
        }

        public Event createEvent(Object... parameters) {
            Event event = new Event();
            event.setKnownType(this);
            event.description = String.format(format, parameters);
            return event;
        }

        @Override
        public String toString() {
            return NAME_NORMALIZER.apply(this.name());
        }
    }
}