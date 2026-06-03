package client;

import java.util.Arrays;

public class ChessClient {
    private final ServerFacade server;
    private UserState state = UserState.LOGGED_OUT;

    public ChessClient(int port) {
        server = new ServerFacade(port);
    }

    /**
     * evaluate user's input and call correct method
     * @param input users input from terminal
     * @return method...
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
     * return help menu to user based on their current state
     */
    public String help() {
        if (state == UserState.LOGGED_OUT) {
            return """
                    - register <USERNAME> <PASSWORD> <EMAIL> - to create an account
                    - login <USERNAME> <PASSWORD> - to play chess
                    - quit - playing chess
                    - help - with possible commands
                    """;
        }
        return """
                - create <NAME> - a game
                - list - games
                - join <ID> [WHITE|BLACK] - a game
                - observe <ID> - a game
                - logout - when you are done
                - quit - playing chess
                - help - with possible commands
                """;
    }

    public UserState getState() {
        return state;
    }
}
