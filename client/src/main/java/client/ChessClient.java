package client;

import java.util.Arrays;
import com.google.gson.Gson;
import model.*;
import websocket.WebSocketCommunicator;
import websocket.ServerMessageObserver;
import websocket.commands.UserGameCommand;
import websocket.messages.ServerMessage;

public class ChessClient implements ServerMessageObserver {
    private final ServerFacade server;
    private final int serverPort;
    private WebSocketCommunicator ws;
    private UserState state = UserState.LOGGED_OUT;
    private AuthData currentAuthentication = null;
    private GameData[] gameList = new GameData[0];
    private chess.ChessGame.TeamColor playerColor = chess.ChessGame.TeamColor.WHITE;

    public ChessClient(int port) {
        this.serverPort = port;
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
                    case "clear" -> clear(); // for developers only
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
     * secret developer function to wipe the database for testing
     */
    public String clear() throws ResponseException {
        server.clear();
        return "Database completely wiped.\n";
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
            int gameIndex;

            // parse the game id number
            try {
                gameIndex = Integer.parseInt(params[0]) - 1;
            } catch (NumberFormatException e) {
                throw new ResponseException(400, "Error: Expected a number for the game ID.");
            }

            // validate the color
            String color = params[1].toUpperCase();
            if (!color.equals("WHITE") && !color.equals("BLACK")) {
                throw new ResponseException(400, "Error: Team color must be WHITE or BLACK.");
            }

            // validate user input against game list array
            if (gameIndex >= 0 && gameIndex < gameList.length) {
                int realGameID = gameList[gameIndex].gameID();

                // hit HTTP endpoint to claim the spot in the database
                server.joinGame(new JoinGameRequest(color, realGameID), currentAuthentication.authToken());

                // Determine the player's color
                if (color.equals("WHITE")) {
                    playerColor = chess.ChessGame.TeamColor.WHITE;
                } else {
                    playerColor = chess.ChessGame.TeamColor.BLACK;
                }

                // open the WebSocket connection
                try {
                    ws = new WebSocketCommunicator("http://localhost:" + serverPort, this);

                    // create connect command and send it as JSON
                    UserGameCommand connectCmd = new UserGameCommand(UserGameCommand.CommandType.CONNECT, currentAuthentication.authToken(), realGameID);
                    ws.send(new Gson().toJson(connectCmd));

                    // change state so the REPL shows [GAMEPLAY] >>>
                    state = UserState.GAMEPLAY;

                    return String.format("Joined game '%s' as %s.\n", gameList[gameIndex].gameName(), color);
                } catch (Exception e) {
                    throw new ResponseException(500, "WebSocket Error: " + e.getMessage());
                }
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
            int gameIndex;

            // parse the game id number
            try {
                gameIndex = Integer.parseInt(params[0]) - 1;
            } catch (NumberFormatException e) {
                throw new ResponseException(400, "Error: Expected a number for the game ID.");
            }

            if (gameIndex >= 0 && gameIndex < gameList.length) {
                int realGameID = gameList[gameIndex].gameID();

                playerColor = chess.ChessGame.TeamColor.WHITE;

                // open the WebSocket connection
                try {
                    ws = new WebSocketCommunicator("http://localhost:" + serverPort, this);

                    UserGameCommand connectCmd = new UserGameCommand(UserGameCommand.CommandType.CONNECT, currentAuthentication.authToken(), realGameID);
                    ws.send(new Gson().toJson(connectCmd));

                    state = UserState.GAMEPLAY;
                    return String.format("Observing game '%s'.\n", gameList[gameIndex].gameName());
                } catch (Exception e) {
                    throw new ResponseException(500, "WebSocket Error: " + e.getMessage());
                }
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

    @Override
    public void notify(ServerMessage message) {
        switch (message.getServerMessageType()) {
            case LOAD_GAME -> {
                websocket.messages.LoadGameMessage loadMessage = (websocket.messages.LoadGameMessage) message;
                boolean isWhite = (playerColor == chess.ChessGame.TeamColor.WHITE);

                // print the board
                System.out.print("\n\n" + ui.BoardUI.drawBoard(loadMessage.getGame().game().getBoard(), isWhite));
                printCurrentPrompt();
            }
            case NOTIFICATION -> {
                websocket.messages.NotificationMessage notification = (websocket.messages.NotificationMessage) message;
                System.out.print("\n" + ui.EscapeSequences.SET_TEXT_COLOR_GREEN + notification.getMessage() + ui.EscapeSequences.RESET_TEXT_COLOR + "\n");
                printCurrentPrompt();
            }
            case ERROR -> {
                websocket.messages.ErrorMessage error = (websocket.messages.ErrorMessage) message;
                System.out.print("\n" + ui.EscapeSequences.SET_TEXT_COLOR_RED + error.getErrorMessage() + ui.EscapeSequences.RESET_TEXT_COLOR + "\n");
                printCurrentPrompt();
            }
        }
    }

    // helper method to reprint the REPL prompt asynchronously
    private void printCurrentPrompt() {
        System.out.print("\n" + ui.EscapeSequences.SET_TEXT_COLOR_GREEN + "[" + state + "] >>> " + ui.EscapeSequences.RESET_TEXT_COLOR);
    }
}
