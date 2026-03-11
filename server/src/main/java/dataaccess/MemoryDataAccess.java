package dataaccess;

import model.*;
import org.mindrot.jbcrypt.BCrypt;

import java.util.HashMap;

public class MemoryDataAccess implements DataAccess {
    final private HashMap<String, String> users = new HashMap<>();
    final private HashMap<String, String> games = new HashMap<>();
    final private HashMap<String, String> auths = new HashMap<>();

    @Override
    public void storeUserPassword(String username, String clearTextPassword) {
        String hashedPassword = BCrypt.hashpw(clearTextPassword, BCrypt.gensalt());
        users.put(username, hashedPassword);
    }

    @Override
    public boolean verifyUser(String username, String providedClearTextPassword) {
        var hashedPassword = users.get(username);
        return BCrypt.checkpw(providedClearTextPassword, hashedPassword);
    }
}
