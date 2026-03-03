package model;

public class loginReturn {
    public String username;
    public String authToken;

    public loginReturn(String un, String t) {
        username = un;
        authToken = t;
    }
}
