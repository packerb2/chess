package model;

public class LoginReturn {
    public String username;
    public String authToken;

    public LoginReturn(String un, String t) {
        username = un;
        authToken = t;
    }
}
