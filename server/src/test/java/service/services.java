package service;

import model.*;
import dataaccess.DataAccess;

import java.util.Collection;

public class service {

    private final DataAccess dataAccess;

    public service(DataAccess dataAccess) {
        this.dataAccess = dataAccess;
    }

    public void clear() {
        DataAccess.deleteUsers(userData);
    }
}
