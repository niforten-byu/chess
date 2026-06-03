package client;

import java.util.Arrays;
import model.AuthData;
import model.UserData;
import model.LoginRequest;

public class ChessClient {
    private final ServerFacade server;
    private UserState state = UserState.LOGGED_OUT;
    private AuthData currentAuthentication = null;

    public ChessClient(int port) {
        server = new ServerFacade(port);
    }

    /**
     * evaluate user's input and call correct method
     * @param input users input from terminal
     * @return string result to print to the console
     */
    public String inputHelper(String input) {
        try {
            // turn user input into multiple tokens
            String[] inputTokens = input.toLowerCase().split(" ");

            // get user's command (first token)
            String cmd;
            if (inputTokens.length > 0) {
                cmd = inputTokens[0];
            } else {
                cmd = "help";
            }
            // get parameter(s) (everything after the first token)
            String[] params = Arrays.copyOfRange(inputTokens, 1, inputTokens.length);

            // call method based user's state
            if (state == UserState.LOGGED_OUT) {
                return switch (cmd) {
                    case "register" -> register(params);
                    case "login" -> login(params);
                    case "quit" -> "quit";
                    default -> help();
                };
            }
            else {
                return switch (cmd) {
                    case "quit" -> "quit";
                    default -> help();
                };
            }
        } catch (Exception e) {
            return e.getMessage();
        }
    }

    /**
     * register a new user to database
     */
    public String register(String[] params) throws ResponseException {
        if(params.length == 3) {
            UserData user = new UserData(params[0], params[1], params[2]);
            currentAuthentication = server.register(user);
            state = UserState.LOGGED_IN;
            return String.format("Successfully registered and logged in as %s.\n", currentAuthentication.username());
        }
        throw new ResponseException(400, "Needed to register: <USERNAME> <PASSWORD> <EMAIL>\n");
    }

    /**
     * logs in existing user
     */
    public String login(String[] params) throws ResponseException {
        if(params.length == 2) {
            LoginRequest request = new LoginRequest(params[0], params[1]);
            currentAuthentication = server.login(request);
            state = UserState.LOGGED_IN;
            return String.format("Successfully logged in as %s.\n", currentAuthentication.username());
        }
        throw new ResponseException(400, "Needed for login: <USERNAME> <PASSWORD>\n");
    }

    /**
     * return help menu to user based on their current state
     */
    public String help() {
        if (state == UserState.LOGGED_OUT) {
            return """
                    Please type in your command, followed by any information needed (items enclosed by <>)
                    Please separate the command and each piece of information by spaces
                    - register <USERNAME> <PASSWORD> <EMAIL> - register a chess account
                    - login <USERNAME> <PASSWORD> - login to your account
                    - quit - quit out of the server
                    - help - display these instructions again
                    """;
        }
        return """
                Please type in your command, followed by any information needed (items enclosed by <>)
                Please separate the command and each piece of information by spaces
                - create <NAME> - create a new game with said NAME
                - list - list all games you have
                - join <ID> [WHITE|BLACK] - join a game as selected color
                - observe <ID> - watch a game in progress
                - logout - logout of the server when done
                - quit - quit out of the server
                - help - display these instructions again
                """;
    }

    public UserState getState() {
        return state;
    }
}
