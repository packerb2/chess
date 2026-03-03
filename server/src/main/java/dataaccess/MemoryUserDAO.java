package dataaccess;

import model.GameData;
import model.UserData;

import java.util.ArrayList;

public class MemoryUserDAO implements UserDAO {

    public ArrayList<UserData> data = new ArrayList<>();

    @Override
    public UserData getUser(UserData info) {
        if (data == null) {
            return null;
        }
        for (UserData person : data) {
            if (person.username().equals(info.username())) {
                return person;
            }
        }
//        if (data.contains(info)) {
//            return info;
//        }
        return null;
    }

    @Override
    public void addUser(UserData info) {
        data.add(info);
    }

    @Override
    public void deleteUsers() {
        if (data != null) {
            data.clear();
        }
    }
}
