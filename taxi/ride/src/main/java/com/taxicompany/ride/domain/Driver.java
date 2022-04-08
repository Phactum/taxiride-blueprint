package com.taxicompany.ride.domain;

public class Driver {

    private String id;

    private String name;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public boolean equals(Object obj) {

        if (!(obj instanceof Driver)) {
            return false;
        }

        final var other = (Driver) obj;
        return id.equals(other.getId());

    }

}
