package com.redhat.services.ninja.entity;

import java.util.Objects;

public class Category {
    private String key;
    private int value;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Category category = (Category) o;
        return key.equals(category.key);
    }

    @Override
    public int hashCode() {
        return Objects.hash(key);
    }
}
