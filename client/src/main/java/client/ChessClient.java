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
