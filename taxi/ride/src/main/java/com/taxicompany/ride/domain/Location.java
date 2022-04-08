package com.taxicompany.ride.domain;

public class Location {

    private double longitude;

    private double latitude;

    private String hint;

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public String getHint() {
        return hint;
    }

    public void setHint(String hint) {
        this.hint = hint;
    }

    @Override
    public int hashCode() {
        return Double.hashCode(longitude + latitude);
    }

    @Override
    public boolean equals(Object obj) {

        if (!(obj instanceof Location)) {
            return false;
        }

        final var other = (Location) obj;
        if (longitude != other.getLongitude()) {
            return false;
        }
        return latitude == other.getLatitude();

    }

}
