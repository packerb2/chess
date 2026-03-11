package dataaccess;

import model.*;

public interface DataAccess {
    void storeUserPassword(String username, String clearTextPassword);
    boolean verifyUser(String username, String providedClearTextPassword);
}
