package com.lms.user.vedantu.user.pojo;


import javax.validation.constraints.NotBlank;

public class UserBasicInfo {
    @NotBlank(message = "Email is required")
    private String email;
    @NotBlank(message = "First name is required")
    public String firstName;
    public String lastName;
    public String contactNumber;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email.toLowerCase();
    }

    public String validate() {
        if (email==null) {
            return "email missing";
        }
        if (firstName==null) {
            return "firstName missing";
        }
        return null;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("UserBasicInfo [email=");
        builder.append(email);
        builder.append(", firstName=");
        builder.append(firstName);
        builder.append(", lastName=");
        builder.append(lastName);
        builder.append(", contactNumber=");
        builder.append(contactNumber);
        builder.append("]");
        return builder.toString();
    }
}
