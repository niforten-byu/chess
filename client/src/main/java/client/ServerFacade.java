package client;

import com.google.gson.Gson;
import model.AuthData;
import model.UserData;
import model.LoginRequest;
import model.CreateGameRequest;
import model.JoinGameRequest;
import model.GameData;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;

public class ServerFacade {
    private final String serverUrl;
    private final HttpClient httpClient;

    public ServerFacade(int port) {
        this.serverUrl = "http://localhost:" + port;
        this.httpClient = HttpClient.newHttpClient();
    }

    /**
     * Calls server to make a new user
     * @param user   UserData record with username, password, and email
     * @return AuthData containing a new authentication token for registered user
     */
    public AuthData register(UserData user) throws ResponseException {
        var path = "/user";
        // send POST request to "/user" using user as body and get AuthData obj back
        return this.makeRequest("POST", path, user, AuthData.class, null);
    }

    /**
     * Calls server to log in an existing user
     * @param request LoginRequest record with username and password
     * @return AuthData with new authentication token
     */
    public AuthData login(LoginRequest request) throws ResponseException {
        var path = "/session";
        // send POST to "/session" with request as body and get AuthData obj back
        return this.makeRequest("POST", path, request, AuthData.class, null);
    }

    /**
     * Calls server to log out user
     * @param authenticationToken Authorization token of user to log out
     */
    public void logout(String authenticationToken) throws ResponseException {
        var path = "/session";
        // send DELETE to "/session" and provide correct authenticationToken to logout correct user
        this.makeRequest("DELETE", path, null, null, authenticationToken);
    }

    /**
     * Calls server to clear database (used for tests)
     */
    public void clear() throws ResponseException {
        var path = "/db";
        // DELETE request to /db
        this.makeRequest("DELETE", path, null, null, null);
    }

    /**
     * Calls server to make new chess game
     * @param request CreateGameRequest containing new game name
     * @param authenticationToken Authentication token of user
     * @return unique gameID made by database
     */
    public int createGame(CreateGameRequest request, String authenticationToken) throws ResponseException {
        var path = "/game";

        // local record to catch JSON response: {"gameID": 1234}
        record CreateGameResponse(int gameID) {}

        CreateGameResponse response = this.makeRequest("POST", path, request, CreateGameResponse.class, authenticationToken);
        return response.gameID();
    }

    /**
     * Gets all active games from server
     * @param authenticationToken Authentication token of user
     * @return Array of GameData records for all games in database asscociated with user
     */
    public GameData[] listGames(String authenticationToken) throws ResponseException {
        var path = "/game";

        // local record to catch JSON {"games": [...]}
        record ListGamesResponse(GameData[] games) {}

        ListGamesResponse response = this.makeRequest("GET", path, null, ListGamesResponse.class, authenticationToken);
        return response.games();
    }

    /**
     * Join existing game as specific color
     * @param request             JoinGameRequest with color and gameID
     * @param authenticationToken Authentication token of user
     */
    public void joinGame(JoinGameRequest request, String authenticationToken) throws ResponseException {
        var path = "/game";

        // PUT request update game in database with user and color
        this.makeRequest("PUT", path, request, null, authenticationToken);
    }

    /**
     * Make HTTP requests to the server
     * @param method              The desired HTTP method, such as "GET", "POST", "PUT", or "DELETE"
     * @param path                The endpoint path, aka "/user"
     * @param request             The object that needs to be serialized into the request body (null if there is no body)
     * @param responseClass       The type of class to parse the response JSON into (null if no response expected)
     * @param authenticationToken The authorization token (null if not required for method)
     * @return the object needed as expected by the responseClass
     */
    private <T> T makeRequest(String method, String path, Object request,
                              Class<T> responseClass, String authenticationToken) throws ResponseException {
        try{
            // make url
            URI uri = URI.create(serverUrl + path);

            // prep the request builder
            HttpRequest.Builder requestBuilder = HttpRequest.newBuilder().uri(uri);

            // add authorization if not null
            if (authenticationToken != null) {
                requestBuilder.header("authorization", authenticationToken);
            }

            // serialize request body and set HTTP method
            if (request != null) {
                String jsonBody = new Gson().toJson(request);
                requestBuilder.header("Content-Type", "application/json");
                requestBuilder.method(method, HttpRequest.BodyPublishers.ofString(jsonBody));
            }
            else {
                requestBuilder.method(method, HttpRequest.BodyPublishers.noBody());
            }

            // send the request
            HttpResponse<InputStream> response = httpClient.send(requestBuilder.build(), HttpResponse.BodyHandlers.ofInputStream());

            // handle response status codes
            if (response.statusCode() >= 400) {
                // if there is an error, parse JSON error message from server
                Map<String, String> errorResponse = new Gson().fromJson(new InputStreamReader(response.body()), Map.class);
                String errorMessage;
                if (errorResponse != null && errorResponse.containsKey("message")) {
                    errorMessage = errorResponse.get("message");
                } else {
                    errorMessage = "Unknown server error";
                }                throw new ResponseException(response.statusCode(), errorMessage);
            }

            // parse response body if return type was needed
            if (responseClass != null) {
                return new Gson().fromJson(new InputStreamReader(response.body()), responseClass);
            }

            return null;

        }
        catch (IOException | InterruptedException e) {
            throw new ResponseException(500, "Error: Failure to connect to server. " + e.getMessage());
        }
    }

}
