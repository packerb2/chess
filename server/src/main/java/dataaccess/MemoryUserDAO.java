package dataaccess;

import model.UserData;

import java.util.ArrayList;

public class MemoryUserDAO implements UserDAO {

    private ArrayList<UserData> data;

    @Override
    public UserData getUser(UserData info) {
        if (data.contains(info)) {
            return info;
        }
        return null;
    }

    @Override
    public void addUser(UserData info) {
        data.add(info);
    }

    @Override
    public void deleteUsers() {
        data.clear();
    }
}
