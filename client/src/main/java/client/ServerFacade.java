package client;

import com.google.gson.Gson;
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
     * function to make HTTP requests to the server
     *
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
                String errorMessage = errorResponse != null ? errorResponse.get("message") : "Unknown server error";
                throw new ResponseException(response.statusCode(), errorMessage);
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
