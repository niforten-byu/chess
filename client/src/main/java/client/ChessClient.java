package client;

import java.util.Arrays;
import model.AuthData;
import model.UserData;
import model.LoginRequest;
import model.CreateGameRequest;
import model.JoinGameRequest;
import model.GameData;

public class ChessClient {
    private final ServerFacade server;
    private UserState state = UserState.LOGGED_OUT;
    private AuthData currentAuthentication = null;
    private GameData[] gamelist = new GameData[0];

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

            // get user's command (first token), default to help
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
                    case "logout" -> logout();
                    case "create" -> createGame(params);
                    case "list" -> listGames();
                    case "join" -> joinGame(params);
                    case "observe" -> observeGame(params);
                    case "quit" -> "quit";
                    default -> help();
                };
            }
        } catch (Exception e) {
            return e.getMessage() + "\n";
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
        throw new ResponseException(400, "Expected: <USERNAME> <PASSWORD> <EMAIL>\n");
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
        throw new ResponseException(400, "Expected: <USERNAME> <PASSWORD>\n");
    }

    /**
     * log out existing user
     */
    public String logout() throws ResponseException {
        server.logout(currentAuthentication.authToken());
        currentAuthentication = null;
        state = UserState.LOGGED_OUT;
        return "Successfully logged out.\n";
    }

    /**
     * create new game
     */
    public String createGame(String[] params) throws ResponseException {
        if (params.length >= 1) {
            // rejoin  parameters in case the game name has spaces
            String gameName = String.join(" ", params);
            server.createGame(new CreateGameRequest(gameName), currentAuthentication.authToken());
            return String.format("Successfully created game: '%s'.\n", gameName);
        }
        throw new ResponseException(400, "Expected: <NAME>");
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
