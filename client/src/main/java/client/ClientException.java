package client;

/**
 * Indicates there was an error connecting to the database
 */
public class ClientException extends Exception{
    public ClientException(String message) {
        super(message);
    }
}
