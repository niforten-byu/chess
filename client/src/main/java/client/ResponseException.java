package client;

/**
 * custom exception class for handling HTTP network errors
 * allows client to understand which HTTP status code caused an error
 */

public class ResponseException extends Exception {
    private final int statusCode;

    /**
     * Constructor
     * @param statusCode HTTP status code returned by the server (ex. 500)
     * @param status readable error message (ex."Error: already taken")
     */
    public ResponseException(int statusCode, String status) {
        super(status); // pass message to standard Java Exception class
        this.statusCode = statusCode;
    }

    public int statusCode() {
        return statusCode;
    }
}
