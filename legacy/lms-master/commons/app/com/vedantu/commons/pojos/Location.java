package com.vedantu.commons.pojos;

import play.data.validation.Constraints.Required;

public class Location {

    public Location() {

        super();
    }

    public Location(String country, String state, String city) {

        super();
        this.country = country;
        this.state = state;
        this.city = city;
    }

    @Required
    public String country;

    @Required
    public String state;

    @Required
    public String city;

    public String validate() {

        if (null == country) {
            return "country missing";
        }
        if (null == state) {
            return "state missing";
        }
        if (null == city) {
            return "city missing";
        }
        return null;
    }

    @Override
    public String toString() {

        StringBuilder builder = new StringBuilder();
        builder.append("Location [country=");
        builder.append(country);
        builder.append(", state=");
        builder.append(state);
        builder.append(", city=");
        builder.append(city);
        builder.append("]");
        return builder.toString();
    }

}
