package com.lms.common.utils;

import lombok.Setter;

import java.util.ArrayList;
import java.util.List;



public class OptionValue {

    public String option;
    public String value;
    public String delimeter = null;

    public void clear() {

        option = null;
        value = null;
        delimeter = null;
    }

    public static OptionValue get(String input, final String delim) {

        if (input.contains(delim)) {
            String[] values = input.split(delim);
            OptionValue value = new OptionValue();

            value.option = values[0];
            value.value = values[1];
            return value;
        }
        return null;

    }

    public List<String> getOptions() {

        List<String> options = new ArrayList<String>();
        if ((this.value).isEmpty()&& !(this.option).isEmpty()) {
            options.add(this.option);
        } else if (!(this.value).isEmpty() && (this.option).isEmpty()) {

            options.add(this.value);
        } else if (!(this.option).isEmpty() && !(this.value).isEmpty()) {
            StringBuilder optionBuilder = new StringBuilder();

            if (this.delimeter!= null && this.delimeter.equals(ShellExecutor.EQAUL)) {
                optionBuilder.append(this.option);
                optionBuilder.append(this.delimeter);
                optionBuilder.append(this.value);
                options.add(optionBuilder.toString().trim().replace(" ", ""));
            } else {
                options.add(this.option);
                options.add(this.value);
            }

        }
        return options;
    }
}