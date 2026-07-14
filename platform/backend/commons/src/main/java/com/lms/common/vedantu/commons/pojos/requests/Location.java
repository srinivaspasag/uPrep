package com.lms.common.vedantu.commons.pojos.requests;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

@Setter
@Getter
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

    @NotBlank(message = "country should not be null")
    public String country;

    @NotBlank(message = "state should not be null")
    public String state;

    @NotBlank(message = "city should not be null")
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
