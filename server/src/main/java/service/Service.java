package service;

import dataaccess.DataAccess;
import dataaccess.UserDataAccess;

public class Service {

    private DataAccess data = new UserDataAccess();

    public Service(DataAccess data) {
        this.data = data;
    }

    public void clear() {
        data.deleteUsers();
    }
}
