package client;

//import chess.ChessGame;
import com.google.gson.Gson;

//import io.javalin.http.Context;

import model.*;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;


public class ServerFacade {

    private final HttpClient client = HttpClient.newHttpClient();
    private final String serverUrl;
    private String auth = null;

    public ServerFacade(String url) {
        serverUrl = url;
    }

    public void clear() throws ClientException {
        var request = buildRequest("DELETE", "/db", null);
        sendRequest(request);
        auth = null;
    }

    public void register(UserData user) throws ClientException {
        try {
            var request = buildRequest("POST", "/user", user);
            var response = sendRequest(request);
            LoginReturn lr = handleResponse(response, LoginReturn.class);
            if (lr != null) {
                auth = lr.authToken;
            }
        } catch (ClientException e) {
            throw new ClientException("could not register user");
        }
    }

    public void login(UserData user) throws ClientException {
        try {
            var request = buildRequest("POST", "/session", user);
            var response = sendRequest(request);
            LoginReturn lr =  handleResponse(response, LoginReturn.class);
            if (lr != null) {
                auth = lr.authToken;
            }
        } catch (ClientException e) {
            throw new ClientException("could not log in");
        }
    }

    public void logout() throws ClientException {
        var request = buildRequest("DELETE", "/session", null);
        sendRequest(request);
        auth = null;
    }

    public GameList listGames() throws ClientException {
        try {
            var request = buildRequest("GET", "/game", null);
            var response = sendRequest(request);
            return handleResponse(response, GameList.class);
        } catch (ClientException e) {
            throw new ClientException(e.getMessage());
        }
    }

    public GameIDs createGame(GameName name) throws ClientException {
        var request = buildRequest("POST", "/game", name);
        var response = sendRequest(request);
        return handleResponse(response, GameIDs.class);
    }

    public ErrorObject joinGame(JoinGameData info) throws ClientException {
        var request = buildRequest("PUT", "/game", info);
        var response = sendRequest(request);
        return handleResponse(response, ErrorObject.class);
    }

    private HttpRequest buildRequest(String method, String path, Object body) {
        var request = HttpRequest.newBuilder()
                .uri(URI.create(serverUrl + path))
                .method(method, makeRequestBody(body));
        if (auth != null) {
            request.setHeader("Authorization", auth);
        }
        return request.build();
    }

    private HttpRequest.BodyPublisher makeRequestBody(Object request) {
        if (request != null) {
            return HttpRequest.BodyPublishers.ofString(new Gson().toJson(request));
        } else {
            return HttpRequest.BodyPublishers.noBody();
        }
    }

    private HttpResponse<String> sendRequest(HttpRequest request) throws ClientException {
        try {
            return client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (Exception ex) {
            throw new ClientException(ex.getMessage());
        }
    }

    private <T> T handleResponse(HttpResponse<String> response, Class<T> responseClass) throws ClientException {
        var status = response.statusCode();
        if (!isSuccessful(status)) {
            var body = response.body();
            if (body != null) {
                ErrorObject error = new Gson().fromJson(response.body(), ErrorObject.class);
                throw new ClientException(error.message);
            }

            throw new ClientException("other failure: " + status);
        }

        if (responseClass != null) {
            return new Gson().fromJson(response.body(), responseClass);
        }

        return null;
    }

    private boolean isSuccessful(int status) {
        return status / 100 == 2;
    }
}
