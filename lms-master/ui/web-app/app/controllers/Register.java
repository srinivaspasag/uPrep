package controllers;

public class Register extends UIComRegister{
    public static void invalidInvite() {
        render("UIComRegister/invalidInvite.html");
    }

    public static void signup3Steps() {
        render("UIComRegister/signup3Steps.html");
    }

    public static void fbConnect() {
        render("UIComRegister/signup3Steps.html");
    }
}
