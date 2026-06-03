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
    private GameData[] gameList = new GameData[0];

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
            String[] inputTokens = input.split(" ");

            // get user's command (first token), default to help
            String cmd;
            if (inputTokens.length > 0 && !inputTokens[0].isEmpty()) {
                cmd = inputTokens[0].toLowerCase(); // turn command into lowercase for switch statement
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
     * list all games associated with user
     */
    public String listGames() throws ResponseException {
        gameList = server.listGames(currentAuthentication.authToken());
        if (gameList.length == 0) {
            return "There are currently no active games.\n";
        }

        StringBuilder builder = new StringBuilder("Active Games:\n");
        for (int i = 0; i < gameList.length; i++) {
            GameData game = gameList[i];
            // white username
            String white;
            if (game.whiteUsername() != null) {
                white = game.whiteUsername();
            } else {
                white = "Empty";
            }

            // black username
            String black;
            if (game.blackUsername() != null) {
                black = game.blackUsername();
            } else {
                black = "Empty";
            }
            builder.append(String.format("  %d. %s (White: %s, Black: %s)\n", i + 1, game.gameName(), white, black));
        }
        return builder.toString();
    }

    /**
     * join the game as specified color, draw the board for them
     */
    public String joinGame(String[] params) throws ResponseException {
        if (params.length == 2) {
            int gameIndex = Integer.parseInt(params[0]) - 1;
            String color = params[1].toUpperCase();

            // validate user input against game list array
            if (gameIndex >= 0 && gameIndex < gameList.length) {
                int realGameID = gameList[gameIndex].gameID();
                server.joinGame(new JoinGameRequest(color, realGameID), currentAuthentication.authToken());

                // draw board
                return String.format("Successfully joined game '%s' as %s.\n(draw board)\n", gameList[gameIndex].gameName(), color);
            }
            throw new ResponseException(400, "Invalid game number. Type 'list' to see valid games.");
        }
        throw new ResponseException(400, "Expected: <GAME_NUMBER> [WHITE or BLACK]");
    }

    /**
     * join the game as an observer, draw the board from white perspective
     */
    public String observeGame(String[] params) throws ResponseException {
        if (params.length == 1) {
            int gameIndex = Integer.parseInt(params[0]) - 1;

            if (gameIndex >= 0 && gameIndex < gameList.length) {
                // draw board
                return String.format("Observing game '%s'.\n(draw board)\n", gameList[gameIndex].gameName());
            }
            throw new ResponseException(400, "Invalid game number. Type 'list' to see valid games.");
        }
        throw new ResponseException(400, "Expected: <GAME_NUMBER>");
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
                - join <ID> [WHITE or BLACK] - join a game as selected color
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
